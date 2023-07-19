package org.bgerp.plugin.bil.invoice.num;

import java.sql.Connection;

import org.bgerp.app.cfg.Config;
import org.bgerp.app.cfg.ConfigMap;
import org.bgerp.plugin.bil.invoice.model.Invoice;
import org.bgerp.plugin.bil.invoice.model.InvoiceType;

/**
 * Number generator.
 *
 * @author Shamil Vakhitov
 */
public abstract class NumberProvider extends Config {

    protected NumberProvider(ConfigMap config) {
        super(config);
    }

    public abstract void number(Connection con, InvoiceType type, Invoice invoice) throws Exception;
}
