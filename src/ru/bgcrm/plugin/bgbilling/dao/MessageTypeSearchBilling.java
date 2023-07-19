package ru.bgcrm.plugin.bgbilling.dao;

import org.bgerp.app.cfg.ConfigMap;

import ru.bgcrm.dao.message.MessageTypeSearch;
import ru.bgcrm.model.BGException;
import ru.bgcrm.util.Utils;

public abstract class MessageTypeSearchBilling extends MessageTypeSearch {
    protected final String billingId;

    protected MessageTypeSearchBilling(ConfigMap config) throws BGException {
        super(config);

        this.billingId = config.get("billingId");
        if (Utils.isBlankString(billingId)) {
            throw new BGException("billingId not defined");
        }
    }
}
