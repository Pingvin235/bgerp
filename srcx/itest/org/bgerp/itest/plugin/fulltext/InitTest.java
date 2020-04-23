package org.bgerp.itest.plugin.fulltext;

import org.bgerp.itest.helper.ConfigHelper;
import org.bgerp.itest.helper.ResourceHelper;
import org.testng.annotations.Test;

@Test(groups = "fulltextInit", dependsOnGroups = { "configInit", "schedulerInit" })
public class InitTest {
    @Test
    public void initConfig() throws Exception {
        ConfigHelper.addIncludedConfig("Plugin Fulltext", ResourceHelper.getResource(this, "config.txt"));
        
        ConfigHelper.addToConfig(org.bgerp.itest.kernel.scheduler.InitTest.configId, ResourceHelper.getResource(this, "configScheduler.txt"));
        
        //TODO: Depends on customer and processes. Run initial indexing!
    }
}