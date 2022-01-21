package org.bgerp.plugin.bil.billing.invoice.pos;

import org.bgerp.plugin.bil.billing.invoice.model.Invoice;

import ru.bgcrm.util.Config;
import ru.bgcrm.util.ParameterMap;
import ru.bgcrm.util.sql.ConnectionSet;

/**
 * Generator of invoice positions.
 *
 * @author Shamil Vakhitov
 */
public abstract class PositionProvider extends Config {
    protected PositionProvider(ParameterMap config) {
        super(config);
    }

    /**
     * Appends position to invoice.
     * @param invoice
     */
    public abstract void addPositions(ConnectionSet conSet, Invoice invoice) throws Exception;
}
