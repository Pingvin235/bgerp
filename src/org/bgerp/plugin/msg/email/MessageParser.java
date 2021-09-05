package org.bgerp.plugin.msg.email;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Part;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MailDateFormat;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;

import ru.bgcrm.util.Utils;
import ru.bgerp.util.Log;

public class MessageParser {
    private static final Log log = Log.getLog();

    private static final Pattern DATE_PATTERN = Pattern.compile("\\w{3}, \\d+ \\w{3} \\d{4} \\d{2}:\\d{2}:\\d{2} \\+\\d{4}");
    private static final MailDateFormat MAIL_DATE_FORMAT = new MailDateFormat();

    private final MimeMessage message;

    public MessageParser(Message message) {
        this.message = (MimeMessage) message;
    }

    public String getMessageId() throws MessagingException {
        return message.getMessageID();
    }

    public String getFrom() throws MessagingException {
        return ((InternetAddress) message.getFrom()[0]).getAddress();
    }

    public Date getFromTime() throws MessagingException {
        // приоритентна дата из заголовка Recieved - время получения нашим IMAP сервером
        String[] headers = message.getHeader("Received");
        if (headers != null && headers.length > 0) {
            Matcher m = DATE_PATTERN.matcher(headers[0]);
            if (m.find()) {
                try {
                    return MAIL_DATE_FORMAT.parse(m.group());
                } catch (Exception e) {}
            }
        }
        return message.getSentDate();
    }

    public String getTo() throws Exception {
        // адреса пришлось выбирать из заголовков, т.к. getReciepients выдавал
        // только по одному адресу каждого типа
        StringBuilder to = new StringBuilder(10);

        final String[] headersTo = message.getHeader("To");
        if (headersTo != null) {
            for (String header : headersTo) {
                for (InternetAddress addr : InternetAddress.parse(header)) {
                    Utils.addSeparated(to, ", ", addr.getAddress());
                }
            }
        }

        String[] headersCc = message.getHeader("CC");
        if (headersCc != null) {
            StringBuilder ccAddresses = new StringBuilder(100);
            for (String header : headersCc) {
                for (InternetAddress addr : InternetAddress.parse(header)) {
                    Utils.addSeparated(ccAddresses, ", ", addr.getAddress());
                }
            }

            to.append("; CC: ");
            to.append(ccAddresses);
        }
        return to.toString();
    }

    // разбор темы сообщений
    // когда конструкция разбита на несколько частей вида, то стандартный парсер
    // разбирает только первый токен
    // =?koi8-r?Q?Re:_=FA=C1=D0=D2=CF=D3_=D4=C5=D3=D4=CF=D7=CF=CA_=CC?=
    // =?koi8-r?Q?=C9=C3=C5=CE=DA=C9=C9_[info=40bgcrm.ru#2213]?=
    public String getMessageSubject() throws Exception {
        String subject = Utils.maskNull(message.getSubject());

        int posFrom = -1, posTo = -1;
        do {
            posFrom = subject.indexOf("=?", posTo);
            int posEndEncoding = subject.indexOf("?Q?", posFrom);
            posTo = subject.indexOf("?=", posEndEncoding + 3);

            if (posFrom >= 0 && posTo > posFrom) {
                subject = subject.substring(0, posFrom) + MimeUtility.decodeText(subject.substring(posFrom, posTo + 2))
                        + subject.substring(posTo + 2);
            }
        } while (posFrom >= 0 && posTo > posFrom);

        return subject;
    }

    public String getTextContent() throws Exception {
        String textContent = new String();

        String contentType = message.getContentType().toLowerCase();
        Object content = message.getContent();

        if (log.isDebugEnabled()) {
            log.debug("Extracting content, contentType: " + contentType);
        }

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

                if (partIsAttachedFile(part)) {
                    continue;
                }

                String partContentType = part.getContentType().toLowerCase();
                Object partContent = part.getContent();

                if (log.isDebugEnabled()) {
                    log.debug("Processing multipart part, type: " + partContentType);
                }

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

    public static class MessageAttach {
        public String title;
        public InputStream inputStream;

        public MessageAttach(String title, InputStream inputStream) {
            this.title = title;
            this.inputStream = inputStream;
        }
    }

    public List<MessageAttach> getAttachContent() throws Exception {
        ArrayList<MessageAttach> attachContent = new ArrayList<MessageAttach>();

        String contentType = message.getContentType().toLowerCase();
        if (contentType.startsWith("multipart/")) {
            MimeMultipart content = (MimeMultipart) message.getContent();
            getAttaches(attachContent, content);
        }

        return attachContent;
    }

    private void getAttaches(ArrayList<MessageAttach> attachContent, MimeMultipart content) throws Exception {
        for (int i = 0; i < content.getCount(); i++) {
            BodyPart part = content.getBodyPart(i);
            if (part.getContentType().startsWith("multipart/")) {
                getAttaches(attachContent, (MimeMultipart) part.getContent());
            } else if (partIsAttachedFile(part)) {
                String attachTitle = MimeUtility.decodeText(part.getFileName() == null ? "attach" : part.getFileName());
                log.debug("Attach: %s", attachTitle);

                MessageAttach attachData = new MessageAttach(attachTitle, part.getInputStream());
                attachContent.add(attachData);
            }
        }
    }

    private boolean partIsAttachedFile(Part part) {
        try {
            return Utils.notBlankString(part.getFileName());
        } catch (MessagingException e) {
            return false;
        }
    }
}