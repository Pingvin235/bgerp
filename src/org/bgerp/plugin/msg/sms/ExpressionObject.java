package org.bgerp.plugin.msg.sms;

import org.bgerp.app.cfg.Setup;

public class ExpressionObject {
    private final Config config = Setup.getSetup().getConfig(Config.class);

    /**
     * Sends a message using default Sender with ID = 0.
     *
     * @param number recipient phone number.
     * @param text message text.
     */
    public void sendSms(String number, String text) {
        sendSms(0, number, text);
    }

    /**
     * Sends a message using a configured Sender.
     *
     * @param senderId the Sender ID.
     * @param number   recipient phone number.
     * @param text     message text.
     */
    public void sendSms(int senderId, String number, String text) {
        var sender = config.getSenders().get(senderId);
        if (sender == null)
            throw new IllegalArgumentException("Not found a sender with ID: " + senderId);
        sender.send(number, text);
    }
}
