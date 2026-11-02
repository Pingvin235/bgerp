package org.bgerp.itest.kernel.process;

import java.util.List;
import java.util.Set;

import org.bgerp.itest.helper.ConfigHelper;
import org.bgerp.itest.helper.MessageHelper;
import org.bgerp.itest.helper.ProcessHelper;
import org.bgerp.itest.helper.ResourceHelper;
import org.bgerp.itest.helper.UserHelper;
import org.testng.annotations.Test;

import ru.bgcrm.model.process.TypeProperties;

@Test(groups = "processCreationGroup", dependsOnGroups = { "process", "message" })
public class ProcessCreationGroupTest {
    private static final String TITLE = "Kernel Process Creation Group";

    private int firstGroupId;
    private int secondGroupId;
    private int thirdGroupId;

    private int processTypeId;

    public void groups() throws Exception {
        int rootGroupId = UserHelper.addGroup(TITLE, 0, "");
        firstGroupId = UserHelper.addGroup(TITLE + " 1", rootGroupId, "");
        secondGroupId = UserHelper.addGroup(TITLE + " 2", rootGroupId, "");
        thirdGroupId = UserHelper.addGroup(TITLE + " 3", rootGroupId, "");
    }

    @Test(dependsOnMethods = "groups")
    public void processType() throws Exception {
        var props = new TypeProperties();
        props.setStatusIds(List.of(ProcessTest.statusOpenId, ProcessTest.statusDoneId));
        props.setCreateStatusId(ProcessTest.statusOpenId);
        props.setCloseStatusIds(Set.of(ProcessTest.statusDoneId));
        props.setConfig(
            ConfigHelper.generateConstants(
                "GROUP_1", firstGroupId,
                "GROUP_2", secondGroupId,
                "GROUP_3", thirdGroupId
            ) +
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
