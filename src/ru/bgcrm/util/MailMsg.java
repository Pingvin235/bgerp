package ru.bgcrm.util;

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

import org.apache.log4j.Logger;

/**
 * Отправщик E-Mail сообщений от имени сервера BGERP.
 * 
 * @author Shamil
 */
public class MailMsg {
    private static final Logger log = Logger.getLogger(MailMsg.class);

    private String result = "";

    private String encoding;
    private MimeMessage msg;

    private boolean _correctInit = false;

    public MailMsg(ParameterMap paramMap) {
        String fromAddress = paramMap.get("mail.from.email", null);
        String fromName = paramMap.get("mail.from.name", "BGERP");

        if (fromAddress != null) {
            encoding = getParamMailEncoding(paramMap);
            Session session = new MailConfig(paramMap).getSmtpSession(null);

            msg = new MimeMessage(session);
            try {
                msg.setFrom(new InternetAddress(fromAddress, fromName, encoding));
                msg.setSentDate(new Date());

                _correctInit = true;
            } catch (Exception ex) {
                log.error(ex.getMessage(), ex);
            }
        } else {
            log.error("Parameter 'mail.from.email' not defined in config!");
        }
    }

    public static String getParamMailEncoding(ParameterMap paramMap) {
        return paramMap.get("mail.encoding", Utils.UTF8.name());
    }

    /**
     * Проверяет, была ли корректной инициализация
     * @return true - если корректна
     */
    public boolean wasInitCorrectly() {
        return _correctInit;
    }

    /**
     * Отправляет простое текствое письмо адресату
     * @param Recipient адресат
     * @param subject тема
     * @param Txt сообщение
     */
    public void sendMessage(String Recipient, String subject, String Txt) {
        List<String> mails = new ArrayList<String>();
        StringTokenizer st = new StringTokenizer(Recipient, ";,\n\r ");
        while (st.hasMoreTokens()) {
            mails.add(st.nextToken());
        }
        if (mails.size() > 0) {
            sendMessage(mails, subject, Txt);
        }
    }

    /**
     * Отправляет простое текстовое письмо нескольким адресатам
     * @param mails адресаты
     * @param subject тема
     * @param Txt сообщение
     */
    public void sendMessage(List<String> mails, String subject, String Txt) {
        for (int i = 0; i < mails.size(); i++) {
            try {
                String mail = mails.get(i).toString();
                result = "Message send to " + mail + " => Ok";
                msg.setSubject(subject, encoding);
                msg.setText(Txt, encoding);
                msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(mail));

                sendMessage();
            } catch (Exception e) {
                e.printStackTrace();
                result = "Error: " + e.toString();
            }
        }
    }

    public void sendMessage(String Recipient, String subject, String txt, String type) {
        result = "Message send => Ok";
        try {
            msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(Recipient));
            msg.setSubject(subject, encoding);
            msg.setContent(txt, type);

            sendMessage();
        } catch (Exception e) {
            e.printStackTrace();
            result = "Error: " + e.toString();
        }
    }

    public void sendMessage(List<String> mails, String subject, Multipart mp) {
        for (int i = 0; i < mails.size(); i++) {
            try {
                String mail = mails.get(i).toString();
                result = "Message send to " + mail + " => Ok";
                msg.setSubject(subject, encoding);
                msg.setContent(mp);
                msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(mail));

                sendMessage();
            } catch (Exception e) {
                e.printStackTrace();
                result = "Error: " + e.toString();
            }
        }
    }

    /**
     * Отправляет письмо адресату
     * @param Recipient адресат
     * @param subject тема
     * @param mp тело сообщения
     */
    public void sendMessage(String Recipient, String subject, Multipart mp) {
        result = "Message send => Ok";
        try {
            msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(Recipient));
            msg.setSubject(subject, encoding);
            msg.setContent(mp);

            sendMessage();
        } catch (Exception e) {
            e.printStackTrace();
            result = "Error: " + e.toString();
        }
    }

    public void sendMessageEx(String mails, String subject, String content, String contentType) {
        result = "Message send => Ok";
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
            e.printStackTrace();
            result = "Error: " + e.toString();
        }
    }

    public void sendMessageAndHandle(String Recipient, String subject, Multipart mp) throws MessagingException {
        try {
            result = "Message send => Ok";

            msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(Recipient));
            msg.setSubject(subject, encoding);
            msg.setContent(mp);

            sendMessage();
        } catch (MessagingException e) {
            e.printStackTrace();
            result = "Error: " + e.toString();
            throw e;
        }
    }

    public void sendMessageAndHandleEx(String mails, String subject, String content, String contentType) throws MessagingException {
        try {
            result = "Message send => Ok";

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
        } catch (MessagingException e) {
            e.printStackTrace();
            result = "Error: " + e.toString();
            throw e;
        }
    }

    public String getResult() {
        return result;
    }

    private void sendMessage() throws MessagingException {
        Transport transport = msg.getSession().getTransport();
        transport.connect();
        transport.sendMessage(msg, msg.getAllRecipients());
        transport.close();
    }
}