package org.bgerp.itest.helper;

import java.util.Date;

import org.bgerp.itest.kernel.config.ConfigTest;
import org.bgerp.itest.kernel.db.DbTest;
import org.testng.Assert;

import ru.bgcrm.dao.ConfigDAO;
import ru.bgcrm.model.Config;
import ru.bgcrm.model.LastModify;
import ru.bgcrm.model.user.User;
import ru.bgcrm.util.Setup;

public class ConfigHelper {

    public static Config createConfig(String title, String content) {
        Config config = new Config();
        config.setTitle(title);
        config.setLastModify(new LastModify(User.USER_SYSTEM_ID, new Date()));
        config.setData(content);
        return config;
    }

    public static int addIncludedConfig(String title, String content) throws Exception {
        var dao = new ConfigDAO(DbTest.conRoot);

        var config = new Config();
        config.setParentId(ConfigTest.configMainId);
        config.setTitle(title);
        config.setData(content);
        config.getLastModify().setTime(new Date());

        dao.updateGlobalConfig(config);

        Assert.assertTrue(config.getId() > 0);

        Setup.resetSetup(DbTest.conPoolRoot);

        return config.getId();
    }

    public static void addToConfig(int configId, String content) throws Exception {
        var con = DbTest.conRoot;
        var dao = new ConfigDAO(con);

        var config = dao.getGlobalConfig(configId);
        Assert.assertNotNull(config);

        config.setData(config.getData() + content);

        dao.updateGlobalConfig(config);
    }

    public static String generateConstants(Object... pairs) {
        var config = new StringBuilder();
        config.append("\n# constants\n");
        //TODO: User ParameterMap.of(pairs)
        for (int i = 0; i < pairs.length; i += 2) {
            config
                .append(pairs[i])
                .append("=")
                .append(pairs[i + 1])
                .append("\n");
        }
        return config.toString();
    }

}