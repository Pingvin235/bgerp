package org.bgerp.itest.configuration.company.branch.software;

import com.beust.jcommander.internal.Lists;
import com.google.common.collect.Sets;

import org.bgerp.itest.helper.ConfigHelper;
import org.bgerp.itest.helper.ProcessHelper;
import org.bgerp.itest.helper.ResourceHelper;
import org.bgerp.itest.kernel.process.InitTest;
import org.testng.annotations.Test;

import ru.bgcrm.model.process.ProcessGroup;
import ru.bgcrm.model.process.TypeProperties;

@Test(groups = "softwareDevelopment", dependsOnGroups = { "configProcessNotificationInit", "processInit", "blowInit", "paramInit" })
public class Development {
    public static volatile int groupDevId;
    
    @Test
    public void addGroups() throws Exception {}
    
    @Test
    public void addTypes() throws Exception {
        var props = new TypeProperties();
        props.setStatusIds(Lists.newArrayList(InitTest.statusOpen, InitTest.statusToDo, InitTest.statusProgress, InitTest.statusWait));
        props.setCreateStatus(InitTest.statusOpen);
        props.setCloseStatusIds(Sets.newHashSet(InitTest.statusDone, InitTest.statusRejected));
        props.setGroups(ProcessGroup.toProcessGroupSet(Sets.newHashSet(groupDevId), org.bgerp.itest.kernel.config.InitTest.ROLE_EXECUTION_ID));
        props.setAllowedGroups(ProcessGroup.toProcessGroupSet(Sets.newHashSet(groupDevId), org.bgerp.itest.kernel.config.InitTest.ROLE_EXECUTION_ID));
        props.setConfig(ConfigHelper.generateConstants("CONFIG_PROCESS_NOTIFICATIONS_ID", org.bgerp.itest.kernel.config.InitTest.configProcessNotificationId) +
                        ResourceHelper.getResource(this, "config.processType.txt"));

        ProcessHelper.addType("BGERP", 0, false, props);

        /*// deadline, next termin*/
    }
}