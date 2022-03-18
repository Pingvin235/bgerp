package org.bgerp.itest.plugin.report;

import org.bgerp.itest.helper.ConfigHelper;
import org.bgerp.itest.helper.ResourceHelper;
import org.bgerp.plugin.report.Plugin;
import org.testng.annotations.Test;

@Test(groups = "report", priority = 100, dependsOnGroups = "config")
public class ReportTest {
    private static final Plugin PLUGIN = new Plugin();

    @Test
    public void config() throws Exception {
        ConfigHelper.addIncludedConfig(PLUGIN, ResourceHelper.getResource(this, "config.txt"));
    }
}