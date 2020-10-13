package org.bgerp.itest.plugin.blow;

import org.bgerp.itest.helper.ConfigHelper;
import org.bgerp.itest.helper.ResourceHelper;
import org.testng.annotations.Test;

@Test(groups = "blow", priority = 100, dependsOnGroups = "config")
public class BlowTest {
    public static volatile int configId;

    @Test
    public void initConfig() throws Exception {
        configId = ConfigHelper.addIncludedConfig("Plugin Blow", ResourceHelper.getResource(this, "config.txt"));
    }
}