package org.bgerp.itest.configuration.department.support;

import static org.bgerp.itest.kernel.config.ConfigTest.ROLE_EXECUTION_ID;
import static org.bgerp.itest.kernel.user.UserTest.USER_ADMIN_ID;

import java.util.Date;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import org.bgerp.itest.configuration.department.development.DevelopmentTest;
import org.bgerp.itest.helper.ConfigHelper;
import org.bgerp.itest.helper.MessageHelper;
import org.bgerp.itest.helper.ProcessHelper;
import org.bgerp.itest.helper.ResourceHelper;
import org.bgerp.itest.helper.UserHelper;
import org.bgerp.itest.kernel.config.ConfigTest;
import org.bgerp.itest.kernel.customer.CustomerTest;
import org.bgerp.itest.kernel.db.DbTest;
import org.bgerp.itest.kernel.message.MessageTest;
import org.bgerp.itest.kernel.process.ProcessTest;
import org.testng.Assert;
import org.testng.annotations.Test;

import ru.bgcrm.cache.ProcessTypeCache;
import ru.bgcrm.dao.message.MessageDAO;
import ru.bgcrm.dao.message.MessageTypeEmail;
import ru.bgcrm.dao.process.ProcessDAO;
import ru.bgcrm.dao.process.StatusChangeDAO;
import ru.bgcrm.model.message.Message;
import ru.bgcrm.model.process.ProcessExecutor;
import ru.bgcrm.model.process.ProcessGroup;
import ru.bgcrm.model.process.StatusChange;
import ru.bgcrm.model.process.TypeProperties;
import ru.bgcrm.model.user.UserGroup;
import ru.bgcrm.util.TimeUtils;

@Test(groups = "depSupport", priority = 200, dependsOnGroups = { "configProcessNotification", "param", "depDev" })
public class SupportTest {
    public static volatile int groupId;

    public static volatile int processTypeSupportId;

    private int userFelixId;
    private int userVyacheslavId;

    public void addGroups() throws Exception {
        groupId = UserHelper.addGroup("Support", 0);
        UserHelper.addUserGroups(USER_ADMIN_ID, Lists.newArrayList(new UserGroup(groupId, new Date(), null)));
    }

    @Test(dependsOnMethods = "addGroups")
    public void addTypes() throws Exception {
        Assert.assertTrue(DevelopmentTest.processTypeProductId > 0);

        var props = new TypeProperties();
        props.setStatusIds(List.of(ProcessTest.statusOpenId, ProcessTest.statusProgressId, ProcessTest.statusWaitId, ProcessTest.statusDoneId));
        props.setCreateStatus(ProcessTest.statusOpenId);
        props.setCloseStatusIds(Set.of(ProcessTest.statusDoneId));
        props.setGroups(ProcessGroup.toProcessGroupSet(Set.of(groupId), ROLE_EXECUTION_ID));
        props.setAllowedGroups(ProcessGroup.toProcessGroupSet(Set.of(groupId, DevelopmentTest.groupId), ROLE_EXECUTION_ID));
        props.setConfig(ConfigHelper.generateConstants("CONFIG_PROCESS_NOTIFICATIONS_ID", ConfigTest.configProcessNotificationId) +
                        ResourceHelper.getResource(this, "config.processType.txt"));

        processTypeSupportId = ProcessHelper.addType("Support", DevelopmentTest.processTypeProductId, false, props);
    }

    @Test(dependsOnMethods = "addGroups")
    public void addUsers() throws Exception {
        userFelixId = UserHelper.addUser("Feliks Dserschinski", "felix", Lists.newArrayList(new UserGroup(groupId, new Date(), null)));
        userVyacheslavId = UserHelper.addUser("Vyacheslav Menzhinsky", "vyacheslav", Lists.newArrayList(new UserGroup(groupId, new Date(), null)));
    }

    @Test (dependsOnMethods =  "addTypes")
    public void addQueues() throws Exception {
        var queueId = ProcessHelper.addQueue("Support", 
            ConfigHelper.generateConstants("GROUP_ID", groupId) +
            ResourceHelper.getResource(this, "queue.txt"), Sets.newHashSet(processTypeSupportId));
        UserHelper.addGroupQueues(groupId, Sets.newHashSet(queueId));
        
        // TODO: Saved filters and counters for administrator.
        // TODO: Configure accept button in queue.
    }

    @Test(dependsOnMethods = "addUsers")
    public void addProcesses() throws Exception {
        addProcess1();
        addProcess2();
        addProcess3();
    }

