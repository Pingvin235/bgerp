package org.bgerp.plugin.bil.subscription.model;

import ru.bgcrm.model.IdTitle;
import ru.bgcrm.util.ParameterMap;

public class Subscription extends IdTitle {
    private static final int TYPE_MONTH_PREPAID = 1;

    /** Process type ID for Subscription. */
    private final int processTypeId;

     /** Param type 'listcount', value IDs same wht 'paramLimitId', set in Service processes. */
     private final int paramLimitPriceId;

    /** Type of calculation, only one type is supported. */
    private final int billingType;
    /** Day of month for sending notification. */
    private final int informDay;
    /** Waiting days. */
    private final int paymentWaitDays;

    public Subscription(int id, ParameterMap config) {
        super(id, config.get("title", "?? [" + id + "]"));

        processTypeId = config.getInt("process.type");

        paramLimitPriceId = config.getInt("param.limit.price");

        billingType = config.getInt("billing.type", TYPE_MONTH_PREPAID);
        informDay = config.getInt("inform.day", 1);
        paymentWaitDays = config.getInt("payment.wait.days", 10);
    }

    public int getProcessTypeId() {
        return processTypeId;
    }

    public int getParamLimitPriceId() {
        return paramLimitPriceId;
    }
}
