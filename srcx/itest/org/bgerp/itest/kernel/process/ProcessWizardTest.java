package org.bgerp.itest.kernel.process;

import java.util.List;
import java.util.Set;

import org.bgerp.itest.helper.ConfigHelper;
import org.bgerp.itest.helper.ParamHelper;
import org.bgerp.itest.helper.ProcessHelper;
import org.bgerp.itest.helper.ResourceHelper;
import org.bgerp.itest.helper.UserHelper;
import org.bgerp.itest.kernel.param.ParamTest;
import org.bgerp.itest.kernel.user.UserTest;
import org.bgerp.model.param.Parameter;
import org.bgerp.model.process.ProcessGroups;
import org.testng.annotations.Test;

import ru.bgcrm.model.process.Process;
import ru.bgcrm.model.process.TypeProperties;

@Test(groups = "processWizard", dependsOnGroups = { "user", "process" })
public class ProcessWizardTest {
    private static final String TITLE = "Kernel Process Wizard";

    private int paramAddressId;
    private int paramListId;

    private int processTypeId;

    @Test
    public void param() throws Exception {
        paramAddressId = ParamHelper.addParam(Process.OBJECT_TYPE, Parameter.TYPE_ADDRESS, TITLE + " type 'address'", ProcessTest.posParam += 2);
        paramListId = ParamHelper.addParam(Process.OBJECT_TYPE, Parameter.TYPE_LIST, TITLE + " type 'list'", ProcessTest.posParam += 2, "", ParamTest.LIST_VALUES_123);
    }

    @Test(dependsOnMethods = "param")
    public void processType() throws Exception {
        var props = new TypeProperties();
        props.setStatusIds(List.of(ProcessTest.statusOpenId, ProcessTest.statusDoneId));
        props.setCreateStatusId(ProcessTest.statusOpenId);
        props.setCloseStatusIds(Set.of(ProcessTest.statusDoneId));
        props.setGroups(new ProcessGroups(UserTest.groupAdminsId));
        props.setParameterIds(List.of(paramAddressId, paramListId));
        props.setConfig(
            ConfigHelper.generateConstants(
                "PARAM_ADDRESS_ID", paramAddressId,
                "PARAM_LIST_ID", paramListId
            ) + ResourceHelper.getResource(this, "process.type.config.txt"));

        processTypeId = ProcessHelper.addType(TITLE, ProcessTest.processTypeTestGroupId, props).getId();
    }

    @Test(dependsOnMethods = "processType")
    public void processQueue() throws Exception {
        int queueId = ProcessHelper.addQueue(TITLE,
                ConfigHelper.generateConstants(
                    "STATUS_OPEN_ID", ProcessTest.statusOpenId,
                    "STATUS_PROGRESS_ID", ProcessTest.statusProgressId,
                    "STATUS_WAIT_ID", ProcessTest.statusWaitId
                ) + ResourceHelper.getResource(this, "process.queue.config.txt"),
                Set.of(processTypeId));
        UserHelper.addUserProcessQueues(UserTest.USER_ADMIN_ID, Set.of(queueId));
    }

    @Test(dependsOnMethods = "processType")
    public void process() throws Exception {
        // ProcessHelper.addProcess(processTypeId, TITLE).getId();
    }
}
