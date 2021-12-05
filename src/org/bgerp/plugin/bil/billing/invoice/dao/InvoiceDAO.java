package org.bgerp.plugin.bil.billing.invoice.dao;

import static org.bgerp.plugin.bil.billing.invoice.dao.Tables.TABLE_INVOICE;
import static org.bgerp.plugin.bil.billing.invoice.dao.Tables.TABLE_INVOICE_POSITION_PREFIX;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;

import org.bgerp.plugin.bil.billing.invoice.model.Invoice;
import org.bgerp.plugin.bil.billing.invoice.model.Position;

import ru.bgcrm.dao.PeriodicDAO;
import ru.bgcrm.struts.action.BaseAction;
import ru.bgcrm.util.sql.PreparedDelay;

public class InvoiceDAO extends PeriodicDAO {
    public InvoiceDAO(Connection con) {
        super(con);
    }

    public void update(Invoice invoice) throws Exception {
        String query;
        if (invoice.getId() > 0) {
            query = SQL_UPDATE + TABLE_INVOICE + SQL_SET
                    + "number=?, amount=?, sent_dt=?, sent_user_id=?, payment_date=?, payment_user_id=?, positions=?";
        } else {
            query = SQL_INSERT + TABLE_INVOICE
                    + "(number, amount, sent_dt, sent_user_id, payment_date, payment_user_id, positions, "
                    + "type_id, process_id, from_date, created_dt, created_user_id)"
                    + SQL_VALUES
                    + "(?, ?, ?, ?, ?, ?, ?, "
                    + "?, ?, ?, NOW(), ?)";
        }

        try (var pd = new PreparedDelay(con, query)) {
            pd.addString(invoice.getNumber());
            pd.addBigDecimal(invoice.getAmount());
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

        updatePositions(invoice);
    }

    private void updatePositions(Invoice invoice) throws SQLException {
        var positionTable = checkAndCreateMonthTable(TABLE_INVOICE_POSITION_PREFIX, invoice.getFromDate(),
            "(invoice_id INT NOT NULL," +
            "id CHAR(20) NOT NULL," +
            "amount DECIMAL(10,2) NOT NULL," +
            "title CHAR(100) NOT NULL," +
            "KEY invoice_id(invoice_id))");

        var query = SQL_DELETE + positionTable + SQL_WHERE + "invoice_id=?";
        try (var pd = new PreparedDelay(con, query)) {
            pd.addInt(invoice.getId()).executeUpdate();
        }

        query = SQL_INSERT + positionTable + "(invoice_id, id, amount, title)" + SQL_VALUES + "(?,?,?,?)";
        try (var pd = new PreparedDelay(con, query)) {
            pd.addInt(invoice.getId());
            for (var position : invoice.getPositions()) {
                pd.setPos(1);
                pd.addString(position.getId()).addBigDecimal(position.getAmount()).addString(position.getTitle());
                pd.executeUpdate();
            }
        }
    }

    public Invoice get(int id) throws Exception {
        Invoice result = null;

        var query = SQL_SELECT_ALL_FROM + TABLE_INVOICE + SQL_WHERE + "id=?";
        try (var pd = new PreparedDelay(con, query)) {
            var rs = pd.addInt(id).executeQuery();
            if (rs.next())
                result = getFromRs(rs);
        }

        return result;
    }

    public void delete(int id) throws Exception {
        var invoice = get(id);
        if (invoice == null)
            return;

        var query = SQL_DELETE + TABLE_INVOICE + SQL_WHERE + "id=?";
        try (var pd = new PreparedDelay(con, query)) {
            pd.addInt(id).executeUpdate();
        }

        var positionTable = getMonthTableName(TABLE_INVOICE_POSITION_PREFIX, invoice.getFromDate());
        if (tableExists(positionTable)) {
            query = SQL_DELETE + positionTable + SQL_WHERE + "invoice_id=?";
            try (var pd = new PreparedDelay(con, query)) {
                pd.addInt(id).executeUpdate();
            }
        }
    }

    public static Invoice getFromRs(ResultSet rs) throws Exception {
        var result = new Invoice();
        result.setAmount(rs.getBigDecimal("amount"));
        result.setSentTime(rs.getTimestamp("sent_dt"));
        result.setSentUserId(rs.getInt("sent_user_id"));
        result.setPaymentDate(rs.getDate("payment_date"));
        result.setPaymentUserId(rs.getInt("payment_user_id"));
        result.setPositions(BaseAction.MAPPER.readValue(rs.getString("positions"), new TypeReference<List<Position>>() {}));

        result.setId(rs.getInt("id"));
        result.setTypeId(rs.getInt("type_id"));
        result.setProcessId(rs.getInt("process_id"));
        result.setNumber(rs.getString("number"));
        result.setFromDate(rs.getDate("from_date"));
        result.setCreatedTime(rs.getTimestamp("created_dt"));
        result.setCreatedUserId(rs.getInt("created_user_id"));

        return result;
    }
}
