package ru.bgcrm.util;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.bgerp.app.db.sql.pool.ConnectionPool;
import org.bgerp.util.Log;

import com.google.common.annotations.VisibleForTesting;

import ru.bgcrm.dao.ConfigDAO;
import ru.bgcrm.event.EventProcessor;
import ru.bgcrm.event.SetupChangedEvent;
import ru.bgcrm.model.Config;

public class Setup extends Preferences {
    private static final Log log = Log.getLog();

    private static volatile Setup instance;
    /** ConnectionPool used for initial load. Used in integration tests. */
    private static volatile ConnectionPool connectionPoolInit;

    private String bundleName;
    private ConnectionPool connectionPool;

    public static String getBundleName() {
        return Utils.getSystemProperty("setup.data", "bgerp");
    }

    /**
     * Use this singleton call wisely, because introducing that
     * everywhere makes code hardly testable.
     * @return
     */
    public static Setup getSetup() {
        return getSetup(true);
    }

    private static Setup getSetup(boolean initConfig) {
        if (instance == null) {
            if (connectionPoolInit == null)
                instance = new Setup(initConfig);
            else
                instance = new Setup(connectionPoolInit);
        }
        return instance;
    }

    @VisibleForTesting
    public static void resetSetup(ConnectionPool pool) {
        instance = null;
        connectionPoolInit = pool;
    }

    public Setup(boolean initConfigAndPool) {
        this.bundleName = getBundleName();

        loadBundle(bundleName, this.data, false);
        if (initConfigAndPool) {
            connectionPool = new ConnectionPool("MAIN", this);

            loadConfigMap(this.data);
            loadBundle(bundleName, data, false);
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
     * @param keys the keys to be copied, will be prepended by {@code bgerp.}
     */
    private void setSystemProperties(String... keys) {
        for (String key : keys) {
            var value = get(key);
            if (!Utils.isBlankString(value))
                System.setProperty("bgerp." + key, value);
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
            Map<String, String> data = new HashMap<>();
            loadConfigMap(data, con);
            if (Utils.notBlankString(bundleName))
                loadBundle(bundleName, data, false);

            for (Map.Entry<String, String> me : data.entrySet())
                this.data.put(me.getKey(), me.getValue());

            // remove no more existing keys
            this.data.keySet().retainAll(data.keySet());

            configMap = null;
        } catch (Exception e) {
            log.error(e);
        }
    }

    private void loadConfigMap(Map<String, String> data) {
        try (var con = getDBConnectionFromPool()) {
            loadConfigMap(data, con);
        } catch (Exception e) {
            log.error(e);
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
     * @return SQL connection pool report.
     */
    public String getPoolStatus() {
        var sb = new StringBuilder("Connections pool to Master status ");
        sb.append(connectionPool.getPoolStatus());
        return sb.toString();
    }
}