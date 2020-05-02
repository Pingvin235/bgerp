package org.bgerp.itest.plugin.mobile;

import org.bgerp.itest.helper.ConfigHelper;
import org.bgerp.itest.helper.ResourceHelper;
import org.testng.annotations.Test;

@Test(groups = "mobileInit", priority = 100, dependsOnGroups = "configInit")
public class InitTest {
    @Test
    public void initConfig() throws Exception {
        ConfigHelper.addIncludedConfig("Plugin Mobile Android App", ResourceHelper.getResource(this, "config.txt"));
    }
}