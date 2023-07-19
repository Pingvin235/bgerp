package org.bgerp.plugin.msg.sms;

import org.bgerp.app.cfg.ConfigMap;

import ru.bgcrm.model.BGMessageException;

public abstract class Sender extends org.bgerp.app.cfg.Config {
    protected Sender(ConfigMap setup) {
        super(setup);
    }

    public static Sender of(ConfigMap setup) throws BGMessageException {
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