    // simple question
    private void addProcess1() throws Exception {
        var mail = CustomerTest.CUSTOMER_ORG_NS_TILL_MAIL; 
        var subject = "BGERP install update problems";

        var processDao = new ProcessDAO(DbTest.conRoot);
        var statusDao = new StatusChangeDAO(DbTest.conRoot);

        var process = ProcessHelper.addProcess(processTypeSupportId, userFelixId, subject);
        ProcessHelper.addCustomerLink(process.getId(), CustomerTest.LINK_TYPE_CUSTOMER, CustomerTest.customerOrgNs);
        
        process.getExecutors().add(new ProcessExecutor(userFelixId, groupId, 0));
        processDao.updateProcessExecutors(process.getExecutors(), process.getId());

        var m = new Message()
            .setTypeId(MessageTest.messageTypeEmailDemo.getId()).setDirection(Message.DIRECTION_INCOMING).setProcessId(process.getId())
            .setFrom(mail).setTo(MessageTest.messageTypeEmailDemo.getEmail())
            .setFromTime(TimeUtils.getDateWithOffset(-7)).setToTime(TimeUtils.getDateWithOffset(-6)).setUserId(userFelixId)
            .setSubject(subject).setText(ResourceHelper.getResource(this, "process.1.message.1.txt"));
        MessageHelper.addMessage(m);

        statusDao.changeStatus(process, ProcessTypeCache.getProcessType(process.getTypeId()),
            new StatusChange(process.getId(), m.getToTime(), m.getUserId(), ProcessTest.statusProgressId, ""));

        m = new Message()
            .setTypeId(MessageTest.messageTypeEmailDemo.getId()).setDirection(Message.DIRECTION_OUTGOING).setProcessId(process.getId())
            .setFrom(MessageTest.messageTypeEmailDemo.getEmail()).setTo(mail)
            .setFromTime(TimeUtils.getDateWithOffset(-6)).setToTime(TimeUtils.getDateWithOffset(-5)).setUserId(userFelixId)
            .setSubject(subject = (MessageTypeEmail.RE_PREFIX + subject)).setText(ResourceHelper.getResource(this, "process.1.message.2.txt"));
        MessageHelper.addMessage(m);
        
        m = new Message()
            .setTypeId(MessageTest.messageTypeEmailDemo.getId()).setDirection(Message.DIRECTION_INCOMING).setProcessId(process.getId())
            .setFrom(mail).setTo(MessageTest.messageTypeEmailDemo.getEmail())
            .setFromTime(TimeUtils.getDateWithOffset(-4)).setToTime(TimeUtils.getDateWithOffset(-4)).setUserId(userFelixId)
            .setSubject(subject).setText(ResourceHelper.getResource(this, "process.1.message.3.txt"));
        MessageHelper.addMessage(m);

        statusDao.changeStatus(process, ProcessTypeCache.getProcessType(process.getTypeId()),
            new StatusChange(process.getId(), m.getToTime(), m.getUserId(), ProcessTest.statusDoneId, ""));
    }

    // connect development
    private void addProcess2() throws Exception {
        var mail =  CustomerTest.CUSTOMER_PERS_IVAN_MAIL;
        var subject = "The program is not running";

        var processDao = new ProcessDAO(DbTest.conRoot);
        var statusDao = new StatusChangeDAO(DbTest.conRoot);
        var messageDao = new MessageDAO(DbTest.conRoot);

        var process = ProcessHelper.addProcess(processTypeSupportId, userVyacheslavId, subject);
        ProcessHelper.addCustomerLink(process.getId(), CustomerTest.LINK_TYPE_CUSTOMER, CustomerTest.customerPersonIvan);

        // connect development group
        process.getGroups().add(new ProcessGroup(DevelopmentTest.groupId));
        processDao.updateProcessGroups(process.getGroups(), process.getId());

        process.getExecutors().add(new ProcessExecutor(userVyacheslavId, groupId, 0));
        process.getExecutors().add(new ProcessExecutor(DevelopmentTest.userVladimirId, DevelopmentTest.groupId, 0));
        processDao.updateProcessExecutors(process.getExecutors(), process.getId());

        var m = new Message()
            .setTypeId(MessageTest.messageTypeEmailDemo.getId()).setDirection(Message.DIRECTION_INCOMING).setProcessId(process.getId())
            .setFrom(mail).setTo(MessageTest.messageTypeEmailDemo.getEmail())
            .setFromTime(TimeUtils.getDateWithOffset(-5)).setToTime(TimeUtils.getDateWithOffset(-4)).setUserId(userVyacheslavId)
            .setSubject(subject).setText(ResourceHelper.getResource(this, "process.2.message.1.txt"));
        MessageHelper.addMessage(m);

        statusDao.changeStatus(process, ProcessTypeCache.getProcessType(process.getTypeId()),
            new StatusChange(process.getId(), m.getToTime(), m.getUserId(), ProcessTest.statusProgressId, ""));

        m = new Message()
            .setTypeId(MessageTest.messageTypeEmailDemo.getId()).setDirection(Message.DIRECTION_OUTGOING).setProcessId(process.getId())
            .setFrom(MessageTest.messageTypeEmailDemo.getEmail()).setTo(mail)
            .setFromTime(TimeUtils.getDateWithOffset(-4)).setToTime(TimeUtils.getDateWithOffset(-4)).setUserId(DevelopmentTest.userVladimirId)
            .setSubject(MessageTypeEmail.RE_PREFIX + subject).setText(ResourceHelper.getResource(this, "process.2.message.2.txt"));
        MessageHelper.addMessage(m);

        m = new Message()
            .setTypeId(MessageTest.messageTypeEmailDemo.getId()).setDirection(Message.DIRECTION_INCOMING).setProcessId(process.getId())
            .setFrom(mail).setTo(MessageTest.messageTypeEmailDemo.getEmail())
            .setFromTime(TimeUtils.getDateWithOffset(-3))
            .setSubject(subject).setText(ResourceHelper.getResource(this, "process.2.message.3.txt"));
        MessageHelper.addMessage(m);
        messageDao.updateMessageTags(m.getId(), Sets.newHashSet(MessageTest.tagAccess.getId()));
    }

    // new process to be taken from
    private void addProcess3() throws Exception {
        var mail =  "user@corp.org";
        var subject = "Demo server";

        var process = ProcessHelper.addProcess(processTypeSupportId, userVyacheslavId, subject);
        
        var m = new Message()
            .setTypeId(MessageTest.messageTypeEmailDemo.getId()).setDirection(Message.DIRECTION_OUTGOING).setProcessId(process.getId())
            .setFrom(MessageTest.messageTypeEmailDemo.getEmail()).setTo(mail)
            .setFromTime(new Date()).setUserId(DevelopmentTest.userVladimirId)
            .setSubject(subject).setText(ResourceHelper.getResource(this, "process.3.message.1.txt"));
        MessageHelper.addMessage(m);
    }
}