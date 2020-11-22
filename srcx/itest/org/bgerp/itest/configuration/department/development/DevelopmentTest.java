package org.bgerp.itest.configuration.department.development;

import static org.bgerp.itest.kernel.config.ConfigTest.ROLE_EXECUTION_ID;
import static org.bgerp.itest.kernel.config.ConfigTest.configProcessNotificationId;
import static org.bgerp.itest.kernel.user.UserTest.USER_ADMIN_ID;

import java.util.Date;
import java.util.List;
import java.util.Set;

import com.beust.jcommander.internal.Lists;
import com.google.common.collect.Sets;

import org.bgerp.itest.helper.ConfigHelper;
import org.bgerp.itest.helper.MessageHelper;
import org.bgerp.itest.helper.ParamHelper;
import org.bgerp.itest.helper.ProcessHelper;
import org.bgerp.itest.helper.ResourceHelper;
import org.bgerp.itest.helper.UserHelper;
import org.bgerp.itest.kernel.process.ProcessTest;
import org.testng.annotations.Test;

import ru.bgcrm.model.param.Parameter;
import ru.bgcrm.model.process.Process;
import ru.bgcrm.model.process.ProcessGroup;
import ru.bgcrm.model.process.ProcessLinkProcess;
import ru.bgcrm.model.process.TypeProperties;
import ru.bgcrm.model.user.UserGroup;

@Test(groups = "depDev", dependsOnGroups = { "user", "configProcessNotification", "process", "param" })
public class DevelopmentTest {
    public static volatile int groupId;

    public static volatile int paramGitBranchId;

    public static volatile int processTypeProductId;
    public static volatile int processTypeTaskId;

    public static volatile int queueTasksId;

    public static volatile int userVladimirId;
    public static volatile int userLeonId;

    @Test
    public void addGroups() throws Exception {
        groupId = UserHelper.addGroup("Development", 0);
        UserHelper.addUserGroups(USER_ADMIN_ID, Lists.newArrayList(new UserGroup(groupId, new Date(), null)));
    }
    
    @Test
    public void addParams() throws Exception {
        paramGitBranchId = ParamHelper.addParam(Process.OBJECT_TYPE, Parameter.TYPE_TEXT, "GIT branch", ProcessTest.posParam += 2, "", "");
    }
    
    @Test(dependsOnMethods = { "addGroups", "addParams" })
    public void addTypes() throws Exception {
        processTypeProductId = ProcessHelper.addType("BGERP", 0, false, null);
        
        var props = new TypeProperties();
        props.setStatusIds(List.of(ProcessTest.statusOpenId, ProcessTest.statusProgressId, ProcessTest.statusWaitId, ProcessTest.statusDoneId, ProcessTest.statusRejectId));
        props.setCreateStatus(ProcessTest.statusOpenId);
        props.setCloseStatusIds(Set.of(ProcessTest.statusDoneId, ProcessTest.statusRejectId));
        props.setGroups(ProcessGroup.toProcessGroupSet(Set.of(groupId), ROLE_EXECUTION_ID));
        props.setAllowedGroups(ProcessGroup.toProcessGroupSet(Sets.newHashSet(groupId), ROLE_EXECUTION_ID));
        props.setConfig(ConfigHelper.generateConstants("CONFIG_PROCESS_NOTIFICATIONS_ID", configProcessNotificationId) +
                        ResourceHelper.getResource(this, "config.processType.txt"));

        //TODO: comment on reject status

        processTypeTaskId = ProcessHelper.addType("Task", processTypeProductId, false, props);

        //TODO: deadline, next appointment
        //TODO: branch name with link
    }

    @Test (dependsOnMethods = "addTypes")
    public void addQueues() throws Exception {
        queueTasksId = ProcessHelper.addQueue("Development", 
            ConfigHelper.generateConstants("GROUP_ID", groupId) +
            ResourceHelper.getResource(this, "queue.tasks.txt"), Sets.newHashSet(processTypeProductId));
        UserHelper.addGroupQueues(groupId, Sets.newHashSet(queueTasksId));

        /* queuePlanId = ProcessHelper.addQueue("BGERP / Plan", ResourceHelper.getResource(this, "queue.plan.txt"), Sets.newHashSet(queuePlanId));
        UserHelper.addGroupQueues(groupId, Sets.newHashSet(queuePlanId)); */
    }

    @Test (dependsOnMethods = "addGroups")
    public void addUsers() throws Exception {
        // make from the date of entrance in party
        userVladimirId = UserHelper.addUser("Vladimir Lenin", "vladimir", Lists.newArrayList(new UserGroup(groupId, new Date(), null)));
        userLeonId = UserHelper.addUser("Leon Trotsky", "leon", Lists.newArrayList(new UserGroup(groupId, new Date(), null)));
    }

    @Test (dependsOnMethods = "addUsers")
    public void addTypeConfig() throws Exception {
        // TODO: Configuration to assign Lenin on 'accept' status.
    }

    @Test(dependsOnMethods = { "addTypes", "addUsers" })
    public void addProcesses() throws Exception {
        addProcess1();
        addProcess2();
        addProcessX();
    }

    private void addProcess1() throws Exception {
        var process = ProcessHelper.addProcess(processTypeTaskId, userVladimirId, "UI");
        MessageHelper.addNoteMessage(process.getId(), userVladimirId, -30, "", "Upload multiple files");
        MessageHelper.addNoteMessage(process.getId(), userVladimirId, -22, "", "Param phone copy values, remove values");
        MessageHelper.addNoteMessage(process.getId(), userLeonId, -17, "", "User photo");
        MessageHelper.addNoteMessage(process.getId(), userLeonId, -5, "", "Push notifications");

        addChildTask(process.getId(), userVladimirId, "Update user name in right top after re-auth", userLeonId);
        addChildTask(process.getId(), userLeonId, "Issues with messages scrolling", userVladimirId);
    }

    private void addProcess2() throws Exception {
        var process = ProcessHelper.addProcess(processTypeTaskId, userVladimirId, "Administration", 4);
        addChildTask(process.getId(), userLeonId, "Download logs in UI", userVladimirId);
        addChildTask(process.getId(), userVladimirId, "Storing large amount of files", userLeonId);
    }

    private void addProcessX() throws Exception {
        var process = ProcessHelper.addProcess(processTypeTaskId, userLeonId, "Tests running too long", 4);
        ProcessHelper.addExecutor(process, userVladimirId, groupId);

        ProcessHelper.addProcess(processTypeTaskId, userLeonId, "Update problem", 8);
        //ProcessHelper.update
    }

    private int addChildTask(int processId, int userCreateId, String description, int userExecutorId) throws Exception {
        var childProcess = ProcessHelper.addProcess(processTypeTaskId, userCreateId, description);
        ProcessHelper.addExecutor(childProcess, userExecutorId, groupId);
        ProcessHelper.addLink(new ProcessLinkProcess.Made(processId, childProcess.getId()));
        return childProcess.getId();
    }

}