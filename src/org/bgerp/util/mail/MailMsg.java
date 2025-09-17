package org.bgerp.util.mail;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

import org.bgerp.app.cfg.ConfigMap;
import org.bgerp.model.file.FileData;
import org.bgerp.util.Log;
import org.bgerp.util.URLTotalEncoder;

import jakarta.activation.DataHandler;
import jakarta.mail.Message.RecipientType;
import jakarta.mail.MessagingException;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import jakarta.mail.util.ByteArrayDataSource;

/**
 * Email sender
 *
 * @author Shamil Vakhitov
 */
public class MailMsg {
    private static final Log log = Log.getLog();

    public static String getParamMailEncoding(ConfigMap config) {
        return config.get("mail.encoding", StandardCharsets.UTF_8.name());
    }

    /***
     * Set 'Content-Disposition' header with name for an attached file
     * @param part email part, containing the attachment
     * @param filename the file's name
     * @throws MessagingException
     */
    public static void setAttachFileName(MimeBodyPart part, String filename) throws MessagingException {
        // the only supported charset
        final String charset = StandardCharsets.UTF_8.toString();
        String encoded = URLTotalEncoder.encode(filename, StandardCharsets.UTF_8);

        StringBuilder cd = new StringBuilder(encoded.length() * 2);

        final int maxLineLength = 60;
        if (encoded.length() <= maxLineLength ) {
            cd.append("attachment; filename*=").append(charset).append("''").append(encoded);
        } else {
            cd.append("attachment;\r\n");

            encoded = charset + "'" + encoded;

            int cnt = 0;
            while (encoded.length() > maxLineLength) {
                String encodedPart = encoded.substring(0, maxLineLength);

                cd.append(" filename*").append(cnt).append("*=");
                // double ' symbol after encoding
                if (cnt == 0) {
                    int pos = encodedPart.indexOf("'");
                    cd.append(encodedPart.substring(0, pos + 1)).append(encodedPart.substring(pos));
                } else
                    cd.append(encodedPart);

                cd.append(";").append("\r\n");
                encoded = encoded.substring(maxLineLength);
                cnt++;
            }

            if (encoded.length() > 0)
                cd.append(" filename*").append(cnt).append("*=").append(encoded);
        }

        part.setHeader("Content-Disposition", cd.toString());
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
                setAttachFileName(part, attachment.getTitle());
                // in "Content-Type" header will be set: "application/octet-stream; name=filename"
                part.setDataHandler(new DataHandler(new ByteArrayDataSource(attachment.getData(), "application/octet-stream")));
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