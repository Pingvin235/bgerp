package org.bgerp.itest.plugin.bgbilling;

import java.util.List;
import java.util.Set;

import org.bgerp.itest.helper.ConfigHelper;
import org.bgerp.itest.helper.ParamHelper;
import org.bgerp.itest.helper.ProcessHelper;
import org.bgerp.itest.helper.ResourceHelper;
import org.bgerp.itest.helper.UserHelper;
import org.bgerp.itest.kernel.db.DbTest;
import org.bgerp.itest.kernel.process.ProcessTest;
import org.bgerp.itest.kernel.user.UserTest;
import org.testng.annotations.Test;

import ru.bgcrm.dao.process.ProcessLinkDAO;
import ru.bgcrm.model.param.Parameter;
import ru.bgcrm.model.process.Process;
import ru.bgcrm.model.process.ProcessGroup;
import ru.bgcrm.model.process.ProcessLink;
import ru.bgcrm.model.process.TypeProperties;
import ru.bgcrm.plugin.bgbilling.Plugin;
import ru.bgcrm.plugin.bgbilling.proto.model.Contract;

@Test(groups = "bgbilling", priority = 100, dependsOnGroups = { "process", "message" })
public class BGBillingTest {
    private static final Plugin PLUGIN = Plugin.INSTANCE;
    private static final String TITLE = PLUGIN.getTitleWithPrefix();
    private static final String TITLE_HD = TITLE + " HD";

    private static final String BILLING_ID = "bill";

    private int processTypeId;

    private int paramHdCostId;
    private int paramHdStatusId;
    private int paramHdAutoCloseId;

    private int processHdTypeId;

    @Test
    public void param() throws Exception {
        paramHdCostId = ParamHelper.addParam(Process.OBJECT_TYPE, Parameter.TYPE_TEXT, TITLE_HD + " Cost", ProcessTest.posParam += 2, "", "");
        paramHdStatusId = ParamHelper.addParam(Process.OBJECT_TYPE, Parameter.TYPE_LIST, TITLE_HD + " Status", ProcessTest.posParam += 2, "",
                ResourceHelper.getResource(this, "param.hd.status.values.txt"));
        paramHdAutoCloseId = ParamHelper.addParam(Process.OBJECT_TYPE, Parameter.TYPE_LIST, TITLE_HD + " AutoClose", ProcessTest.posParam += 2, "",
                ResourceHelper.getResource(this, "param.hd.autoclose.values.txt"));
    }

    @Test(dependsOnMethods = "param")
    public void processType() throws Exception {
        processTypeId = ProcessHelper.addType(TITLE, ProcessTest.processTypeTestGroupId, true, null).getId();

        var props = new TypeProperties();
        props.setStatusIds(List.of(ProcessTest.statusOpenId, ProcessTest.statusDoneId));
        props.setCreateStatus(ProcessTest.statusOpenId);
        props.setCloseStatusIds(Set.of(ProcessTest.statusDoneId));
        props.setParameterIds(List.of(paramHdCostId, paramHdStatusId, paramHdAutoCloseId));
        props.setGroups(ProcessGroup.toProcessGroupSet(Set.of(UserTest.USER_GROUP_ADMIN_ID), 0));

        processHdTypeId = ProcessHelper.addType(TITLE_HD, processTypeId, false, props).getId();
    }

    @Test(dependsOnMethods = "processType")
    public void processQueue() throws Exception {
        int queueId = ProcessHelper.addQueue(TITLE,
                ConfigHelper.generateConstants(
                    "BILLING_ID", BILLING_ID
                ) + ResourceHelper.getResource(this, "process.queue.config.txt"), Set.of(processTypeId));
        UserHelper.addUserProcessQueues(UserTest.USER_ADMIN_ID, Set.of(queueId));

        queueId = ProcessHelper.addQueue(TITLE_HD,
                ConfigHelper.generateConstants(
                    "BILLING_ID", BILLING_ID,
                    "PARAM_HD_COST_ID", paramHdCostId,
                    "PARAM_HD_STATUS_ID", paramHdStatusId
                ) + ResourceHelper.getResource(this, "process.hd.queue.config.txt"),
                Set.of(processHdTypeId));
        UserHelper.addUserProcessQueues(UserTest.USER_ADMIN_ID, Set.of(queueId));
    }

    @Test(dependsOnMethods = "processType")
    public void config() throws Exception {
        ConfigHelper.addIncludedConfig(PLUGIN,
            ConfigHelper.generateConstants(
                "BILLING_ID", BILLING_ID,
                "PROCESS_HD_TYPE_ID", processHdTypeId,
                "PARAM_HD_COST_ID", paramHdCostId,
                "PARAM_HD_STATUS_ID", paramHdStatusId,
                "PARAM_HD_AUTO_CLOSE_ID", paramHdAutoCloseId,
                "PROCESS_HD_OPEN_STATUS_ID", ProcessTest.statusOpenId,
                "PROCESS_HD_CLOSE_STATUS_ID", ProcessTest.statusDoneId,
                "PROCESS_HD_READ_STATUS_IDS", ""
            ) + ResourceHelper.getResource(this, "config.txt"));
    }

    @Test(dependsOnMethods = "processType")
    public void process() throws Exception {
        int processId = ProcessHelper.addProcess(processTypeId, UserTest.USER_ADMIN_ID, TITLE).getId();
        new ProcessLinkDAO(DbTest.conRoot).addLinkIfNotExist(new ProcessLink(processId, Contract.OBJECT_TYPE + ":" + BILLING_ID, 455, "x0000"));

        // HD processes have to be imported
    }
}
