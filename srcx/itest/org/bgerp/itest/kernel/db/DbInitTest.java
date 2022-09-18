package org.bgerp.itest.kernel.db;

import org.apache.log4j.PropertyConfigurator;
import org.testng.annotations.Test;

import ru.bgcrm.plugin.PluginManager;
import ru.bgcrm.util.Setup;

/** For running tests without complete re-creation of DB, change dependsOnGroups to "db" */
@Test(groups = "dbInit", dependsOnGroups = "dbReset")
public class DbInitTest {
    public static final String DBNAME = "bgerp";

    @Test
    public void init() throws Exception {
        PropertyConfigurator.configure("srcx/itest/log4j.properties");

        DbTest.initPoolAndConnection("/" + DBNAME);
        Setup.resetSetup(DbTest.conPoolRoot);
        // TODO: Workaround.
        Setup.getSetup().put("plugin.enable.default", "0");
        PluginManager.init();
    }
}