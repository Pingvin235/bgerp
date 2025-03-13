package org.bgerp.plugin.bil.subscription.model.config;

import org.bgerp.app.cfg.Config;
import org.bgerp.app.cfg.ConfigMap;
import org.bgerp.app.cfg.Setup;
import org.bgerp.model.msg.config.MessageTypeConfig;
import org.bgerp.plugin.msg.email.message.MessageTypeEmail;

import ru.bgcrm.dao.expression.Expression;
import ru.bgcrm.util.Utils;

/**
 * Subscription configuration for paid invoices.
 *
 * @author Shamil Vakhitov
 */
public class PaidInvoiceConfig extends Config {
    private final String dateToExpression;
    private final MessageTypeEmail emailMessageType;
    private final String emailSubject;
    private final String emailTextExpression;

    protected PaidInvoiceConfig(ConfigMap config) throws InitStopException {
        super(null);
        config = config.sub("paid.invoice.");
        dateToExpression = config.get("dateTo." + Expression.EXPRESSION_CONFIG_KEY);
        emailMessageType = loadOnPaidInvoiceEmailMessageType(config.getInt("email.message.type", -1));
        emailSubject = config.get("email.subject", "BGERP License");
        emailTextExpression = config.get("email.text." + Expression.EXPRESSION_CONFIG_KEY, "\"As an attachment you will find a new license file.\"");
        initWhen(Utils.notBlankString(dateToExpression));
    }

    private MessageTypeEmail loadOnPaidInvoiceEmailMessageType(int id) {
        MessageTypeEmail result = null;

        var messageTypesConfig = Setup.getSetup().getConfig(MessageTypeConfig.class);
        if (id == 0)
            result = messageTypesConfig.getMessageType(MessageTypeEmail.class);
        else if (id > 0)
            result = (MessageTypeEmail) messageTypesConfig.getTypeMap().get(id);

        return result;
    }

    /**
     * @return expression for calculation of subscription dateTo on paid invoice.
     */
    public String getDateToExpression() {
        return dateToExpression;
    }

    /**
     * @return Email message type for sending email on paid invoice or {@code null}.
     */
    public MessageTypeEmail getEmailMessageType() {
        return emailMessageType;
    }

    /**
     * @return Email subject.
     */
    public String getEmailSubject() {
        return emailSubject;
    }

    /**
     * @return Email text expression.
     */
    public String getEmailTextExpression() {
        return emailTextExpression;
    }
}
