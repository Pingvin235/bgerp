package org.bgerp.model.process.queue.filter;

import ru.bgcrm.util.ParameterMap;

public class FilterLinkObject extends Filter {
    private final String objectType;
    private final String whatFilter;

    public static final String WHAT_FILTER_ID = "id";
    public static final String WHAT_FILTER_TITLE = "title";

    public FilterLinkObject(int id, ParameterMap filter, String objectType, String whatFilter) {
        super(id, filter);
        this.objectType = objectType;
        this.whatFilter = whatFilter;
    }

    public String getObjectType() {
        return objectType;
    }

    public String getWhatFilter() {
        return whatFilter;
    }

    public String getParamName() {
        return objectType + "-" + whatFilter;
    }
}
