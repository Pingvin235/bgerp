package org.bgerp.itest.plugin.asterisk;

import java.util.List;
import java.util.Set;

import org.bgerp.dao.param.ParamValueDAO;
import org.bgerp.itest.helper.ConfigHelper;
import org.bgerp.itest.helper.MessageHelper;
import org.bgerp.itest.helper.ParamHelper;
import org.bgerp.itest.helper.ProcessHelper;
import org.bgerp.itest.helper.ResourceHelper;
import org.bgerp.itest.kernel.customer.CustomerTest;
import org.bgerp.itest.kernel.db.DbTest;
import org.bgerp.itest.kernel.message.MessageTest;
import org.bgerp.itest.kernel.process.ProcessTest;
import org.bgerp.itest.kernel.user.UserTest;
import org.bgerp.model.param.Parameter;
import org.testng.annotations.Test;

import ru.bgcrm.model.param.ParameterPhoneValue;
import ru.bgcrm.model.param.ParameterPhoneValueItem;
import ru.bgcrm.model.process.Process;
import ru.bgcrm.model.process.TypeProperties;
import ru.bgcrm.model.user.User;
import ru.bgcrm.plugin.asterisk.Plugin;

@Test(groups = "asterisk", priority = 100, dependsOnGroups = {"config", "message", "user", "process"})
public class AsteriskTest {
    private static final Plugin PLUGIN = Plugin.INSTANCE;
    private static final String TITLE = PLUGIN.getTitleWithPrefix();

    private int paramUserNumberId;
    private int paramProcessPhoneId;

    private int processTypeId;

    @Test
    public void param() throws Exception {
        paramUserNumberId = ParamHelper.addParam(User.OBJECT_TYPE, Parameter.TYPE_TEXT, TITLE + " User Number", UserTest.posParam += 2);
        paramProcessPhoneId = ParamHelper.addParam(Process.OBJECT_TYPE, Parameter.TYPE_PHONE, TITLE + " phone", ProcessTest.posParam += 2);
    }

    @Test(dependsOnMethods = "param")
    public void user() throws Exception {
        var dao = new ParamValueDAO(DbTest.conRoot);
        dao.updateParamText(UserTest.USER_ADMIN_ID, paramUserNumberId, "333");
    }

    @Test(dependsOnMethods = "param")
    public void config() throws Exception {
        ConfigHelper.addPluginConfig(PLUGIN,
            ConfigHelper.generateConstants(
                "CALL_MESSAGE_TYPE_ID", MessageTest.messageTypeCall.getId(),
                "USER_NUMBER_PARAM_ID", paramUserNumberId
            ) + ResourceHelper.getResource(this, "config.txt"));
    }

    @Test(dependsOnMethods = "param")
    public void processType() throws Exception {
        var props = new TypeProperties();
        props.setStatusIds(List.of(ProcessTest.statusOpenId, ProcessTest.statusDoneId));
        props.setCreateStatusId(ProcessTest.statusOpenId);
        props.setCloseStatusIds(Set.of(ProcessTest.statusDoneId));
        props.setParameterIds(List.of(paramProcessPhoneId));

        processTypeId = ProcessHelper.addType(TITLE, ProcessTest.processTypeTestGroupId, props).getId();
    }

    @Test(dependsOnMethods = "processType")
    public void process() throws Exception {
        int processId = ProcessHelper.addProcess(processTypeId, TITLE).getId();

        new ParamValueDAO(DbTest.conRoot).updateParamPhone(processId, paramProcessPhoneId,
                new ParameterPhoneValue(List.of(new ParameterPhoneValueItem(CustomerTest.CUSTOMER_PERS_IVAN_PHONE, "Ivan"))));

        MessageHelper.addHowToTestNoteMessage(processId, this);
    }
}