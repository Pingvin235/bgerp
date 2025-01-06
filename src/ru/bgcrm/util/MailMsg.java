package ru.bgcrm.util;

import javax.mail.MessagingException;

import org.bgerp.app.cfg.ConfigMap;
import org.bgerp.util.Log;

@Deprecated
public class MailMsg extends org.bgerp.util.mail.MailMsg {
    private static final Log log = Log.getLog();

    public MailMsg(ConfigMap config) {
        super(config);
        log.warndClass(MailMsg.class, org.bgerp.util.mail.MailMsg.class);
    }

    @Deprecated
    public void sendMessage(String recipients, String subject, String txt) throws MessagingException {
        log.warndMethod("sendMessage", "send");
        send(recipients, subject, txt);
    }
}
