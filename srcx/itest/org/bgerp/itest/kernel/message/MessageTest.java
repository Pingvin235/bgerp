package org.bgerp.itest.kernel.message;

import org.bgerp.itest.helper.ConfigHelper;
import org.bgerp.itest.helper.ResourceHelper;
import org.bgerp.itest.kernel.customer.CustomerTest;
import org.testng.Assert;
import org.testng.annotations.Test;

import ru.bgcrm.dao.message.MessageTypeEmail;
import ru.bgcrm.dao.message.MessageTypeNote;
import ru.bgcrm.dao.message.config.MessageTypeConfig;
import ru.bgcrm.model.config.TagConfig;
import ru.bgcrm.model.config.TagConfig.Tag;
import ru.bgcrm.util.Setup;

@Test(groups = "message", dependsOnGroups = { "customer", "process", "scheduler" })
public class MessageTest {
    public static volatile int configId;

    public static volatile MessageTypeEmail messageTypeEmailDemo;
    public static volatile MessageTypeNote messageTypeNote;

    public static volatile Tag tagAccess;
    public static volatile Tag tagSpecification;
    public static volatile Tag tagTodo;
    
    @Test
    public void addConfig() throws Exception {
        var config = 
                ConfigHelper.generateConstants("PARAM_CUSTOMER_EMAIL_ID", CustomerTest.paramEmailId) +
                ResourceHelper.getResource(this, "config.messages.txt");
        configId = ConfigHelper.addIncludedConfig("Messages", config);
        
        ConfigHelper.addToConfig(org.bgerp.itest.kernel.scheduler.SchedulerTest.configId, ResourceHelper.getResource(this, "config.scheduler.txt"));

        var messageTypeConfig = Setup.getSetup().getConfig(MessageTypeConfig.class);
        Assert.assertNotNull(messageTypeEmailDemo = (MessageTypeEmail) messageTypeConfig.getTypeMap().get(1));
        Assert.assertNotNull(messageTypeNote = (MessageTypeNote) messageTypeConfig.getTypeMap().get(100));

        var tagsConfig = Setup.getSetup().getConfig(TagConfig.class);
        Assert.assertNotNull(tagAccess = tagsConfig.getTagMap().get(1));
        Assert.assertNotNull(tagSpecification = tagsConfig.getTagMap().get(2));
        Assert.assertNotNull(tagTodo = tagsConfig.getTagMap().get(3));
    }
}
