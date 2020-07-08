package org.bgerp.itest.plugin.report;

import org.bgerp.itest.helper.ConfigHelper;
import org.bgerp.itest.helper.PluginHelper;
import org.bgerp.itest.helper.ResourceHelper;
import org.testng.annotations.Test;

@Test(groups = "reportInit", priority = 100, dependsOnGroups = "configInit")
public class InitTest {
    @Test
    public void initConfig() throws Exception {
        ConfigHelper.addIncludedConfig("Plugin Report", 
            PluginHelper.initPlugin(new ru.bgcrm.plugin.report.Plugin()) + ResourceHelper.getResource(this, "config.txt"));
    }
}