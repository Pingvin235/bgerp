package org.bgerp.itest.configuration.department.sales;

import static org.bgerp.itest.kernel.config.ConfigTest.ROLE_EXECUTION_ID;

import java.io.File;
import java.util.Date;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import org.bgerp.itest.configuration.department.development.DevelopmentTest;
import org.bgerp.itest.helper.ConfigHelper;
import org.bgerp.itest.helper.FileHelper;
import org.bgerp.itest.helper.MessageHelper;
import org.bgerp.itest.helper.ProcessHelper;
import org.bgerp.itest.helper.ResourceHelper;
import org.bgerp.itest.helper.UserHelper;
import org.bgerp.itest.kernel.config.ConfigTest;
import org.bgerp.itest.kernel.customer.CustomerTest;
import org.bgerp.itest.kernel.db.DbTest;
import org.bgerp.itest.kernel.message.MessageTest;
import org.bgerp.itest.kernel.process.ProcessTest;
import org.bgerp.itest.kernel.user.UserTest;
import org.bgerp.plugin.msg.email.MessageTypeEmail;
import org.testng.annotations.Test;

import ru.bgcrm.dao.process.ProcessDAO;
import ru.bgcrm.model.message.Message;
import ru.bgcrm.model.process.ProcessExecutor;
import ru.bgcrm.model.process.ProcessGroup;
import ru.bgcrm.model.process.TypeProperties;
import ru.bgcrm.model.user.UserGroup;
import ru.bgcrm.util.TimeUtils;

@Test(groups = "depSales", dependsOnGroups = { "user", "configProcessNotification", "process", "param", "depDev", "document" })
public class SalesTest {
    private int groupId;

    public static volatile int processTypeSaleId;

    public static volatile int queueId;

    @Test
    public void addGroups() throws Exception {
        groupId = UserHelper.addGroup("Sales", 0);
    }

    @Test (dependsOnMethods = "addGroups")
    public void addTypes() throws Exception {
        var props = new TypeProperties();
        props.setStatusIds(Lists.newArrayList(ProcessTest.statusOpenId, ProcessTest.statusProgressId, ProcessTest.statusWaitId,
                ProcessTest.statusDoneId, ProcessTest.statusRejectId));
        props.setCreateStatus(ProcessTest.statusOpenId);
        props.setCloseStatusIds(Sets.newHashSet(ProcessTest.statusDoneId, ProcessTest.statusRejectId));
        props.setGroups(ProcessGroup.toProcessGroupSet(Sets.newHashSet(groupId), ROLE_EXECUTION_ID));
        props.setAllowedGroups(ProcessGroup.toProcessGroupSet(Sets.newHashSet(groupId, DevelopmentTest.groupId), ROLE_EXECUTION_ID));
        props.setConfig(ConfigHelper.generateConstants("CONFIG_PROCESS_NOTIFICATIONS_ID", ConfigTest.configProcessNotificationId) +
                        ResourceHelper.getResource(this, "config.processType.txt"));

        processTypeSaleId = ProcessHelper.addType("Sale", DevelopmentTest.processTypeProductId, false, props);

        //TODO: deadline, next appointment
    }

    @Test (dependsOnMethods = "addTypes")
    public void addQueues() throws Exception {
        queueId = ProcessHelper.addQueue("Sales", ResourceHelper.getResource(this, "queue.txt"), Sets.newHashSet(processTypeSaleId));
        UserHelper.addGroupQueues(groupId, Sets.newHashSet(queueId));

        UserHelper.addUserProcessQueues(UserTest.USER_ADMIN_ID, Set.of(queueId));
    }

    private int userKarlId;
    private int userFriedrichId;

    @Test (dependsOnMethods = "addGroups")
    public void addUsers() throws Exception {
        userKarlId = UserHelper.addUser("Karl Marx", "karl", Lists.newArrayList(new UserGroup(groupId, new Date(), null))).getId();
        userFriedrichId = UserHelper.addUser("Friedrich Engels", "friedrich", Lists.newArrayList(new UserGroup(groupId, new Date(), null))).getId();
    }

    @Test(dependsOnMethods = "addUsers")
    public void addProcesses() throws Exception {
        addProcess1();
        addProcess2();
    }

