package org.bgerp.plugin.msg.sms;

import ru.bgcrm.model.BGMessageException;
import ru.bgcrm.util.ParameterMap;

public abstract class Sender extends ru.bgcrm.util.Config {
    protected Sender(ParameterMap setup) {
        super(setup);
    }

    public static Sender of(ParameterMap setup) throws BGMessageException {
        setup = setup.sub(Plugin.ID + ":");
        var type = setup.get("type", "");
        switch (type) {
            case "mts":
                return new SenderMTS(setup);
            case "tele2":
                return new SenderTele2(setup);
            case "smsc":
                return new SenderSMSC(setup);
        }
        throw new IllegalArgumentException("Unsupported type: " + type);
    }

    public abstract void send(String number, String text);
}
