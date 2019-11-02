package ru.bgcrm.plugin.fulltext.dao;

import java.sql.Connection;
import static ru.bgcrm.dao.Tables.*;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import ru.bgcrm.dao.CommonDAO;
import ru.bgcrm.dao.CustomerDAO;
import ru.bgcrm.model.BGException;
import ru.bgcrm.model.Customer;
import ru.bgcrm.model.SearchResult;
import ru.bgcrm.plugin.fulltext.model.SearchItem;

public class SearchDAO extends CommonDAO {
    
    private final static Logger log = Logger.getLogger(SearchDAO.class);
    
    private final static String TABLE = " fulltext_data ";

    public SearchDAO(Connection con) {
        super(con);
    }

    /**
     * Полнотекстовый поиск контрагентов.
     * @param result
     * @param filter строка запроса с символами + и - для добавления / удаления слов.
     * @throws SQLException
     */
    public void searchCustomer(SearchResult<Customer> result, String filter) throws BGException {
        try {
            String query = SQL_SELECT_COUNT_ROWS + "c.* FROM " + TABLE + " AS ft "
                    + SQL_INNER_JOIN + TABLE_CUSTOMER + " AS c ON ft.object_id=c.id"
                    + SQL_WHERE + "ft.object_type=? AND MATCH(ft.data) AGAINST (? IN BOOLEAN MODE) "
                    + getMySQLLimit(result.getPage());
            PreparedStatement ps = con.prepareStatement(query);
            ps.setString(1, Customer.OBJECT_TYPE);
            ps.setString(2, filter);

            ResultSet rs = ps.executeQuery();
            while (rs.next())
                result.getList().add(CustomerDAO.getCustomerFromRs(rs, "c."));
            result.getPage().setPageCount(getFoundRows(ps));
            ps.close();
        } catch (SQLException e) {
            throw new BGException(e);
        }
    }
    
    /**
     * Помечает объект необходимым для обновления.
     * @param objectType
     * @param objectId
     * @throws BGException
     */
    public void scheduleUpdate(String objectType, int objectId) throws BGException {
        if (log.isDebugEnabled())
            log.debug("Updated record, objectType: " + objectType + "; objectId: " + objectId);
        updateOrInsert(
                SQL_UPDATE + TABLE + SQL_SET + "scheduled_dt=NOW()" + SQL_WHERE + "object_type=? AND object_id=?",
                SQL_INSERT + TABLE + "(scheduled_dt, object_type, object_id) VALUES (NOW(),?,?)",
                objectType, objectId);
    }
    
    /**
     * Удаляет запись об объекте.
     * @param objectType
     * @param objectId
     * @throws BGException
     */
    public void delete(String objectType, int objectId) throws BGException {
        if (log.isDebugEnabled())
            log.debug("Deleted record, objectType: " + objectType + "; objectId: " + objectId);
        try {
            String query = SQL_DELETE + TABLE + SQL_WHERE + "object_type=? AND object_id=?";
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
     * @throws BGException
     */
    public void delete(SearchItem item) throws BGException {
        delete(item.getObjectType(), item.getObjectId());
    }
    
    /**
     * Выбирает записи необходимые для обновления.
     * @param secondsOld последнее изменение объекта более чем секунд назад.
     * @param maxCount максимальное количество. 
     * @throws BGException
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
     * @throws BGException
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

}
