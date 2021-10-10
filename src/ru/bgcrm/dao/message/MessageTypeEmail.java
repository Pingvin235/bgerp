package ru.bgcrm.dao.message;

import org.bgerp.util.Log;

import ru.bgcrm.model.BGException;
import ru.bgcrm.util.ParameterMap;
import ru.bgcrm.util.Setup;

@Deprecated
public class MessageTypeEmail extends org.bgerp.plugin.msg.email.MessageTypeEmail {
    private static final Log log = Log.getLog();

    public MessageTypeEmail(Setup setup, int id, ParameterMap config) throws BGException {
        super(setup, id, config);
        log.warn("Called constructor of deprecated {} instead of {}",
            this.getClass(), org.bgerp.plugin.msg.email.MessageTypeEmail.class);
    }
}
