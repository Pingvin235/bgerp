package org.bgerp.itest.kernel.open;

import org.bgerp.itest.configuration.department.development.DevelopmentTest;
import org.bgerp.itest.helper.ConfigHelper;
import org.bgerp.itest.helper.ParamHelper;
import org.bgerp.itest.helper.ResourceHelper;
import org.bgerp.itest.kernel.message.MessageTest;
import org.bgerp.itest.kernel.user.UserTest;
import org.testng.annotations.Test;

import ru.bgcrm.model.param.Parameter;
import ru.bgcrm.model.user.User;

@Test(groups = "openIface", dependsOnGroups = { "message", "depDev" })
public class OpenIfaceTest {
    private static int userEnableParamId;

    @Test
    public void addUserEnableParam() throws Exception {
        userEnableParamId = ParamHelper.addParam(User.OBJECT_TYPE, Parameter.TYPE_LIST, "OPEN", UserTest.pos += 2, "", "1=YES\n");
    }

    @Test(dependsOnMethods = "addUserEnableParam")
    public void addConfig() throws Exception {
        var config = 
                ConfigHelper.generateConstants(
                    "USER_ENABLE_PARAM_ID", userEnableParamId,
                    "USER_SHOW_PARAM_IDS", UserTest.paramEmailId,
                    "PROCESS_IDS", DevelopmentTest.processTypeTaskId,
                    "PROCESS_SHOW_PARAM_IDS", DevelopmentTest.paramGitBranchId,
                    "MESSAGE_OPEN_TAG_IDS", MessageTest.tagOpen.getId()
                ) +
                ResourceHelper.getResource(this, "config.txt");
        ConfigHelper.addIncludedConfig("Kernel Open Interface", config);
    }
}
