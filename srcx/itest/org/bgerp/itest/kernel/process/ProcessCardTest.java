package org.bgerp.itest.kernel.process;

import java.util.List;
import java.util.Set;

import org.bgerp.itest.helper.MessageHelper;
import org.bgerp.itest.helper.ProcessHelper;
import org.bgerp.itest.helper.ResourceHelper;
import org.bgerp.itest.kernel.user.UserTest;
import org.bgerp.model.process.ProcessGroups;
import org.testng.annotations.Test;

import ru.bgcrm.model.process.TypeProperties;

@Test(groups = "processCard", dependsOnGroups = { "user", "process" })
public class ProcessCardTest {
    private static final String TITLE = "Kernel Process Card";

    private int processTypeId;

    @Test
    public void processType() throws Exception {
        var props = new TypeProperties();
        props.setStatusIds(List.of(ProcessTest.statusOpenId, ProcessTest.statusDoneId));
        props.setCreateStatusId(ProcessTest.statusOpenId);
        props.setCloseStatusIds(Set.of(ProcessTest.statusDoneId));
        props.setGroups(new ProcessGroups(UserTest.groupAdminsId));
        props.setConfig(ResourceHelper.getResource(this, "process.type.config.txt"));

        processTypeId = ProcessHelper.addType(TITLE, ProcessTest.processTypeTestGroupId, props).getId();
    }

    @Test(dependsOnMethods = "processType")
    public void process() throws Exception {
        int processId = ProcessHelper.addProcess(processTypeId, TITLE).getId();
        MessageHelper.addHowToTestNoteMessage(processId, this);
    }
}
