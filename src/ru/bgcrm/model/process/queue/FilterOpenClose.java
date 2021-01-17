package ru.bgcrm.model.process.queue;

import ru.bgcrm.util.ParameterMap;

public class FilterOpenClose extends Filter {
    public static final String OPEN = "open";
    public static final String CLOSE = "close";

    private String defaultValue;

    public FilterOpenClose(int id, ParameterMap filter) {
        super(id, filter);
        defaultValue = filter.get("defaultValue", OPEN);
    }

    public String getDefaultValue() {
        return defaultValue;
    }
}
