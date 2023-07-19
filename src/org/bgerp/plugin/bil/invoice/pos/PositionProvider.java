package org.bgerp.plugin.bil.invoice.pos;

import org.bgerp.app.cfg.Config;
import org.bgerp.app.cfg.ConfigMap;
import org.bgerp.plugin.bil.invoice.model.Invoice;

import ru.bgcrm.util.sql.ConnectionSet;

/**
 * Generator of invoice positions.
 *
 * @author Shamil Vakhitov
 */
public abstract class PositionProvider extends Config {
    protected PositionProvider(ConfigMap config) {
        super(config);
    }

    /**
     * Appends position to invoice.
     * @param invoice
     */
    public abstract void addPositions(ConnectionSet conSet, Invoice invoice) throws Exception;
}
