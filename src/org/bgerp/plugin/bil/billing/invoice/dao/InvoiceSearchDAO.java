package org.bgerp.plugin.bil.billing.invoice.dao;

import java.sql.Connection;

import org.bgerp.plugin.bil.billing.invoice.model.Invoice;

import ru.bgcrm.dao.CommonDAO;
import ru.bgcrm.model.SearchResult;
import ru.bgcrm.util.sql.PreparedDelay;

/**
 * Fluent invoice search DAO.
 *
 * @author Shamil Vakhitov
 */
public class InvoiceSearchDAO extends CommonDAO {
    private int processId;

    public InvoiceSearchDAO(Connection con) {
        super(con);
    }

    public InvoiceSearchDAO withProcessId(int value) {
        this.processId = value;
        return this;
    }

    public void search(SearchResult<Invoice> result) throws Exception {
        var query = SQL_SELECT_COUNT_ROWS + "*" + SQL_FROM + Tables.TABLE_INVOICE + SQL_WHERE + "1>0";
        try (var pd = new PreparedDelay(con, query)) {
            if (processId > 0) {
                pd.addQuery(SQL_AND).addQuery("process_id=?").addInt(processId);
            }
            var rs = pd.executeQuery();
            while (rs.next()) {
                result.getList().add(InvoiceDAO.getFromRs(rs));
            }
        }
    }
}
