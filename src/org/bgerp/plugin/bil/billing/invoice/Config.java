package org.bgerp.plugin.bil.billing.invoice;

import java.util.Collection;
import java.util.Collections;
import java.util.SortedMap;
import java.util.TreeMap;

import org.bgerp.plugin.bil.billing.invoice.model.InvoiceType;

import javassist.NotFoundException;
import ru.bgcrm.util.ParameterMap;
public class Config extends ru.bgcrm.util.Config {
    private final SortedMap<Integer, InvoiceType> types;

    protected Config(ParameterMap config) throws Exception{
        super(null);
        config = config.sub(Plugin.ID + ":");
        types = loadTypes(config);
    }

    private SortedMap<Integer, InvoiceType> loadTypes(ParameterMap config) throws Exception {
        var result = new TreeMap<Integer, InvoiceType>();

        for (var me : config.subIndexed("type.").entrySet()) {
            result.put(me.getKey(), new InvoiceType(me.getKey(), me.getValue()));
        }

        return Collections.unmodifiableSortedMap(result);
    }

    /**
     * @return types sorted by IDs.
     */
    public Collection<InvoiceType> getTypes() {
        return types.values();
    }

    public InvoiceType getType(int id) throws NotFoundException {
        var result = types.get(id);
        if (result == null)
            throw new NotFoundException("Not found type: " + id);
        return result;
    }
}
