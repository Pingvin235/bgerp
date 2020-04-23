package org.bgerp.itest.helper;

import java.util.Date;

import org.bgerp.itest.kernel.InitConfigTest;
import org.bgerp.itest.kernel.db.DbTest;
import org.testng.Assert;

import ru.bgcrm.dao.ConfigDAO;
import ru.bgcrm.model.Config;
import ru.bgcrm.model.LastModify;
import ru.bgcrm.model.user.User;
import ru.bgcrm.util.Preferences;

public class ConfigHelper {

    public static Config createConfig(String title, String content) {
        Config config = new Config();
        config.setTitle(title);
        config.setLastModify(new LastModify(User.USER_SYSTEM_ID, new Date()));
        config.setData(content);
        return config;
    }

    public static int addIncludedConfig(String title, String content) throws Exception {
        try (var con = DbTest.conPoolRoot.getDBConnectionFromPool()) {
            var dao = new ConfigDAO(con);

            var config = createConfig(title, content);

            dao.updateGlobalConfig(config);
            Preferences.processIncludes(dao, config.getData(), true);

            int configId = config.getId();
            Assert.assertTrue(configId > 0);

            ConfigHelper.addMainConfigInclude(dao, title, configId);
            con.commit();
            
            return configId;
        }
    }
    
    private static void addMainConfigInclude(ConfigDAO dao, String title, int configId) throws Exception {
        synchronized (InitConfigTest.class) {
            var configMain = dao.getGlobalConfig(InitConfigTest.configMainId);
            configMain.setData(configMain.getData() + "\n#\n# " + title + "\ninclude." + configId + "=1");
            dao.updateGlobalConfig(configMain);
            
            Preferences.processIncludes(dao, configMain.getData(), true);
        }
    }
    
    public static void addToConfig(int configId, String content) throws Exception {
        try (var con = DbTest.conPoolRoot.getDBConnectionFromPool()) {
            var dao = new ConfigDAO(con);

            var config = dao.getGlobalConfig(configId);
            Assert.assertNotNull(config);
            
            config.setData(config.getData() + content);
            
            dao.updateGlobalConfig(config);
            
            con.commit();
        }
    }
    
    public static String generateConstants(Object... pairs) {
        var config = new StringBuilder();
        config.append("# constants\n");
        //TODO: User ParameterMap.of(pairs)
        for (int i = 0; i < pairs.length; i += 2) {
            config
                .append(pairs[i])
                .append("=")
                .append(pairs[i + 1]);
        }
        return config.toString();
    }

}