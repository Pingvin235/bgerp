package ru.bgcrm.plugin.fulltext.dao;

import static ru.bgcrm.dao.Tables.TABLE_CUSTOMER;
import static ru.bgcrm.dao.message.Tables.TABLE_MESSAGE;
import static ru.bgcrm.dao.process.Tables.TABLE_PROCESS;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.bgerp.app.exception.BGException;
import org.bgerp.model.Pageable;
import org.bgerp.util.Log;

import ru.bgcrm.dao.CommonDAO;
import ru.bgcrm.dao.CustomerDAO;
import ru.bgcrm.dao.message.MessageDAO;
import ru.bgcrm.dao.process.ProcessDAO;
import ru.bgcrm.model.Pair;
import ru.bgcrm.model.customer.Customer;
import ru.bgcrm.model.message.Message;
import ru.bgcrm.model.process.Process;
import ru.bgcrm.plugin.fulltext.model.SearchItem;

public class SearchDAO extends CommonDAO {
    private final static Log log = Log.getLog();

    public final static String TABLE = " fulltext_data ";

    public SearchDAO(Connection con) {
        super(con);
    }

    /**
     * Полнотекстовый поиск контрагентов.
     * @param result
     * @param filter строка запроса с символами + и - для добавления / удаления слов.
     * @throws SQLException
     */
    public void searchCustomer(Pageable<Customer> result, String filter) throws SQLException {
        searchObjects(result, filter, TABLE_CUSTOMER, Customer.OBJECT_TYPE, rs -> {
            try {
                return CustomerDAO.getCustomerFromRs(rs, "o.");
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * Полнотекстовый поиск процессов.
     * @param result
     * @param filter строка запроса с символами + и - для добавления / удаления слов.
     * @throws SQLException
     */
    public void searchProcess(Pageable<Process> result, String filter) throws SQLException {
        searchObjects(result, filter, TABLE_PROCESS, Process.OBJECT_TYPE, rs -> {
            try {
                return ProcessDAO.getProcessFromRs(rs, "o.");
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private <T> void searchObjects(Pageable<T> result, String filter,
        String tableName, String objectType, Function<ResultSet, T> extractor) throws SQLException {
        String query = SQL_SELECT_COUNT_ROWS + "o.* FROM " + TABLE + " AS ft "
                + SQL_INNER_JOIN + tableName + " AS o ON ft.object_id=o.id"
                + SQL_WHERE + "ft.object_type=? AND MATCH(ft.data) AGAINST (? IN BOOLEAN MODE) "
                + getPageLimit(result.getPage());
        PreparedStatement ps = con.prepareStatement(query);
        ps.setString(1, objectType);
        ps.setString(2, filter);

        ResultSet rs = ps.executeQuery();
        while (rs.next())
            result.getList().add(extractor.apply(rs));
        result.getPage().setRecordCount(foundRows(ps));
        ps.close();
    }

    /**
     * Полнотекстовый поиск сообщений, привязанных к процессам.
     * @param result
     * @param filter строка запроса с символами + и - для добавления / удаления слов.
     * @throws SQLException
     */
    public void searchMessages(Pageable<Pair<Message, Process>> result, String filter) throws SQLException {
        String query = SQL_SELECT_COUNT_ROWS + "m.*, p.* FROM " + TABLE + " AS ft "
                + SQL_INNER_JOIN + TABLE_MESSAGE + " AS m ON ft.object_id=m.id "
                + SQL_LEFT_JOIN + TABLE_PROCESS + " AS p ON m.process_id=p.id "
                + SQL_WHERE + "ft.object_type=? AND MATCH(ft.data) AGAINST (? IN BOOLEAN MODE) "
                + getPageLimit(result.getPage());
        PreparedStatement ps = con.prepareStatement(query);
        ps.setString(1, Message.OBJECT_TYPE);
        ps.setString(2, filter);

        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            Message m = MessageDAO.getMessageFromRs(rs, "m.");
            Process p = ProcessDAO.getProcessFromRs(rs, "p.");
            result.getList().add(new Pair<Message, Process>(m, p));
        }
        result.getPage().setRecordCount(foundRows(ps));
        ps.close();
    }

    /**
     * Помечает объект необходимым для обновления.
     * @param objectType
     * @param objectId
     */
    public void scheduleUpdate(String objectType, int objectId) throws SQLException {
        log.debug("Updated record, objectType: {}; objectId: {}", objectType, objectId);
        updateOrInsert(
                SQL_UPDATE + TABLE + SQL_SET + "scheduled_dt=NOW()" + SQL_WHERE + "object_type=? AND object_id=?",
                SQL_INSERT_INTO + TABLE + "(scheduled_dt, object_type, object_id) VALUES (NOW(),?,?)",
                objectType, objectId);
    }

    /**
     * Удаляет запись об объекте.
     * @param objectType
     * @param objectId
     */
    public void delete(String objectType, int objectId) throws BGException {
        log.debug("Deleted record, objectType: {}; objectId: {}", objectType, objectId);
        try {
            String query = SQL_DELETE_FROM + TABLE + SQL_WHERE + "object_type=? AND object_id=?";
            PreparedStatement ps = con.prepareStatement(query);
            ps.setString(1, objectType);
            ps.setInt(2, objectId);
            ps.executeUpdate();
            ps.close();
        } catch (SQLException e) {
            throw new BGException(e);
        }
    }

    /**
     * Удаляет запись об объекте.
     * @param item
     */
    public void delete(SearchItem item) throws BGException {
        delete(item.getObjectType(), item.getObjectId());
    }

    /**
     * Выбирает записи необходимые для обновления.
     * @param secondsOld последнее изменение объекта более чем секунд назад.
     * @param maxCount максимальное количество.
     */
    public List<SearchItem> getScheduledUpdates(int secondsOld, int maxCount) throws BGException {
        List<SearchItem> result = new ArrayList<>();
        try {
            String query =
                    SQL_SELECT_COUNT_ROWS + "object_type, object_id" + SQL_FROM + TABLE + SQL_WHERE +
                    "scheduled_dt<=DATE_SUB(NOW(), INTERVAL ? SECOND)" +
                    SQL_ORDER_BY + "scheduled_dt" +
                    SQL_LIMIT + "?";
            PreparedStatement ps = con.prepareStatement(query);
            ps.setInt(1, secondsOld);
            ps.setInt(2, maxCount);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                SearchItem item = new SearchItem();
                item.setObjectType(rs.getString(1));
                item.setObjectId(rs.getInt(2));
                result.add(item);
            }
            ps.close();
        } catch (SQLException e) {
            throw new BGException(e);
        }
        return result;
    }

    /**
     * Обновляет искомый текст записи.
     * @param item
     */
    public void update(SearchItem item) throws BGException {
        try {
            String query = SQL_UPDATE + TABLE + SQL_SET + "data=?, scheduled_dt=NULL" + SQL_WHERE + "object_type=? AND object_id=?";
            PreparedStatement ps = con.prepareStatement(query);
            ps.setString(1, item.getText());
            ps.setString(2, item.getObjectType());
            ps.setInt(3, item.getObjectId());
            ps.executeUpdate();
            ps.close();
        } catch (SQLException e) {
            throw new BGException(e);
        }
    }

    /**
     * Initialize indexing for object types.
     * @param objectType
     * @param objectTable
     * @throws SQLException
     */
    public void init(String objectType, String objectTable) throws SQLException {
        String query = SQL_INSERT_INTO + TABLE + " (object_type, object_id, scheduled_dt) "
            + "SELECT ?, t.id, NOW() FROM " + objectTable + " AS t "
            + "LEFT JOIN " + TABLE + " AS fd ON fd.object_type=? AND t.id=fd.object_id WHERE fd.object_id IS NULL";
        PreparedStatement ps = con.prepareStatement(query);
        ps.setString(1, objectType);
        ps.setString(2, objectType);
        ps.executeUpdate();
        ps.close();
    }

}
