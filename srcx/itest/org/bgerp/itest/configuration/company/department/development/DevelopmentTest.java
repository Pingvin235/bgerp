package org.bgerp.itest.configuration.company.department.development;

import static org.bgerp.itest.kernel.config.ConfigTest.ROLE_EXECUTION_ID;
import static org.bgerp.itest.kernel.config.ConfigTest.configProcessNotificationId;
import static org.bgerp.itest.kernel.user.UserTest.USER_ADMIN_ID;

import java.util.Date;

import com.beust.jcommander.internal.Lists;
import com.google.common.collect.Sets;

import org.bgerp.itest.helper.ConfigHelper;
import org.bgerp.itest.helper.ProcessHelper;
import org.bgerp.itest.helper.ResourceHelper;
import org.bgerp.itest.helper.UserHelper;
import org.bgerp.itest.kernel.process.ProcessTest;
import org.testng.annotations.Test;

import ru.bgcrm.model.process.ProcessGroup;
import ru.bgcrm.model.process.TypeProperties;
import ru.bgcrm.model.user.UserGroup;

@Test(groups = "depDev", dependsOnGroups = { "user", "configProcessNotification", "process", "blow", "param" })
public class DevelopmentTest {
    private int groupId;

    public static volatile int processTypeProductId;
    public static volatile int processTypeSupportId;
    public static volatile int processTypePluginId;
    public static volatile int processTypeTaskId;

    private int queueTasksId;
    private int queuePlanId;
    
    @Test
    public void addGroups() throws Exception {
        groupId = UserHelper.addGroup("Development", 0);
        UserHelper.addUserGroups(USER_ADMIN_ID, Lists.newArrayList(new UserGroup(groupId, new Date(), null)));
    }
    
    @Test (dependsOnMethods = "addGroups")
    public void addTypes() throws Exception {
        var props = new TypeProperties();
        props.setStatusIds(Lists.newArrayList(ProcessTest.statusOpen, ProcessTest.statusToDo, ProcessTest.statusProgress, ProcessTest.statusWait));
        props.setCreateStatus(ProcessTest.statusOpen);
        props.setCloseStatusIds(Sets.newHashSet(ProcessTest.statusDone, ProcessTest.statusRejected));
        props.setGroups(ProcessGroup.toProcessGroupSet(Sets.newHashSet(groupId), ROLE_EXECUTION_ID));
        props.setAllowedGroups(ProcessGroup.toProcessGroupSet(Sets.newHashSet(groupId), ROLE_EXECUTION_ID));
        props.setConfig(ConfigHelper.generateConstants("CONFIG_PROCESS_NOTIFICATIONS_ID", configProcessNotificationId) +
                        ResourceHelper.getResource(this, "config.processType.txt"));

        processTypeProductId = ProcessHelper.addType("BGERP", 0, false, props);
        processTypeSupportId = ProcessHelper.addType("Support", processTypeProductId, true, null);
        processTypePluginId = ProcessHelper.addType("Plugin", processTypeProductId, true, null);
        processTypeTaskId = ProcessHelper.addType("Task", processTypeProductId, true, null);

        /* deadline, next appointment */
    }

    @Test (dependsOnMethods = "addTypes")
    public void addQueues() throws Exception {
        queueTasksId = ProcessHelper.addQueue("Tasks", ResourceHelper.getResource(this, "queue.tasks.txt"), Sets.newHashSet(queueTasksId));
        UserHelper.addGroupQueues(groupId, Sets.newHashSet(queueTasksId));
        queuePlanId = ProcessHelper.addQueue("Plan", ResourceHelper.getResource(this, "queue.plan.txt"), Sets.newHashSet(queuePlanId));
        UserHelper.addGroupQueues(groupId, Sets.newHashSet(queuePlanId));
    }

    @Test (dependsOnMethods = "addGroups")
    public void addUsers() throws Exception {
        // make from the date of entrance in party
        UserHelper.addUser("Vladimir Lenin", "vladimir", Lists.newArrayList(new UserGroup(groupId, new Date(), null)));
        UserHelper.addUser("Leon Trotsky", "leon", Lists.newArrayList(new UserGroup(groupId, new Date(), null)));
    }
}