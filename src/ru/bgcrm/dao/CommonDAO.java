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

import org.bgerp.util.Log;
import org.bgerp.util.sql.PreparedQuery;

import ru.bgcrm.model.BGException;
import ru.bgcrm.model.BGMessageException;
import ru.bgcrm.model.Id;
import ru.bgcrm.model.Page;
import ru.bgcrm.model.Pair;
import ru.bgcrm.model.Period;
import ru.bgcrm.util.TimeUtils;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.sql.SQLUtils;

public class CommonDAO {
    protected final Log log = Log.getLog(this.getClass());

    protected final static String SQL_SELECT = "SELECT ";
    protected final static String SQL_SELECT_ALL_FROM = "SELECT * FROM";
    protected final static String SQL_SELECT_COUNT_ROWS = "SELECT SQL_CALC_FOUND_ROWS ";
    protected final static String SQL_INSERT_IGNORE = "INSERT IGNORE INTO ";
    protected final static String SQL_INSERT = "INSERT INTO ";
    protected final static String SQL_VALUES = "VALUES ";
    protected final static String SQL_SET = " SET ";
    protected final static String SQL_UPDATE = "UPDATE ";
    protected final static String SQL_DELETE = "DELETE FROM ";
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
     * Use {@link #foundRows(Statement)}
     */
    @Deprecated
    protected int getFoundRows(Statement st) throws SQLException {
        return foundRows(st);
    }

    /**
     * Selects {@code FOUND_ROWS()} for given statement.
     * @param st
     * @return
     * @throws SQLException
     */
    protected int foundRows(Statement st) throws SQLException {
        int result = -1;
        ResultSet rs = st.executeQuery("SELECT FOUND_ROWS()");
        if (rs.last()) {
            result = rs.getInt(1);
        }
        return result;
    }

    /**
     * Will be changed to private later,
     * use {@link #getLikePatternEnd(String)}, {@link #getLikePatternStart(String)}, {@link #getLikePatternSub(String)}
     * instead.
     */
    @Deprecated
    public static final String getLikePattern(String substring, String mode) {
        StringBuilder builder = new StringBuilder();
        if (Utils.notBlankString(substring)) {
            if (mode == null || mode.equals("subs") || mode.equalsIgnoreCase("end")) {
                builder.append("%");
            }
            builder.append(substring);
            if (mode == null || mode.equals("subs") || mode.equalsIgnoreCase("start")) {
                builder.append("%");
            }
        }
        return builder.toString();
    }

    /**
     * Returns SQL LIKE pattern "%" + {@param substring} + "%"
     * @param substring
     * @return
     */
    public static final String getLikePatternSub(String substring) {
        return getLikePattern(substring, "subs");
    }

    /**
     * Returns SQL LIKE pattern {@param substring} + "%"
     * @param substring
     * @return
     */
    public static final String getLikePatternStart(String substring) {
        return getLikePattern(substring, "start");
    }

    /**
     * Returns SQL LIKE pattern "%" + {@param substring}
     * @param substring
     * @return
     */
    public static final String getLikePatternEnd(String substring) {
        return getLikePattern(substring, "end");
    }

    /**
     * Updates and if no
     * @param updatePsQuery UPDATE query with ? placeholders.
     * @param insertPsQuery INSERT query with ? placeholders.
     * @param params {@link PreparedQuery} parameters for both queries.
     * @throws SQLException
     */
    protected void updateOrInsert(String updatePsQuery, String insertPsQuery, Object... params) throws SQLException {
        var pq = new PreparedQuery(con, updatePsQuery);
        pq.addObjects(params);
        if (pq.executeUpdate() == 0) {
            pq.close();
            pq.setQuery(insertPsQuery);
            pq.addObjects(params);
            pq.executeUpdate();
        }
        pq.close();
    }

    /**
     * Generates page {@code LIMIT offset, rows} query.
     * @param page page instance.
     * @return
     */
    protected String getPageLimit(Page page) {
        StringBuilder sql = new StringBuilder(20);
        if (page != null && page.getPageSize() > 0 && page.getPageIndex() != Page.PAGE_INDEX_NO_PAGING) {
            sql.append(" LIMIT ");
            sql.append(page.getPageFirstRecordNumber());
            sql.append(", ");
            sql.append(page.getPageSize());
        }
        return sql.toString();
    }

    protected void setRecordCount(Page page, PreparedStatement ps) throws SQLException {
        if (page != null) {
            page.setRecordCount(foundRows(ps));
        }
    }

    protected String getPeriodSql(Period period, String fieldName) {
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

    protected int setPeriodParamValue(Period period, PreparedStatement ps, int index) throws SQLException {
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

    protected Set<Integer> getIds(String tableName, String linkColumn, String selectColumn, int id) throws SQLException {
        Set<Integer> result = new HashSet<Integer>();

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
    protected List<Integer> getIds(String tableName, String linkColumn, String selectColumn, String posColumn, int id)
            throws SQLException {
        List<Integer> result = new ArrayList<Integer>();

        String query = SQL_SELECT + selectColumn + " FROM " + tableName + " WHERE " + linkColumn + "=? "
                + " ORDER BY " + posColumn;

        try (var ps = con.prepareStatement(query)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                result.add(rs.getInt(1));
            }
        }

        return result;
    }

    protected Map<Integer, Set<Integer>> getGroupedIds(String tableName, String linkColumn, String selectColumn)
            throws BGException {
        try {
            Map<Integer, Set<Integer>> result = new HashMap<Integer, Set<Integer>>();

            String query = SQL_SELECT + linkColumn + "," + selectColumn + SQL_FROM + tableName + SQL_ORDER_BY
                    + linkColumn;
            PreparedStatement ps = con.prepareStatement(query);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                int key = rs.getInt(1);
                int value = rs.getInt(2);

                Set<Integer> values = result.get(key);
                if (values == null) {
                    result.put(key, values = new HashSet<Integer>());
                }
                values.add(value);
            }
            ps.close();

            return result;
        } catch (SQLException e) {
            throw new BGException(e);
        }
    }

    protected Map<Integer, List<Integer>> getGroupedIds(String tableName, String linkColumn, String selectColumn,
            String posColumn) throws BGException {
        try {
            Map<Integer, List<Integer>> result = new HashMap<Integer, List<Integer>>();

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
                    result.put(key, values = new ArrayList<Integer>());
                }
                values.add(value);
            }
            ps.close();

            return result;
        } catch (SQLException e) {
            throw new BGException(e);
        }
    }

