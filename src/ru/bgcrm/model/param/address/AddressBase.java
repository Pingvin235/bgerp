package ru.bgcrm.model.param.address;

import java.util.HashMap;
import java.util.Map;

import org.bgerp.model.base.IdTitle;

/**
 * Base class for all address related entities.
 *
 * @author Shamil Vakhitov
 */
public class AddressBase extends IdTitle {
    private Map<String, String> config = new HashMap<>();

    /**
     * Key value configuration.
     * @return
     */
    public Map<String, String> getConfig() {
        return config;
    }

    public void setConfig(Map<String, String> value) {
        this.config = value;
    }

    public void setConfig(String value) {
        this.config.clear();
        for (String line : value.split("\\n")) {
            int pos = line.indexOf('=');
            if (pos > -1) {
                this.config.put(line.substring(0, pos), line.substring(pos + 1));
            }
        }
    }
}
