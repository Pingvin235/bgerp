package org.bgerp.itest.kernel;

import java.sql.Connection;

import org.bgerp.itest.helper.ConfigHelper;
import org.bgerp.itest.helper.ResourceHelper;
import org.bgerp.itest.kernel.db.DbTest;
import org.testng.Assert;
import org.testng.annotations.Test;

import ru.bgcrm.dao.ConfigDAO;
import ru.bgcrm.model.Config;

@Test(groups = "configInit", dependsOnGroups = "dbInit")
public class InitConfigTest {
    public static volatile int configMainId;

    @Test
    public void initConfig() throws Exception {
        try (Connection con = DbTest.conPoolRoot.getDBConnectionFromPool()) {
            ConfigDAO dao = new ConfigDAO(con);

            Config config = ConfigHelper.createConfig("Main", ResourceHelper.getResource(this, "config.main.txt"));

            dao.updateGlobalConfig(config);

            configMainId = config.getId();
            Assert.assertTrue(configMainId > 0);

            dao.setActiveGlobalConfig(configMainId);

            con.commit();
        }
    }
}