package ru.bgcrm.model.param.address;

import java.util.HashMap;
import java.util.Map;

import ru.bgcrm.model.IdTitle;

public class AddressBase extends IdTitle {
    private Map<String, String> config = new HashMap<String, String>();

    public Map<String, String> getConfig() {
        return config;
    }

    public void setConfig(Map<String, String> config) {
        this.config = config;
    }

    public void setConfig(String config) {
        this.config.clear();
        for (String line : config.split("\\n")) {
            int pos = line.indexOf('=');
            if (pos > -1) {
                this.config.put(line.substring(0, pos), line.substring(pos + 1));
            }
        }
    }
}
