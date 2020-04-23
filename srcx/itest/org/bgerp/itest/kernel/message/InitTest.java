package org.bgerp.itest.kernel.message;

import org.bgerp.itest.helper.ConfigHelper;
import org.bgerp.itest.helper.ResourceHelper;
import org.testng.annotations.Test;

@Test(groups = "messageInit", dependsOnGroups = { "paramInit", "processInit", "schedulerInit" })
public class InitTest {
    // defined in config file
    public static final int MESSAGE_TYPE_EMAIL_DEMO_ID = 1;
    
    public static volatile int configId;
    
    @Test
    public void initConfig() throws Exception {
        var config = 
                ConfigHelper.generateConstants("PARAM_CUSTOMER_EMAIL", org.bgerp.itest.kernel.param.InitTest.paramCustomerEmailId) +
                ResourceHelper.getResource(this, "config.txt");
        configId = ConfigHelper.addIncludedConfig("Messages", config);
        
        ConfigHelper.addToConfig(org.bgerp.itest.kernel.scheduler.InitTest.configId, ResourceHelper.getResource(this, "configScheduler.txt"));
    }
}
