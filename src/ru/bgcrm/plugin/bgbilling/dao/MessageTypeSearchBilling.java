package ru.bgcrm.plugin.bgbilling.dao;

import ru.bgcrm.dao.message.MessageTypeSearch;
import ru.bgcrm.model.BGException;
import ru.bgcrm.util.ParameterMap;
import ru.bgcrm.util.Utils;

public abstract class MessageTypeSearchBilling extends MessageTypeSearch {
    protected final String billingId;

    protected MessageTypeSearchBilling(ParameterMap config) throws BGException {
        super(config);

        this.billingId = config.get("billingId");
        if (Utils.isBlankString(billingId)) {
            throw new BGException("billingId not defined");
        }
    }
}
