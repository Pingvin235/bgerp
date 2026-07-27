package ru.bgcrm.util.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.bgerp.util.Log;

public class SQLUtils {
    private static final Log log = Log.getLog();

    private static final Map<String, String> formatMap = new ConcurrentHashMap<>();
    public static Set<String> tables = Collections.newSetFromMap(new ConcurrentHashMap<>(64));
    private static Set<String> existColumns = Collections.newSetFromMap(new ConcurrentHashMap<>(32));

    /**
     * Safe closing of DB connection if it isn't {@code null} and not closed already.
     * Faster comparing to {@link #closeConnection(Connection...)}, doesn't create arrays on every call.
     * @param con connection, {@code null} safe
     */
    public static final void closeConnection(Connection con) {
        if (con != null) {
            try {
                // if isClosed is not checked, connections happily close twice, creating
                // a negative number of active connections in the pool
                if (!con.isClosed()) {
                    con.close();
                }
            } catch (SQLException ex) {
                log.error(ex);
            }
        }
    }

    /**
     * Safe closing of DB connections if each of them isn't {@code null} and not closed already
     * @param con connections
     */
    public static final void closeConnection(Connection... con) {
        for (Connection c : con) {
            if (c == null) {
                continue;
            }

            try {
                // if isClosed is not checked, connections happily close twice, creating
                // a negative number of active connections in the pool
                if (!c.isClosed()) {
                    c.close();
                }
            } catch (Exception e) {
                log.error(e);
            }
        }
    }

    public static int getConnectionId(Connection connection) throws SQLException {
        String query = "SELECT CONNECTION_ID()";
        PreparedStatement ps = connection.prepareStatement(query);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            return rs.getInt(1);
        }
        ps.close();

        return -1;
    }

    public static int lastInsertId(PreparedStatement ps) throws SQLException {
        int id = -1;

        ResultSet rs = ps.getGeneratedKeys();
        if (rs.last())
            id = rs.getInt(1);

        return id;
    }

    /**
     * Checks whether a table exists in the DB
     * @param con the DB connection
     * @param tableName the checked table name
     * @return {@code true} if the table exists, {@code false} if it doesn't exist or the DB is not accessible
     */
    public static boolean tableExists(Connection con, String tableName) {
        boolean result = false;
        try {
            if (SQLUtils.tables.contains(tableName)) {
                result = true;
            } else {
                if (con != null && tableName != null) {
                    String query = "SHOW TABLES LIKE ?";
                    PreparedStatement ps = con.prepareStatement(query);
                    ps.setString(1, tableName);
                    ResultSet rs = ps.executeQuery();
                    if (rs.next())
                        result = true;

                    ps.close();
                }

                if (result) {
                    SQLUtils.tables.add(tableName);
                }
            }
        } catch (Exception ex) {
            log.error(ex);
        }

        return result;
    }

    public static boolean columnExist(Connection con, String table, String column) {
        boolean result = false;

        final String key = table + "." + column;

        result = existColumns.contains(key);
        if (!result) {
            try {
                String query = "SHOW COLUMNS FROM " + table;
                PreparedStatement ps = con.prepareStatement(query);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    if (rs.getString(1).equals(column)) {
                        result = true;
                        break;
                    }
                }
                ps.close();
            } catch (Exception e) {
                log.error(e);
            }

            if (result) {
                existColumns.add(key);
            }
        }

        return result;
    }

    /**
     * Commits a single DB connection
     * @param con the connection
     */
    public static final void commitConnection(Connection con) {
        if (con != null) {
            try {
                // if not checked, it fails in the logs when autocommit=true
                if (!con.getAutoCommit()) {
                    con.commit();
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    /**
     * Sets automatic commit (autocommit) for the given connection
     * @param connection the connection
     */
    public static final void setAutoCommit(Connection connection) {
        if (connection != null) {
            boolean alreadyAutoCommit = false;

            try {
                alreadyAutoCommit = connection.getAutoCommit();
            } catch (SQLException exception) {
                log.error(exception.getMessage());
            }

            if (!alreadyAutoCommit) {
                try {
                    connection.setAutoCommit(true);
                } catch (SQLException exception) {
                    log.error(exception.getMessage());
                }
            }
        }
    }

    /**
     * Converts a {@link java.text.SimpleDateFormat}-style date format to the format for the MySQL DATE_FORMAT function
     * @param format the input format
     * @return the converted format
     */
    public static final String javaDateFormatToSql(String format) {
        String result = formatMap.get(format);
        if (result == null) {
            result = format.replaceAll("yyyy", "%Y").replaceAll("MM", "%m").replaceAll("dd", "%d").replaceAll("HH", "%H").replaceAll("mm", "%i")
                    .replaceAll("ss", "%s");
            formatMap.put(format, result);
        }

        return result;
    }

    public Set<String> getTableColumns(Connection con, String tableName) throws SQLException {
        Set<String> result = new HashSet<>();

        String query = "SHOW COLUMNS FROM " + tableName;
        PreparedStatement ps = con.prepareStatement(query);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            result.add(rs.getString(1));
        }
        ps.close();

        return result;
    }
}
