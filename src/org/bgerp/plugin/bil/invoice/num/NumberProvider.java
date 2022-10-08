package org.bgerp.plugin.bil.invoice.num;

import java.sql.Connection;

import org.bgerp.plugin.bil.invoice.model.Invoice;
import org.bgerp.plugin.bil.invoice.model.InvoiceType;

import ru.bgcrm.util.Config;
import ru.bgcrm.util.ParameterMap;

/**
 * Number generator.
 *
 * @author Shamil Vakhitov
 */
public abstract class NumberProvider extends Config {

    protected NumberProvider(ParameterMap config) {
        super(config);
    }

    public abstract void number(Connection con, InvoiceType type, Invoice invoice) throws Exception;
}
