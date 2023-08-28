package org.bgerp.plugin.msg.sms;

import org.bgerp.app.cfg.ConfigMap;

public abstract class Sender extends org.bgerp.app.cfg.Config {
    protected Sender(ConfigMap setup) {
        super(setup);
    }

    public static Sender of(ConfigMap config) {
        var type = config.get("type", "");
        try {
            switch (type) {
                case "mts":
                    return new SenderMTS(config);
                case "tele2":
                    return new SenderTele2(config);
                case "smsc":
                    return new SenderSMSC(config);
                default:
                    return null;
            }
        } catch (InitStopException e) {
            return null;
        }
    }

    public abstract void send(String number, String text);
}
