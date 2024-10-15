package org.bgerp.model.process.config;

import java.util.Set;

import org.bgerp.app.cfg.Config;
import org.bgerp.app.cfg.ConfigMap;

import ru.bgcrm.util.Utils;

public class ProcessCreateInConfig extends Config {
    private final Set<String> areas;
    private final Set<String> objectTypes;
    private final String copyParams;
    private final ConfigMap config;

    protected ProcessCreateInConfig(ConfigMap config) {
        super(null);
        config = config.sub("create.in.");
        areas = Utils.toSet(config.get("areas", "*"));
        objectTypes = Utils.toSet(config.get("objectTypes", "*"));
        copyParams = config.get("copyParams", "");
        this.config = config;
    }

    public boolean check(String area, String objectType) {
        return (areas.contains(area) || areas.contains("*")) && (objectType == null || objectTypes.contains(objectType) || objectTypes.contains("*"));
    }

    public String getCopyParams() {
        return copyParams;
    }

    public boolean openCreated(String objectType) {
        return config.getBoolean(objectType + ".openCreated", config.getBoolean("*.openCreated"));
    }
}
