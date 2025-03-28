package ru.bgcrm.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bgerp.model.base.Id;
import org.bgerp.util.Log;
import org.bgerp.util.sql.PreparedQuery;

import ru.bgcrm.model.Page;
import ru.bgcrm.model.Pair;
import ru.bgcrm.model.Period;
import ru.bgcrm.util.TimeUtils;
import ru.bgcrm.util.sql.SQLUtils;

public class CommonDAO {
    protected final Log log = Log.getLog(this.getClass());

    protected final static String SQL_SELECT = "SELECT ";
    protected final static String SQL_SELECT_ALL_FROM = "SELECT * FROM ";
    protected final static String SQL_SELECT_COUNT_ROWS = "SELECT SQL_CALC_FOUND_ROWS ";
    protected final static String SQL_DISTINCT = "DISTINCT ";
    protected final static String SQL_INSERT_IGNORE = "INSERT IGNORE INTO ";
    protected final static String SQL_INSERT_INTO = "INSERT INTO ";
    protected final static String SQL_VALUES = " VALUES ";
    protected final static String SQL_VALUES_1 = SQL_VALUES + "(?)";
    protected final static String SQL_VALUES_2 = SQL_VALUES + "(?, ?)";
    protected final static String SQL_VALUES_3 = SQL_VALUES + "(?, ?, ?)";
    protected final static String SQL_VALUES_4 = SQL_VALUES + "(?, ?, ?, ?)";
    protected final static String SQL_VALUES_5 = SQL_VALUES + "(?, ?, ?, ?, ?)";
    protected final static String SQL_SET = " SET ";
    protected final static String SQL_UPDATE = "UPDATE ";
    protected final static String SQL_DELETE = "DELETE ";
    protected final static String SQL_DELETE_FROM = "DELETE FROM ";
    protected final static String SQL_FROM = " FROM ";
    protected final static String SQL_LEFT_JOIN = " LEFT JOIN ";
    protected final static String SQL_INNER_JOIN = " INNER JOIN ";
    protected final static String SQL_WHERE = " WHERE ";
    protected final static String SQL_ORDER_BY = " ORDER BY ";
    protected final static String SQL_GROUP_BY = " GROUP BY ";
    protected final static String SQL_AND = " AND ";
    protected final static String SQL_LIMIT = " LIMIT ";
    protected final static String SQL_DESC = " DESC ";
    protected final static String SQL_REPLACE = " REPLACE INTO ";
    protected final static String SQL_ON_DUP_KEY_UPDATE = " ON DUPLICATE KEY UPDATE ";
    protected final static String SQL_UNION_ALL = " UNION ALL ";

    /**
     * Selects {@code FOUND_ROWS()} for given statement.
     * @param st
     * @return
     * @throws SQLException
     */
    public static int foundRows(Statement st) throws SQLException {
        int result = -1;
        ResultSet rs = st.executeQuery("SELECT FOUND_ROWS()");
        if (rs.next()) {
            result = rs.getInt(1);
        }
        return result;
    }

    protected static interface ObjectExtractor<T> {
        public T extract(ResultSet rs) throws SQLException;
    }

    protected Connection con;

    protected CommonDAO() {}

    protected CommonDAO(Connection con) {
        this.con = con;
    }

    /**
     * Takes last generated key from {@code ps}.
     * @param ps
     * @return
     * @throws SQLException
     */
    protected int lastInsertId(PreparedStatement ps) throws SQLException {
        int id = -1;
        ResultSet rs = ps.getGeneratedKeys();
        while (rs.next()) {
            id = rs.getInt(1);
        }
        return id;
    }

    /**
     * Updates and if no records updated then inserting a new one.
     * @param updatePsQuery UPDATE query with ? placeholders.
     * @param insertPsQuery INSERT query with ? placeholders.
     * @param params {@link PreparedQuery} parameters for both queries.
     * @throws SQLException
     * @return ID of a newly inserted record, or {@code 0}
     */
    protected int updateOrInsert(String updatePsQuery, String insertPsQuery, Object... params) throws SQLException {
        int result = 0;

        var pq = new PreparedQuery(con, updatePsQuery);
        pq.addObjects(params);
        if (pq.executeUpdate() == 0) {
            pq.close();

            pq = new PreparedQuery(con, insertPsQuery);
            pq.addObjects(params);
            pq.executeInsert();
            result = lastInsertId(pq.getPrepared());
        }
        pq.close();

        return result;
    }

