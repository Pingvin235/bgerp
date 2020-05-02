package org.bgerp.itest.kernel.message;

import java.io.File;
import java.io.FileInputStream;

import org.apache.commons.io.IOUtils;
import org.bgerp.itest.helper.ConfigHelper;
import org.bgerp.itest.helper.FileHelper;
import org.bgerp.itest.helper.ResourceHelper;
import org.testng.annotations.Test;

@Test(groups = "messageInit", dependsOnGroups = { "paramInit", "processInit", "schedulerInit" })
public class InitTest {
    // defined in config file
    public static final int MESSAGE_TYPE_EMAIL_DEMO_ID = 1;
    
    public static volatile int configId;
    
    @Test
    public void addConfig() throws Exception {
        var config = 
                ConfigHelper.generateConstants("PARAM_CUSTOMER_EMAIL_ID", org.bgerp.itest.kernel.param.InitTest.paramCustomerEmailId) +
                ResourceHelper.getResource(this, "config.messages.txt");
        configId = ConfigHelper.addIncludedConfig("Messages", config);
        
        ConfigHelper.addToConfig(org.bgerp.itest.kernel.scheduler.InitTest.configId, ResourceHelper.getResource(this, "config.scheduler.txt"));

        var file = new File("srcx/doc/_res/image.png");
        var fd = FileHelper.addFile(file);
        IOUtils.copy(new FileInputStream(file), fd.getOutputStream());
    }
}
