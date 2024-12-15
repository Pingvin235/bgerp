package org.bgerp.plugin.bil.invoice.dao;

import static org.bgerp.plugin.bil.invoice.dao.Tables.TABLE_INVOICE;
import static org.bgerp.plugin.bil.invoice.dao.Tables.TABLE_INVOICE_POSITION_PREFIX;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.bgerp.action.base.BaseAction;
import org.bgerp.plugin.bil.invoice.model.Invoice;
import org.bgerp.util.sql.PreparedQuery;

import com.fasterxml.jackson.core.type.TypeReference;

import javassist.NotFoundException;
import ru.bgcrm.dao.PeriodicDAO;

/**
 * Invoice DAO.
 *
 * @author Shamil Vakhitov
 */
public class InvoiceDAO extends PeriodicDAO {
    public InvoiceDAO(Connection con) {
        super(con);
    }

    public void update(Invoice invoice) throws Exception {
        String query;
        if (invoice.getId() > 0) {
            query = SQL_UPDATE + TABLE_INVOICE + SQL_SET
                    + "amount=?, sent_dt=?, sent_user_id=?, payment_date=?, payment_user_id=?, positions=?"
                    + SQL_WHERE + "id=?";
        } else {
            query = SQL_INSERT_INTO + TABLE_INVOICE
                    + "(amount, sent_dt, sent_user_id, payment_date, payment_user_id, positions, "
                    + "type_id, process_id, date_from, date_to, create_dt, create_user_id, number_cnt, number)"
                    + SQL_VALUES
                    + "(?, ?, ?, ?, ?, ?,"
                    + "?, ?, ?, ?, NOW(), ?, ?, ?)";
        }

        try (var pq = new PreparedQuery(con, query)) {
            pq.addBigDecimal(invoice.getAmount());
            pq.addTimestamp(invoice.getSentTime());
            pq.addInt(invoice.getSentUserId());
            pq.addDate(invoice.getPaymentDate());
            pq.addInt(invoice.getPaymentUserId());
            pq.addString(BaseAction.MAPPER.writeValueAsString(invoice.getPositions()));

            if (invoice.getId() <= 0) {
                pq.addInt(invoice.getTypeId());
                pq.addInt(invoice.getProcessId());
                pq.addDate(invoice.getDateFrom());
                pq.addDate(invoice.getDateTo());
                pq.addInt(invoice.getCreateUserId());
                pq.addInt(invoice.getNumberCnt());
                pq.addString(invoice.getNumber());

                invoice.setId(pq.executeUpdate());
            } else {
                pq.addInt(invoice.getId());
                pq.executeUpdate();
            }
        }

        updatePositions(invoice);
    }

    private void updatePositions(Invoice invoice) throws SQLException {
        var positionTable = checkAndCreateMonthTable(TABLE_INVOICE_POSITION_PREFIX, invoice.getDateFrom(),
            "(invoice_id INT NOT NULL," +
            "id CHAR(20) NOT NULL," +
            "amount DECIMAL(10,2) NOT NULL," +
            "title CHAR(100) NOT NULL," +
            "KEY invoice_id(invoice_id))");

        var query = SQL_DELETE_FROM + positionTable + SQL_WHERE + "invoice_id=?";
        try (var pq = new PreparedQuery(con, query)) {
            pq.addInt(invoice.getId()).executeUpdate();
        }

        query = SQL_INSERT_INTO + positionTable + "(invoice_id, id, amount, title)" + SQL_VALUES + "(?,?,?,?)";
        try (var pq = new PreparedQuery(con, query)) {
            pq.addInt(invoice.getId());
            for (var position : invoice.getPositions()) {
                pq.setPos(1);
                pq.addString(position.getId()).addBigDecimal(position.getAmount()).addString(position.getTitle());
                pq.executeUpdate();
            }
        }
    }

    public Invoice get(int id) throws Exception {
        Invoice result = null;

        var query = SQL_SELECT_ALL_FROM + TABLE_INVOICE + SQL_WHERE + "id=?";
        try (var pq = new PreparedQuery(con, query)) {
            var rs = pq.addInt(id).executeQuery();
            if (rs.next())
                result = getFromRs(rs);
        }

        return result;
    }

    public Invoice getOrThrow(int id) throws Exception {
        var result = get(id);
        if (result == null)
            throw new NotFoundException("Not found invoice, id=" + id);
        return result;
    }

    public void delete(int id) throws Exception {
        var invoice = get(id);
        if (invoice == null)
            return;

        var query = SQL_DELETE_FROM + TABLE_INVOICE + SQL_WHERE + "id=?";
        try (var pq = new PreparedQuery(con, query)) {
            pq.addInt(id).executeUpdate();
        }

        var positionTable = getMonthTableName(TABLE_INVOICE_POSITION_PREFIX, invoice.getDateFrom());
        if (tableExists(positionTable)) {
            query = SQL_DELETE_FROM + positionTable + SQL_WHERE + "invoice_id=?";
            try (var pq = new PreparedQuery(con, query)) {
                pq.addInt(id).executeUpdate();
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
        result.setPositions(BaseAction.MAPPER.readValue(rs.getString("positions"), new TypeReference<>() {
        }));

        result.setId(rs.getInt("id"));
        result.setTypeId(rs.getInt("type_id"));
        result.setProcessId(rs.getInt("process_id"));
        result.setNumber(rs.getString("number"));
        result.setDateFrom(rs.getDate("date_from"));
        result.setDateTo(rs.getDate("date_to"));
        result.setCreateTime(rs.getTimestamp("create_dt"));
        result.setCreateUserId(rs.getInt("create_user_id"));

        return result;
    }
}
