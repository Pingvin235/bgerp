package ru.bgcrm.plugin.bgbilling;

import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.bgerp.app.cfg.Config;
import org.bgerp.app.cfg.ConfigMap;

import ru.bgcrm.plugin.bgbilling.model.ContractType;

public class ContractTypesConfig extends Config {
    private SortedMap<Integer, ContractType> typeMap = new TreeMap<>();

    public ContractTypesConfig(ConfigMap config) {
        this(config, "bgbilling:contractType.");
    }

    public ContractTypesConfig(ConfigMap config, String prefix) {
        super(null);

        for (Map.Entry<Integer, ConfigMap> me : config.subIndexed(prefix).entrySet()) {
            int id = me.getKey();
            ConfigMap param = me.getValue();
            typeMap.put(id, new ContractType(id, param));
        }
    }

    public Map<Integer, ContractType> getTypeMap() {
        return typeMap;
    }
}
