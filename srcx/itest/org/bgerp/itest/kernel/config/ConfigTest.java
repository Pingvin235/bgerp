package org.bgerp.itest.kernel.config;

import java.util.Date;

import org.bgerp.itest.helper.ConfigHelper;
import org.bgerp.itest.helper.ResourceHelper;
import org.bgerp.itest.kernel.db.DbTest;
import org.testng.Assert;
import org.testng.annotations.Test;

import ru.bgcrm.dao.ConfigDAO;
import ru.bgcrm.util.TimeUtils;

public class ConfigTest {
    // defined in configuration
    public static final int ROLE_EXECUTION_ID = 0;
    public static final int ROLE_FOLLOW_ID = 1;

    public static volatile int configMainId;

    @Test(groups = "config", dependsOnGroups = "dbInit")
    public void initMainConfig() throws Exception {
        var con = DbTest.conRoot;
        var dao = new ConfigDAO(con);

        var config = ConfigHelper.createConfig("Main",
            "# remove this key case the DB used as production one !!!\n" +
            "generation.time=" + TimeUtils.format(new Date(), TimeUtils.FORMAT_TYPE_YMDHMS) + "\n" +
            ResourceHelper.getResource(this, "config.main.txt"));

        dao.updateGlobalConfig(config);

        configMainId = config.getId();
        Assert.assertTrue(configMainId > 0);

        dao.setActiveGlobalConfig(configMainId);
    }
}