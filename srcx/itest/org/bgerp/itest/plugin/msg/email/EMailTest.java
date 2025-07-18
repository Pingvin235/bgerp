package org.bgerp.itest.plugin.msg.email;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.bgerp.app.cfg.Setup;
import org.bgerp.itest.helper.ConfigHelper;
import org.bgerp.itest.helper.MessageHelper;
import org.bgerp.itest.helper.ProcessHelper;
import org.bgerp.itest.helper.ResourceHelper;
import org.bgerp.itest.kernel.customer.CustomerTest;
import org.bgerp.itest.kernel.process.ProcessTest;
import org.bgerp.itest.kernel.user.UserTest;
import org.bgerp.model.msg.Message;
import org.bgerp.model.msg.config.MessageTypeConfig;
import org.bgerp.plugin.msg.email.Plugin;
import org.bgerp.plugin.msg.email.message.MessageTypeEmail;
import org.testng.Assert;
import org.testng.annotations.Test;

import ru.bgcrm.model.process.TypeProperties;

@Test(groups = "email", priority = 100, dependsOnGroups = { "config", "openIface", "user", "message" })
public class EMailTest {
    private static final Plugin PLUGIN = Plugin.INSTANCE;
    private static final String TITLE = PLUGIN.getTitleWithPrefix();

    private int processTypeId;
    private MessageTypeEmail messageType;

    @Test
    public void processType() throws Exception {
        var props = new TypeProperties();
        props.setStatusIds(List.of(ProcessTest.statusOpenId, ProcessTest.statusProgressId, ProcessTest.statusDoneId));
        props.setCreateStatusId(ProcessTest.statusOpenId);
        props.setCloseStatusIds(Set.of(ProcessTest.statusDoneId));
        props.setConfig(ResourceHelper.getResource(this, "process.type.config.txt"));
        processTypeId = ProcessHelper.addType(TITLE, ProcessTest.processTypeTestGroupId, props).getId();
    }

    @Test
    public void config() throws Exception {
        ConfigHelper.addPluginConfig(PLUGIN, ConfigHelper.generateConstants(
                "PARAM_CUSTOMER_EMAIL_ID", CustomerTest.paramEmailId
            ) + ResourceHelper.getResource(this, "config.txt"));

        var messageTypeConfig = Setup.getSetup().getConfig(MessageTypeConfig.class);
        Assert.assertNotNull(messageType = (MessageTypeEmail) messageTypeConfig.getTypeMap().get(1));
    }

    @Test(dependsOnMethods = "processType")
    public void process() throws Exception {
        var process = ProcessHelper.addProcess(processTypeId, TITLE);
        ProcessHelper.addGroup(process, UserTest.groupAdminsId);
        ProcessHelper.addExecutor(process, UserTest.USER_ADMIN_ID, UserTest.groupAdminsId);
        ProcessHelper.addExecutor(process, UserTest.userKarlId, UserTest.groupAdminsId);

        MessageHelper.addHowToTestNoteMessage(process.getId(), this);

        var m = new Message()
            .withTypeId(messageType.getId()).withDirection(Message.DIRECTION_INCOMING).withProcessId(process.getId())
            .withFrom("test@bgerp.org")
            .withTo(messageType.getEmail() + ", test1@bgerp.org, CC: test2@bgerp.org, test3@bgerp.org, BCC: test4@bgerp.org")
            .withFromTime(Date.from(Instant.now().plus(Duration.ofDays(-2)))).withToTime(Date.from(Instant.now().plus(Duration.ofDays(-1)))).withUserId(UserTest.userFelixId)
            .withSubject(TITLE + " Incoming Message").withText(ResourceHelper.getResource(this, "process.message.txt"));
        MessageHelper.addMessage(m);
    }
}