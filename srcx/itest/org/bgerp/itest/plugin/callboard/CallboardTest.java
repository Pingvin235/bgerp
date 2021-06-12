package org.bgerp.itest.plugin.callboard;

import org.bgerp.itest.helper.ConfigHelper;
import org.bgerp.itest.helper.PluginHelper;
import org.bgerp.itest.helper.ResourceHelper;
import org.testng.annotations.Test;

@Test(groups = "callboard", priority = 100, dependsOnGroups = { "config" })
public class CallboardTest {
    @Test
    public void initConfig() throws Exception {
        ConfigHelper.addIncludedConfig("Plugin Callboard", 
            PluginHelper.initPlugin(new org.bgerp.plugin.callboard.Plugin()) + ResourceHelper.getResource(this, "config.txt"));
    }
}