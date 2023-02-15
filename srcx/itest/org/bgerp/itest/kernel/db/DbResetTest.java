package org.bgerp.itest.kernel.db;

import static org.bgerp.itest.kernel.db.DbInitTest.DBNAME;

import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.bgerp.util.Log;
import org.testng.annotations.Test;

import ru.bgcrm.util.Utils;
import ru.bgcrm.util.distr.call.ExecuteSQL;

@Test(groups = "dbReset", dependsOnGroups = "db")
public class DbResetTest {
    private static final Log log = Log.getLog();

    // Ugly workaround for disabling heavy operation.
    // Checked solutions, which didn't help:
    // 1. SkipException - skips all the dependent methods as well.
    // 2. BeforeClass - simple fails everything.
    // Also possibilities, didn't check completely:
    // 1. Assumptions for NG, again something with SkipException.
    // 2. Test listeners and so on.
    private boolean conditionalSkip() {
        boolean result = Utils.parseBoolean(System.getProperty("skip.dbReset"));
        if (result)
            log.info("Method is disabled");
        return result;
    }

    @Test
    public void cleanUp() throws Exception {
        if (conditionalSkip()) return;

        var con = DbTest.conRoot;

        log.info("Delete existing database..");
        con.createStatement().executeUpdate("DROP DATABASE IF EXISTS " + DBNAME);
        con.createStatement().executeUpdate("CREATE DATABASE " + DBNAME);
    }

    private static ExecuteSQL sqlCall = new ExecuteSQL() {
        @Override
        protected Set<String> getQueryHashes(Connection con) throws SQLException {
            return Collections.emptySet();
        }

        @Override
        protected void doQuery(Statement st, String query, Set<String> existingHashes, Set<String> newHashes) throws SQLException {
            try {
                log.debug("Executing: {}", query);
                st.executeUpdate(query);
            } catch (SQLException ex) {
                throw new SQLException("QUERY: " + query, ex);
            }
        }
    };

    @Test(dependsOnMethods = "cleanUp")
    public void createDb() throws Exception {
        if (conditionalSkip()) return;

        log.info("Creating database content..");

        var con = DbTest.conRoot;

        con.setAutoCommit(false);
        sqlCall.call(con, IOUtils.toString(new FileInputStream("build/bgerp/db_init.sql"), StandardCharsets.UTF_8));
        sqlCall.call(con, IOUtils.toString(new FileInputStream("build/update/db_init.sql"), StandardCharsets.UTF_8));
        con.setAutoCommit(true);
    }
}