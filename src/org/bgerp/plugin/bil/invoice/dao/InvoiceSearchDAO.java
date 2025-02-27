package org.bgerp.plugin.bil.invoice.dao;

import java.sql.Connection;

import org.bgerp.model.Pageable;
import org.bgerp.plugin.bil.invoice.model.Invoice;
import org.bgerp.util.sql.PreparedQuery;

import ru.bgcrm.dao.CommonDAO;

/**
 * Fluent invoice search DAO.
 *
 * @author Shamil Vakhitov
 */
public class InvoiceSearchDAO extends CommonDAO {
    private int processId;
    private Boolean paid;
    private boolean orderDefault;

    public InvoiceSearchDAO(Connection con) {
        super(con);
    }

    public InvoiceSearchDAO withProcessId(int value) {
        this.processId = value;
        return this;
    }

    public InvoiceSearchDAO withPaid(boolean value) {
        this.paid = value;
        return this;
    }

    /**
     * Reverse order by date from desc, than ID desc.
     * @return
     */
    public InvoiceSearchDAO orderDefault() {
        this.orderDefault = true;
        return this;
    }

    public void search(Pageable<Invoice> result) throws Exception {
        var query = SQL_SELECT_COUNT_ROWS + "*" + SQL_FROM + Tables.TABLE_INVOICE + SQL_WHERE + "1>0";
        try (var pq = new PreparedQuery(con, query)) {
            if (processId > 0)
                pq.addQuery(SQL_AND).addQuery("process_id=?").addInt(processId);

            if (paid != null)
                pq.addQuery(SQL_AND).addQuery("payment_date IS ").addQuery(paid ? "NOT" : "").addQuery("NULL");

            if (orderDefault)
                pq.addQuery(SQL_ORDER_BY)
                    .addQuery("date_from").addQuery(SQL_DESC)
                    .addQuery(", id").addQuery(SQL_DESC);

            var rs = pq.executeQuery();
            while (rs.next()) {
                result.getList().add(InvoiceDAO.getFromRs(rs));
            }

            result.getPage().setRecordCount(pq.getPrepared());
        }
    }
}
