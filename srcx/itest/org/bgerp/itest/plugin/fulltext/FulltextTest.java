package org.bgerp.itest.plugin.fulltext;

import java.sql.Connection;

import org.bgerp.itest.helper.ConfigHelper;
import org.bgerp.itest.helper.PluginHelper;
import org.bgerp.itest.helper.ResourceHelper;
import org.bgerp.itest.kernel.db.DbTest;
import org.testng.Assert;
import org.testng.annotations.Test;

import ru.bgcrm.util.sql.SQLUtils;

@Test(groups = "fulltext", priority = 100, dependsOnGroups = { "config", "scheduler" })
public class FulltextTest {
    @Test
    public void initConfig() throws Exception {
        ConfigHelper.addIncludedConfig("Plugin FullText",
            PluginHelper.initPlugin(new ru.bgcrm.plugin.fulltext.Plugin()) + ResourceHelper.getResource(this, "config.fulltext.txt"));

        Connection con = DbTest.conRoot;
        Assert.assertTrue(SQLUtils.tableExists(con, ru.bgcrm.plugin.fulltext.dao.SearchDAO.TABLE.trim()));

        //TODO: Depends on customer and processes. Run initial indexing!
    }
}