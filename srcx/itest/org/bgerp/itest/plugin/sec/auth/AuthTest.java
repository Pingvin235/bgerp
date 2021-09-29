package org.bgerp.itest.plugin.sec.auth;

import org.bgerp.itest.helper.ConfigHelper;
import org.bgerp.itest.helper.PluginHelper;
import org.bgerp.itest.helper.ResourceHelper;
import org.testng.annotations.Test;

@Test(groups = "auth", priority = 100, dependsOnGroups = { "config" })
public class AuthTest {
    @Test
    public void initConfig() throws Exception {
        ConfigHelper.addIncludedConfig("Plugin Auth", 
            PluginHelper.initPlugin(new org.bgerp.plugin.sec.auth.Plugin()) + ResourceHelper.getResource(this, "config.txt"));
    }
}