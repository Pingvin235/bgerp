package org.bgerp.itest.plugin.bgbilling;

import org.bgerp.itest.helper.ConfigHelper;
import org.bgerp.itest.helper.PluginHelper;
import org.bgerp.itest.helper.ResourceHelper;
import org.testng.annotations.Test;

@Test(groups = "bgbilling", priority = 100, dependsOnGroups = "config")
public class BgbillingTest {
    @Test
    public void initConfig() throws Exception {
        ConfigHelper.addIncludedConfig("Plugin Bgbilling", 
            PluginHelper.initPlugin(new ru.bgcrm.plugin.bgbilling.Plugin()) + ResourceHelper.getResource(this, "config.txt"));
    }
}
