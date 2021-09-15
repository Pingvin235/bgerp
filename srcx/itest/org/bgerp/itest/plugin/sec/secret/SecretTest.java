package org.bgerp.itest.plugin.sec.secret;

import org.bgerp.itest.helper.ConfigHelper;
import org.bgerp.itest.helper.PluginHelper;
import org.bgerp.itest.helper.ResourceHelper;
import org.testng.annotations.Test;

@Test(groups = "secret", priority = 100, dependsOnGroups = { "config", "openIface" })
public class SecretTest {
    @Test
    public void initConfig() throws Exception {
        ConfigHelper.addIncludedConfig("Plugin Secret", 
            PluginHelper.initPlugin(new org.bgerp.plugin.sec.secret.Plugin()) + ResourceHelper.getResource(this, "config.txt"));
    }
}