    // request of enhancements
    private void addProcess1() throws Exception {
        var mail = CustomerTest.CUSTOMER_ORG_NS_TILL_MAIL;
        var subject = "BGERP order";

        var processDao = new ProcessDAO(DbTest.conRoot);

        var process = ProcessHelper.addProcess(processTypeSaleId, userFriedrichId, subject);
        ProcessHelper.addCustomerLink(process.getId(), CustomerTest.LINK_TYPE_CUSTOMER, CustomerTest.customerOrgNs);

        // sales manager, Karl
        process.getExecutors().add(new ProcessExecutor(userKarlId, groupId, 0));

        // connect development group and developer Leon
        process.getGroups().add(new ProcessGroup(DevelopmentTest.groupId));
        processDao.updateProcessGroups(process.getGroups(), process.getId());
        process.getExecutors().add(new ProcessExecutor(DevelopmentTest.userLeonId, DevelopmentTest.groupId, 0));
        processDao.updateProcessExecutors(process.getExecutors(), process.getId());

        // original message
        var m = new Message()
            .withTypeId(MessageTest.messageTypeEmailDemo.getId()).withDirection(Message.DIRECTION_INCOMING).withProcessId(process.getId())
            .withFrom(mail).withTo(MessageTest.messageTypeEmailDemo.getEmail())
            .withFromTime(TimeUtils.getDateWithOffset(-10)).withToTime(TimeUtils.getDateWithOffset(-9)).withUserId(userKarlId)
            .withSubject(subject).withText(ResourceHelper.getResource(this, "process.1.message.1.txt"));
        m.addAttach(FileHelper.addFile(new File("srcx/doc/_res/image.png")));
        MessageHelper.addMessage(m);

        // comment for developers -> sales
        m = new Message()
            .withTypeId(MessageTest.messageTypeNote.getId()).withDirection(Message.DIRECTION_INCOMING).withProcessId(process.getId())
            .withFromTime(TimeUtils.getDateWithOffset(-9)).withUserId(userKarlId)
            .withSubject("Check possibility").withText(ResourceHelper.getResource(this, "process.1.message.2.txt"));
        MessageHelper.addMessage(m);

        //TODO: message 3 - response note from developer with link to related process

        // clarification
        m = new Message()
            .withTypeId(MessageTest.messageTypeEmailDemo.getId()).withDirection(Message.DIRECTION_OUTGOING).withProcessId(process.getId())
            .withFrom(MessageTest.messageTypeEmailDemo.getEmail()).withTo(mail)
            .withFromTime(TimeUtils.getDateWithOffset(-4)).withToTime(TimeUtils.getDateWithOffset(-4)).withUserId(userKarlId)
            .withSubject(MessageTypeEmail.RE_PREFIX + subject).withText(ResourceHelper.getResource(this, "process.1.message.4.txt"));
        MessageHelper.addMessage(m);
    }

    // simple sale
    private void addProcess2() throws Exception {
        var mail = CustomerTest.CUSTOMER_PERS_IVAN_MAIL;
        var subject = "Buy your software";

        var processDao = new ProcessDAO(DbTest.conRoot);

        var process = ProcessHelper.addProcess(processTypeSaleId, userKarlId, subject);
        ProcessHelper.addCustomerLink(process.getId(), CustomerTest.LINK_TYPE_CONTACT, CustomerTest.customerPersonIvan);

        // sales manager, Friedrich
        process.getExecutors().add(new ProcessExecutor(userFriedrichId, groupId, 0));
        processDao.updateProcessExecutors(process.getExecutors(), process.getId());

        var m = new Message()
            .withTypeId(MessageTest.messageTypeEmailDemo.getId()).withDirection(Message.DIRECTION_INCOMING).withProcessId(process.getId())
            .withFrom(mail).withTo(MessageTest.messageTypeEmailDemo.getEmail())
            .withFromTime(TimeUtils.getDateWithOffset(-5)).withToTime(TimeUtils.getDateWithOffset(-4)).withUserId(userFriedrichId)
            .withSubject(subject).withText(ResourceHelper.getResource(this, "process.2.message.1.txt"));
        MessageHelper.addMessage(m);

        m = new Message()
            .withTypeId(MessageTest.messageTypeEmailDemo.getId()).withDirection(Message.DIRECTION_OUTGOING).withProcessId(process.getId())
            .withFrom(MessageTest.messageTypeEmailDemo.getEmail()).withTo(mail)
            .withFromTime(TimeUtils.getDateWithOffset(-4)).withToTime(TimeUtils.getDateWithOffset(-3)).withUserId(userFriedrichId)
            .withSubject(MessageTypeEmail.RE_PREFIX + subject).withText(ResourceHelper.getResource(this, "process.2.message.2.txt"));
        MessageHelper.addMessage(m);
    }
}
