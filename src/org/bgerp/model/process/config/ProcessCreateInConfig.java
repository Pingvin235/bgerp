package org.bgerp.model.process.config;

import java.util.Set;

import org.bgerp.app.cfg.Config;
import org.bgerp.app.cfg.ConfigMap;
import org.bgerp.util.Log;

import ru.bgcrm.util.Utils;

public class ProcessCreateInConfig extends Config {
    private static final Log log = Log.getLog();

    private final Set<String> areas;
    private final String copyParams;
    private final ConfigMap config;

    protected ProcessCreateInConfig(ConfigMap config) {
        super(null);
        config = config.sub("create.in.");
        areas = Utils.toSet(config.get("areas", "*"));
        if (areas.contains("linked"))
            log.warnd("Used deprecated 'linked' value in 'create.in.areas'");

        Set<String> objectTypes = Utils.toSet(config.get("objectTypes"));
        if (!objectTypes.isEmpty()) {
            log.warnd("Used deprecated key 'create.in.objectTypes', place the values to 'create.in.areas' instead");
            areas.addAll(objectTypes);
        }

        copyParams = config.get("copyParams", "");
        this.config = config;
    }

    public boolean check(String area) {
        return areas.contains(area) || areas.contains("*");
    }

    public String getCopyParams() {
        return copyParams;
    }

    public boolean openCreated(String area) {
        return config.getBoolean(area + ".openCreated", config.getBoolean("*.openCreated"));
    }

    public boolean selected(String area) {
        return config.getBoolean(area + ".selected", config.getBoolean("*.selected"));
    }
}
