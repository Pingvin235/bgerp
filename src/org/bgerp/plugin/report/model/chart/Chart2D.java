package org.bgerp.plugin.report.model.chart;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.bgerp.plugin.report.model.Column;
import org.bgerp.plugin.report.model.Column.ColumnString;
import org.bgerp.plugin.report.model.Data;

import ru.bgcrm.util.Utils;

/**
 * Two dimensional chart.
 * One column with categories, another one with values.
 *
 * @author Shamil Vakhitov
 */
public abstract class Chart2D extends Chart {
    protected final Column categories;

    public Chart2D(String ltitle, Column categories) {
        super(ltitle);
        this.categories = categories;
    }

    /**
     * Aggregate values column by categories.
     * @param data
     * @return
     */
    protected Map<String, Integer> prepareData(Data data) {
        Map<String, Integer> result = new TreeMap<>();

        final boolean commaSeparated = categories instanceof ColumnString column && column.isCommaSeparatedValues();

        for (var r : data.getList()) {
            String categoryValue = r.getString(categories.getId());
            if (Utils.isBlankString(categoryValue))
                continue;

            var categoryValues = commaSeparated ? List.of(categoryValue) : Utils.toList(categoryValue);
            for (String cat : categoryValues) {
                var val = result.get(cat);
                result.put(cat, val == null ? 1 : val + 1);
            }
        }

        return result;
    }
}
