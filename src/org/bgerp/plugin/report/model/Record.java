package org.bgerp.plugin.report.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize()
public class Record {
    private final Data data;
    private final List<Object> values;
    private int pos;

    public Record(Data data) {
        this.data = data;
        this.values = new ArrayList<>(data.getColumns().size());
    }

    /**
     * Add the value of the next column.
     * @param value
     */
    public void add(Object value) {
        int index = values.size();
        if (index >= data.getColumns().size())
            throw new IndexOutOfBoundsException("Too many column values");
        var column = data.getColumns().getByIndex(index);
        values.add(column.accept(value));
    }

    /**
     * Get value by column ID.
     * @param id the column ID.
     * @return
     * @throws IllegalArgumentException - if no column found.
     */
    public Object get(String id) {
        return values.get(data.getColumns().getIndex(id));
    }

    /**
     * Get a value formatted to string by column ID.
     * @param id the column ID.
     * @return
     * @throws IllegalArgumentException - if no column found.
     */
    public String getString(String id) {
        var column = data.getColumns().get(id);
        return column.toString(values.get(column.getIndex()));
    }

    /**
     * Get position for {@link java.sql.ResultSet}.
     * Starts from 1, and for each call incremented on 1.
     * @return
     */
    public int pos() {
        return ++pos;
    }

    @JsonAnyGetter
    public Map<String, Object> getData() {
        var result = new HashMap<String, Object>(data.getColumns().getVisibleColumns().size());
        for (var col : data.getColumns().getVisibleColumns()) {
            result.put(col.getId(), getString(col.getId()));
        }
        return result;
    }
}
