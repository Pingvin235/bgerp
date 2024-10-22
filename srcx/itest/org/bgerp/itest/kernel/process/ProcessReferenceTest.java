package org.bgerp.itest.kernel.process;

import java.util.List;
import java.util.Set;

import org.bgerp.itest.helper.ConfigHelper;
import org.bgerp.itest.helper.ProcessHelper;
import org.bgerp.itest.helper.ResourceHelper;
import org.testng.annotations.Test;

import ru.bgcrm.model.process.TypeProperties;
import ru.bgcrm.model.process.config.ProcessReferenceConfig;

@Test(groups = "processReference", dependsOnGroups = { "process", "processParam" })
public class ProcessReferenceTest {
    private static final String TITLE = "Kernel Process Reference";

    private int processTypeId;

    @Test
    public void processType() throws Exception {
        var props = new TypeProperties();
        props.setStatusIds(List.of(ProcessTest.statusOpenId, ProcessTest.statusDoneId));
        props.setCreateStatusId(ProcessTest.statusOpenId);
        props.setCloseStatusIds(Set.of(ProcessTest.statusDoneId));
        props.setParameterIds(List.of(ProcessParamTest.paramListId));
        props.setConfig(ConfigHelper.generateConstants(
            "PARAM_LIST_ID", ProcessParamTest.paramListId
        ) + ResourceHelper.getResource(this, "process.type.config.txt"));

        processTypeId = ProcessHelper.addType(TITLE, ProcessTest.processTypeTestGroupId, props).getId();

        // only to do not forget remove the test with the config
        props.getConfigMap().getConfig(ProcessReferenceConfig.class);
    }

    @Test(dependsOnMethods = "processType")
    public void process() throws Exception {
        ProcessHelper.addProcess(processTypeId, TITLE);
    }
}
