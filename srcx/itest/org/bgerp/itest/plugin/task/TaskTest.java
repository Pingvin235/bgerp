package org.bgerp.itest.plugin.task;

import java.util.List;
import java.util.Set;

import org.bgerp.itest.helper.ConfigHelper;
import org.bgerp.itest.helper.MessageHelper;
import org.bgerp.itest.helper.ParamHelper;
import org.bgerp.itest.helper.ProcessHelper;
import org.bgerp.itest.helper.ResourceHelper;
import org.bgerp.itest.kernel.process.ProcessTest;
import org.bgerp.itest.kernel.user.UserTest;
import org.bgerp.model.param.Parameter;
import org.testng.annotations.Test;

import ru.bgcrm.model.process.Process;
import ru.bgcrm.model.process.TypeProperties;
import ru.bgcrm.plugin.task.Plugin;


@Test(groups = "task", priority = 100, dependsOnGroups = { "config", "process", "message" })
public class TaskTest {
    private static final Plugin PLUGIN = Plugin.INSTANCE;
    private static final String TITLE = PLUGIN.getTitleWithPrefix();

    @Test
    public void config() throws Exception {
        ConfigHelper.addPluginConfig(PLUGIN, ResourceHelper.getResource(this, "config.txt"));
    }

    @Test
    public void process() throws Exception {
        int paramDeadlineId = ParamHelper.addParam(Process.OBJECT_TYPE, Parameter.TYPE_DATE, TITLE + " Deadline", 0, "", "");

        var props = new TypeProperties();
        props.setStatusIds(List.of(ProcessTest.statusOpenId, ProcessTest.statusDoneId));
        props.setCreateStatus(ProcessTest.statusOpenId);
        props.setCloseStatusIds(Set.of(ProcessTest.statusDoneId));
        props.setParameterIds(List.of(paramDeadlineId));
        props.setConfig(ConfigHelper.generateConstants("DEADLINE_PARAM_ID", paramDeadlineId) +
            ResourceHelper.getResource(this, "process.type.config.txt"));

        int processTypeId = ProcessHelper.addType(TITLE, ProcessTest.processTypeTestGroupId, false, props).getId();

        var process = ProcessHelper.addProcess(processTypeId, UserTest.USER_ADMIN_ID, TITLE);

        MessageHelper.addHowToTestNoteMessage(process.getId(), this);
    }
}