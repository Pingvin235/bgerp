package org.bgerp.itest.plugin.pln.agree;

import java.util.List;
import java.util.Set;

import org.bgerp.itest.helper.ConfigHelper;
import org.bgerp.itest.helper.ProcessHelper;
import org.bgerp.itest.helper.ResourceHelper;
import org.bgerp.itest.helper.UserHelper;
import org.bgerp.itest.kernel.process.ProcessTest;
import org.bgerp.itest.kernel.user.UserTest;
import org.bgerp.plugin.pln.agree.Plugin;
import org.testng.annotations.Test;

import ru.bgcrm.model.process.TypeProperties;

@Test(groups = "agree", priority = 100, dependsOnGroups = "process")
public class AgreeTest {
    private static final Plugin PLUGIN = Plugin.INSTANCE;
    private static final String TITLE = PLUGIN.getTitleWithPrefix();

    private int userGroupId;

    private int statusFromId;
    private int statusToId;

    private int processTypeId;

    @Test
    public void userGroup() throws Exception {
        userGroupId = UserHelper.addGroup(TITLE, 0, "");

        UserHelper.addUserGroups(UserTest.USER_ADMIN_ID, userGroupId);
        UserHelper.addUserGroups(UserTest.userKarlId, userGroupId);
        UserHelper.addUserGroups(UserTest.userLeonId, userGroupId);
    }

    @Test
    public void processStatus() throws Exception {
        statusFromId = ProcessHelper.addStatus("Agreement", 0);
        statusToId = ProcessHelper.addStatus("Agreed", 0);
    }

    @Test(dependsOnMethods = { "userGroup", "processStatus" })
    public void processType() throws Exception {
        var props = new TypeProperties();
        props.setStatusIds(List.of(ProcessTest.statusOpenId, statusFromId, statusToId, ProcessTest.statusDoneId));
        props.setCreateStatusId(ProcessTest.statusOpenId);
        props.setCloseStatusIds(Set.of(ProcessTest.statusDoneId));
        props.setTransactionProperties(ProcessTest.statusOpenId, statusFromId, true);
        props.setTransactionProperties(statusFromId, ProcessTest.statusOpenId, true);
        props.setTransactionProperties(statusFromId, statusToId, true);
        props.setTransactionProperties(statusToId, ProcessTest.statusDoneId, true);
        props.setConfig(ConfigHelper.generateConstants(
            "STATUS_FROM_ID", statusFromId,
            "STATUS_TO_ID", statusToId,
            "GROUP_ID", userGroupId
        ) + ResourceHelper.getResource(this, "process.type.config.txt"));
        processTypeId = ProcessHelper.addType(TITLE, ProcessTest.processTypeTestGroupId, props).getId();
    }

    @Test(dependsOnMethods = "processType")
    public void config() throws Exception {
        ConfigHelper.addPluginConfig(PLUGIN, ResourceHelper.getResource(this, "config.txt"));
    }

    @Test(dependsOnMethods = "processType")
    public void processQueue() throws Exception {
        int queueId = ProcessHelper.addQueue(TITLE, ResourceHelper.getResource(this, "process.queue.config.txt"), Set.of(processTypeId));
        UserHelper.addUserProcessQueues(UserTest.USER_ADMIN_ID, Set.of(queueId));
        UserHelper.addUserProcessQueues(UserTest.userKarlId, Set.of(queueId));
        UserHelper.addUserProcessQueues(UserTest.userLeonId, Set.of(queueId));
    }

    @Test(dependsOnMethods = "processType")
    public void process() throws Exception {
        ProcessHelper.addProcess(processTypeId, TITLE);
    }
}
