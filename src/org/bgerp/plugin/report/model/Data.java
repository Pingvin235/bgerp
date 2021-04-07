package org.bgerp.plugin.report.model;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.google.common.annotations.VisibleForTesting;

import org.bgerp.plugin.report.action.ReportActionBase;

import ru.bgcrm.model.SearchResult;
import ru.bgcrm.struts.form.DynActionForm;

public class Data extends SearchResult<Record> {
    private final ReportActionBase action;
    private final DynActionForm form;
    private final Columns columns;

    @VisibleForTesting
    Data(Columns columns) {
        this.action = null;
        this.form = null;
        this.columns = columns;
    }

    public Data(ReportActionBase action, DynActionForm form, Columns columns) {
        super(form);
        this.action = action;
        this.form = form;
        this.columns = columns;
    }

    /**
     * Report's action.
     * @return
     */
    public ReportActionBase getAction() {
        return action;
    }

    public DynActionForm getForm() {
        return form;
    }

    public Columns getColumns() {
        return columns;
    }

    /**
     * Adds a new record and returns it. 
     * @return
     */
    public Record addRecord() {
        final var result = new Record(this);
        getList().add(result);
        return result;
    }

    /**
     * Adds a new record out of current row of a result set.
     * @param rs
     * @throws SQLException
     */
    public void addRecord(ResultSet rs) throws SQLException {
        final var record = addRecord();
        for (int i = 1; i <= columns.size(); i++) {
            record.add(rs.getObject(i));
        }
    }
}
