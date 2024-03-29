package org.bgerp.itest.plugin.sec.auth;

import org.bgerp.itest.helper.ConfigHelper;
import org.bgerp.itest.helper.ResourceHelper;
import org.bgerp.plugin.sec.auth.Plugin;
import org.testng.annotations.Test;

@Test(groups = "auth", priority = 100, dependsOnGroups = "config")
public class AuthTest {
    private static final Plugin PLUGIN = Plugin.INSTANCE;

    @Test
    public void config() throws Exception {
        ConfigHelper.addPluginConfig(PLUGIN, ResourceHelper.getResource(this, "config.txt"));
    }
}