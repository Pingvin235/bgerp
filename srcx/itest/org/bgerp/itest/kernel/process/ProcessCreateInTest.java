package org.bgerp.itest.kernel.process;

import java.util.List;
import java.util.Set;

import org.bgerp.itest.helper.ConfigHelper;
import org.bgerp.itest.helper.MessageHelper;
import org.bgerp.itest.helper.ParamHelper;
import org.bgerp.itest.helper.ProcessHelper;
import org.bgerp.itest.helper.ResourceHelper;
import org.bgerp.itest.kernel.customer.CustomerTest;
import org.bgerp.model.param.Parameter;
import org.testng.annotations.Test;

import ru.bgcrm.model.process.Process;
import ru.bgcrm.model.process.TypeProperties;

@Test(groups = "processCreateIn", dependsOnGroups = { "processParam", "message", "customer" })
public class ProcessCreateInTest {
    private static final String TITLE = "Kernel Process Create In";

    private int paramTextAddressId;
    private int processTypeId;

    @Test
    public void param() throws Exception {
        paramTextAddressId = ParamHelper.addParam(Process.OBJECT_TYPE, Parameter.TYPE_TEXT, TITLE, ProcessTest.posParam += 2, "", "");
    }

    @Test(dependsOnMethods = "param")
    public void processType() throws Exception {
        var props = new TypeProperties();
        props.setStatusIds(List.of(ProcessTest.statusOpenId, ProcessTest.statusDoneId));
        props.setCreateStatusId(ProcessTest.statusOpenId);
        props.setCloseStatusIds(Set.of(ProcessTest.statusDoneId));
        props.setParameterIds(List.of(paramTextAddressId));
        props.setConfig(
            ConfigHelper.generateConstants(
                "PARAM_CUSTOMER_ADDR", CustomerTest.paramAddressId,
                "PARAM_PROCESS_ADDR", paramTextAddressId) +
            ResourceHelper.getResource(this, "process.type.config.txt")
        );

        processTypeId = ProcessHelper.addType(TITLE, ProcessTest.processTypeTestGroupId, props).getId();
    }

    @Test(dependsOnMethods = "processType")
    public void process() throws Exception {
        int processId = ProcessHelper.addProcess(processTypeId, TITLE).getId();
        MessageHelper.addHowToTestNoteMessage(processId, this);
    }
}
