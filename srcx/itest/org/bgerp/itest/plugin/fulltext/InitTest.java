package org.bgerp.itest.plugin.fulltext;

import org.bgerp.itest.helper.ConfigHelper;
import org.bgerp.itest.helper.ResourceHelper;
import org.testng.annotations.Test;

@Test(groups = "fulltextInit", priority = 100, dependsOnGroups = { "configInit", "schedulerInit" })
public class InitTest {
    @Test
    public void initConfig() throws Exception {
        ConfigHelper.addIncludedConfig("Plugin Fulltext", ResourceHelper.getResource(this, "config.fulltext.txt"));
        
        ConfigHelper.addToConfig(org.bgerp.itest.kernel.scheduler.InitTest.configId, ResourceHelper.getResource(this, "config.scheduler.txt"));
        
        //TODO: Depends on customer and processes. Run initial indexing!
    }
}