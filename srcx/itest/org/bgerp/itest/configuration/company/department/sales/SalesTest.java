package org.bgerp.itest.configuration.company.department.sales;

import static org.bgerp.itest.kernel.config.ConfigTest.ROLE_EXECUTION_ID;
import static org.bgerp.itest.kernel.user.UserTest.USER_ADMIN_ID;

import java.io.File;
import java.util.Date;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import org.bgerp.itest.helper.ConfigHelper;
import org.bgerp.itest.helper.FileHelper;
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

import ru.bgcrm.dao.message.MessageDAO;
import ru.bgcrm.dao.message.MessageTypeEmail;
import ru.bgcrm.dao.message.config.MessageTypeConfig;
import ru.bgcrm.dao.process.ProcessLinkDAO;
import ru.bgcrm.model.CommonObjectLink;
import ru.bgcrm.model.message.Message;
import ru.bgcrm.model.process.Process;
import ru.bgcrm.model.process.ProcessGroup;
import ru.bgcrm.model.process.TypeProperties;
import ru.bgcrm.model.user.UserGroup;
import ru.bgcrm.util.Setup;
import ru.bgcrm.util.TimeUtils;

@Test(groups = "depSales", dependsOnGroups = { "user", "configProcessNotification", "process", "param" })
public class SalesTest {
    private int groupId;

    public static volatile int processTypeSaleId;

    public static volatile int queueSalesId;

    @Test
    public void addGroups() throws Exception {
        groupId = UserHelper.addGroup("Sales", 0);
        UserHelper.addUserGroups(USER_ADMIN_ID, Lists.newArrayList(new UserGroup(groupId, new Date(), null)));
    }

    @Test (dependsOnMethods = "addGroups")
    public void addTypes() throws Exception {
        var props = new TypeProperties();
        props.setStatusIds(Lists.newArrayList(ProcessTest.statusOpen, ProcessTest.statusToDo, ProcessTest.statusProgress, ProcessTest.statusWait,
                ProcessTest.statusDone, ProcessTest.statusRejected));
        props.setCreateStatus(ProcessTest.statusOpen);
        props.setCloseStatusIds(Sets.newHashSet(ProcessTest.statusDone, ProcessTest.statusRejected));
        props.setGroups(ProcessGroup.toProcessGroupSet(Sets.newHashSet(groupId), ROLE_EXECUTION_ID));
        props.setAllowedGroups(ProcessGroup.toProcessGroupSet(Sets.newHashSet(groupId), ROLE_EXECUTION_ID));
        props.setConfig(ConfigHelper.generateConstants("CONFIG_PROCESS_NOTIFICATIONS_ID", ConfigTest.configProcessNotificationId) +
                        ResourceHelper.getResource(this, "config.processType.txt"));

        processTypeSaleId = ProcessHelper.addType("Sale", 0, false, props);

        /* deadline, next appointment */
    }

    @Test (dependsOnMethods = "addTypes")
    public void addQueues() throws Exception {
        queueSalesId = ProcessHelper.addQueue("Sales", ResourceHelper.getResource(this, "queue.txt"), Sets.newHashSet(processTypeSaleId));
        UserHelper.addGroupQueues(groupId, Sets.newHashSet(queueSalesId));
    }

    private int userKarlId;
    private int userFriedrichId;

    @Test (dependsOnMethods = "addGroups")
    public void addUsers() throws Exception {
        userKarlId = UserHelper.addUser("Karl Marx", "karl", Lists.newArrayList(new UserGroup(groupId, new Date(), null)));
        userFriedrichId = UserHelper.addUser("Friedrich Engels", "friedrich", Lists.newArrayList(new UserGroup(groupId, new Date(), null)));
    }

    @Test (dependsOnMethods = "addUsers")
    public void addProcesses() throws Exception {
        var linkDao = new ProcessLinkDAO(DbTest.conRoot);
        var messageDao = new MessageDAO(DbTest.conRoot);

        var messageTypeConfig = Setup.getSetup().getConfig(MessageTypeConfig.class);
        var messageTypeEmail = (MessageTypeEmail) messageTypeConfig.getTypeMap().get(MessageTest.MESSAGE_TYPE_EMAIL_DEMO_ID);

        Assert.assertNotNull(messageTypeEmail);

        var processId = ProcessHelper.addProcess(processTypeSaleId, userFriedrichId, "BGERP order, messages");
        linkDao.addLink(new CommonObjectLink(Process.OBJECT_TYPE, processId, CustomerTest.LINK_TYPE_CONTACT, 
            CustomerTest.customerPersonIvan.getId(), CustomerTest.customerPersonIvan.getTitle()));

        var m = new Message()
            .setTypeId(messageTypeEmail.getId()).setDirection(Message.DIRECTION_INCOMING).setProcessId(processId)
            .setFrom("till.gates@corp.com").setTo(messageTypeEmail.getEmail())
            .setFromTime(TimeUtils.getDateWithOffset(-10))
            .setSubject("BGERP order").setText(ResourceHelper.getResource(this, "process.1.message.1.txt"));
        m.addAttach(FileHelper.addFile(new File("srcx/doc/_res/image.png")));
            
        messageDao.updateMessage(m);

        processId = ProcessHelper.addProcess(processTypeSaleId, userKarlId, "Buy your software");
        linkDao.addLink(new CommonObjectLink(Process.OBJECT_TYPE, processId, CustomerTest.LINK_TYPE_CUSTOMER, 
            CustomerTest.customerOrgNs.getId(), CustomerTest.customerOrgNs.getTitle()));
    }
}
