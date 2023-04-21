package org.bgerp.itest.kernel.db;

import java.sql.Connection;

import org.testng.annotations.Test;

import ru.bgcrm.util.ParameterMap;
import ru.bgcrm.util.Setup;
import ru.bgcrm.util.Utils;
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
    public static int initPoolAndConnection(String db) throws Exception {
        if (conPoolRoot != null)
            conPoolRoot.close();

        conPoolRoot = initConnectionPool(db);

        conRoot = conPoolRoot.getDBConnectionFromPool();
        if (conRoot == null) {
            Utils.errorAndExit(10, "SQL connection error");
        }
        conRoot.setAutoCommit(true);

        return 0;
    }

    /**
     * Creates DB connection pool using JVM arguments, or properties file.
     * @param db
     * @return
     */
    public static ConnectionPool initConnectionPool(String db) {
        var setup = new Setup(false);

        final var dbUserKey = "db.user";
        final var dbPswdKey = "db.pswd";
        final var dbUrlKey = "db.url";

        final var dbUser = System.getProperty(dbUserKey);
        final var dbPswd = System.getProperty(dbPswdKey);

        final var cfg =
            // if some of parameters defined as VM arguments
            Utils.notBlankString(dbUser) || Utils.notBlankString(dbPswd) ?
            ParameterMap.of(
                dbUrlKey, "jdbc:mysql://" + System.getProperty("db.host", "localhost") + ":3306" + db,
                dbUserKey, dbUser,
                dbPswdKey, dbPswd
            ) :
            // bgerp.properties, 'db' parameter is ignored in this case
            setup;

        return new ConnectionPool("root", cfg);
    }
}