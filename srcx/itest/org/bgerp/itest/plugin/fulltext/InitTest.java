package org.bgerp.itest.plugin.fulltext;

import java.sql.Connection;

import org.bgerp.itest.helper.ConfigHelper;
import org.bgerp.itest.helper.PluginHelper;
import org.bgerp.itest.helper.ResourceHelper;
import org.bgerp.itest.kernel.db.DbTest;
import org.testng.Assert;
import org.testng.annotations.Test;

import ru.bgcrm.util.sql.SQLUtils;

@Test(groups = "fulltextInit", priority = 100, dependsOnGroups = { "configInit", "schedulerInit" })
public class InitTest {
    @Test
    public void initConfig() throws Exception {
        ConfigHelper.addIncludedConfig("Plugin FullText", 
            PluginHelper.initPlugin(new ru.bgcrm.plugin.fulltext.Plugin()) + ResourceHelper.getResource(this, "config.fulltext.txt"));
        
        ConfigHelper.addToConfig(org.bgerp.itest.kernel.scheduler.InitTest.configId, ResourceHelper.getResource(this, "config.scheduler.txt"));

        try (Connection con = DbTest.conPoolRoot.getDBConnectionFromPool()) {
            Assert.assertTrue(SQLUtils.tableExists(con, ru.bgcrm.plugin.fulltext.dao.SearchDAO.TABLE.trim()));
        }
        
        //TODO: Depends on customer and processes. Run initial indexing!
    }
}