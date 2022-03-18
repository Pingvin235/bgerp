package org.bgerp.itest.plugin.bgbilling;

import org.bgerp.itest.helper.ConfigHelper;
import org.bgerp.itest.helper.ResourceHelper;
import org.testng.annotations.Test;

import ru.bgcrm.plugin.Plugin;

@Test(groups = "bgbilling", priority = 100, dependsOnGroups = "config")
public class BGBillingTest {
    private static final Plugin PLUGIN = new ru.bgcrm.plugin.bgbilling.Plugin();

    @Test
    public void config() throws Exception {
        ConfigHelper.addIncludedConfig(PLUGIN, ResourceHelper.getResource(this, "config.txt"));
    }
}
