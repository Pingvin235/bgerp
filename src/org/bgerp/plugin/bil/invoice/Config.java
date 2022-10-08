package org.bgerp.plugin.bil.invoice;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

import org.bgerp.plugin.bil.invoice.model.InvoiceType;

import javassist.NotFoundException;
import ru.bgcrm.model.IdStringTitle;
import ru.bgcrm.util.ParameterMap;

public class Config extends ru.bgcrm.util.Config {
    private final Map<Integer, InvoiceType> types;
    private final Map<String, IdStringTitle> positions;

    protected Config(ParameterMap config) throws Exception{
        super(null);
        config = config.sub(Plugin.ID + ":");
        types = loadTypes(config);
        positions = loadPositions(config);
    }

    private Map<Integer, InvoiceType> loadTypes(ParameterMap config) throws Exception {
        var result = new TreeMap<Integer, InvoiceType>();

        for (var me : config.subIndexed("type.").entrySet()) {
            result.put(me.getKey(), new InvoiceType(me.getKey(), me.getValue()));
        }

        return Collections.unmodifiableMap(result);
    }

    private Map<String, IdStringTitle> loadPositions(ParameterMap config) throws Exception {
        var result = new LinkedHashMap<String, IdStringTitle>();

        for (var me : config.subIndexed("position.").entrySet()) {
            var pos = new IdStringTitle(me.getValue().get("id"), me.getValue().get("title"));
            result.put(pos.getId(), pos);
        }

        return Collections.unmodifiableMap(result);
    }

    /**
     * @return types sorted by IDs.
     */
    public Collection<InvoiceType> getTypes() {
        return types.values();
    }

    /**
     * Gets invoice type by ID.
     * @param id
     * @return
     * @throws NotFoundException
     */
    public InvoiceType getType(int id) throws NotFoundException {
        var result = types.get(id);
        if (result == null)
            throw new NotFoundException("Not found type: " + id);
        return result;
    }

    /**
     * @return positions sorted by configuration index.
     */
    public Collection<IdStringTitle> getPositions() {
        return positions.values();
    }
}
