package org.bgerp.itest.plugin.sec.secret;

import org.bgerp.itest.helper.ConfigHelper;
import org.bgerp.itest.helper.ResourceHelper;
import org.bgerp.plugin.sec.secret.Plugin;
import org.testng.annotations.Test;

@Test(groups = "secret", priority = 100, dependsOnGroups = { "config", "openIface" })
public class SecretTest {
    private static final Plugin PLUGIN = Plugin.INSTANCE;

    @Test
    public void config() throws Exception {
        ConfigHelper.addIncludedConfig(PLUGIN, ResourceHelper.getResource(this, "config.txt"));
    }
}