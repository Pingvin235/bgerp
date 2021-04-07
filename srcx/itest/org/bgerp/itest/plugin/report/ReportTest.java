package org.bgerp.itest.plugin.report;

import org.bgerp.itest.helper.ConfigHelper;
import org.bgerp.itest.helper.PluginHelper;
import org.bgerp.itest.helper.ResourceHelper;
import org.testng.annotations.Test;

@Test(groups = "report", priority = 100, dependsOnGroups = "config")
public class ReportTest {
    @Test
    public void initConfig() throws Exception {
        ConfigHelper.addIncludedConfig("Plugin Report", 
            PluginHelper.initPlugin(new org.bgerp.plugin.report.Plugin()) + ResourceHelper.getResource(this, "config.txt"));
    }
}