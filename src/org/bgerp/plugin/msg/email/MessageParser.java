package org.bgerp.plugin.msg.email;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.james.mime4j.codec.DecodeMonitor;
import org.apache.james.mime4j.codec.DecoderUtil;
import org.apache.james.mime4j.dom.Body;
import org.apache.james.mime4j.dom.Message;
import org.apache.james.mime4j.dom.Multipart;
import org.apache.james.mime4j.message.DefaultMessageBuilder;
import org.apache.james.mime4j.message.DefaultMessageWriter;
import org.apache.james.mime4j.stream.MimeConfig;
import org.bgerp.model.file.FileData;
import org.bgerp.util.Log;
import org.bgerp.util.mail.MailConfig;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;

import com.sun.mail.imap.IMAPMessage;

import jakarta.mail.BodyPart;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MailDateFormat;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import ru.bgcrm.util.Utils;

/**
 * IMAP message parser.
 *
 * @author Shamil Vakhitov
 */
public class MessageParser {
    private static final Log log = Log.getLog();

    private static final Pattern DATE_PATTERN = Pattern.compile("\\w{3}, \\d+ \\w{3} \\d{4} \\d{2}:\\d{2}:\\d{2} \\+\\d{4}");
    private static final MailDateFormat MAIL_DATE_FORMAT = new MailDateFormat();

     /** JavaMail message. */
     private final MimeMessage message;
     /** MIME raw data, used for testing. */
     private final byte[] mimeData;
     /** Lazy loaded Mime4j message. */
     private Message mime4j;

     /**
     * Main constructor.
     * @param message must be {@link IMAPMessage} instance.
     * @throws IllegalArgumentException not {@link IMAPMessage} was passed.
     */
    public MessageParser(jakarta.mail.Message message) {
        if (!(message instanceof IMAPMessage))
            throw new IllegalArgumentException("Parameter must be IMAPMessage");
        this.message = (MimeMessage) message;
        this.mimeData = null;
    }

    /**
     * Test constructor.
     * @param inputStream message input stream.
     * @throws Exception
     */
    MessageParser(InputStream inputStream) throws Exception {
        this.mimeData = IOUtils.toByteArray(inputStream);
        this.message = new MimeMessage(Session.getInstance(MailConfig.getImapSessionStaticProperties()), new ByteArrayInputStream(mimeData));
    }

    public String getMessageId() throws MessagingException {
        return message.getMessageID();
    }

    public String getFrom() throws MessagingException {
        return ((InternetAddress) message.getFrom()[0]).getAddress();
    }

    public Date getFromTime() throws MessagingException {
        Date result = null;

        // header 'Received'
        String[] headers = message.getHeader("Received");
        if (headers != null && headers.length > 0) {
            Matcher m = DATE_PATTERN.matcher(headers[0]);
            if (m.find()) {
                try {
                    result = MAIL_DATE_FORMAT.parse(m.group());
                } catch (Exception e) {}
            }
        }

        // header 'Date'
        if (result == null)
            result = message.getSentDate();

        // current time
        if (result == null)
            result = new Date();

        return result;
    }

    public String getTo() throws Exception {
        // адреса пришлось выбирать из заголовков, т.к. getReciepients выдавал только по одному адресу каждого типа
        StringBuilder to = new StringBuilder(100);

        final String[] valuesTo = message.getHeader("To");
        if (valuesTo != null) {
            extractAddresses(valuesTo, to);
        }

        String[] valuesCc = message.getHeader("CC");
        if (valuesCc != null) {
            StringBuilder cc = new StringBuilder(100);
            extractAddresses(valuesCc, cc);
            to.append("; CC: ").append(cc);
        }

        return to.toString();
    }

    private void extractAddresses(String[] values, StringBuilder result) {
        for (String value : values) {
            for (String token : Utils.toList(value)) {
                try {
                    Utils.addSeparated(result, ", ", InternetAddress.parse(token)[0].getAddress());
                } catch (AddressException e) {
                    log.error(e);
                }
            }
        }
    }

    public String getMessageSubject() throws Exception {
        // handling encoded tokens like:
        // =?koi8-r?Q?Re:_=FA=C1=D0=D2=CF=D3_=D4=C5=D3=D4=CF=D7=CF=CA_=CC?=
        // =?koi8-r?Q?=C9=C3=C5=CE=DA=C9=C9_[info=40bgcrm.ru#2213]?=
        return DecoderUtil.decodeEncodedWords(Utils.maskNull(message.getSubject()), DecodeMonitor.SILENT);
    }

