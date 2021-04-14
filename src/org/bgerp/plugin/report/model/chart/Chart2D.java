package org.bgerp.plugin.report.model.chart;

import java.util.Map;
import java.util.TreeMap;

import org.bgerp.plugin.report.model.Column;
import org.bgerp.plugin.report.model.Data;

/**
 * Two dimensional chart. 
 * One column with categories, another one with values.
 * The second column must be an aggregating function.
 * 
 * @author Shamil Vakhitov
 */
public abstract class Chart2D extends Chart {
    protected final Column categories; 
    protected final Column values;

    public Chart2D(String ltitle, Column categories, Column values) {
        super(ltitle);
        this.categories = categories;
        this.values = values;
        if (!(values instanceof Column.ColumnCount))
            throw new IllegalArgumentException("Unsupported values column: " + values);
    }

    /**
     * Aggregate values column by categories. 
     * @param data
     * @return
     */
    protected Map<String, Integer> prepareData(Data data) {
        Map<String, Integer> result = new TreeMap<>();

        if (values instanceof Column.ColumnCount) {
            for (var r : data.getList()) {
                var cat = r.getString(categories.getId());
                var val = result.get(cat);
                result.put(cat, val == null ? 1 : val + 1);
            }
        }

        return result;
    }
}
