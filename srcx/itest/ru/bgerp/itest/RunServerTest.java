package ru.bgerp.itest;

import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.Test;

import ru.bgcrm.util.Preferences;
import ru.bgcrm.util.distr.call.ExecuteSQL;
import ru.bgcrm.util.sql.ConnectionPool;
import ru.bgcrm.util.sql.SQLUtils;

@Test (groups = "runServer")
public class RunServerTest {
    private static final Logger log = Logger.getLogger(RunServerTest.class);
    
    private static final String PATH = "/opt/BGERP";
    private static final String DBNAME = "bgerp";
    
    private static File zip;
    private static volatile ConnectionPool conPoolRoot;
    
    @Test
    public void init() throws Exception {
        conPoolRoot = initConnectionPool("");
        
        // find zip
        File[] files = new File("build/bgerp").listFiles(f -> f.getName().matches("bgerp.+zip"));
        Assert.assertEquals(1, files.length);
        zip = files[0];
    }
    
    @Test(dependsOnMethods = "init")
    public void cleanUp() throws Exception {
        Connection con = conPoolRoot.getDBConnectionFromPool();
        try {
            log.info("Delete existing database..");            
            con.createStatement().executeUpdate("DROP DATABASE IF EXISTS " + DBNAME);
        } finally {
            SQLUtils.closeConnection(con);
        }
         
        // server
        File dir = new File(PATH);
        if (dir.exists()) {
            log.info("Delete existing installation: " + PATH);
            FileUtils.deleteDirectory(dir);
        }
    }
    
    @Test(dependsOnMethods = "cleanUp")
    public void unpackServer() throws Exception {
        log.info("Extracting BGERP zip: " + zip);
        
        // эта сложность со скриптом в надежде использовать его потом для установки сервера
        Process p = new ProcessBuilder("sh", "build/bgerp/install/bgerp.sh", zip.getPath()).start();
        Executors.newSingleThreadExecutor().submit(new StreamGobbler(p.getInputStream(), str -> log.debug(str)));
        Assert.assertEquals(true, p.waitFor(1, TimeUnit.MINUTES));
    }
    
    private static ExecuteSQL sqlCall = new ExecuteSQL() {
        @Override
        protected Set<String> getQueryHashes(Connection con, String mid) throws SQLException {
            return Collections.emptySet();
        }

        @Override
        protected void doQuery(Statement st, String line, Set<String> hashes, boolean noHash) throws SQLException {
            log.info("Executing: " + line);
            st.executeUpdate(line);
        }
    };
    
    @Test(dependsOnMethods = "unpackServer")
    public void createDb() throws Exception {
        log.info("Creating database..");
        
        Connection con = conPoolRoot.getDBConnectionFromPool();
        try {
            sqlCall.call(con, IOUtils.toString(new FileInputStream(PATH + "/db.sql"), StandardCharsets.UTF_8));
        } finally {
            SQLUtils.closeConnection(con);
        }
    }
    
    @Test(dependsOnMethods = "createDb")
    public void patchDb() throws Exception {
        log.info("Applying database patch..");
        
        conPoolRoot = initConnectionPool("/" + DBNAME);
        
        Connection con = conPoolRoot.getDBConnectionFromPool();
        try {
            sqlCall.call(con, IOUtils.toString(this.getClass().getResourceAsStream("RunServerTest.patch.db.sql"), StandardCharsets.UTF_8));
        } finally {
            SQLUtils.closeConnection(con);
        }
    }

    @Test(dependsOnMethods = "patchDb")
    public void serverStart() {
        Assert.assertEquals(true, true);
    }
    
    private ConnectionPool initConnectionPool(String db) {
        String cfg = "db.driver=com.mysql.jdbc.Driver\n" +
                "db.url=jdbc:mysql://" + System.getProperty("db.host", "localhost") + ":3306" + db + "?jdbcCompliantTruncation=false&useUnicode=true&characterEncoding=UTF-8&allowMultiQueries=true\n" +
                "db.user=" + System.getProperty("db.user") + "\n" +
                "db.pswd=" + System.getProperty("db.pswd") + "\n";
        return new ConnectionPool("root", new Preferences(cfg));
    }
}