    protected Set<Integer> getIds(String tableName, String linkColumn, String selectColumn, int id) throws SQLException {
        Set<Integer> result = new HashSet<>();

        String query = SQL_SELECT + selectColumn + " FROM " + tableName + " WHERE " + linkColumn + "=?";

        PreparedStatement ps = con.prepareStatement(query);
        ps.setInt(1, id);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            result.add(rs.getInt(1));
        }
        ps.close();

        return result;
    }

    /**
     * Queries int list SELECT {@code selectColumn} FROM {@code tableName} WHERE {@code linkColumn} = {@code id} ORDER BY {@code posColumn}
     * @param tableName
     * @param linkColumn
     * @param selectColumn
     * @param posColumn
     * @param id
     * @return
     * @throws SQLException
     */
    protected List<Integer> getIds(String tableName, String linkColumn, String selectColumn, String posColumn, int id) throws SQLException {
        List<Integer> result = new ArrayList<>();

        String query = SQL_SELECT + selectColumn + " FROM " + tableName + " WHERE " + linkColumn + "=? " + " ORDER BY " + posColumn;
        try (var ps = con.prepareStatement(query)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                result.add(rs.getInt(1));
            }
        }

        return result;
    }

    protected Map<Integer, Set<Integer>> getGroupedIds(String tableName, String linkColumn, String selectColumn) throws SQLException {
        Map<Integer, Set<Integer>> result = new HashMap<>();

        String query = SQL_SELECT + linkColumn + "," + selectColumn + SQL_FROM + tableName + SQL_ORDER_BY + linkColumn;
        PreparedStatement ps = con.prepareStatement(query);

        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            int key = rs.getInt(1);
            int value = rs.getInt(2);

            Set<Integer> values = result.get(key);
            if (values == null) {
                result.put(key, values = new HashSet<>());
            }
            values.add(value);
        }
        ps.close();

        return result;
    }

    protected Map<Integer, List<Integer>> getGroupedIds(String tableName, String linkColumn, String selectColumn, String posColumn)
            throws SQLException {
        Map<Integer, List<Integer>> result = new HashMap<>();

        String query = SQL_SELECT + linkColumn + "," + selectColumn + SQL_FROM + tableName +
                // сортировка по выбираемому значению для однозначного порядка в случае нулевых позиций
                SQL_ORDER_BY + linkColumn + ", " + posColumn + ", " + selectColumn;
        PreparedStatement ps = con.prepareStatement(query);

        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            int key = rs.getInt(1);
            int value = rs.getInt(2);

            List<Integer> values = result.get(key);
            if (values == null) {
                result.put(key, values = new ArrayList<>());
            }
            values.add(value);
        }
        ps.close();

        return result;
    }

    protected <T> T getById(String tableName, int id, ObjectExtractor<T> extractor) throws SQLException {
        T result = null;

        String query = SQL_SELECT_ALL_FROM + tableName + SQL_WHERE + "id=?";

        PreparedStatement ps = con.prepareStatement(query);
        ps.setInt(1, id);

        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            result = extractor.extract(rs);
        }

        ps.close();

        return result;
    }

    protected void deleteById(String tableName, int id) throws SQLException {
        String query = SQL_DELETE_FROM + tableName + SQL_WHERE + "id=?";
        PreparedStatement ps = con.prepareStatement(query);
        ps.setInt(1, id);
        ps.executeUpdate();
        ps.close();
    }

    protected abstract class RecordUpdater<T extends Id> {
        public abstract String getInsertQuery() throws SQLException;

        public Pair<String, Integer> getUpdateQuery() throws SQLException {
            return null;
        }

        //TODO: Может понадобиться getInsertPrepared и getUpdatePrepared, если будут устанавливаться несколько полей и т.п.

        public abstract void fillCommonFields(T record, PreparedStatement ps) throws SQLException;
    }

    protected <T extends Id> void update(T record, RecordUpdater<T> updater) throws SQLException {
        PreparedStatement ps = null;
        if (record.getId() <= 0) {
            ps = con.prepareStatement(updater.getInsertQuery(), PreparedStatement.RETURN_GENERATED_KEYS);
        } else {
            Pair<String, Integer> updateQuery = updater.getUpdateQuery();
            if (updateQuery != null) {
                ps = con.prepareStatement(updateQuery.getFirst());
                ps.setInt(updateQuery.getSecond(), record.getId());
            }
        }

        updater.fillCommonFields(record, ps);

        ps.executeUpdate();

        if (record.getId() <= 0) {
            record.setId(SQLUtils.lastInsertId(ps));
        }
        ps.close();
    }

    protected void updateIds(String tableName, String linkColumn, String valueColumn, Object id, Set<Integer> values)
            throws SQLException {
        var query = SQL_DELETE_FROM + tableName + " WHERE " + linkColumn + "=?";
        var ps = con.prepareStatement(query);
        ps.setObject(1, id);
        ps.executeUpdate();
        ps.close();

        query = SQL_INSERT_INTO + tableName + "(" + linkColumn + "," + valueColumn + ") VALUES (?, ?)";
        ps = con.prepareStatement(query);
        ps.setObject(1, id);

        for (Integer paramId : values) {
            ps.setInt(2, paramId);
            ps.executeUpdate();
        }
        ps.close();
    }

    protected void updateIds(String tableName, String linkColumn, String valueColumn, String posColumn, int id,
            List<Integer> values) throws SQLException {
        var query = SQL_DELETE_FROM + tableName + " WHERE " + linkColumn + "=?";
        var ps = con.prepareStatement(query);
        ps.setInt(1, id);
        ps.executeUpdate();
        ps.close();

        int pos = 1;

        query = SQL_INSERT_INTO + tableName + "(" + linkColumn + "," + valueColumn + "," + posColumn
                + ") VALUES (?, ?, ?)";
        ps = con.prepareStatement(query);
        ps.setInt(1, id);

        for (Integer paramId : values) {
            ps.setInt(2, paramId);
            ps.setInt(3, pos++);
            ps.executeUpdate();
        }
        ps.close();
    }

    protected void updateColumn(String tableName, int id, String columnName, String value) throws SQLException {
        String query = "UPDATE " + tableName + " SET `" + columnName + "`=? WHERE id=?";
        PreparedStatement ps = con.prepareStatement(query);
        ps.setString(1, value);
        ps.setInt(2, id);
        ps.executeUpdate();
        ps.close();
    }

    @Deprecated
    protected String getPageLimit(Page page) {
        log.warndMethod("getPageLimit", "Page.getLimitSql");
        return page.getLimitSql();
    }

    @Deprecated
    protected void setRecordCount(Page page, PreparedStatement ps) throws SQLException {
        log.warndMethod("setRecordCount", "Page.setRecordCount");
        if (page != null) {
            page.setRecordCount(ps);
        }
    }

    @Deprecated
    protected String getPeriodSql(Period period, String fieldName) {
        log.warndMethod("getPeriodSql", null);
        StringBuilder sql = new StringBuilder();
        if (period != null && fieldName != null) {
            if (period.getDateFrom() != null) {
                sql.append(" AND ");
                sql.append(fieldName);
                sql.append(" >= ? ");
            }
            if (period.getDateTo() != null) {
                sql.append(" AND ");
                sql.append(fieldName);
                sql.append(" <= ? ");
            }
        }
        return sql.toString();
    }

    @Deprecated
    protected int setPeriodParamValue(Period period, PreparedStatement ps, int index) throws SQLException {
        log.warndMethod("setPeriodParamValue", null);
        if (period != null && ps != null) {
            if (period.getDateFrom() != null) {
                ps.setDate(index++, TimeUtils.convertDateToSqlDate(period.getDateFrom()));
            }
            if (period.getDateTo() != null) {
                ps.setDate(index++, TimeUtils.convertDateToSqlDate(period.getDateTo()));
            }
        }
        return index;
    }
}