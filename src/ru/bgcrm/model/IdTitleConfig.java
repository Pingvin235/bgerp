package ru.bgcrm.model;

import java.util.HashMap;
import java.util.Map;

import org.bgerp.model.base.IdTitle;

public class IdTitleConfig extends IdTitle {
    private String config;
    private Map<String, String> configMap = new HashMap<String, String>();

    public String getConfig() {
        return config;
    }

    public void setConfig(String config) {
        this.config = config;
        configMap.clear();
        if (config != null) {
            for (String line : config.split("\\n")) {
                int pos = line.indexOf('=');
                if (pos > -1) {
                    configMap.put(line.substring(0, pos), line.substring(pos + 1));
                }
            }
        }
    }

    public Map<String, String> getConfigMap() {
        return configMap;
    }
}
