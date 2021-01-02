package ru.bgcrm.util.distr.call;

import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipInputStream;

import com.google.common.annotations.VisibleForTesting;

import ru.bgcrm.util.Preferences;
import ru.bgcrm.util.Setup;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.ZipUtils;
import ru.bgcrm.util.sql.ConnectionPool;
import ru.bgcrm.util.sql.PreparedDelay;
import ru.bgcrm.util.sql.SQLUtils;

public class ExecuteSQL implements InstallationCall {
    private static final String SQL_PATCHES_HISTORY = "sql_patches_history";
    private String id = "";

    public boolean call(Preferences setup, File zip, String param) {
        boolean result = false;

        try {
            FileInputStream fis = new FileInputStream(zip);

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
            fis.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return result;
    }

    public void call(Connection con, String query) throws SQLException {
        String[] queries = query.split(";\\s*\n");

        executeSqlComands(con, queries, id);

        con.commit();
    }

    private void executeSqlComands(Connection con, String[] lines, String midStr) throws SQLException {
        Set<String> hashes = getQueryHashes(con, midStr);
        StringBuilder blockQuery = new StringBuilder();
        boolean blockStarted = false;
        boolean noHash = false;

        Statement st = con.createStatement();
        for (String line : lines) {
            if (Utils.isBlankString(line)) {
                continue;
            }

            if (line.indexOf("#BLOCK#") >= 0) {
                blockStarted = true;
                continue;
            } else if (line.indexOf("#ENDB#") >= 0) {
                blockStarted = false;

                String query = blockQuery.toString().replaceAll("delimiter\\s*\\$\\$", "")
                        .replaceAll("delimiter\\s*;", "").replaceAll("END\\$\\$", "END;");
                doQuery(st, query, hashes, noHash);

                blockQuery.setLength(0);
                continue;
            } else if (line.indexOf("#NOHASH#") >= 0) {
                noHash = true;
            } else if (line.indexOf("#ENDNO#") >= 0) {
                noHash = false;
            } else if (line.startsWith("--")) {
                continue;
            }

            //если читаем блок, то добавляем запросы в коллекцию
            if (blockStarted) {
                blockQuery.append(line);
                blockQuery.append(";\n");
                continue;
            }

            doQuery(st, line, hashes, noHash);
        }
        st.close();

        if (!hashes.isEmpty())
            updateHashes(con, Utils.toString(hashes), midStr);
    }

    @VisibleForTesting
    protected void doQuery(Statement st, String line, Set<String> hashes, boolean noHash) throws SQLException {
        String hash = Utils.getDigest(line);
        if (noHash || !hashes.contains(hash)) {
            try {
                long time = System.currentTimeMillis();
                st.executeUpdate(line);
                System.out.println("OK (" + (System.currentTimeMillis() - time) + " ms.) => " + line);

                if (!noHash) {
                    hashes.add(hash);
                }
            } catch (SQLException ex) {
                System.err.println("ERROR (" + ex.getErrorCode() + ") " + ex.getMessage() + " => " + line);
                throw ex;
            }
        }
    }

    // mid столбец никак не используется в данный момент, скопирован из биллинга
    @VisibleForTesting
    protected Set<String> getQueryHashes(Connection con, String mid) throws SQLException {
        Set<String> result = new HashSet<String>();

        if (!SQLUtils.tableExists(con, SQL_PATCHES_HISTORY)) {
            String sql = "CREATE TABLE " + SQL_PATCHES_HISTORY + "( "
                + " `mid` varchar(20) NOT NULL, "
                + " `versions` text, " +
                " PRIMARY KEY (`mid`))";
            con.createStatement().executeUpdate(sql);

            return result;
        }

        String query = "SELECT versions FROM " + SQL_PATCHES_HISTORY + " WHERE mid=?";
        PreparedStatement ps = con.prepareStatement(query);
        ps.setString(1, mid);

        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            result = Utils.toSet(rs.getString(1));
        }
        ps.close();

        return result;
    }

    private void updateHashes(Connection con, String versions, String mid) throws SQLException {
        String query = "UPDATE " + SQL_PATCHES_HISTORY + " SET versions=? WHERE mid=?";
        PreparedStatement ps = con.prepareStatement(query);
        ps.setString(1, versions);
        ps.setString(2, mid);

        if (ps.executeUpdate() == 0) {
            ps.close();

            query = "INSERT INTO " + SQL_PATCHES_HISTORY + "(versions, mid) VALUES (?,?)";
            ps = con.prepareStatement(query);
            ps.setString(1, versions);
            ps.setString(2, mid);

            ps.executeUpdate();
        }
        ps.close();
    }

    public static void clearHashById(String mid) {
        try (var con = Setup.getSetup().getDBConnectionFromPool();
            var pd = new PreparedDelay(con);) {

            pd.addQuery("DELETE FROM " + SQL_PATCHES_HISTORY);
            if (Utils.notBlankString(mid)) {
                pd.addQuery(" WHERE mid=?");
                pd.addString(mid);
            }
            pd.executeUpdate();

            con.commit();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
}