package org.bgerp.plugin.bil.subscription.model;

import org.bgerp.app.cfg.ConfigMap;
import org.bgerp.model.base.IdTitle;
import org.bgerp.plugin.bil.subscription.model.config.PaidInvoiceConfig;

public class Subscription extends IdTitle {
    private final int processTypeId;
    private final int paramLimitPriceId;
    private final PaidInvoiceConfig paidInvoiceConfig;

    public Subscription(int id, ConfigMap config) {
        super(id, config.get("title", "?? [" + id + "]"));

        processTypeId = config.getInt("process.type");
        paramLimitPriceId = config.getInt("param.limit.price");
        paidInvoiceConfig = config.getConfig(PaidInvoiceConfig.class);
        /* billingType = config.getInt("billing.type", TYPE_MONTH_PREPAID);
        informDay = config.getInt("inform.day", 1);
        paymentWaitDays = config.getInt("payment.wait.days", 10); */
    }

    /**
     * @return process type ID for Subscription.
     */
    public int getProcessTypeId() {
        return processTypeId;
    }

    /**
     * @return param type 'listcount', value IDs same wht 'paramLimitId', set in Service processes.
     */
    public int getParamLimitPriceId() {
        return paramLimitPriceId;
    }

    /**
     * @return configuration for paid invoice or {@code null}.
     */
    public PaidInvoiceConfig getPaidInvoiceConfig() {
        return paidInvoiceConfig;
    }
}
