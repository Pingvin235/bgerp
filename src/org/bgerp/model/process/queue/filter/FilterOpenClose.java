package org.bgerp.model.process.queue.filter;

import org.bgerp.app.cfg.ConfigMap;

public class FilterOpenClose extends Filter {
    public static final String OPEN = "open";
    public static final String CLOSE = "close";

    private String defaultValue;

    public FilterOpenClose(int id, ConfigMap filter) {
        super(id, filter);
        defaultValue = filter.get("defaultValue", OPEN);
    }

    public String getDefaultValue() {
        return defaultValue;
    }
}
