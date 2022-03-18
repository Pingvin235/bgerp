package ru.bgerp.plugin.task;

import java.util.List;

import org.bgerp.itest.helper.ConfigHelper;
import org.bgerp.itest.helper.ParamHelper;
import org.bgerp.itest.helper.ProcessHelper;
import org.bgerp.itest.helper.ResourceHelper;
import org.bgerp.itest.kernel.process.ProcessTest;
import org.bgerp.itest.kernel.user.UserTest;
import org.testng.annotations.Test;

import ru.bgcrm.cache.ProcessTypeCache;
import ru.bgcrm.model.param.Parameter;
import ru.bgcrm.model.process.Process;
import ru.bgcrm.plugin.task.Plugin;


@Test(groups = "task", priority = 100, dependsOnGroups = { "config", "process" })
public class TaskTest {
    private static final Plugin PLUGIN = new Plugin();
    private static final String TITLE = PLUGIN.getTitleWithPrefix();

    @Test
    public void config() throws Exception {
        ConfigHelper.addIncludedConfig(PLUGIN, ResourceHelper.getResource(this, "config.txt"));
    }

    @Test
    public void process() throws Exception {
        int paramDeadlineId = ParamHelper.addParam(Process.OBJECT_TYPE, Parameter.TYPE_DATE, TITLE + " Deadline", 0, "", "");

        var props = ProcessTypeCache.getProcessType(ProcessTest.processTypeTestId).getProperties();
        props.setParameterIds(List.of(paramDeadlineId));
        props.setConfig(ConfigHelper.generateConstants("DEADLINE_PARAM_ID", paramDeadlineId) +
            ResourceHelper.getResource(this, "processType.config.txt"));

        int processTypeId = ProcessHelper.addType(TITLE, ProcessTest.processTypeTestGroupId, false, props);

        ProcessHelper.addProcess(processTypeId, UserTest.USER_ADMIN_ID, TITLE);
    }
}