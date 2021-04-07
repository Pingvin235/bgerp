package org.bgerp.plugin.report.model;

import java.util.List;

public class Chart {
    private final List<Column> categories; 
    private final List<Column> values;

    private Chart(List<Column> categories, List<Column> values) {
        this.categories = categories;
        this.values = values;
    }

    public List<Column> getCategories() {
        return categories;
    }

    public List<Column> getValues() {
        return values;
    }

    public static class ChartBar extends Chart {
        public ChartBar(List<Column> categories, List<Column> values) {
            super(categories, values);
        }
    }
}
