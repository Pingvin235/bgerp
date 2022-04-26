package org.bgerp.itest.plugin.svc.log;

import org.bgerp.itest.helper.ConfigHelper;
import org.bgerp.itest.helper.ResourceHelper;
import org.testng.annotations.Test;

@Test(groups = "log", priority = 100, dependsOnGroups = "config")
public class LogTest {
    @Test
    public void config() throws Exception {
        ConfigHelper.addIncludedConfig(new org.bgerp.plugin.svc.log.Plugin(), ResourceHelper.getResource(this, "config.txt"));
    }
}