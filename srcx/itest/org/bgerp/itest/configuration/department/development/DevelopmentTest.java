package org.bgerp.itest.configuration.department.development;

import static org.bgerp.itest.kernel.config.ConfigTest.ROLE_EXECUTION_ID;
import static org.bgerp.itest.kernel.config.ConfigTest.configProcessNotificationId;
import static org.bgerp.itest.kernel.user.UserTest.userLeonId;
import static org.bgerp.itest.kernel.user.UserTest.userVladimirId;

import java.time.Duration;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Sets;

import org.bgerp.itest.configuration.department.Department;
import org.bgerp.itest.helper.ConfigHelper;
import org.bgerp.itest.helper.MessageHelper;
import org.bgerp.itest.helper.ParamHelper;
import org.bgerp.itest.helper.ProcessHelper;
import org.bgerp.itest.helper.ResourceHelper;
import org.bgerp.itest.helper.UserHelper;
import org.bgerp.itest.kernel.process.ProcessTest;
import org.bgerp.itest.kernel.user.UserTest;
import org.testng.annotations.Test;

import ru.bgcrm.model.param.Parameter;
import ru.bgcrm.model.process.Process;
import ru.bgcrm.model.process.ProcessGroup;
import ru.bgcrm.model.process.ProcessLinkProcess;
import ru.bgcrm.model.process.TypeProperties;

@Test(groups = "depDev", dependsOnGroups = { "user", "configProcessNotification", "process", "param" })
public class DevelopmentTest {
    private static final String TITLE = Department.TITLE + " Development";

    public static volatile int groupId;

    public static volatile int paramGitBranchId;
    private static volatile int paramSpecId;

    public static volatile int processTypeProductId;
    private static volatile int processTypeTaskId;

    private static volatile int queueId;

    @Test
    public void userGroup() throws Exception {
        groupId = UserHelper.addGroup(TITLE, 0, UserHelper.GROUP_CONFIG_ISOLATION);
        UserHelper.addUserGroups(userVladimirId, groupId);
        UserHelper.addUserGroups(userLeonId, groupId);
    }

    @Test
    public void param() throws Exception {
        paramGitBranchId = ParamHelper.addParam(Process.OBJECT_TYPE, Parameter.TYPE_TEXT, "GIT branch", ProcessTest.posParam += 2, "", "");
        paramSpecId = ParamHelper.addParam(Process.OBJECT_TYPE, Parameter.TYPE_FILE, "Specification", ProcessTest.posParam += 2,
            "multiple=1\n", "");
    }

    @Test(dependsOnMethods = { "userGroup", "param" })
    public void processType() throws Exception {
        processTypeProductId = ProcessHelper.addType("BGERP", 0, false, null).getId();

        var props = new TypeProperties();
        props.setStatusIds(List.of(ProcessTest.statusOpenId, ProcessTest.statusProgressId, ProcessTest.statusWaitId, ProcessTest.statusDoneId, ProcessTest.statusRejectId));
        props.setCreateStatus(ProcessTest.statusOpenId);
        props.setCloseStatusIds(Set.of(ProcessTest.statusDoneId, ProcessTest.statusRejectId));
        props.setParameterIds(List.of(paramGitBranchId, paramSpecId));
        props.setGroups(ProcessGroup.toProcessGroupSet(Set.of(groupId), ROLE_EXECUTION_ID));
        props.setAllowedGroups(ProcessGroup.toProcessGroupSet(Sets.newHashSet(groupId), ROLE_EXECUTION_ID));
        props.setConfig(ConfigHelper.generateConstants("CONFIG_PROCESS_NOTIFICATIONS_ID", configProcessNotificationId) +
                        ResourceHelper.getResource(this, "process.type.config.txt"));

        //TODO: comment on reject status

        processTypeTaskId = ProcessHelper.addType("Task", processTypeProductId, false, props).getId();

        //TODO: deadline, next appointment
        //TODO: generated branch name
    }

    @Test (dependsOnMethods = "processType")
    public void processQueue() throws Exception {
        queueId = ProcessHelper.addQueue(TITLE,
            ConfigHelper.generateConstants("GROUP_ID", groupId,
                "STATUS_OPEN_ID", ProcessTest.statusOpenId,
                "STATUS_PROGRESS_ID", ProcessTest.statusProgressId,
                "STATUS_WAIT_ID", ProcessTest.statusWaitId) +
            ResourceHelper.getResource(this, "queue.tasks.txt"), Sets.newHashSet(processTypeProductId));
        UserHelper.addGroupQueues(groupId, Sets.newHashSet(queueId));

        UserHelper.addUserProcessQueues(UserTest.USER_ADMIN_ID, Set.of(queueId));
    }

    @Test (dependsOnMethods = "userGroup")
    public void typeConfig() throws Exception {
        // TODO: Configuration to assign Lenin on 'accept' status.
    }

    @Test(dependsOnMethods = { "userGroup", "processType" })
    public void addProcesses() throws Exception {
        process1();
        process2();
        processX();
    }

    private void process1() throws Exception {
        var process = ProcessHelper.addProcess(processTypeTaskId, userVladimirId, "UI");
        MessageHelper.addNoteMessage(process.getId(), userVladimirId, Duration.ofDays(-30), "", "Upload multiple files");
        MessageHelper.addNoteMessage(process.getId(), userVladimirId, Duration.ofDays(-22), "", "Param phone copy values, remove values");
        MessageHelper.addNoteMessage(process.getId(), userLeonId, Duration.ofDays(-17), "", "User photo");
        MessageHelper.addNoteMessage(process.getId(), userLeonId, Duration.ofDays(-5), "", "Push notifications");

        addChildTask(process.getId(), userVladimirId, "Update user name in right top after re-auth", userLeonId);
        addChildTask(process.getId(), userLeonId, "Issues with messages scrolling", userVladimirId);
    }

    private void process2() throws Exception {
        var process = ProcessHelper.addProcess(processTypeTaskId, userVladimirId, "Administration", 4);
        addChildTask(process.getId(), userLeonId, "Download logs in UI", userVladimirId);
        addChildTask(process.getId(), userVladimirId, "Storing large amount of files", userLeonId);
    }

    private void processX() throws Exception {
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