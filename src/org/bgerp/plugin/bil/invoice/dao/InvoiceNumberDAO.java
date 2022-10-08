package org.bgerp.plugin.bil.invoice.dao;

import java.sql.Connection;
import java.sql.SQLException;

import org.bgerp.plugin.bil.invoice.model.Invoice;
import org.bgerp.util.sql.PreparedQuery;

import ru.bgcrm.dao.CommonDAO;

/**
 * Builder DAO for retrieving next invoice counter number.
 * At the end of uniqueness selection functions must be called {@link #get()},
 * performing the SQL query and returning required value.
 * Used from scripts.
 *
 * @author Shamil Vakhitov
 */
public class InvoiceNumberDAO extends CommonDAO {
    private final Invoice invoice;
    private final PreparedQuery pq;

    public InvoiceNumberDAO(Connection con, Invoice invoice) {
        super(con);
        this.invoice = invoice;
        this.pq = new PreparedQuery(con, SQL_SELECT + "MAX(number_cnt)" + SQL_FROM + Tables.TABLE_INVOICE + SQL_WHERE + "1>0");
    }

    /**
     * Selects for the current process.
     * @return
     */
    public InvoiceNumberDAO process() {
        pq.addQuery(SQL_AND + "process_id=?");
        pq.addInt(invoice.getProcessId());
        return this;
    }

    /**
     * Selects for the current month.
     * @return
     */
    public InvoiceNumberDAO month() {
        pq.addQuery(SQL_AND + "date_from=?");
        pq.addDate(invoice.getDateFrom());
        return this;
    }

    /**
     * Selects the next counter value.
     * Terminating function.
     * @return
     * @throws SQLException
     */
    public int next() throws SQLException {
        int cnt = 0;

        try (pq) {
            var rs = pq.executeQuery();
            if (rs.next())
                cnt = rs.getInt(1);
        }

        return cnt + 1;
    }
}
