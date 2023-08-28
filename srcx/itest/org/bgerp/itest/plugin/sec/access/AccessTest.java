package org.bgerp.itest.plugin.sec.access;

import org.bgerp.itest.helper.ConfigHelper;
import org.bgerp.itest.helper.ResourceHelper;
import org.bgerp.plugin.sec.access.Plugin;
import org.testng.annotations.Test;

@Test(groups = "access", priority = 100, dependsOnGroups = "config")
public class AccessTest {
    private static final Plugin PLUGIN = Plugin.INSTANCE;

    @Test
    public void config() throws Exception {
        ConfigHelper.addPluginConfig(PLUGIN, ResourceHelper.getResource(this, "config.txt"));
    }
}