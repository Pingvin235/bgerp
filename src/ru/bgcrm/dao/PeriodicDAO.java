package ru.bgcrm.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * DAO with month tables support.
 *
 * @author Iakov Volkov
 * @author Michael Kozlov
 * @author Shamil Vakhitov
 */
public class PeriodicDAO extends CommonDAO {
    /** Cache of existing table names. */
    protected static final Set<String> EXISTING_TABLES = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());

    protected PeriodicDAO(Connection con) {
        super(con);
    }

    protected String checkAndCreateMonthTable(String tableNamePrefix, Date date, String createQuery) throws SQLException {
        var table = getMonthTableName(tableNamePrefix, date);
        if (tableExists(table))
            return table;

        var query = "CREATE TABLE IF NOT EXISTS `" + table + "` " + createQuery;
        try (var ps = con.prepareStatement(query)) {
            ps.executeUpdate();
            EXISTING_TABLES.add(table);
        }

        return table;
    }

    protected String getMonthTableName(String tableNamePrefix, Date date) {
        SimpleDateFormat getModuleMonthTableNameFormat = new SimpleDateFormat("_yyyyMM");
        StringBuilder sb = new StringBuilder(tableNamePrefix.trim());
        sb.append(getModuleMonthTableNameFormat.format(date));
        return sb.toString();
    }

    /**
     * Checks table existence.
     * @param tableName table name.
     * @return is the table exists.
     * @throws SQLException
     */
    protected boolean tableExists(String tableName) throws SQLException {
        if (EXISTING_TABLES.isEmpty()) {
            try (var ps = con.prepareStatement("SHOW TABLES")) {
                var rs = ps.executeQuery();
                while (rs.next()) {
                    EXISTING_TABLES.add(rs.getString(1));
                }
            }
        }
        return EXISTING_TABLES.contains(tableName);
    }
}
