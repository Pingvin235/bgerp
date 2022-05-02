package org.bgerp.plugin.svc.dba.model;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.bgerp.model.Pageable;

import ru.bgcrm.struts.form.DynActionForm;

/**
 * Query execution resulting table.
 *
 * @author Shamil Vakhitov
 */
public class QueryTable extends Pageable<String[]> {
    private final List<String> columns = new ArrayList<>();

    public QueryTable(DynActionForm form) {
        super(form);
    }

    /**
     * Sets column labels and rows from result set.
     * @param rs
     * @throws SQLException
     */
    public void set(ResultSet rs) throws SQLException {
        var meta = rs.getMetaData();
        final int columnCount = meta.getColumnCount();
        for (int i = 0; i < columnCount; i++) {
            columns.add(meta.getColumnLabel(i + 1));
        }

        var list = getList();
        while (rs.next()) {
            var row = new String[columnCount];
            for (int i = 0; i < columnCount; i++) {
                row[i] = rs.getString(i + 1);
            }
            list.add(row);
        }
    }

    /**
     * Sets single cell result.
     * @param label
     * @param value
     */
    public void set(String label, String value) {
        columns.add(label);
        getList().add(new String[] { value });
    }

    /**
     * @return column labels.
     */
    public List<String> getColumns() {
        return columns;
    }
}
