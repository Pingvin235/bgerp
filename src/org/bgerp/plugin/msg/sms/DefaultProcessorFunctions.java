package org.bgerp.plugin.msg.sms;

import ru.bgcrm.dao.expression.ExpressionBasedFunction;
import ru.bgcrm.model.BGException;
import ru.bgcrm.util.Setup;

public class DefaultProcessorFunctions extends ExpressionBasedFunction {
    public DefaultProcessorFunctions() {}

    /**
     * Send SMS message.
     *
     * @param number recipient number
     * @param text   text message
     * @throws BGException
     */
    public void sendSms(String number, String text)
            throws BGException {
        Config config = Setup.getSetup().getConfig(Config.class);
        if (config == null)
            return;
        
        config.sendSms(number, text);
    }
}
