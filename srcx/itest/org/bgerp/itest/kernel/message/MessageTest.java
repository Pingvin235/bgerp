package org.bgerp.itest.kernel.message;

import org.bgerp.itest.helper.ConfigHelper;
import org.bgerp.itest.helper.ResourceHelper;
import org.bgerp.itest.kernel.customer.CustomerTest;
import org.testng.annotations.Test;

@Test(groups = "message", dependsOnGroups = { "customer", "process", "scheduler" })
public class MessageTest {
    // defined in config file
    public static final int MESSAGE_TYPE_EMAIL_DEMO_ID = 1;
    public static final int MESSAGE_TYPE_NOTE_ID = 100;
    
    public static volatile int configId;
    
    @Test
    public void addConfig() throws Exception {
        var config = 
                ConfigHelper.generateConstants("PARAM_CUSTOMER_EMAIL_ID", CustomerTest.paramEmailId) +
                ResourceHelper.getResource(this, "config.messages.txt");
        configId = ConfigHelper.addIncludedConfig("Messages", config);
        
        ConfigHelper.addToConfig(org.bgerp.itest.kernel.scheduler.SchedulerTest.configId, ResourceHelper.getResource(this, "config.scheduler.txt"));

       
    }
}
