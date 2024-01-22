package org.bgerp.itest.kernel.iface.open;

import java.time.Duration;
import java.util.List;
import java.util.Set;

import org.bgerp.dao.param.ParamValueDAO;
import org.bgerp.itest.helper.ConfigHelper;
import org.bgerp.itest.helper.MessageHelper;
import org.bgerp.itest.helper.ParamHelper;
import org.bgerp.itest.helper.ProcessHelper;
import org.bgerp.itest.helper.ResourceHelper;
import org.bgerp.itest.kernel.db.DbTest;
import org.bgerp.itest.kernel.message.MessageTest;
import org.bgerp.itest.kernel.process.ProcessTest;
import org.bgerp.itest.kernel.user.UserTest;
import org.bgerp.model.param.Parameter;
import org.testng.annotations.Test;

import ru.bgcrm.dao.message.MessageDAO;
import ru.bgcrm.model.process.Process;
import ru.bgcrm.model.process.TypeProperties;
import ru.bgcrm.model.user.User;

@Test(groups = "openIface", dependsOnGroups = { "user", "process", "message" })
public class OpenIfaceTest {
    private static final String TITLE = "Kernel Open Interface";

    private int userOpenParamId;
    private int processOpenParamId;
    private int processTypeId;
    private int processId;

    @Test
    public void param() throws Exception {
        userOpenParamId = ParamHelper.addParam(User.OBJECT_TYPE, Parameter.TYPE_LIST, TITLE + " OPEN", UserTest.posParam += 2, "", "1=YES\n");
        processOpenParamId = ParamHelper.addParam(Process.OBJECT_TYPE, Parameter.TYPE_TEXT, TITLE + " Text", ProcessTest.posParam += 2, "",  "");
    }

    @Test(dependsOnMethods = "param")
    public void user() throws Exception {
        new ParamValueDAO(DbTest.conRoot).updateParamList(UserTest.userKarlId, userOpenParamId, Set.of(1));
    }

    @Test(dependsOnMethods = "param")
    public void process() throws Exception {
        var props = new TypeProperties();
        props.setStatusIds(List.of(ProcessTest.statusOpenId, ProcessTest.statusDoneId));
        props.setCreateStatus(ProcessTest.statusOpenId);
        props.setCloseStatusIds(Set.of(ProcessTest.statusDoneId));
        props.setParameterIds(List.of(processOpenParamId));

        processTypeId = ProcessHelper.addType(TITLE, ProcessTest.processTypeTestGroupId, false, props).getId();

        processId = ProcessHelper.addProcess(processTypeId, UserTest.USER_ADMIN_ID, TITLE).getId();

        new ParamValueDAO(DbTest.conRoot).updateParamText(processId, processOpenParamId, "The value should be visible in open interface");
    }

    @Test(dependsOnMethods = "process")
    public void message() throws Exception {
        int messageId = MessageHelper.addNoteMessage(processId, UserTest.USER_ADMIN_ID, Duration.ZERO, TITLE, "The message should be visible in open interface").getId();
        new MessageDAO(DbTest.conRoot).updateMessageTags(messageId, Set.of(MessageTest.tagOpen.getId()));
    }

    @Test(dependsOnMethods = { "user", "process" })
    public void config() throws Exception {
        var config = ConfigHelper.generateConstants(
                "USER_ENABLE_PARAM_ID", userOpenParamId,
                "USER_SHOW_PARAM_IDS", UserTest.paramEmailId,
                "PROCESS_TYPE_IDS", processTypeId,
                "PROCESS_SHOW_PARAM_IDS", processOpenParamId,
                "MESSAGE_OPEN_TAG_IDS", MessageTest.tagOpen.getId()
            ) + ResourceHelper.getResource(this, "config.txt");
        ConfigHelper.addIncludedConfig(TITLE, config);
    }
}
