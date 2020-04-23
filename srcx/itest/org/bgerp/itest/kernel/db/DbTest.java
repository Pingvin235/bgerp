package org.bgerp.itest.kernel.db;

import org.testng.Assert;
import org.testng.annotations.Test;

import ru.bgcrm.util.Preferences;
import ru.bgcrm.util.sql.ConnectionPool;

@Test(groups = "db")
public class DbTest {
    public static volatile ConnectionPool conPoolRoot;

    @Test
    public void init() throws Exception {
        conPoolRoot = initConnectionPool("");
        try (var con = conPoolRoot.getDBConnectionFromPool()) {
            Assert.assertNotNull(con);
        }
    }
    
    public static ConnectionPool initConnectionPool(String db) {
        String cfg =
            "db.url=jdbc:mysql://" + System.getProperty("db.host", "localhost") + ":3306" + db + "\n" +
            "db.user=" + System.getProperty("db.user") + "\n" +
            "db.pswd=" + System.getProperty("db.pswd") + "\n";
        return new ConnectionPool("root", new Preferences(cfg));
    }
}