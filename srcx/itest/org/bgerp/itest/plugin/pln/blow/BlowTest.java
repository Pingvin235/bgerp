package org.bgerp.itest.plugin.pln.blow;

import java.time.Duration;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.bgerp.cache.ProcessTypeCache;
import org.bgerp.itest.helper.ConfigHelper;
import org.bgerp.itest.helper.MessageHelper;
import org.bgerp.itest.helper.ProcessHelper;
import org.bgerp.itest.helper.ResourceHelper;
import org.bgerp.itest.helper.UserHelper;
import org.bgerp.itest.kernel.db.DbTest;
import org.bgerp.itest.kernel.process.ProcessTest;
import org.bgerp.itest.kernel.user.UserTest;
import org.bgerp.model.process.ProcessGroups;
import org.bgerp.model.process.link.ProcessLinkProcess;
import org.bgerp.plugin.pln.blow.Plugin;
import org.testng.annotations.Test;

import ru.bgcrm.dao.process.StatusChangeDAO;
import ru.bgcrm.model.process.Process;
import ru.bgcrm.model.process.StatusChange;
import ru.bgcrm.model.process.TypeProperties;

@Test(groups = "blow", priority = 100, dependsOnGroups = { "process", "user", "message" })
public class BlowTest {
    private static final Plugin PLUGIN = Plugin.INSTANCE;
    private static final String TITLE = PLUGIN.getTitleWithPrefix();

    private int userGroupId;

    private int userKarlId;
    private int userLeonId;
    private int userFelixId;

    private int processTypeId;
    private int processTypeTaskId;
    private int processTypeIncidentId;

    private int processQueueId;

    @Test
    public void userGroup() throws Exception {
        userGroupId = UserHelper.addGroup(TITLE, 0, "");

        UserHelper.addUserGroups(userKarlId = UserTest.userKarlId, userGroupId);
        UserHelper.addUserGroups(userLeonId = UserTest.userLeonId, userGroupId);
        UserHelper.addUserGroups(userFelixId = UserTest.userFelixId, userGroupId);
    }

    @Test(dependsOnMethods = "userGroup")
    public void processType() throws Exception {
        var props = new TypeProperties();
        props.setStatusIds(List.of(ProcessTest.statusOpenId, ProcessTest.statusProgressId, ProcessTest.statusWaitId, ProcessTest.statusDoneId));
        props.setGroups(new ProcessGroups(userGroupId));
        props.setCreateStatusId(ProcessTest.statusOpenId);
        props.setCloseStatusIds(Set.of(ProcessTest.statusDoneId));

        processTypeId = ProcessHelper.addType(TITLE, ProcessTest.processTypeTestGroupId, false, props).getId();
        processTypeTaskId = ProcessHelper.addType(TITLE + " Task", processTypeId, true, null).getId();
        processTypeIncidentId = ProcessHelper.addType(TITLE + " Incident", processTypeId, true, null).getId();
    }

    @Test(dependsOnMethods = "processType")
    public void processQueue() throws Exception {
        processQueueId = ProcessHelper.addQueue(TITLE,
            ResourceHelper.getResource(this, "process.queue.config.txt"),
            Set.of(processTypeTaskId, processTypeIncidentId));

        // only admin sees the queue
        UserHelper.addUserProcessQueues(UserTest.USER_ADMIN_ID, Set.of(processQueueId));
    }

    @Test(dependsOnMethods = { "processQueue", "userGroup" })
    public void config() throws Exception {
        // TODO: Highlight processes, staying too long in 'progress' status.
        ConfigHelper.addPluginConfig(PLUGIN,
            ConfigHelper.generateConstants(
                "BOARD_TITLE", TITLE,
                "PROCESS_QUEUE_ID", processQueueId,
                "COL_STATUS_CHANGED", 20,
                "COL_MESSAGES", 10,
                "COL_MESSAGES_UNREAD", 12,
                "PROCESS_STATUS_PROGRESS_ID", ProcessTest.statusProgressId,
                "PROCESS_STATUS_WAIT_ID", ProcessTest.statusWaitId,
                "PROCESS_TYPE_INCIDENT_ID", processTypeIncidentId,
                "GROUP_ID", userGroupId
            ) + ResourceHelper.getResource(this, "config.txt")
        );
    }

    @Test(dependsOnMethods = "processType")
    public void process() throws Exception {
        int processId = ProcessHelper.addProcess(processTypeId, UserTest.USER_ADMIN_ID, TITLE).getId();
        MessageHelper.addHowToTestNoteMessage(processId, this);

        processUi();
        processArch();
        processIncidents();
    }

    private void processUi() throws Exception {
        int processUiId = ProcessHelper.addProcess(processTypeTaskId, UserTest.USER_ADMIN_ID, TITLE + " UI").getId();
        MessageHelper.addNoteMessage(processUiId, userFelixId, Duration.ofDays(-30), "", "Upload multiple files");
        MessageHelper.addNoteMessage(processUiId, userFelixId, Duration.ofDays(-22), "", "Param phone copy values, remove values");
        MessageHelper.addNoteMessage(processUiId, userLeonId, Duration.ofDays(-17), "", "User photo");
        MessageHelper.addNoteMessage(processUiId, userLeonId, Duration.ofDays(-5), "", "Push notifications");

        var statusDao = new StatusChangeDAO(DbTest.conRoot);

        var process = addChildTask(processUiId, userFelixId, TITLE + " Update user name in right top after re-auth", userLeonId);
        statusDao.changeStatus(process, ProcessTypeCache.getProcessType(processTypeTaskId),
                new StatusChange(process.getId(), new Date(), userLeonId, ProcessTest.statusProgressId, ""));

        addChildTask(processUiId, userLeonId, TITLE + " Issues with messages scrolling", userFelixId);
    }

    private void processArch() throws Exception {
        var process = ProcessHelper.addProcess(processTypeTaskId, userKarlId, TITLE + " Architecture", 4);
        addChildTask(process.getId(), userKarlId, TITLE + " Download logs in UI", userFelixId);

        var statusDao = new StatusChangeDAO(DbTest.conRoot);

        process = addChildTask(process.getId(), userKarlId, TITLE + " Storing large amount of files", userLeonId);
        statusDao.changeStatus(process, ProcessTypeCache.getProcessType(processTypeTaskId),
                new StatusChange(process.getId(), new Date(), userLeonId, ProcessTest.statusWaitId, ""));
    }

    private void processIncidents() throws Exception {
        var process = ProcessHelper.addProcess(processTypeIncidentId, userLeonId, TITLE + " Tests are running too long", 4);
        ProcessHelper.addExecutor(process, userFelixId, userGroupId);

        ProcessHelper.addProcess(processTypeIncidentId, userLeonId, TITLE + " Update problem", 8);
    }

    private Process addChildTask(int processId, int userCreateId, String description, int userExecutorId) throws Exception {
        var result = ProcessHelper.addProcess(processTypeTaskId, userCreateId, description);

        ProcessHelper.addExecutor(result, userExecutorId, userGroupId);
        ProcessHelper.addLink(new ProcessLinkProcess.Made(processId, result.getId()));

        return result;
    }
}