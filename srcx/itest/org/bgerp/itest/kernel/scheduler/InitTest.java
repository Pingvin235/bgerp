package org.bgerp.itest.kernel.scheduler;

import org.bgerp.itest.helper.ConfigHelper;
import org.bgerp.itest.helper.ResourceHelper;
import org.testng.annotations.Test;

@Test(groups = "schedulerInit", dependsOnGroups = "configInit")
public class InitTest {
    public static volatile int configId;
    
    @Test
    public void addConfig() throws Exception {
        configId = ConfigHelper.addIncludedConfig("Scheduler", ResourceHelper.getResource(this, "config.txt"));
    }
}