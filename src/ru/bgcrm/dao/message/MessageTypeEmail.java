package ru.bgcrm.dao.message;

import org.bgerp.app.cfg.ConfigMap;
import org.bgerp.app.cfg.Setup;
import org.bgerp.util.Log;

import ru.bgcrm.model.BGException;

@Deprecated
public class MessageTypeEmail extends org.bgerp.plugin.msg.email.MessageTypeEmail {
    private static final Log log = Log.getLog();

    public MessageTypeEmail(Setup setup, int id, ConfigMap config) throws BGException {
        super(setup, id, config);
        log.warn("Called constructor of deprecated {} instead of {}",
            this.getClass(), org.bgerp.plugin.msg.email.MessageTypeEmail.class);
    }
}
