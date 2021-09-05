package ru.bgcrm.dao.message;

import ru.bgcrm.model.BGException;
import ru.bgcrm.util.ParameterMap;
import ru.bgerp.util.Log;

@Deprecated
public class MessageTypeEmail extends org.bgerp.plugin.msg.email.MessageTypeEmail {
    private static final Log log = Log.getLog();

    public MessageTypeEmail(int id, ParameterMap config) throws BGException {
        super(id, config);
        log.warn("Called constructor of deprecated {} instead of {}", 
            this.getClass(), org.bgerp.plugin.msg.email.MessageTypeEmail.class);
    }
}