    public String getTextContent() throws Exception {
        String textContent = new String();

        String contentType = message.getContentType().toLowerCase();
        Object content = message.getContent();

        log.debug("Extracting content, contentType: {}", contentType);

        // если пришел обычный текст
        if (contentType.startsWith("text/plain")) {
            textContent = (String) content;
        }
        // если пришел текст в виде HTML
        else if (contentType.startsWith("text/html")) {
            textContent = htmlToPlainText((String) content);
        }
        // если пришел и обычный текст, и HTML текст
        else if (contentType.startsWith("multipart/alternative")) {
            textContent = getTextFromMultipartAlternative((MimeMultipart) message.getContent());
        }
        // если сообщение с файлами (multipart): multipart/mixed, multipart/related, multipart/alternate
        else if (contentType.startsWith("multipart/")) {
            textContent = getTextFromMultipartMixed((MimeMultipart) message.getContent());
        } else {
            return "Message type '" + contentType + "' is not supported";
        }

        return textContent
            .replace("\r", "")
            .replaceAll("(\n\\s*){2,}", "\n\n")
            .trim();
    }

    private String htmlToPlainText(String text) {
        return buildStringFromNode(Jsoup.parse(text).body(), "").toString();
    }

    private StringBuffer buildStringFromNode(Node node, String citation) {
        StringBuffer buffer = new StringBuffer();

        if (node instanceof Element) {
            Element element = (Element) node;
            String tagName = element.tagName();

            if ("blockquote".equals(tagName)) {
                citation = "> " + citation;
            } else if ("p".equals(tagName) || "div".equals(tagName) || "br".equals(tagName)) {
                buffer.append("\n");
            } else if ("a".equals(tagName)) {
                buffer.append(" [");
                Node firstChild = Utils.getFirst(node.childNodes());
                if (firstChild != null)
                    buffer.append(buildStringFromNode(firstChild, citation));
                buffer.append("] ");
                buffer.append(element.attr("href"));
                return buffer;
            }
        }

        if (node instanceof TextNode) {
            TextNode textNode = (TextNode) node;
            buffer.append(citation + textNode.text().trim());
        }

        for (Node childNode : node.childNodes()) {
            buffer.append(buildStringFromNode(childNode, citation));
        }

        return buffer;
    }

    private String getTextFromMultipartAlternative(MimeMultipart content) {
        String textContent = new String();
        try {
            for (int i = 0; i < content.getCount(); i++) {
                BodyPart messagePart = content.getBodyPart(i);
                String partContentType = messagePart.getContentType().toLowerCase();
                Object partContent = messagePart.getContent();

                log.debug("Extracting multipart part, contentType: {}", partContentType);

                if (partContentType.startsWith("text/plain")) {
                    textContent = (String) partContent;
                    break;
                } else if (partContentType.startsWith("text/html")) {
                    textContent = htmlToPlainText((String) partContent);
                }
            }
        } catch (Exception ex) {
            log.error("Multipart alternative message error: " + ex.getMessage(), ex);
        }

        return textContent;
    }

    private String getTextFromMultipartMixed(MimeMultipart content) {
        String textContent = new String();
        try {
            for (int i = 0; i < content.getCount(); i++) {
                BodyPart part = content.getBodyPart(i);

                String partContentType = part.getContentType().toLowerCase();
                Object partContent = part.getContent();

                log.debug("Processing multipart part, type: {}", partContentType);

                if (partContentType.startsWith("multipart/alternative")) {
                    textContent = getTextFromMultipartAlternative((MimeMultipart) partContent);
                } else if (partContentType.startsWith("text/plain")) {
                    textContent = (String) partContent;
                } else if (partContentType.startsWith("text/html")) {
                    textContent = htmlToPlainText((String) partContent);
                } else if (partContentType.startsWith("multipart/mixed")
                        || partContentType.startsWith("multipart/related")) {
                    textContent = getTextFromMultipartMixed((MimeMultipart) part.getContent());
                }
            }
        } catch (Exception ex) {
            log.error("Multipart mixed message error: " + ex.getMessage(), ex);
        }
        return textContent;
    }

    public List<FileData> getAttachContent() throws Exception {
        ArrayList<FileData> attachContent = new ArrayList<>();

        // lazy initialization of mimi4j
        if (mime4j == null) {
            DefaultMessageBuilder builder = new DefaultMessageBuilder();
            builder.setMimeEntityConfig(MimeConfig.PERMISSIVE);
            mime4j = builder.parseMessage(
                mimeData != null ?
                new ByteArrayInputStream(mimeData) :
                ((IMAPMessage) this.message).getMimeStream()
            );
        }

        var body = mime4j.getBody();
        if (body instanceof Multipart) {
            var multipart = (Multipart) body;
            for (var part : multipart.getBodyParts()) {
                String filename = part.getFilename();
                log.debug("Possible attachment part, filename: {}", filename);
                if (Utils.isBlankString(filename)) {
                    log.debug("Skip");
                    continue;
                }
                attachContent.add(new FileData(part.getFilename(), getContent(part.getBody())));
            }
        }

        return attachContent;
    }

    private byte[] getContent(Body body) throws IOException {
        DefaultMessageWriter messageWriter = new DefaultMessageWriter();
        ByteArrayOutputStream out = new ByteArrayOutputStream(20000);
        messageWriter.writeBody(body, out);
        return out.toByteArray();
    }
}