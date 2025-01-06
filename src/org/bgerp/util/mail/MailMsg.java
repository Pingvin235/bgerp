package org.bgerp.util.mail;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

import javax.activation.DataHandler;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;
import javax.mail.util.ByteArrayDataSource;

import org.bgerp.app.cfg.ConfigMap;
import org.bgerp.util.Log;

import ru.bgcrm.model.FileData;

/**
 * E-Mail sender
 *
 * @author Shamil Vakhitov
 */
public class MailMsg {
    private static final Log log = Log.getLog();

    public static String getParamMailEncoding(ConfigMap config) {
        return config.get("mail.encoding", StandardCharsets.UTF_8.name());
    }

    public static void setAttachContentTypeHeader(MimeBodyPart part) throws MessagingException {
        part.setHeader("Content-Type", "charset=\"UTF-8\"; format=\"flowed\"");
    }

    public static void setAttachFileName(MimeBodyPart part, String value, String encoding) throws MessagingException {
        try {
            part.setFileName(MimeUtility.encodeWord(value, encoding, null));
        } catch (UnsupportedEncodingException e) {
            log.error(e);
        }
    }

    private final String encoding;
    private final MimeMessage msg;
    private List<FileData> attachments;

    public MailMsg(ConfigMap config) {
        String fromAddress = config.get("mail.from.email", null);
        String fromName = config.get("mail.from.name", "BGERP");

        if (fromAddress == null)
            throw new IllegalArgumentException("Parameter 'mail.from.email' not defined in config!");

        encoding = getParamMailEncoding(config);

        msg = new MimeMessage(new MailConfig(config).getSmtpSession(null));
        try {
            msg.setSentDate(new Date());
            msg.setFrom(new InternetAddress(fromAddress, fromName, encoding));
        } catch (MessagingException | UnsupportedEncodingException ex) {
            log.error(ex);
        }
    }

    /**
     * Adds message attachment files
     * @param value the attachments
     * @return
     */
    public MailMsg withAttachments(List<FileData> value) {
        attachments = value;
        return this;
    }

    /**
     * Sends simple text message.
     * @param recipients comma separated addresses.
     * @param subject subject.
     * @param txt body text.
     * @throws MessagingException
    */
    public void send(String recipients, String subject, String txt) throws MessagingException {
        msg.setRecipients(RecipientType.TO, Addresses.parseSafe(recipients).recipients().get(RecipientType.TO));
        msg.setSubject(subject, encoding);

        if (attachments == null || attachments.isEmpty())
            msg.setText(txt, encoding);
        else {
            var mp = new MimeMultipart();

            MimeBodyPart part = new MimeBodyPart();
            part.setText(txt, encoding);
            mp.addBodyPart(part);

            for (var attachment : attachments) {
                part = new MimeBodyPart();
                setAttachContentTypeHeader(part);
                setAttachFileName(part, attachment.getTitle(), encoding);
                part.setDataHandler(new DataHandler(new ByteArrayDataSource(attachment.getData(), "application/octet-stream; name=" + attachment.getTitle())));
                mp.addBodyPart(part);
            }

            msg.setContent(mp);
        }

        send();
    }

    private void send() throws MessagingException {
        try {
            var bos = new ByteArrayOutputStream(1000);
            msg.writeTo(bos);
            log.info("Sending a mail:\n{}", new String(bos.toByteArray(), encoding));
        } catch (Exception e) {
            log.error(e);
        }

        Transport transport = msg.getSession().getTransport();
        transport.connect();
        transport.sendMessage(msg, msg.getAllRecipients());
        transport.close();
    }
}