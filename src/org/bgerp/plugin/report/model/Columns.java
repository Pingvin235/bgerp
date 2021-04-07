package org.bgerp.plugin.report.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * List of report's columns.
 * 
 * @author Shamil Vakhitov
 */
public class Columns {
    private final List<Column> columnList;
    private final Map<String, Column> columnMap;
    private final List<Column> visibleColumns;

    public Columns(Column... column) {
        this.columnList = List.of(column);

        Map<String, Column> columnMap = new HashMap<>(column.length);
        for (int i = 0; i < columnList.size(); i++) {
            var col = columnList.get(i);
            col.setIndex(i);
            columnMap.put(col.getId(), col);
        }
        this.columnMap = Collections.unmodifiableMap(columnMap);

        this.visibleColumns = columnList.stream()
            .filter(Column::isVisible)
            .collect(Collectors.toUnmodifiableList());
    }

    /**
     * Quantity of all columns.
     * @return
     */
    public int size() {
        return columnList.size();
    }

    /**
     * List of visible columns with not blank title.
     * @return
     */
    public List<Column> getVisibleColumns() {
        return visibleColumns;
    }

    /**
     * Column by ID.
     * @param id
     * @return 
     * @throws IllegalArgumentException - if no column found.
     */
    public Column get(String id) {
        var result = columnMap.get(id);
        if (result == null)
            throw new IllegalArgumentException("Column not found: " + id);
        return result;
    }

    /**
     * Column by index.
     * @param index
     * @return
     */
    public Column getByIndex(int index) {
        return columnList.get(index);
    }

    /**
     * Column index by ID.
     * @param id
     * @return
     * @throws IllegalArgumentException - if no column found.
     */
    public int getIndex(String id) {
        return get(id).getIndex();
    }
}
