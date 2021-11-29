package org.bgerp.plugin.msg.sms;

import ru.bgcrm.util.Setup;

public class ExpressionObject {
    /**
     * Send SMS message.
     *
     * @param number recipient number
     * @param text   text message
     */
    public void sendSms(String number, String text) {
        Config config = Setup.getSetup().getConfig(Config.class);
        if (config == null)
            return;

        config.sendSms(number, text);
    }
}
