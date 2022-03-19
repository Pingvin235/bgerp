package org.bgerp.itest.plugin.fulltext;

import java.sql.Connection;

import org.bgerp.itest.helper.ConfigHelper;
import org.bgerp.itest.helper.ResourceHelper;
import org.bgerp.itest.kernel.db.DbTest;
import org.testng.Assert;
import org.testng.annotations.Test;

import ru.bgcrm.plugin.fulltext.Plugin;
import ru.bgcrm.util.sql.SQLUtils;

@Test(groups = "fulltext", priority = 100, dependsOnGroups = { "config", "scheduler" })
public class FullTextTest {
    private static final Plugin PLUGIN = new Plugin();

    @Test
    public void config() throws Exception {
        ConfigHelper.addIncludedConfig(PLUGIN, ResourceHelper.getResource(this, "config.fulltext.txt"));

        Connection con = DbTest.conRoot;
        Assert.assertTrue(SQLUtils.tableExists(con, ru.bgcrm.plugin.fulltext.dao.SearchDAO.TABLE.trim()));

        //TODO: Depends on customer and processes. Run initial indexing!
    }
}