package org.bgerp.plugin.bil.billing.invoice.dao;

import static org.bgerp.plugin.bil.billing.invoice.dao.Tables.TABLE_INVOICE;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;

import org.bgerp.plugin.bil.billing.invoice.model.Invoice;
import org.bgerp.plugin.bil.billing.invoice.model.Position;

import ru.bgcrm.dao.CommonDAO;
import ru.bgcrm.struts.action.BaseAction;
import ru.bgcrm.util.sql.PreparedDelay;

public class InvoiceDAO extends CommonDAO {
    public InvoiceDAO(Connection con) {
        super(con);
    }

    public void update(Invoice invoice) throws Exception {
        String query;
        if (invoice.getId() > 0) {
            query = SQL_UPDATE + TABLE_INVOICE + SQL_SET
                    + "summa=?, sent_dt=?, sent_user_id=?, payment_date=?, payment_user_id=?, positions=?";
        } else {
            query = SQL_INSERT + TABLE_INVOICE
                    + "(summa, sent_dt, sent_user_id, payment_date, payment_user_id, positions, "
                    + "type_id, process_id, from_date, created_dt, created_user_id)"
                    + SQL_VALUES
                    + "(?, ?, ?, ?, ?, ?, ?, "
                    + "?, ?, ?, NOW(), ?)";
        }

        try (var pd = new PreparedDelay(con, query)) {
            pd.addBigDecimal(invoice.getSumma());
            pd.addTimestamp(invoice.getSentTime());
            pd.addInt(invoice.getSentUserId());
            pd.addDate(invoice.getPaymentDate());
            pd.addInt(invoice.getPaymentUserId());
            pd.addString(BaseAction.MAPPER.writeValueAsString(invoice.getPositions()));

            if (invoice.getId() <= 0) {
                pd.addInt(invoice.getTypeId());
                pd.addInt(invoice.getProcessId());
                pd.addDate(invoice.getFromDate());
                pd.addInt(invoice.getCreatedUserId());
                invoice.setId(pd.executeUpdate());
            } else {
                pd.executeUpdate();
            }
        }
    }

    public static Invoice getFromRs(ResultSet rs) throws Exception {
        var result = new Invoice();
        result.setSumma(rs.getBigDecimal("summa"));
        result.setSentTime(rs.getTimestamp("sent_dt"));
        result.setSentUserId(rs.getInt("sent_user_id"));
        result.setPaymentDate(rs.getDate("payment_date"));
        result.setPaymentUserId(rs.getInt("payment_user_id"));
        result.setPositions(BaseAction.MAPPER.readValue(rs.getString("positions"), new TypeReference<List<Position>>() {}));

        result.setId(rs.getInt("id"));
        result.setTypeId(rs.getInt("type_id"));
        result.setProcessId(rs.getInt("process_id"));
        result.setFromDate(rs.getDate("from_date"));
        result.setCreatedTime(rs.getTimestamp("created_dt"));
        result.setCreatedUserId(rs.getInt("created_user_id"));

        return result;
    }
}
