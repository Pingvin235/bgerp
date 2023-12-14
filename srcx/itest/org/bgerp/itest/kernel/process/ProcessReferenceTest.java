package org.bgerp.itest.kernel.process;

import java.util.List;
import java.util.Set;

import org.bgerp.itest.helper.ConfigHelper;
import org.bgerp.itest.helper.ProcessHelper;
import org.bgerp.itest.helper.ResourceHelper;
import org.bgerp.itest.kernel.user.UserTest;
import org.testng.annotations.Test;

import ru.bgcrm.model.process.TypeProperties;

@Test(groups = "processReference", dependsOnGroups = { "process", "processParam" })
public class ProcessReferenceTest {
    private static final String TITLE = "Kernel Process Reference";

    private int processTypeId;

    @Test
    public void processType() throws Exception {
        var props = new TypeProperties();
        props.setStatusIds(List.of(ProcessTest.statusOpenId, ProcessTest.statusDoneId));
        props.setCreateStatus(ProcessTest.statusOpenId);
        props.setCloseStatusIds(Set.of(ProcessTest.statusDoneId));
        props.setParameterIds(List.of(ProcessParamTest.paramListId));
        props.setConfig(ConfigHelper.generateConstants(
            "PARAM_LIST_ID", ProcessParamTest.paramListId
        ) + ResourceHelper.getResource(this, "process.type.config.txt"));

        processTypeId = ProcessHelper.addType(TITLE, ProcessTest.processTypeTestGroupId, false, props).getId();
    }

    @Test(dependsOnMethods = "processType")
    public void process() throws Exception {
        ProcessHelper.addProcess(processTypeId, UserTest.USER_ADMIN_ID, TITLE).getId();
    }
}
