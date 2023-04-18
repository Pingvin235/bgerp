package org.bgerp.itest.plugin.mobile;

import org.bgerp.itest.helper.ConfigHelper;
import org.bgerp.itest.helper.ResourceHelper;
import org.testng.annotations.Test;

import ru.bgcrm.plugin.mobile.Plugin;

@Test(groups = "mobile", priority = 100, dependsOnGroups = { "config", "usermobIface" })
public class MobileTest {
    private static final Plugin PLUGIN = Plugin.INSTANCE;

    @Test
    public void config() throws Exception {
        ConfigHelper.addIncludedConfig(PLUGIN, ResourceHelper.getResource(this, "config.txt"));
    }
}