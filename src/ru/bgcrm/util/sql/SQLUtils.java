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

import ru.bgcrm.util.Utils;
import ru.bgerp.util.Log;

public class SQLUtils {
    private static final Log log = Log.getLog();

    private static final Map<String, String> formatMap = new ConcurrentHashMap<String, String>();
    public static Set<String> tables = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>(64));
    private static Set<String> existColumns = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>(32));

    /**
     * Безопасное закрытие одного соединения с БД.
     * Более быстр по сравнению с {@link #closeConnection(Connection...)}, т.к. на каждый вызов не создаётся массив.
     *
     * @param con - соединение.
     */
    public static final void closeConnection(Connection con) {
        if (con != null) {
            try {
                // если не проверять isClosed то коннекты замечательно
                // закрываются 2 раза, создавая отрицательное число активных
                // соединений в пуле
                if (!con.isClosed()) {
                    con.close();
                }
            } catch (SQLException ex) {
                log.error(ex.getMessage(), ex);
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

    /**
     * Безопасное закрытие одного или нескольких соединений с БД.
     *
     * @param con - одно или несколько соединений.
     */
    public static final void closeConnection(Connection... con) {
        for (Connection c : con) {
            if (c == null) {
                continue;
            }

            try {
                // если не проверять isClosed то коннекты замечательно
                // закрываются 2 раза, создавая отрицательное число активных
                // соединений в пуле
                if (!c.isClosed()) {
                    c.close();
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    public static int lastInsertId(PreparedStatement ps) throws SQLException {
        int id = -1;

        ResultSet rs = ps.getGeneratedKeys();
        if (rs.last())
            id = rs.getInt(1);

        return id;
    }

    /**
     * Проверка на существование таблицы в БД
     * @param con объект доступа к БД
     * @param tableName имя проверяемой таблицы
     * @return true - таблица существует, false - таблица не существует
     * или нет доступа к БД
     * @throws SQLException если возникают проблемы с доступом к БД
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
            Utils.log.error(ex);
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
                Utils.log.error(e.getMessage(), e);
            }

            if (result) {
                existColumns.add(key);
            }
        }

        return result;
    }

    /**
     * Каммит одного соединения с БД.
     * @param con - соединение.
     */
    public static final void commitConnection(Connection con) {
        if (con != null) {
            try {
                // если не проверять, то при autocommit=true падает в логах
                if (!con.getAutoCommit()) {
                    con.commit();
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    /**
     * Функция устанавливает автоматическое подтвержение изменений (autocommit)
     * для указанного соединения
     * @param connection
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
     * Преобразует формат даты под SimpleDateFormat в формат для MySQL функции DATE_FORMAT.
     * @param format
     * @return
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
        Set<String> result = new HashSet<String>();

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