    protected static interface ObjectExtractor<T> {
        public T extract(ResultSet rs) throws SQLException, BGException;
    }

    protected <T> T getById(String tableName, int id, ObjectExtractor<T> extractor) throws BGException {
        T result = null;

        try {
            String query = SQL_SELECT_ALL_FROM + tableName + SQL_WHERE + "id=?";

            PreparedStatement ps = con.prepareStatement(query);
            ps.setInt(1, id);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                result = extractor.extract(rs);
            }

            ps.close();
        } catch (SQLException e) {
            throw new BGException(e);
        }

        return result;
    }

    protected void deleteById(String tableName, int id) throws BGException {
        try {
            String query = SQL_DELETE + tableName + SQL_WHERE + "id=?";
            PreparedStatement ps = con.prepareStatement(query);
            ps.setInt(1, id);
            ps.executeUpdate();
            ps.close();
        } catch (SQLException e) {
            throw new BGException(e);
        }
    }

    protected abstract class RecordUpdater<T extends Id> {
        public abstract String getInsertQuery() throws SQLException;

        public Pair<String, Integer> getUpdateQuery() throws SQLException {
            return null;
        }

        //TODO: Может понадобиться getInsertPrepared и getUpdatePrepared, если будут устанавливаться несколько полей и т.п.

        public abstract void fillCommonFields(T record, PreparedStatement ps) throws SQLException;
    }

    protected <T extends Id> void update(T record, RecordUpdater<T> updater) throws BGException {
        try {
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
        } catch (SQLException e) {
            throw new BGException(e);
        }
    }

    protected void updateIds(String tableName, String linkColumn, String valueColumn, Object id, Set<Integer> values)
            throws SQLException {
        var query = SQL_DELETE + tableName + " WHERE " + linkColumn + "=?";
        var ps = con.prepareStatement(query);
        ps.setObject(1, id);
        ps.executeUpdate();
        ps.close();

        query = SQL_INSERT + tableName + "(" + linkColumn + "," + valueColumn + ") VALUES (?, ?)";
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
        var query = SQL_DELETE + tableName + " WHERE " + linkColumn + "=?";
        var ps = con.prepareStatement(query);
        ps.setInt(1, id);
        ps.executeUpdate();
        ps.close();

        int pos = 1;

        query = SQL_INSERT + tableName + "(" + linkColumn + "," + valueColumn + "," + posColumn
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

    public void updateColumn(String tableName, int id, String columnName, String value) throws SQLException {
        String query = "UPDATE " + tableName + " SET `" + columnName + "`=? WHERE id=?";
        PreparedStatement ps = con.prepareStatement(query);
        ps.setString(1, value);
        ps.setInt(2, id);
        ps.executeUpdate();
        ps.close();
    }

    protected void sqlToBgException(SQLException e) throws BGMessageException, BGException {
        if (e.getMessage().startsWith("Duplicate entry")) {
            throw new BGMessageException("Дубликат записи.");
        } else {
            throw new BGException(e);
        }
    }
}