package ru.bgcrm.plugin.bgbilling;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.bgerp.app.cfg.ConfigMap;
import org.bgerp.app.cfg.Setup;
import org.bgerp.app.exception.BGException;
import org.bgerp.util.Log;

import ru.bgcrm.util.Utils;

public class DBInfoManager {
    private static final Log log = Log.getLog();

    private static DBInfoManager instance;

    private Map<String, DBInfo> dbInfoMap = new HashMap<>();
    private List<DBInfo> dbInfoList = new ArrayList<>();

    static final String[] SUPPORTED_VERSIONS = { "10.2", "9.2", "8.2", "8.0", "7.2", "7.1", "7.0" };

    private DBInfoManager(Setup setup) {
        final String prefix = "bgbilling:server.", prefixOld = "bgbilling.";

        for (Map.Entry<Integer, ConfigMap> me : setup.subSokIndexed(prefix, prefixOld).entrySet()) {
            ConfigMap params = me.getValue();
            try {
                DBInfo dbInfo = new DBInfo(params.get("id"));
                dbInfo.setUrl(params.get("url"));
                dbInfo.setTitle(params.get("title"));
                dbInfo.setVersion(params.get("version", ""));
                dbInfo.setSetup(setup.subSok(prefix + me.getKey() + ".", prefixOld + me.getKey() + "."));

                if (Utils.notBlankString(dbInfo.getVersion()) && !StringUtils.startsWithAny(dbInfo.getVersion(), SUPPORTED_VERSIONS))
                    throw new BGException("Unsupported billing version: {}", dbInfo.getVersion());

                dbInfoMap.put(dbInfo.getId(), dbInfo);
                dbInfoList.add(dbInfo);
            } catch (Exception e) {
                log.error(Log.format("Parsing server: {}", me.getKey()), e);
            }
        }
    }

    public static void flush() {
        // закрытие соединений к БД
        if (instance != null) {
            for (DBInfo dbInfo : instance.getDbInfoList()) {
                dbInfo.getConnectionPool().close();
            }
        }
        instance = null;
    }

    public static DBInfoManager getInstance() {
        if (instance == null) {
            instance = new DBInfoManager(Setup.getSetup());
        }
        return instance;
    }

    public static DBInfo getDbInfo(String billingId) {
        DBInfo dbInfo = DBInfoManager.getInstance().getDbInfoMap().get(billingId);
        if (dbInfo == null) {
            throw new BGException("Не найден биллинг: {}", billingId);
        }
        return dbInfo;
    }

    public List<DBInfo> getDbInfoList() {
        return dbInfoList;
    }

    public Map<String, DBInfo> getDbInfoMap() {
        return dbInfoMap;
    }
}