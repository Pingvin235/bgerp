package org.bgerp.itest.plugin.git;

import java.util.List;
import java.util.Set;

import org.bgerp.app.cfg.Setup;
import org.bgerp.itest.helper.ConfigHelper;
import org.bgerp.itest.helper.ParamHelper;
import org.bgerp.itest.helper.ProcessHelper;
import org.bgerp.itest.helper.ResourceHelper;
import org.bgerp.itest.kernel.db.DbTest;
import org.bgerp.itest.kernel.process.ProcessTest;
import org.bgerp.itest.kernel.user.UserTest;
import org.bgerp.plugin.git.Config;
import org.bgerp.plugin.git.Plugin;
import org.testng.Assert;
import org.testng.annotations.Test;

import ru.bgcrm.cache.ParameterCache;
import ru.bgcrm.dao.ParamValueDAO;
import ru.bgcrm.event.ParamChangingEvent;
import ru.bgcrm.model.param.Parameter;
import ru.bgcrm.model.process.Process;
import ru.bgcrm.model.process.TypeProperties;
import ru.bgcrm.model.user.User;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.sql.SingleConnectionSet;

@Test(groups = "git", priority = 100, dependsOnGroups = { "config", "process", "user" })
public class GitTest {
    private static final Plugin PLUGIN = Plugin.INSTANCE;
    private static final String TITLE = PLUGIN.getTitleWithPrefix();

    private int paramBranchId;
    private int processTypeId;

    @Test
    public void param() throws Exception {
        paramBranchId = ParamHelper.addParam(Process.OBJECT_TYPE, Parameter.TYPE_TEXT, TITLE + " Branch",
                ProcessTest.posParam += 2, "", "");
    }

    @Test(dependsOnMethods = "param")
    public void processType() throws Exception {
        var props = new TypeProperties();
        props.setStatusIds(List.of(ProcessTest.statusOpenId, ProcessTest.statusProgressId, ProcessTest.statusDoneId));
        props.setCreateStatus(ProcessTest.statusOpenId);
        props.setCloseStatusIds(Set.of(ProcessTest.statusDoneId));
        props.setConfig(ResourceHelper.getResource(this, "process.type.config.txt"));
        props.setParameterIds(List.of(paramBranchId));
        processTypeId = ProcessHelper.addType(TITLE, ProcessTest.processTypeTestGroupId, false, props).getId();
    }

    @Test(dependsOnMethods = "param")
    public void config() throws Exception {
        ConfigHelper.addIncludedConfig(PLUGIN,
            ConfigHelper.generateConstants(
                "PARAM_BRANCH_ID", paramBranchId,
                "PARAM_EMAIL_ID", UserTest.paramEmailId,
                "PROCESS_STATUS_PROGRESS_ID", ProcessTest.statusProgressId,
                "PROCESS_STATUS_DONE_ID", ProcessTest.statusDoneId
            ) + ResourceHelper.getResource(this, "config.txt"));
    }

    @Test(dependsOnMethods = "processType")
    public void process() throws Exception {
        var paramDao = new ParamValueDAO(DbTest.conRoot);

        var process = ProcessHelper.addProcess(processTypeId, User.USER_SYSTEM_ID, TITLE + " Task");
        ProcessHelper.addGroup(process, UserTest.groupAdminsId);
        ProcessHelper.addExecutor(process, UserTest.groupAdminsId, UserTest.USER_ADMIN_ID);

        var config = Setup.getSetup().getConfig(Config.class);

        int processId = process.getId();

        var event = new ParamChangingEvent(DynActionForm.SYSTEM_FORM, ParameterCache.getParameter(paramBranchId),
                processId, "branch_name");
        config.paramChanging(event, new SingleConnectionSet(DbTest.conRoot));
        Assert.assertEquals(paramDao.getParamText(processId, paramBranchId), "p" + processId + "-branch-name");
    }
}