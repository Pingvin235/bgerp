package org.bgerp.itest.kernel.process;

import java.util.List;
import java.util.Set;

import org.bgerp.itest.helper.ConfigHelper;
import org.bgerp.itest.helper.ParamHelper;
import org.bgerp.itest.helper.ProcessHelper;
import org.bgerp.itest.helper.ResourceHelper;
import org.bgerp.itest.helper.UserHelper;
import org.bgerp.itest.kernel.user.UserTest;
import org.testng.annotations.Test;

import ru.bgcrm.model.param.Parameter;
import ru.bgcrm.model.process.Process;
import ru.bgcrm.model.process.TypeProperties;

@Test(groups = "process", dependsOnGroups = "dbInit")
public class ProcessTest {
    /** Common used statuses and params. */
    public static volatile int statusOpenId;
    public static volatile int statusProgressId;
    public static volatile int statusWaitId;
    public static volatile int statusDoneId;
    public static volatile int statusRejectId;

    public static volatile int paramAddressId;
    public static volatile int paramNextDateId;
    public static volatile int paramDeadlineDateId;

    /** Test process type, all other are linked as parents. */
    public static volatile int processTypeTestGroupId;
    public static volatile int processTypeTestId;
    //public static volatile int queueId;

    public static volatile int posParam = 0;

    @Test
    public void addStatuses() throws Exception {
        int pos = 0;
        statusOpenId = ProcessHelper.addStatus("Open", pos += 2);
        statusProgressId = ProcessHelper.addStatus("Progress", pos += 2);
        statusWaitId = ProcessHelper.addStatus("Wait", pos += 2);
        statusDoneId = ProcessHelper.addStatus("Done", pos += 2);
        statusRejectId = ProcessHelper.addStatus("Reject", pos += 2);
    }

    @Test
    public void addParams() throws Exception {
        paramAddressId = ParamHelper.addParam(Process.OBJECT_TYPE, Parameter.TYPE_ADDRESS, "Address", posParam += 2, "", "");
        // TODO: Make date chooser configuration.
        paramNextDateId = ParamHelper.addParam(Process.OBJECT_TYPE, Parameter.TYPE_DATE, "Next date", posParam += 2, "", "");
        paramDeadlineDateId = ParamHelper.addParam(Process.OBJECT_TYPE, Parameter.TYPE_DATE, "Deadline", posParam += 2, "", "");
    }

    @Test(dependsOnMethods = "addStatuses")
    public void addTypes() throws Exception {
        processTypeTestGroupId = ProcessHelper.addType("Test", 0, false, null);

        var props = new TypeProperties();
        props.setStatusIds(List.of(ProcessTest.statusOpenId, ProcessTest.statusProgressId, ProcessTest.statusDoneId));
        props.setCreateStatus(ProcessTest.statusOpenId);
        props.setCloseStatusIds(Set.of(ProcessTest.statusDoneId));
        props.setConfig(ResourceHelper.getResource(this, "processType.txt"));

        processTypeTestId = ProcessHelper.addType("Test", processTypeTestGroupId, false, props);
    }

    @Test(dependsOnMethods = "addTypes")
    public void addQueues() throws Exception {
        int queueId = ProcessHelper.addQueue("Test",
                ConfigHelper.generateConstants(
                    "STATUS_OPEN_ID", ProcessTest.statusOpenId,
                    "STATUS_PROGRESS_ID", ProcessTest.statusProgressId,
                    "STATUS_WAIT_ID", ProcessTest.statusWaitId) +
                    ResourceHelper.getResource(this, "queue.txt"),
                Set.of(processTypeTestGroupId));
        UserHelper.addUserProcessQueues(UserTest.USER_ADMIN_ID, Set.of(queueId));
    }
}
