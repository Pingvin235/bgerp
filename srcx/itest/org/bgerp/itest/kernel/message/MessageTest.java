package org.bgerp.itest.kernel.message;

import java.io.File;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Set;

import org.bgerp.app.cfg.Setup;
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
import org.bgerp.model.msg.Message;
import org.bgerp.model.msg.config.TagConfig;
import org.bgerp.model.msg.config.TagConfig.Tag;
import org.bgerp.model.process.link.ProcessLink;
import org.bgerp.plugin.msg.email.message.MessageTypeEmail;
import org.testng.Assert;
import org.testng.annotations.Test;

import ru.bgcrm.dao.message.MessageDAO;
import ru.bgcrm.dao.message.MessageTypeCall;
import ru.bgcrm.dao.message.MessageTypeNote;
import ru.bgcrm.dao.process.ProcessLinkDAO;
import ru.bgcrm.model.customer.Customer;
import ru.bgcrm.model.message.config.MessageTypeConfig;

@Test(groups = "message", dependsOnGroups = { "customer", "user", "process" })
public class MessageTest {
    private static final String TITLE = "Kernel Message";

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

    private int processTypeId;
    private int process1Id;
    private int process2Id;

    @Test
    public void config() throws Exception {
        var config = ConfigHelper.generateConstants(
                "PARAM_CUSTOMER_EMAIL_ID", CustomerTest.paramEmailId,
                "PARAM_CUSTOMER_PHONE_ID", CustomerTest.paramPhoneId
            ) + ResourceHelper.getResource(this, "config.txt");
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
    public void processType() throws Exception {
        processTypeId = ProcessHelper.addType(TITLE, ProcessTest.processTypeTestGroupId, null).getId();
    }

    @Test(dependsOnMethods = "processType")
    public void processQueue() throws Exception {
        // title is not TITLE to be properly sorted in the queues list
        int queueId = ProcessHelper.addQueue("Kernel Process Message", ResourceHelper.getResource(this, "process.queue.config.txt"), Set.of(processTypeId));
        UserHelper.addUserProcessQueues(UserTest.USER_ADMIN_ID, Set.of(queueId));
    }

    @Test(dependsOnMethods = { "user", "config", "processType" })
    public void process() throws Exception {
        process1Id = ProcessHelper.addProcess(processTypeId, TITLE + " Many Messages, Large Messages, Tags").getId();
        process2Id = ProcessHelper.addProcess(processTypeId, TITLE + " Unread").getId();
    }

    @Test(dependsOnMethods = "process")
    public void process1Message() throws Exception {
        int i = 0;

        for ( ; i < 100; i++)
            MessageHelper.addNoteMessage(process1Id, UserTest.USER_ADMIN_ID, Duration.ofSeconds(i), "Test message " + i, "Test message " + i + " text");

        MessageHelper.addNoteMessage(process1Id, UserTest.USER_ADMIN_ID, Duration.ofSeconds(i++), "Full width", "THE MESSAGE MUST BE SHOWN ON FULL-WIDTH SCREEN by hideLeftAreaOnScroll JS function");

        MessageHelper.addNoteMessage(process1Id, UserTest.USER_ADMIN_ID, Duration.ofSeconds(i++), "Large text", ResourceHelper.getResource(this, "log.txt"));

        MessageHelper.addNoteMessage(process1Id, UserTest.USER_ADMIN_ID, Duration.ofSeconds(i++), "Long text line break",
            "The message lines must be wrapped. The message lines must be wrapped. The message lines must be wrapped. The message lines must be wrapped. " +
            "The message lines must be wrapped. The message lines must be wrapped. The message lines must be wrapped. The message lines must be wrapped. \n" +
            "Use 'Toggle line breaks' menu item to change it and enable scrolling.");

        var dao = new MessageDAO(DbTest.conRoot);

        var m = MessageHelper.addNoteMessage(process1Id, UserTest.USER_ADMIN_ID, Duration.ofSeconds(i++), "One Tag", "The message must contain a single tag.");
        dao.updateMessageTags(m.getId(), Set.of(MessageTest.tagAccess.getId()), false);

        m = MessageHelper.addNoteMessage(process1Id, UserTest.USER_ADMIN_ID, Duration.ofSeconds(i++), "Two Tags", "The message must be pinned on the first place and contain two tags.");
        dao.updateMessageTags(m.getId(), Set.of(TagConfig.Tag.TAG_PIN_ID, MessageTest.tagSpecification.getId(), MessageTest.tagTodo.getId()), false);

        m = MessageHelper.addNoteMessage(process1Id, UserTest.USER_ADMIN_ID, Duration.ofSeconds(i++), "Attachment", "The message must contain an attachment with preview.");
        m.addAttach(FileHelper.addFile(new File("srcx/doc/_res/image.png")));
        dao.updateMessage(m);
    }

    @Test(dependsOnMethods = "process")
    public void process2Message() throws Exception {
        var m = new Message()
            .withTypeId(MessageTest.messageTypeNote.getId())
            .withDirection(Message.DIRECTION_INCOMING)
            .withProcessId(process2Id)
            .withFromTime(Date.from(Instant.now()))
            .withUserId(UserTest.USER_ADMIN_ID)
            .withSubject("Unread message").withText("Unread message text");
        new MessageDAO(DbTest.conRoot).updateMessage(m);
    }

    @Test(dependsOnMethods = "process")
    public void customer() throws Exception {
        var dao = new ProcessLinkDAO(DbTest.conRoot);
        dao.addLink(new ProcessLink(process1Id, Customer.OBJECT_TYPE, CustomerTest.customerPersonIvan.getId(),
                CustomerTest.customerPersonIvan.getTitle()));
        dao.addLink(new ProcessLink(process2Id, Customer.OBJECT_TYPE, CustomerTest.customerOrgNs.getId(), CustomerTest.customerOrgNs.getTitle()));
    }

    @Test(dependsOnMethods =  "config")
    public void message() throws Exception {
        MessageHelper.addCallMessage(0, UserTest.USER_ADMIN_ID, Duration.ZERO, CustomerTest.CUSTOMER_PERS_IVAN_PHONE, "100",
                TITLE + " Unprocessed message from Ivan", "");
    }
}
