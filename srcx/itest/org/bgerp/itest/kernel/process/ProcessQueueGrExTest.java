package org.bgerp.itest.kernel.process;

import java.util.List;
import java.util.Set;

import org.bgerp.itest.helper.ConfigHelper;
import org.bgerp.itest.helper.ProcessHelper;
import org.bgerp.itest.helper.ResourceHelper;
import org.bgerp.itest.helper.UserHelper;
import org.bgerp.itest.kernel.db.DbTest;
import org.bgerp.itest.kernel.user.UserTest;
import org.bgerp.model.process.ProcessGroups;
import org.testng.annotations.Test;

import ru.bgcrm.dao.process.ProcessDAO;
import ru.bgcrm.model.process.ProcessExecutor;
import ru.bgcrm.model.process.ProcessGroup;
import ru.bgcrm.model.process.TypeProperties;

@Test(groups = "processQueueGrEx", dependsOnGroups = { "process", "user" })
public class ProcessQueueGrExTest {
    private static final String TITLE = "Kernel Process Queue GrEx";

    private int userGroup1Id;
    private int userGroup2Id;

    private int processTypeId;

    @Test
    public void userGroup() throws Exception {
        int groupId = UserHelper.addGroup(TITLE, 0, "");
        userGroup1Id = UserHelper.addGroup(TITLE + " Group 1", groupId, "");
        userGroup2Id = UserHelper.addGroup(TITLE + " Group 2", groupId, "");

        UserHelper.addUserGroups(UserTest.userKarlId, userGroup1Id, userGroup2Id);
        UserHelper.addUserGroups(UserTest.userFriedrichId, userGroup1Id, userGroup2Id);
    }

    @Test(dependsOnMethods = "userGroup")
    public void processType() throws Exception {
        var props = new TypeProperties();
        props.setStatusIds(List.of(ProcessTest.statusOpenId, ProcessTest.statusDoneId));
        props.setCreateStatusId(ProcessTest.statusOpenId);
        props.setCloseStatusIds(Set.of(ProcessTest.statusDoneId));
        props.setGroups(new ProcessGroups(Set.of(new ProcessGroup(userGroup1Id), new ProcessGroup(userGroup2Id), new ProcessGroup(userGroup1Id, 1),
                new ProcessGroup(userGroup2Id, 1))));

        processTypeId = ProcessHelper.addType(TITLE, ProcessTest.processTypeTestGroupId, props).getId();
    }

    @Test(dependsOnMethods = "processType")
    public void processQueue() throws Exception {
        int queueId = ProcessHelper.addQueue(TITLE,
            ConfigHelper.generateConstants(
                "GROUP_1_ID", userGroup1Id,
                "GROUP_2_ID", userGroup1Id
            ) + ResourceHelper.getResource(this, "process.queue.config.txt"),
            Set.of(processTypeId));
        UserHelper.addUserProcessQueues(UserTest.USER_ADMIN_ID, Set.of(queueId));
    }

    @Test(dependsOnMethods = "processType")
    public void process() throws Exception {
        var dao = new ProcessDAO(DbTest.conRoot);

        var process = ProcessHelper.addProcess(processTypeId, TITLE + " 1");
        dao.updateProcessExecutors(Set.of(new ProcessExecutor(UserTest.userKarlId, userGroup1Id), new ProcessExecutor(UserTest.userFriedrichId, userGroup2Id, 1)), process.getId());

        process = ProcessHelper.addProcess(processTypeId, TITLE + " 2");
        dao.updateProcessExecutors(Set.of(new ProcessExecutor(UserTest.userFriedrichId, userGroup1Id), new ProcessExecutor(UserTest.userKarlId, userGroup2Id, 1)), process.getId());

        ProcessHelper.addProcess(processTypeId, TITLE + " No Executors");
    }
}
