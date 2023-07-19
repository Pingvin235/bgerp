package ru.bgcrm.util;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.bgerp.app.cfg.ConfigMap;
import org.bgerp.util.Log;

/**
 * E-Mail sender from BGERP.
 *
 * @author Shamil Vakhitov
 */
public class MailMsg {
    private static final Log log = Log.getLog();

    private String encoding;
    private MimeMessage msg;

    public MailMsg(ConfigMap paramMap) {
        String fromAddress = paramMap.get("mail.from.email", null);
        String fromName = paramMap.get("mail.from.name", "BGERP");

        if (fromAddress == null)
            throw new IllegalArgumentException("Parameter 'mail.from.email' not defined in config!");

        encoding = getParamMailEncoding(paramMap);
        Session session = new MailConfig(paramMap).getSmtpSession(null);

        msg = new MimeMessage(session);
        try {
            msg.setFrom(new InternetAddress(fromAddress, fromName, encoding));
            msg.setSentDate(new Date());
        } catch (Exception ex) {
            log.error(ex);
        }
    }

    public static String getParamMailEncoding(ConfigMap paramMap) {
        return paramMap.get("mail.encoding", StandardCharsets.UTF_8.name());
    }

    /**
     * Sends simple text message.
     * @param recipients comma separated addresses.
     * @param subject subject.
     * @param txt body text.
     */
    public void sendMessage(String recipients, String subject, String txt) {
        List<String> mails = new ArrayList<>();
        StringTokenizer st = new StringTokenizer(recipients, ";,\n\r ");
        while (st.hasMoreTokens()) {
            mails.add(st.nextToken());
        }
        if (mails.size() > 0) {
            sendMessage(mails, subject, txt);
        }
    }

    /**
     * Sends simple text message.
     * @param recipients addresses.
     * @param subject subject.
     * @param txt body text.
     */
    public void sendMessage(List<String> recipients, String subject, String txt) {
        for (int i = 0; i < recipients.size(); i++) {
            try {
                String mail = recipients.get(i).toString();

                msg.setSubject(subject, encoding);
                msg.setText(txt, encoding);
                msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(mail));

                sendMessage();
            } catch (Exception e) {
                log.error(e);
            }
        }
    }

    public void sendMessage(String recipient, String subject, String txt, String type) {
        try {
            msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipient));
            msg.setSubject(subject, encoding);
            msg.setContent(txt, type);

            sendMessage();
        } catch (Exception e) {
            log.error(e);
        }
    }

    public void sendMessage(List<String> mails, String subject, Multipart mp) {
        for (int i = 0; i < mails.size(); i++) {
            try {
                String mail = mails.get(i).toString();

                msg.setSubject(subject, encoding);
                msg.setContent(mp);
                msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(mail));

                sendMessage();
            } catch (Exception e) {
                log.error(e);
            }
        }
    }

    /**
     * Отправляет письмо адресату
     * @param recipient адресат
     * @param subject тема
     * @param mp тело сообщения
     */
    public void sendMessage(String recipient, String subject, Multipart mp) {
        try {
            msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipient));
            msg.setSubject(subject, encoding);
            msg.setContent(mp);

            sendMessage();
        } catch (Exception e) {
            log.error(e);
        }
    }

    public void sendMessageEx(String mails, String subject, String content, String contentType) {
        try {
            StringTokenizer st = new StringTokenizer(mails, ",;\r\n ");
            while (st.hasMoreTokens())
                try {
                    String token = st.nextToken().trim();
                    msg.addRecipient(Message.RecipientType.TO, InternetAddress.parse(token)[0]);
                } catch (Exception ex) {
                }

            msg.setSubject(subject, encoding);
            msg.setContent(content, contentType + ";charset=" + encoding);

            sendMessage();
        } catch (Exception e) {
            log.error(e);
        }
    }

    public void sendMessageAndHandle(String Recipient, String subject, Multipart mp) throws MessagingException {
        msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(Recipient));
        msg.setSubject(subject, encoding);
        msg.setContent(mp);

        sendMessage();
    }

    public void sendMessageAndHandleEx(String mails, String subject, String content, String contentType) throws MessagingException {
        StringTokenizer st = new StringTokenizer(mails, ",;\r\n ");
        while (st.hasMoreTokens())
            try {
                String token = st.nextToken().trim();
                msg.addRecipient(Message.RecipientType.TO, InternetAddress.parse(token)[0]);
            } catch (Exception ex) {}

        msg.setSubject(subject, encoding);
        msg.setContent(content, contentType + ";charset=" + encoding);

        sendMessage();
    }

    private void sendMessage() throws MessagingException {
        Transport transport = msg.getSession().getTransport();
        transport.connect();
        transport.sendMessage(msg, msg.getAllRecipients());
        transport.close();
    }
}