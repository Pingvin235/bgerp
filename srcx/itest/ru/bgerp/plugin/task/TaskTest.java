package ru.bgerp.plugin.task;

import org.bgerp.itest.helper.ConfigHelper;
import org.bgerp.itest.helper.PluginHelper;
import org.bgerp.itest.helper.ProcessHelper;
import org.bgerp.itest.helper.ResourceHelper;
import org.bgerp.itest.kernel.process.ProcessTest;
import org.bgerp.itest.kernel.user.UserTest;
import org.testng.annotations.Test;

import ru.bgcrm.cache.ProcessTypeCache;

@Test(groups = "task", priority = 100, dependsOnGroups = { "config", "process" })
public class TaskTest {
    private static final String TITLE = "Plugin Task";

    @Test
    public void initConfig() throws Exception {
        ConfigHelper.addIncludedConfig(TITLE,
            PluginHelper.initPlugin(new ru.bgcrm.plugin.task.Plugin()) + ResourceHelper.getResource(this, "config.txt"));
    }

    @Test
    public void addProcess() throws Exception {
        var props = ProcessTypeCache.getProcessType(ProcessTest.processTypeTestId).getProperties();
        props.setConfig(ResourceHelper.getResource(this, "processType.config.txt"));

        int processTypeId = ProcessHelper.addType(TITLE, ProcessTest.processTypeTestGroupId, false, props);

        ProcessHelper.addProcess(processTypeId, UserTest.USER_ADMIN_ID, TITLE);
    }
}