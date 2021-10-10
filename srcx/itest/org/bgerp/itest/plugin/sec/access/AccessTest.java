package org.bgerp.itest.plugin.sec.access;

import org.bgerp.itest.helper.ConfigHelper;
import org.bgerp.itest.helper.PluginHelper;
import org.bgerp.itest.helper.ResourceHelper;
import org.testng.annotations.Test;

@Test(groups = "access", priority = 100, dependsOnGroups = { "config" })
public class AccessTest {
    @Test
    public void initConfig() throws Exception {
        ConfigHelper.addIncludedConfig("Plugin Access",
            PluginHelper.initPlugin(new org.bgerp.plugin.sec.access.Plugin()) + ResourceHelper.getResource(this, "config.txt"));
    }
}