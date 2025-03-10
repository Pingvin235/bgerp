package org.bgerp.itest.kernel.process;

import java.util.List;
import java.util.Set;

import org.bgerp.itest.helper.ConfigHelper;
import org.bgerp.itest.helper.ProcessHelper;
import org.bgerp.itest.helper.ResourceHelper;
import org.bgerp.itest.helper.UserHelper;
import org.bgerp.itest.kernel.user.UserTest;
import org.testng.annotations.Test;

import ru.bgcrm.model.process.TypeProperties;

@Test(groups = "process", dependsOnGroups = { "config", "user" })
public class ProcessTest {
    private static final String TITLE = "Kernel Process";

    /** Common used statuses and params. */
    public static volatile int statusOpenId;
    public static volatile int statusProgressId;
    public static volatile int statusWaitId;
    public static volatile int statusDoneId;
    public static volatile int statusRejectId;

    /** Test process type, all other are linked as parents. */
    public static volatile int processTypeTestGroupId;
    public static volatile int processTypeTestId;

    public static volatile int queueId;

    // TODO: Think about moving the variable toe ProcessParamTest
    public static volatile int posParam = 0;

    @Test
    public void processStatus() throws Exception {
        int pos = 0;
        statusOpenId = ProcessHelper.addStatus("Open", pos += 2);
        statusProgressId = ProcessHelper.addStatus("Progress", pos += 2);
        statusWaitId = ProcessHelper.addStatus("Wait", pos += 2);
        statusDoneId = ProcessHelper.addStatus("Done", pos += 2);
        statusRejectId = ProcessHelper.addStatus("Reject", pos += 2);
    }

    @Test(dependsOnMethods = "processStatus")
    public void processType() throws Exception {
        var props = new TypeProperties();
        props.setStatusIds(List.of(ProcessTest.statusOpenId, ProcessTest.statusProgressId, ProcessTest.statusDoneId));
        props.setCreateStatusId(ProcessTest.statusOpenId);
        props.setCloseStatusIds(Set.of(ProcessTest.statusDoneId));

        processTypeTestGroupId = ProcessHelper.addType(TITLE, 0, props).getId();

        processTypeTestId = ProcessHelper.addType(TITLE, processTypeTestGroupId, null).getId();
    }

    @Test(dependsOnMethods = "processType")
    public void processQueue() throws Exception {
        queueId = ProcessHelper.addQueue(TITLE,
                ConfigHelper.generateConstants(
                    "STATUS_OPEN_ID", ProcessTest.statusOpenId,
                    "STATUS_PROGRESS_ID", ProcessTest.statusProgressId,
                    "STATUS_WAIT_ID", ProcessTest.statusWaitId) +
                    ResourceHelper.getResource(this, "process.queue.config.txt"),
                Set.of(processTypeTestGroupId));
        UserHelper.addUserProcessQueues(UserTest.USER_ADMIN_ID, Set.of(queueId));
    }

    @Test(dependsOnMethods = "processType")
    public void process() throws Exception {
        for (int i = 1; i <= 9; i += 2)
            ProcessHelper.addProcess(processTypeTestId, UserTest.USER_ADMIN_ID, TITLE + " Priority " + i, i);
    }
}
