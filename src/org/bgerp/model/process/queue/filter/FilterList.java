package org.bgerp.model.process.queue.filter;

import java.util.ArrayList;

public class FilterList {
    private ArrayList<Filter> filterList = new ArrayList<>();

    public void add(Filter filter) {
        filterList.add(filter);
    }

    public ArrayList<Filter> getFilterList() {
        return filterList;
    }

    public Filter getByType(String type) {
        for (Filter filter : filterList) {
            if (filter.getType().equals(type)) {
                return filter;
            }
        }
        return null;
    }
}
