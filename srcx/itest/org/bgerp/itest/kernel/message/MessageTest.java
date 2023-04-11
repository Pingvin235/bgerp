package org.bgerp.itest.kernel.message;

import java.io.File;
import java.time.Duration;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.bgerp.itest.helper.ConfigHelper;
import org.bgerp.itest.helper.FileHelper;
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
import ru.bgcrm.dao.message.MessageDAO;
import ru.bgcrm.dao.message.MessageTypeCall;
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

@Test(groups = "message", dependsOnGroups = { "customer", "user", "process" })
public class MessageTest {
    static final String TITLE = "Kernel Message";

    // defined in config.messages
    public static final int CALL_MESSAGE_TYPE_ID = 50;

    public static volatile int configId;

    // TODO: Logically the message type has to be added by EMailTest.
    public static volatile MessageTypeEmail messageTypeEmailDemo;
    public static volatile MessageTypeNote messageTypeNote;
    public static volatile MessageTypeCall messageTypeCall;

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
                ResourceHelper.getResource(this, "config.txt");
        configId = ConfigHelper.addIncludedConfig(TITLE, config);

        var messageTypeConfig = Setup.getSetup().getConfig(MessageTypeConfig.class);
        Assert.assertNotNull(messageTypeEmailDemo = (MessageTypeEmail) messageTypeConfig.getTypeMap().get(1));
        Assert.assertNotNull(messageTypeNote = (MessageTypeNote) messageTypeConfig.getTypeMap().get(100));
        Assert.assertNotNull(messageTypeCall = (MessageTypeCall) messageTypeConfig.getTypeMap().get(50));

        var tagsConfig = Setup.getSetup().getConfig(TagConfig.class);
        Assert.assertNotNull(tagAccess = tagsConfig.getTagMap().get(1));
        Assert.assertNotNull(tagSpecification = tagsConfig.getTagMap().get(2));
        Assert.assertNotNull(tagTodo = tagsConfig.getTagMap().get(3));
        Assert.assertNotNull(tagOpen = tagsConfig.getTagMap().get(4));
    }

    @Test
    public void user() throws Exception {
        groupId = UserHelper.addGroup(TITLE, 0, UserHelper.GROUP_CONFIG_ISOLATION);
        userId = UserHelper.addUser(TITLE + " User", "message", List.of(new UserGroup(groupId, new Date(), null))).getId();

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
    }

    @Test(dependsOnMethods = "process")
    public void processMessage() throws Exception {
        int i = 0;

        for ( ; i < 100; i++)
            MessageHelper.addNoteMessage(processId, UserTest.USER_ADMIN_ID, Duration.ofSeconds(i), "Test message " + i, "Test message " + i + " text");

        MessageHelper.addNoteMessage(processId, UserTest.USER_ADMIN_ID, Duration.ofSeconds(i++), "Full width", "THE MESSAGE MUST BE SHOWN ON FULL-WIDTH SCREEN by hideLeftAreaOnScroll JS function");

        MessageHelper.addNoteMessage(processId, UserTest.USER_ADMIN_ID, Duration.ofSeconds(i++), "Line break", ResourceHelper.getResource(this, "log.txt"));

        var dao = new MessageDAO(DbTest.conRoot);

        var m = MessageHelper.addNoteMessage(processId, UserTest.USER_ADMIN_ID, Duration.ofSeconds(i++), "One Tag", "The message must contain a single tag.");
        dao.updateMessageTags(m.getId(), Set.of(MessageTest.tagAccess.getId()));

        m = MessageHelper.addNoteMessage(processId, UserTest.USER_ADMIN_ID, Duration.ofSeconds(i++), "Two Tags", "The message must contain two tags.");
        dao.updateMessageTags(m.getId(), Set.of(MessageTest.tagSpecification.getId(), MessageTest.tagTodo.getId()));

        m = MessageHelper.addNoteMessage(processId, UserTest.USER_ADMIN_ID, Duration.ofSeconds(i++), "Attachment", "The message must contain an attachment with preview.");
        m.addAttach(FileHelper.addFile(new File("srcx/doc/_res/image.png")));
        dao.updateMessage(m);
    }

    @Test(dependsOnMethods = "process")
    public void customer() throws Exception {
        new ProcessLinkDAO(DbTest.conRoot).addLink(new ProcessLink(processId, Customer.OBJECT_TYPE,
            CustomerTest.customerPersonIvan.getId(), CustomerTest.customerPersonIvan.getTitle()));
    }
}
