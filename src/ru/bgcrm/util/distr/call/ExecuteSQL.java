package ru.bgcrm.util.distr.call;

import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.zip.ZipInputStream;

import com.google.common.annotations.VisibleForTesting;

import ru.bgcrm.util.Preferences;
import ru.bgcrm.util.Setup;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.ZipUtils;
import ru.bgcrm.util.sql.ConnectionPool;
import ru.bgcrm.util.sql.SQLUtils;

/**
 * Executer of SQL queries for updating DB structure.
 * Running as {@link InstallationCall} and also directly.
 *
 * @author Shamil Vakhitov
 */
public class ExecuteSQL implements InstallationCall {
    /** Table for storing applied SQL updates. */
    private static final String TABLE_DB_UPDATE = "db_update_log";
    /** Column with query hash. */
    private static final String HASH_COLUMN = "query_hash";

    @Override
    public boolean call(Preferences setup, File zip, String param) {
        boolean result = false;

        try (var fis = new FileInputStream(zip)) {
            Map<String, byte[]> map = ZipUtils.getEntriesFromZip(new ZipInputStream(fis), param);
            if (!map.containsKey(param)) {
                System.out.println("Can't find " + param + " in module zip!!!");
            } else {
                var pool = new ConnectionPool("exec", setup);
                try (var con = pool.getDBConnectionFromPool()) {
                    byte[] file = (byte[]) map.get(param);

                    String query = new String(file, StandardCharsets.UTF_8);

                    call(con, query);

                    System.out.println("Executing database update...OK");
                    result = true;
                } catch (Exception e) {
                    e.printStackTrace();
                }
                pool.close();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return result;
    }

    /**
     * Executes multiline SQL script.
     * @param con connection.
     * @param query SQL script, tokenized to commands by {@code ;}.
     * @throws SQLException
     */
    public void call(Connection con, String query) throws SQLException {
        String[] queries = query.split(";\\s*\n");

        executeSqlCommands(con, queries);

        con.commit();
    }

    /**
     * Executes SQL commands.
     * @param con
     * @param queries
     * @throws SQLException
     */
    private void executeSqlCommands(Connection con, String[] queries) throws SQLException {
        Set<String> existingHashes = getQueryHashes(con);
        Set<String> newHashes = new TreeSet<>();

        StringBuilder blockQuery = null;

        Statement st = con.createStatement();
        for (String query : queries) {
            if (Utils.isBlankString(query)) {
                continue;
            }

            if (query.indexOf("#BLOCK#") >= 0) {
                blockQuery = new StringBuilder();
                continue;
            } else if (query.indexOf("#ENDB#") >= 0) {
                if (blockQuery == null)
                    throw new IllegalStateException("Block query hasn't started.");

                String blockQueryStr = blockQuery.toString()
                        .replaceAll("delimiter\\s*\\$\\$", "")
                        .replaceAll("delimiter\\s*;", "")
                        .replaceAll("END\\$\\$", "END;");
                doQuery(st, blockQueryStr, existingHashes, newHashes);

                blockQuery = null;
                continue;
            } else if (query.startsWith("--")) {
                continue;
            }

            if (blockQuery != null) {
                blockQuery.append(query).append(";\n");
                continue;
            }

            doQuery(st, query, existingHashes, newHashes);
        }
        st.close();

        if (!newHashes.isEmpty())
            addHashes(con, newHashes);
    }

    /**
     * Executes SQL query.
     * @param st SQL statement, running the query.
     * @param query the query.
     * @param existingHashes hashes of already applied queries.
     * @param newHashes set there added hash of executed {@code query} if it wasn't presented in {@code hashes}.
     * @throws SQLException
     */
    @VisibleForTesting
    protected void doQuery(Statement st, String query, Set<String> existingHashes, Set<String> newHashes) throws SQLException {
        String hash = Utils.getDigest(query);
        if (existingHashes.contains(hash)) {
            return;
        }
        try {
            long time = System.currentTimeMillis();
            st.executeUpdate(query);
            System.out.println("OK (" + (System.currentTimeMillis() - time) + " ms.) => [" + hash + "] " + query);
            newHashes.add(hash);
        } catch (SQLException ex) {
            throw new SQLException(ex.getMessage() + " => [" + hash + "] " + query, ex);
        }
    }

    /**
     * Loads applied query hashes.
     * @param con SQL connection.
     * @return set with hashes.
     * @throws SQLException
     */
    @VisibleForTesting
    protected Set<String> getQueryHashes(Connection con) throws SQLException {
        Set<String> result = new TreeSet<>();

        if (!SQLUtils.tableExists(con, TABLE_DB_UPDATE)) {
            String sql =
                "CREATE TABLE IF NOT EXISTS " + TABLE_DB_UPDATE
                + "(dt DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, "
                + HASH_COLUMN + " CHAR(32) NOT NULL, " +
                " PRIMARY KEY (" + HASH_COLUMN + "))";
            con.createStatement().executeUpdate(sql);

            return result;
        }

        String query = "SELECT " + HASH_COLUMN + " FROM " + TABLE_DB_UPDATE;
        try (var ps = con.prepareStatement(query)) {
            var rs = ps.executeQuery();
            while (rs.next()) {
                result.add(rs.getString(1));
            }
        }

        return result;
    }

    /**
     * Stores executed queries hashes.
     * @param con SQL connection.
     * @param hashes set with hashes.
     * @throws SQLException
     */
    private void addHashes(Connection con, Set<String> hashes) throws SQLException {
        if (hashes.isEmpty())
            return;

        var sql = "INSERT INTO " + TABLE_DB_UPDATE + "(" + HASH_COLUMN + ") VALUES (?)";
        try (var ps = con.prepareStatement(sql)) {
            for (var hash : hashes) {
                ps.setString(1, hash);
                ps.executeUpdate();
            }
        }
    }

    public static void clearHashes() {
        try (var con = Setup.getSetup().getDBConnectionFromPool()) {
            con.createStatement().executeUpdate("DELETE FROM " + TABLE_DB_UPDATE);
            con.commit();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
}