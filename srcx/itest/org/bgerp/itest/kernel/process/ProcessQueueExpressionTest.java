package org.bgerp.itest.kernel.process;

import java.util.Set;

import org.bgerp.itest.helper.ProcessHelper;
import org.bgerp.itest.helper.ResourceHelper;
import org.bgerp.itest.helper.UserHelper;
import org.bgerp.itest.kernel.db.DbTest;
import org.bgerp.itest.kernel.user.UserTest;
import org.testng.annotations.Test;

import ru.bgcrm.dao.process.ProcessDAO;

@Test(groups = "processQueueExpression", dependsOnGroups = "process")
public class ProcessQueueExpressionTest {
    private static final String TITLE = "Kernel Process Queue Expression";

    private int processTypeId;

    @Test
    public void processType() throws Exception {
       processTypeId = ProcessHelper.addType(TITLE, ProcessTest.processTypeTestGroupId, null).getId();
    }

    @Test(dependsOnMethods = "processType")
    public void processQueue() throws Exception {
        int queueId = ProcessHelper.addQueue(TITLE, ResourceHelper.getResource(this, "process.queue.config.txt"), Set.of(processTypeId));
        UserHelper.addUserProcessQueues(UserTest.USER_ADMIN_ID, Set.of(queueId));
    }

    @Test(dependsOnMethods = "processType")
    public void process() throws Exception {
        ProcessHelper.addProcess(processTypeId, TITLE + " 1");
        var p2 = ProcessHelper.addProcess(processTypeId, TITLE + " 2");
        p2.setPriority(4);
        new ProcessDAO(DbTest.conRoot).updateProcess(p2);
    }
}
