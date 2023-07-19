package org.bgerp.plugin.msg.sms;

import org.bgerp.app.cfg.ConfigMap;

import ru.bgcrm.model.BGMessageException;

public class Config extends org.bgerp.app.cfg.Config {
    // TODO: Later may be supported many senders with routing rules.
    private final Sender sender;

    protected Config(ConfigMap setup) throws BGMessageException {
        super(setup);
        this.sender = Sender.of(setup);
    }

    public void sendSms(String number, String text) {
        this.sender.send(number, text);
    }
}
