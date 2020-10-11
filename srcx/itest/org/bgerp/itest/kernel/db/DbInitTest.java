package org.bgerp.itest.kernel.db;

import org.testng.annotations.Test;

import ru.bgcrm.plugin.PluginManager;
import ru.bgcrm.util.Setup;

/** For running tests without complete re-creation of DB, change dependsOnGroups to "db" */
@Test(groups = "dbInit", dependsOnGroups = "dbReset")
public class DbInitTest {
    public static final String DBNAME = "bgerp";

    @Test
    public void init() throws Exception {
        DbTest.initPoolAndConnection("/" + DBNAME);
        Setup.resetSetup(DbTest.conPoolRoot);
        PluginManager.init();
    }
}