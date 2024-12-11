package ru.bgcrm.model.message;

import org.bgerp.util.Log;

@Deprecated
public class Message extends org.bgerp.model.msg.Message {
    private static final Log log = Log.getLog();

    public Message() {
        log.warndClass(Message.class, org.bgerp.model.msg.Message.class);
    }
}
