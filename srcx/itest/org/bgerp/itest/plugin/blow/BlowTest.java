package org.bgerp.itest.plugin.blow;

import org.bgerp.itest.helper.ConfigHelper;
import org.bgerp.itest.helper.ResourceHelper;
import org.testng.annotations.Test;

@Test(groups = "blow", priority = 100, dependsOnGroups = "config")
public class BlowTest {
    @Test
    public void initConfig() throws Exception {
        ConfigHelper.addIncludedConfig("Plugin Blow", ResourceHelper.getResource(this, "config.txt"));
    }
}