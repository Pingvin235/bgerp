package org.bgerp.itest.kernel.message;

import java.time.Duration;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.bgerp.itest.helper.ConfigHelper;
import org.bgerp.itest.helper.MessageHelper;
import org.bgerp.itest.helper.ProcessHelper;
import org.bgerp.itest.helper.ResourceHelper;
import org.bgerp.itest.helper.UserHelper;
import org.bgerp.itest.kernel.customer.CustomerTest;
import org.bgerp.itest.kernel.db.DbTest;
import org.bgerp.itest.kernel.process.ProcessTest;
import org.bgerp.itest.kernel.user.UserTest;
import org.bgerp.plugin.msg.email.MessageTypeEmail;
import org.testng.Assert;
import org.testng.annotations.Test;

import ru.bgcrm.dao.ParamValueDAO;
import ru.bgcrm.dao.message.MessageTypeNote;
import ru.bgcrm.dao.message.config.MessageTypeConfig;
import ru.bgcrm.dao.process.ProcessDAO;
import ru.bgcrm.dao.process.ProcessLinkDAO;
import ru.bgcrm.model.customer.Customer;
import ru.bgcrm.model.message.TagConfig;
import ru.bgcrm.model.message.TagConfig.Tag;
import ru.bgcrm.model.param.ParameterEmailValue;
import ru.bgcrm.model.process.ProcessExecutor;
import ru.bgcrm.model.process.ProcessGroup;
import ru.bgcrm.model.process.ProcessLink;
import ru.bgcrm.model.user.UserGroup;
import ru.bgcrm.util.Setup;

@Test(groups = "message", dependsOnGroups = { "customer", "user", "process", "scheduler" })
public class MessageTest {
    private static final String TITLE = "Kernel Messages";

    public static volatile int configId;

    public static volatile MessageTypeEmail messageTypeEmailDemo;
    public static volatile MessageTypeNote messageTypeNote;

    public static volatile Tag tagAccess;
    public static volatile Tag tagSpecification;
    public static volatile Tag tagTodo;
    public static volatile Tag tagOpen;

    private static volatile int groupId;
    private static volatile int userId;
    private static volatile int processId;

    @Test
    public void config() throws Exception {
        var config =
                ConfigHelper.generateConstants(
                    "PARAM_CUSTOMER_EMAIL_ID", CustomerTest.paramEmailId,
                    "PARAM_CUSTOMER_PHONE_ID", CustomerTest.paramPhoneId
                ) +
                ResourceHelper.getResource(this, "config.messages.txt");
        configId = ConfigHelper.addIncludedConfig(TITLE, config);

        ConfigHelper.addToConfig(org.bgerp.itest.kernel.scheduler.SchedulerTest.configId, ResourceHelper.getResource(this, "config.scheduler.txt"));

        var messageTypeConfig = Setup.getSetup().getConfig(MessageTypeConfig.class);
        Assert.assertNotNull(messageTypeEmailDemo = (MessageTypeEmail) messageTypeConfig.getTypeMap().get(1));
        Assert.assertNotNull(messageTypeNote = (MessageTypeNote) messageTypeConfig.getTypeMap().get(100));

        var tagsConfig = Setup.getSetup().getConfig(TagConfig.class);
        Assert.assertNotNull(tagAccess = tagsConfig.getTagMap().get(1));
        Assert.assertNotNull(tagSpecification = tagsConfig.getTagMap().get(2));
        Assert.assertNotNull(tagTodo = tagsConfig.getTagMap().get(3));
        Assert.assertNotNull(tagOpen = tagsConfig.getTagMap().get(4));
    }

    @Test
    public void user() throws Exception {
        groupId = UserHelper.addGroup(TITLE, 0);
        userId = UserHelper.addUser(TITLE, "message", List.of(new UserGroup(groupId, new Date(), null))).getId();

        var paramDao = new ParamValueDAO(DbTest.conRoot);
        paramDao.updateParamEmail(userId, UserTest.paramEmailId, 0, new ParameterEmailValue("onlymail@domain.org"));
        paramDao.updateParamEmail(userId, UserTest.paramEmailId, 0, new ParameterEmailValue("mail@domain.org", TITLE + " Display"));
    }

    @Test(dependsOnMethods = { "user", "config" })
    public void process() throws Exception {
        processId = ProcessHelper.addProcess(ProcessTest.processTypeTestId, UserTest.USER_ADMIN_ID, TITLE).getId();
        var dao = new ProcessDAO(DbTest.conRoot);
        dao.updateProcessGroups(Set.of(new ProcessGroup(groupId)), processId);
        dao.updateProcessExecutors(Set.of(new ProcessExecutor(userId, groupId, 0)), processId);

        for (int i = 0; i < 100; i++) {
            MessageHelper.addNoteMessage(processId, UserTest.USER_ADMIN_ID, Duration.ofSeconds(i), "Test message " + i, "Test message " + i + " text");
        }

        MessageHelper.addNoteMessage(processId, UserTest.USER_ADMIN_ID, Duration.ofSeconds(101), "Line break", ResourceHelper.getResource(this, "log.txt"));
    }

    @Test(dependsOnMethods = "process")
    public void customer() throws Exception {
        new ProcessLinkDAO(DbTest.conRoot).addLink(new ProcessLink(processId, Customer.OBJECT_TYPE,
            CustomerTest.customerPersonIvan.getId(), CustomerTest.customerPersonIvan.getTitle()));
    }
}
