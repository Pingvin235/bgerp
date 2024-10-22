package org.bgerp.itest.kernel.process;

import java.util.List;
import java.util.Set;

import org.bgerp.itest.helper.ConfigHelper;
import org.bgerp.itest.helper.MessageHelper;
import org.bgerp.itest.helper.ProcessHelper;
import org.bgerp.itest.helper.ResourceHelper;
import org.bgerp.itest.helper.UserHelper;
import org.bgerp.itest.kernel.user.UserTest;
import org.testng.annotations.Test;

import ru.bgcrm.model.process.TypeProperties;

@Test(groups = "processDefaultChangeListener", dependsOnGroups = { "process", "message" })
public class ProcessDefaultChangeListenerTest {
    private static final String TITLE = "Kernel Process Default Change Listener";

    private int processTypeId;

    @Test
    public void processType() throws Exception {
        var props = new TypeProperties();
        props.setStatusIds(List.of(ProcessTest.statusOpenId, ProcessTest.statusProgressId, ProcessTest.statusDoneId));
        props.setCreateStatusId(ProcessTest.statusOpenId);
        props.setCloseStatusIds(Set.of(ProcessTest.statusDoneId));
        props.setConfig(ConfigHelper.generateConstants(
            "STATUS_PROGRESS_ID", ProcessTest.statusProgressId,
            "STATUS_DONE_ID", ProcessTest.statusDoneId
        ) + ResourceHelper.getResource(this, "process.type.config.txt"));

        processTypeId = ProcessHelper.addType(TITLE, ProcessTest.processTypeTestGroupId, props).getId();
    }

    @Test(dependsOnMethods = "processType")
    public void processQueue() throws Exception {
        int queueId = ProcessHelper.addQueue(TITLE,
            ResourceHelper.getResource(this, "process.queue.config.txt"),
            Set.of(processTypeId));
        UserHelper.addUserProcessQueues(UserTest.USER_ADMIN_ID, Set.of(queueId));
    }

    @Test(dependsOnMethods = "processType")
    public void process() throws Exception {
        var process = ProcessHelper.addProcess(processTypeId, TITLE);
        ProcessHelper.addGroup(process, UserTest.groupAdminsId);
        MessageHelper.addHowToTestNoteMessage(process.getId(), this);
    }
}
