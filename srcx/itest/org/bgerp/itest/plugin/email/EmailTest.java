package org.bgerp.itest.plugin.email;

import org.bgerp.itest.helper.ConfigHelper;
import org.bgerp.itest.helper.PluginHelper;
import org.bgerp.itest.helper.ResourceHelper;
import org.testng.annotations.Test;

@Test(groups = "email", priority = 100, dependsOnGroups = { "config", "openIface" })
public class EmailTest {
    @Test
    public void initConfig() throws Exception {
        ConfigHelper.addIncludedConfig("Plugin Email", 
            PluginHelper.initPlugin(new org.bgerp.plugin.msg.email.Plugin()) + ResourceHelper.getResource(this, "config.txt"));
    }
}