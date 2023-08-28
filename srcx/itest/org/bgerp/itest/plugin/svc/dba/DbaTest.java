package org.bgerp.itest.plugin.svc.dba;

import org.bgerp.itest.helper.ConfigHelper;
import org.bgerp.itest.helper.ResourceHelper;
import org.testng.annotations.Test;

@Test(groups = "dba", priority = 100, dependsOnGroups = "config")
public class DbaTest {
    @Test
    public void config() throws Exception {
        ConfigHelper.addPluginConfig(org.bgerp.plugin.svc.dba.Plugin.INSTANCE, ResourceHelper.getResource(this, "config.txt"));
    }
}
