package org.bgerp.itest.plugin.pln.sla;

import java.sql.SQLException;
import java.util.List;
import java.util.Set;

import org.bgerp.itest.helper.ConfigHelper;
import org.bgerp.itest.helper.ParamHelper;
import org.bgerp.itest.helper.ProcessHelper;
import org.bgerp.itest.helper.ResourceHelper;
import org.bgerp.itest.helper.UserHelper;
import org.bgerp.itest.kernel.process.ProcessTest;
import org.bgerp.itest.kernel.user.UserTest;
import org.bgerp.model.param.Parameter;
import org.bgerp.plugin.pln.sla.Plugin;
import org.testng.annotations.Test;

import ru.bgcrm.model.process.Process;
import ru.bgcrm.model.process.TypeProperties;
import ru.bgcrm.model.user.User;

@Test(groups = "sla", priority = 100, dependsOnGroups = "process")
public class SlaTest {
    private static final Plugin PLUGIN = Plugin.INSTANCE;
    private static final String TITLE = PLUGIN.getTitleWithPrefix();

    private int paramCloseBeforeId;
    private int paramUpdateBeforeId;

    private int processTypeId;

    @Test
    public void param() throws SQLException {
        paramCloseBeforeId = ParamHelper.addParam(Process.OBJECT_TYPE, Parameter.TYPE_DATETIME, TITLE + " Close Before", ProcessTest.posParam += 2,
                "type=ymdhm\nreadonly=1", "");
        paramUpdateBeforeId = ParamHelper.addParam(Process.OBJECT_TYPE, Parameter.TYPE_DATETIME, TITLE + " Update Before", ProcessTest.posParam += 2,
                "type=ymdhm\nreadonly=1", "");
    }

    @Test(dependsOnMethods = "param")
    public void processType() throws Exception {
        var props = new TypeProperties();
        props.setStatusIds(List.of(ProcessTest.statusOpenId, ProcessTest.statusDoneId));
        props.setCreateStatusId(ProcessTest.statusOpenId);
        props.setCloseStatusIds(Set.of(ProcessTest.statusDoneId));
        props.setConfig(ResourceHelper.getResource(this, "process.type.config.txt"));
        props.setParameterIds(List.of(paramCloseBeforeId, paramUpdateBeforeId));
        processTypeId = ProcessHelper.addType(TITLE, ProcessTest.processTypeTestGroupId, false, props).getId();
    }

    @Test(dependsOnMethods = "processType")
    public void config() throws Exception {
        ConfigHelper.addPluginConfig(PLUGIN,
            ConfigHelper.generateConstants(
                "PARAM_CLOSE_BEFORE_ID", paramCloseBeforeId,
                "PARAM_UPDATE_BEFORE_ID", paramUpdateBeforeId
            ) + ResourceHelper.getResource(this, "config.txt"));
    }

    @Test(dependsOnMethods = "config")
    public void processQueue() throws Exception {
        int queueId = ProcessHelper.addQueue(TITLE, ResourceHelper.getResource(this, "process.queue.config.txt"), Set.of(processTypeId));
        UserHelper.addUserProcessQueues(UserTest.USER_ADMIN_ID, Set.of(queueId));
    }

    @Test(dependsOnMethods = "config")
    public void process() throws Exception {
        ProcessHelper.addProcess(processTypeId, User.USER_SYSTEM_ID, TITLE);
        // TODO: Multiple processes with different times?
    }
}
