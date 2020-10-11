package org.bgerp.itest.plugin.mobile;

import org.bgerp.itest.helper.ConfigHelper;
import org.bgerp.itest.helper.PluginHelper;
import org.bgerp.itest.helper.ResourceHelper;
import org.testng.annotations.Test;

@Test(groups = "mobile", priority = 100, dependsOnGroups = "config")
public class MobileTest {
    @Test
    public void initConfig() throws Exception {
        ConfigHelper.addIncludedConfig("Plugin Mobile Android App", 
            PluginHelper.initPlugin(new ru.bgcrm.plugin.mobile.Plugin()) + ResourceHelper.getResource(this, "config.txt"));
    }
}