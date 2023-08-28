package org.bgerp.itest.plugin.msg.email;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.bgerp.itest.helper.ConfigHelper;
import org.bgerp.itest.helper.MessageHelper;
import org.bgerp.itest.helper.ProcessHelper;
import org.bgerp.itest.helper.ResourceHelper;
import org.bgerp.itest.kernel.message.MessageTest;
import org.bgerp.itest.kernel.process.ProcessTest;
import org.bgerp.itest.kernel.user.UserTest;
import org.bgerp.plugin.msg.email.Plugin;
import org.testng.annotations.Test;

import ru.bgcrm.model.message.Message;
import ru.bgcrm.model.process.TypeProperties;
import ru.bgcrm.model.user.User;

@Test(groups = "email", priority = 100, dependsOnGroups = { "config", "openIface", "user", "message" })
public class EMailTest {
    private static final Plugin PLUGIN = Plugin.INSTANCE;
    private static final String TITLE = PLUGIN.getTitleWithPrefix();

    private int processTypeId;

    public void processType() throws Exception {
        var props = new TypeProperties();
        props.setStatusIds(List.of(ProcessTest.statusOpenId, ProcessTest.statusProgressId, ProcessTest.statusDoneId));
        props.setCreateStatus(ProcessTest.statusOpenId);
        props.setCloseStatusIds(Set.of(ProcessTest.statusDoneId));
        props.setConfig(ResourceHelper.getResource(this, "process.type.config.txt"));
        processTypeId = ProcessHelper.addType(TITLE, ProcessTest.processTypeTestGroupId, false, props).getId();
    }

    @Test
    public void config() throws Exception {
        ConfigHelper.addPluginConfig(PLUGIN, ResourceHelper.getResource(this, "config.txt"));
    }

    @Test(dependsOnMethods = "processType")
    public void process() throws Exception {
        var process = ProcessHelper.addProcess(processTypeId, User.USER_SYSTEM_ID, TITLE);
        ProcessHelper.addGroup(process, UserTest.groupAdminsId);
        ProcessHelper.addExecutor(process, UserTest.USER_ADMIN_ID, UserTest.groupAdminsId);
        ProcessHelper.addExecutor(process, UserTest.userKarlId, UserTest.groupAdminsId);

        MessageHelper.addHowToTestNoteMessage(process.getId(), this);

        var m = new Message()
            .withTypeId(MessageTest.messageTypeEmailDemo.getId()).withDirection(Message.DIRECTION_INCOMING).withProcessId(process.getId())
            .withFrom("test@bgerp.org")
            .withTo(MessageTest.messageTypeEmailDemo.getEmail() + ", test1@bgerp.org, CC: test2@bgerp.org, test3@bgerp.org, BCC: test4@bgerp.org")
            .withFromTime(Date.from(Instant.now().plus(Duration.ofDays(-2)))).withToTime(Date.from(Instant.now().plus(Duration.ofDays(-1)))).withUserId(UserTest.userFelixId)
            .withSubject(TITLE + " Incoming Message").withText(ResourceHelper.getResource(this, "process.message.txt"));
        MessageHelper.addMessage(m);
    }
}