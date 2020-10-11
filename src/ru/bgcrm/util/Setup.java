package ru.bgcrm.util;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import com.google.common.annotations.VisibleForTesting;

import org.apache.commons.lang3.StringUtils;

import ru.bgcrm.dao.ConfigDAO;
import ru.bgcrm.event.EventProcessor;
import ru.bgcrm.event.SetupChangedEvent;
import ru.bgcrm.model.Config;
import ru.bgcrm.util.sql.ConnectionPool;
import ru.bgcrm.util.sql.SQLUtils;
import ru.bgerp.util.Log;

public class Setup extends Preferences {
    private static final Log log = Log.getLog();

    private static volatile Setup setupData;

    /** ConnectionPool used for initial load. Used in integration tests. */
    private static volatile ConnectionPool connectionPoolInit;

    private String bundleName;
    private ConnectionPool connectionPool;

    // глобальные конфигурации, получаемые отдельно по коду
    private Map<Integer, ParameterMap> globalConfigMap = new HashMap<Integer, ParameterMap>();

    public static Setup getSetup() {
        return getSetup(getBundleName(), true);
    }
    
    public static String getBundleName() {
        return System.getProperty("bgerp.setup.data", System.getProperty("bgcrm.setup.data", "bgerp"));
    }

    public static Setup getSetup(String bundleName, boolean initConfig) {
        if (setupData == null) {
            if (connectionPoolInit == null)
                setupData = new Setup(bundleName, initConfig);
            else
                setupData = new Setup(connectionPoolInit);
        }
        return setupData;
    }

    @VisibleForTesting
    public static void resetSetup(ConnectionPool pool) {
        setupData = null;
        connectionPoolInit = pool;
    }

    public Setup(String bundleName, boolean initConfigAndPool) {
        this.bundleName = bundleName;

        loadBundle(bundleName, this.data, false);
        if (initConfigAndPool) {
            connectionPool = new ConnectionPool("MAIN", this);
            loadConfigMap(this.data);

            EventProcessor.subscribe((e, conSet) -> {
                reloadConfig(conSet.getConnection());
            }, SetupChangedEvent.class);
        }
    }

    private Setup(ConnectionPool pool) {
        this.connectionPool = pool;
        loadConfigMap(this.data);
        EventProcessor.subscribe((e, conSet) -> {
            reloadConfig(conSet.getConnection());
        }, SetupChangedEvent.class);
    }

    public DataSource getDataSource() {
        return connectionPool.getDataSource();
    }

    public DataSource getSlaveDataSource() {
        return connectionPool.getSlaveDataSource();
    }

    public ConnectionPool getConnectionPool() {
        return connectionPool;
    }

    public Connection getDBConnectionFromPool() {
        return connectionPool.getDBConnectionFromPool();
    }
    
    public Connection getDBSlaveConnectionFromPool() {
        return connectionPool.getDBSlaveConnectionFromPool();
    }

    private void reloadConfig(Connection con) {
        try {
            Map<String, String> data = new HashMap<String, String>();
            loadBundle(bundleName, data, false);
            loadConfigMap(data, con);

            for (Map.Entry<String, String> me : data.entrySet())
                this.data.put(me.getKey(), me.getValue());
            
            // удаление пропавших ключей
            this.data.keySet().retainAll(data.keySet());

            configMap = null;
            globalConfigMap.clear();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    private void loadConfigMap(Map<String, String> data) {
        try (var con = getDBConnectionFromPool()) {
            loadConfigMap(data, con);
            if (StringUtils.isNotBlank(bundleName))
                loadBundle(bundleName, data, false);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    private void loadConfigMap(Map<String, String> data, Connection con) throws Exception {
        ConfigDAO configDAO = new ConfigDAO(con);

        ru.bgcrm.model.Config config = configDAO.getActiveGlobalConfig();
        if (config == null) {
            log.error("Active config not found!");
            return;
        }

        data.putAll(Preferences.processIncludes(configDAO, config.getData(), false));
    }
    

    /**
     * Возвращает глобальную конфигурацию по её коду.
     * 
     * @param id
     * @return
     */
    public ParameterMap getGlobalConfig(int id) {
        synchronized (globalConfigMap) {
            ParameterMap config = (ParameterMap) globalConfigMap.get(id);

            if (config == null) {
                Connection con = getDBConnectionFromPool();
                try {
                    Config conf = new ConfigDAO(con).getGlobalConfig(id);
                    if (conf != null) {
                        config = new Preferences(conf.getData());
                    } else {
                        config = new Preferences("");
                    }

                    globalConfigMap.put(id, config);
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                } finally {
                    SQLUtils.closeConnection(con);
                }
            }

            return config;
        }
    }

    /**
     * Отчет по статусу пулов соединений
     * @return
     */
    public String getPoolStatus() {
        StringBuffer sb = new StringBuffer("Connections pool to Master status ");
        // статус пула
        sb.append(connectionPool.getPoolStatus());

        return sb.toString();
    }
}