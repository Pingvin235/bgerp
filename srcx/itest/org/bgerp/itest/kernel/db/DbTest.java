package org.bgerp.itest.kernel.db;

import java.sql.Connection;

import org.testng.Assert;
import org.testng.annotations.Test;

import ru.bgcrm.util.Preferences;
import ru.bgcrm.util.sql.ConnectionPool;

@Test(groups = "db")
public class DbTest {
    public static volatile ConnectionPool conPoolRoot;
    public static volatile Connection conRoot;

    /**
     * Primary connection, used for database creation and reset.
     * @throws Exception
     */
    @Test
    public void init() throws Exception {
        initPoolAndConnection("");
    }

    /**
     * Init connection pool and connection.
     * @param db
     * @return is not used, only to not be executed as an test.
     * @throws Exception
     */
    public static Object initPoolAndConnection(String db) throws Exception {
        if (conPoolRoot != null)
            conPoolRoot.close();

        conPoolRoot = initConnectionPool(db);

        conRoot = conPoolRoot.getDBConnectionFromPool();
        conRoot.setAutoCommit(true);
        Assert.assertNotNull(conRoot);

        return null;
    }
    
    public static ConnectionPool initConnectionPool(String db) {
        String cfg =
            "db.url=jdbc:mysql://" + System.getProperty("db.host", "localhost") + ":3306" + db + "\n" +
            "db.user=" + System.getProperty("db.user") + "\n" +
            "db.pswd=" + System.getProperty("db.pswd") + "\n";
        return new ConnectionPool("root", new Preferences(cfg));
    }
}