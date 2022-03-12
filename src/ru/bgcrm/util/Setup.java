package ru.bgcrm.util;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import com.google.common.annotations.VisibleForTesting;

import org.apache.commons.lang3.StringUtils;
import org.bgerp.util.Log;

import ru.bgcrm.dao.ConfigDAO;
import ru.bgcrm.event.EventProcessor;
import ru.bgcrm.event.SetupChangedEvent;
import ru.bgcrm.model.Config;
import ru.bgcrm.util.sql.ConnectionPool;
import ru.bgcrm.util.sql.SQLUtils;

public class Setup extends Preferences {
    private static final Log log = Log.getLog();

    private static volatile Setup setupData;

    /** ConnectionPool used for initial load. Used in integration tests. */
    private static volatile ConnectionPool connectionPoolInit;

    private String bundleName;
    private ConnectionPool connectionPool;

    // глобальные конфигурации, получаемые отдельно по коду
    private Map<Integer, ParameterMap> globalConfigMap = new HashMap<Integer, ParameterMap>();

    /**
     * Use this singleton call wisely, because introducing that
     * everywhere makes code hardly testable.
     * @return
     */
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
            setSystemProperties(TimeUtils.CONF_KEY_FORMAT_YMD, TimeUtils.CONF_KEY_FORMAT_YMDH, TimeUtils.CONF_KEY_FORMAT_YMDHM, TimeUtils.CONF_KEY_FORMAT_YMDHMS);
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

    /**
     * Set system properties out of the setup.
     * @param keys
     */
    private void setSystemProperties(String... keys) {
        for (String key : keys) {
            var value = get(key);
            if (!Utils.isBlankString(value))
                System.setProperty(key, value);
        }
    }

    /**
     * Use {@link #getDBConnectionFromPool()}.
     */
    @Deprecated
    public DataSource getDataSource() {
        return connectionPool.getDataSource();
    }

    /**
    * Use {@link #getDBSlaveConnectionFromPool()}.
    */
    @Deprecated
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

        Map<Integer, ParameterMap> includes = configDAO.getIncludes(config.getId());
        oldIncludes(con, includes, config);
        data.putAll(new Preferences(config.getData(), includes.values(), false));
    }

    /**
     * Handles old style includes and updates parents for included configs.
     * @param con DB connection.
     * @param includes map, already containing new style includes.
     * @param config config with includes.
     * @throws SQLException
     */
    private void oldIncludes(Connection con, Map<Integer, ParameterMap> includes, Config config) throws SQLException {
        var configDAO = new ConfigDAO(con);

        for (Map.Entry<String, String> me : new Preferences(config.getData()).sub(Config.INCLUDE_PREFIX).entrySet()) {
            int includedId = Utils.parseInt(me.getKey());

            log.warn("Used old-style included config {} in config {}", includedId, config.getId());

            if (!includes.containsKey(includedId)) {
                var included = configDAO.getGlobalConfig(includedId);
                if (included == null) {
                    log.warn("Not found included config {} in config {}", includedId, config.getId());
                    continue;
                }

                included.setParentId(config.getId());
                configDAO.updateGlobalConfig(included);
                con.commit();

                includes.put(included.getId(), new Preferences(included.getData()));
            }
        }
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