package ru.bgcrm.plugin.bgbilling.message;

import org.bgerp.app.cfg.ConfigMap;
import org.bgerp.app.exception.BGException;

import ru.bgcrm.dao.message.MessageTypeSearch;
import ru.bgcrm.util.Utils;

public abstract class MessageTypeSearchBilling extends MessageTypeSearch {
    protected final String billingId;

    protected MessageTypeSearchBilling(ConfigMap config) {
        super(config);

        this.billingId = config.get("billingId");
        if (Utils.isBlankString(billingId)) {
            throw new BGException("billingId not defined");
        }
    }
}
