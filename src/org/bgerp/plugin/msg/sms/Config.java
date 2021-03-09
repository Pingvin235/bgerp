package org.bgerp.plugin.msg.sms;

import ru.bgcrm.model.BGMessageException;
import ru.bgcrm.util.ParameterMap;

public class Config extends ru.bgcrm.util.Config {
    // TODO: Later may be supported many senders with routing rules.
    private final Sender sender;

    protected Config(ParameterMap setup) throws BGMessageException {
        super(setup);
        this.sender = Sender.of(setup);
    }

    public void sendSms(String number, String text) {
        this.sender.send(number, text);
    }
}
