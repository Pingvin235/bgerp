package org.bgerp.itest.kernel.config;

import java.sql.Connection;

import org.bgerp.itest.helper.ConfigHelper;
import org.bgerp.itest.helper.ResourceHelper;
import org.bgerp.itest.kernel.db.DbTest;
import org.testng.Assert;
import org.testng.annotations.Test;

import ru.bgcrm.dao.ConfigDAO;

public class InitTest {
    // defined in configuration
    public static final int ROLE_EXECUTION_ID = 0;
    public static final int ROLE_FOLLOW_ID = 1;

    public static volatile int configMainId;
    public static volatile int configProcessNotificationId;

    @Test(groups = "configInit", dependsOnGroups = "dbInit")
    public void initMainConfig() throws Exception {
        try (Connection con = DbTest.conPoolRoot.getDBConnectionFromPool()) {
            var dao = new ConfigDAO(con);

            var config = ConfigHelper.createConfig("Main", ResourceHelper.getResource(this, "config.main.txt"));

            dao.updateGlobalConfig(config);

            configMainId = config.getId();
            Assert.assertTrue(configMainId > 0);

            dao.setActiveGlobalConfig(configMainId);

            con.commit();
        }
    }

    @Test(groups = "configProcessNotificationInit", dependsOnGroups = "paramInit")
    public void initProcessNotificationConfig() throws Exception {
        try (Connection con = DbTest.conPoolRoot.getDBConnectionFromPool()) {
            var dao = new ConfigDAO(con);

            var config = ConfigHelper.createConfig("Process Notification", ResourceHelper.getResource(this, "config.process.notification.txt"));

            dao.updateGlobalConfig(config);

            configProcessNotificationId = config.getId();
            Assert.assertTrue(configProcessNotificationId > 0);

            con.commit();
        }
    }

}