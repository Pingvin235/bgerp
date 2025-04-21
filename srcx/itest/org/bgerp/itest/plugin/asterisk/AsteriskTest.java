package org.bgerp.itest.plugin.asterisk;

import org.bgerp.dao.param.ParamValueDAO;
import org.bgerp.itest.helper.ConfigHelper;
import org.bgerp.itest.helper.ParamHelper;
import org.bgerp.itest.helper.ResourceHelper;
import org.bgerp.itest.kernel.db.DbTest;
import org.bgerp.itest.kernel.message.MessageTest;
import org.bgerp.itest.kernel.user.UserTest;
import org.bgerp.model.param.Parameter;
import org.testng.annotations.Test;

import ru.bgcrm.model.user.User;
import ru.bgcrm.plugin.asterisk.Plugin;

@Test(groups = "asterisk", priority = 100, dependsOnGroups = {"config", "message", "user"})
public class AsteriskTest {
    private static final Plugin PLUGIN = Plugin.INSTANCE;
    private static final String TITLE = PLUGIN.getTitleWithPrefix();

    private int paramExtensionId;

    @Test
    public void param() throws Exception {
        paramExtensionId = ParamHelper.addParam(User.OBJECT_TYPE, Parameter.TYPE_TEXT, TITLE + " Extension number", UserTest.posParam += 2);
    }

    @Test(dependsOnMethods = "param")
    public void user() throws Exception {
        var dao = new ParamValueDAO(DbTest.conRoot);
        dao.updateParamText(UserTest.USER_ADMIN_ID, paramExtensionId, "101");
    }

    @Test(dependsOnMethods = "param")
    public void config() throws Exception {
        ConfigHelper.addIncludedConfig(TITLE,
            ResourceHelper.getResource(this, "config.prefix.txt") +
            ConfigHelper.generateConstants(
                "CALL_MESSAGE_TYPE_ID", MessageTest.CALL_MESSAGE_TYPE_ID,
                "USER_NUMBER_PARAM_ID", paramExtensionId
            ) + ResourceHelper.getResource(this, "config.txt"));
    }
}