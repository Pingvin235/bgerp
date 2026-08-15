package org.bgerp.util.sql.pool;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.Duration;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

import javax.sql.DataSource;

import org.apache.commons.dbcp2.ConnectionFactory;
import org.apache.commons.dbcp2.DriverManagerConnectionFactory;
import org.apache.commons.dbcp2.PoolableConnection;
import org.apache.commons.dbcp2.PoolableConnectionFactory;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.bgerp.app.cfg.ConfigMap;
import org.bgerp.app.exception.alarm.AlarmSender;
import org.bgerp.util.Log;
import org.bgerp.util.sql.pool.fakesql.FakeConnection;

import ru.bgcrm.util.Utils;
import ru.bgcrm.util.sql.ConnectionSet;

public class ConnectionPool {
    private static final Log log = Log.getLog();

    private static final int MAX_IDLE_DEFAULT = 20;
    private static final int MAX_ACTIVE_DEFAULT = 300;

    private static final String PROPERTY_USER = "user";
    private static final String PROPERTY_CHAR_SET = "charSet";
    private static final String PROPERTY_PASSWORD = "password";

    public static final int RETURN_NULL = -1;
    public static final int RETURN_FAKE = 0;
    public static final int RETURN_SLAVE = 1;
    public static final int RETURN_MASTER = 2;

    private final String name;

    private boolean dbTrace;
    /**
     * prevention of slave connection overuse and server freezing - if slave connections run out
     * (e.g. when a replica hangs), master connections are used instead
     */
    private boolean disablePreventionSlaveOverrun = false;

    private final ConcurrentMap<Object, StackTraceElement[]> trace = new ConcurrentHashMap<>();

    private GuardSupportedPool connectionPool;
    private DataSource dataSource;

    // last time a connection attempt to Master DB failed
    private final AtomicLong lastMasterErrorTime = new AtomicLong();
    // how many milliseconds after the error a new connection attempt can be made
    private static final long MASTER_RETEST_INTERVAL = 5000;

    // connection pools to Slave databases
    private final ConcurrentHashMap<String, GuardSupportedPool> slavePools = new ConcurrentHashMap<>();
    // times when getting a connection from the slave pool failed
    private ConcurrentHashMap<String, Long> slaveErrorTimes = new ConcurrentHashMap<>();
    // if getting a connection from the slave pool failed - the minimum time before the attempt is retried
    private static final long MIN_TIME_FOR_SLAVE_USE = 10000;

    // connection pools to "trash" databases
    private final ConcurrentHashMap<String, GuardSupportedPool> trashPools = new ConcurrentHashMap<>();
    // selector of the needed "trash" database
    private TrashDatabaseSelector trashSelector;

    // replication management
    private final Object repMutex = new Object();
    // flag of slave lagging behind master
    private final Set<String> behindMasterReplications = new TreeSet<>();
    // flag of disabled slaves
    private final Set<String> notAvailableReplications = new TreeSet<>();

    public ConnectionPool(String name, ConfigMap map) {
        this.name = name;
        try {
            log.info(name + "Init DB connection pools.");

            dbTrace = map.getInt("db.trace", 0) > 0;
            disablePreventionSlaveOverrun = map.getInt("db.disable.prevention.slave.overrun", 0) > 0;

            connectionPool = initConnectionPool(map, "db.");
            for (String slaveId : map.subKeyed("db.slave.").keySet()) {
                log.info(name + "Init slave pool {}", slaveId);
                slavePools.put(slaveId, initConnectionPool(map, "db.slave." + slaveId + "."));
            }

            log.info(name + "Init trash pools..");
            for (String trashId : map.subKeyed("db.trash.").keySet()) {
                log.info(name + "Init trash pool {}", trashId);
                trashPools.put(trashId, initConnectionPool(map, "db.trash." + trashId + "."));
            }
            trashSelector = new TrashDatabaseSelector(map);

            if (connectionPool != null) {
                this.dataSource = connectionPool.dataSource;
            }
        } catch (Exception ex) {
            log.error(name + ex.getMessage(), ex);
        }
    }

    /**
     * Initializes a connection pool to the database
     * @param prefix - prefix for config variables containing the connection options
     * @return the created connection pool, or {@code null} if URL is not configured
     * @throws Exception
     */
    private GuardSupportedPool initConnectionPool(ConfigMap prefs, String prefix) throws Exception {
        final var dbURL = prefs.get(prefix + "url", null);
        log.info("url: " + dbURL);
        if (Utils.notBlankString(dbURL)) {
            Properties properties = new Properties();
            properties.setProperty(PROPERTY_USER, prefs.get(prefix + "user"));
            properties.setProperty(PROPERTY_PASSWORD, prefs.get(prefix + "pswd"));
            properties.setProperty(PROPERTY_CHAR_SET, prefs.get(prefix + "charset", ""));

            properties.setProperty("jdbcCompliantTruncation", "false");
            properties.setProperty("useUnicode", "true");
            properties.setProperty("characterEncoding", "UTF-8");
            properties.setProperty("zeroDateTimeBehavior", "convertToNull");
            properties.setProperty("allowMultiQueries", "true");
            properties.setProperty("useLegacyDatetimeCode", "false");
            properties.setProperty("serverTimezone", TimeZone.getDefault().getID());

            final ConnectionFactory connectionFactory = new DriverManagerConnectionFactory(dbURL, properties);

            final PoolableConnectionFactory poolableConFactory = new PoolableConnectionFactory(connectionFactory, null);
            poolableConFactory.setValidationQuery("SELECT 1");
            poolableConFactory.setValidationQueryTimeout(prefs.getInt(prefix + "validationTimeout", -1));

            GenericObjectPool<PoolableConnection> connectionPool = null;

            if (dbTrace) {
                connectionPool = new GenericObjectPool<>(poolableConFactory) {
                    @Override
                    public PoolableConnection borrowObject() throws Exception {
                        final PoolableConnection result = super.borrowObject();

                        trace.put(result, Thread.currentThread().getStackTrace());

                        return result;
                    }

                    @Override
                    public void returnObject(PoolableConnection obj) {
                        trace.remove(obj);

                        super.returnObject(obj);
                    }
                };
            } else {
                connectionPool = new GenericObjectPool<>(poolableConFactory);
            }

            connectionPool.setMaxIdle(prefs.getInt(prefix + "maxIdle", MAX_IDLE_DEFAULT));
            connectionPool.setMaxTotal((int) prefs.getSokLong(MAX_ACTIVE_DEFAULT, prefix + "maxTotal", prefix + "maxActive"));
            connectionPool.setTestOnBorrow(true);
            connectionPool.setTestOnReturn(true);
            connectionPool.setTimeBetweenEvictionRuns(Duration.ofMillis(prefs.getLong(prefix + "timeBetweenEvictionRunsMillis", 30)));
            connectionPool.setMinEvictableIdle(Duration.ofMinutes(prefs.getLong(prefix + "minEvictableIdleTimeMillis", 30)));
            connectionPool.setTestWhileIdle(prefs.getLong(prefix + "testWhileIdle", 1) > 0);
            connectionPool.setSoftMinEvictableIdle(Duration.ofMillis(prefs.getLong(prefix + "softMinEvictableIdleTimeMillis", -1)));
            connectionPool.setNumTestsPerEvictionRun(prefs.getInt(prefix + "numTestsPerEvictionRun", 3));
            connectionPool.setLifo(prefs.getBoolean(prefix + "lifo", false));

            poolableConFactory.setPool(connectionPool);

            return new GuardSupportedPool(connectionPool);
        } else {
            return null;
        }
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public ConnectionSet getConnectionSet() {
        return new ConnectionSet(this, false);
    }

    public ConnectionSet getConnectionSet(boolean autoCommit) {
        return new ConnectionSet(this, autoCommit);
    }

    public void close() {
        if (connectionPool != null) {
            try {
                connectionPool.pool.close();
                // TODO: implement closing of various replica and trash database connections
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    /**
     * Returns a connection to the Master DB from the pool
     * @return the connection to the Master DB, or {@code null} if unavailable
     */
    public final Connection getDBConnectionFromPool() {
        if (connectionPool == null) {
            return null;
        }

        Connection con = null;

        // since a connection failure is detected by a 1 second query execution timeout, to avoid delays
        // after a failure, connection attempts within MASTER_RETEST_INTERVAL return null without an actual reconnect attempt
        final long lastMasterErrorTime = this.lastMasterErrorTime.get();
        if (lastMasterErrorTime != 0) {
            long now = System.currentTimeMillis();
            if (now - lastMasterErrorTime < MASTER_RETEST_INTERVAL) {
                return null;
            }

            this.lastMasterErrorTime.compareAndSet(lastMasterErrorTime, 0);
        }

        try {
            if (connectionPool.isOverload()) {
                log.error("Master pool '{}' connections limit is over", name);

                AlarmSender.send("db.master.connection.limit.over", 30 * 1000, "Master DB connections limit is over", () ->
                    "That can slow down the app instance." +
                    "\nSomething has to be done to speed up work of Master DB." +
                    "\n\n" + poolStatus());
            }

            con = connectionPool.dataSource.getConnection();
            con.setAutoCommit(false);
        } catch (Exception ex) {
            log.error(name + " " + ex.getMessage(), ex);

            AlarmSender.send("db.master.connect.error", 10 * 1000, "Master DB connection error",
                    () -> "Commection to Master DB '" + name + "' must be urgently restored", ex, null);

            this.lastMasterErrorTime.set(System.currentTimeMillis());
        }

        return con;
    }

    private final GuardSupportedPool getSlaveConnectionPool() {
        if (slavePools.size() > 0) {
            try {
                GuardSupportedPool prefPool = null;
                String prefId = null;

                long now = System.currentTimeMillis();

                float minRatio = Float.MAX_VALUE;
                // selecting the least loaded database by the minimal ratio of active connections
                // to the maximum number of active connections
                for (Map.Entry<String, GuardSupportedPool> me : slavePools.entrySet()) {
                    String key = me.getKey();
                    GuardSupportedPool pool = me.getValue();

                    // lagging database
                    if (!isReplicationNotBehindMaster(key)) {
                        continue;
                    }

                    // there was an error getting a connection from the slave and the required time hasn't passed yet
                    Long errorTime = slaveErrorTimes.get(key);
                    if (errorTime != null && now - errorTime < MIN_TIME_FOR_SLAVE_USE) {
                        continue;
                    }

                    final float ratio = pool.getLoadRatio();
                    if (ratio < minRatio) {
                        prefPool = pool;
                        prefId = key;
                        minRatio = ratio;
                    }
                }

                try {
                    // null can result if all Slave databases are lagging
                    if (prefPool != null) {
                        boolean slaveOk = true;

                        if (prefPool.isOverload()) {
                            log.error("Slave pool '{}' connections limit is over", prefId);

                            AlarmSender.send("db.slave.connection.limit.over",  30 * 1000, "Slave DB connections limit is over", () ->
                                "That can slow down the app instance." +
                                "\nSomething has to be done to speed up work of Slave DB." +
                                "\n\n" + poolStatus());

                            // slave can be used only if protection against DB hanging is not enabled
                            slaveOk = disablePreventionSlaveOverrun;

                            slaveErrorTimes.remove(prefId);
                        }

                        if (slaveOk) {
                            return prefPool;
                        }
                    }
                } catch (Exception ex) {
                    log.error(name + " " + ex.getMessage(), ex);

                    AlarmSender.send("db.slave.connect.error", 30 * 1000, "Slave DB connection error",
                        () -> "Commection to Slave DB '" + name + "' must be urgently restored", ex, null);

                    slaveErrorTimes.put(prefId, now);
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }

        // no Slave database was returned
        return connectionPool;
    }

    public final DataSource getSlaveDataSource() {
        GuardSupportedPool pool = getSlaveConnectionPool();
        if (pool != null) {
            return pool.dataSource;
        }
        return connectionPool.dataSource;
    }

    /**
     * Returns a connection to the Slave DB from the pool. If Slave DB isn't defined in
     * the configuration - returns from the Master pool.
     * @return the connection
     */
    public final Connection getDBSlaveConnectionFromPool() {
        return getDBSlaveConnectionFromPool(null);
    }

    /**
     * Returns a connection to the Slave DB from the pool. If Slave DB isn't defined in
     * the configuration - returns from the Master pool.
     * @param master if {@code false}, returns {@code null} when there are no slave bases
     * @return the connection
     */
    public final Connection getDBSlaveConnectionFromPool(final Connection master) {
        Connection con = null;
        GuardSupportedPool pool = getSlaveConnectionPool();

        if (pool != null) {
            try {
                con = pool.dataSource.getConnection();
            } catch (SQLException e) {
                log.error(e.getMessage(), e);
            }
        }

        // no slave database was returned
        if (con == null) {
            log.warn("Failed to get connection to the slave database!\n" + poolStatus());

            if (master == null) {
                con = getDBConnectionFromPool();
            } else {
                con = master;
            }
        }

        return con;
    }

    /**
     * Returns a connection to the trash DB if it's configured for the table, or depending on retType
     * @param tableName table name
     * @param retType {@link #RETURN_FAKE}, {@link #RETURN_SLAVE} or {@link #RETURN_MASTER}
     * @return if trash DB configuration is not specified, a connection to the master DB, a fake connection, or a connection to the Slave DB may be returned
     */
    public final Connection getDBTrashConnectionFromPool(String tableName, int retType) {
        Connection result = null;

        try {
            String trashBase = trashSelector.getDatabaseName(tableName);
            if (trashBase != null) {
                GuardSupportedPool trashPool = trashPools.get(trashBase);
                if (trashPool != null) {
                    if (trashPool.isOverload()) {
                        result = new FakeConnection();

                        log.error("Trash pool '{}' connections limit is over", trashBase);

                        AlarmSender.send("db.trash.connection.limit.over", 30 * 1000, "Trash DB connections limit is over", () ->
                            "That can cause missing of some data for users." +
                            "\nSomething has to be done to speed up work of Trash DB." +
                            "\n\n" + poolStatus());
                    } else {
                        result = trashPool.dataSource.getConnection();
                        result.setAutoCommit(false);
                    }
                }
            }

            // Trash DB configuration is not defined
            if (result == null) {
                switch (retType) {
                    case RETURN_FAKE: {
                        result = new FakeConnection();
                        break;
                    }
                    case RETURN_MASTER: {
                        result = getDBConnectionFromPool();
                        break;
                    }
                    case RETURN_SLAVE: {
                        result = getDBSlaveConnectionFromPool();
                        break;
                    }
                    case RETURN_NULL: {
                        break;
                    }
                }
            }
        } catch (Exception e) {
            // connection stub, does nothing, returns empty interface implementations
            result = new FakeConnection();
            log.error(e.getMessage(), e);
        }
        return result;
    }

    /**
     * Returns a connection to the dedicated "trash" or Master database (if
     * the trash database is not found)
     * @param tableName
     * @return the connection
     */
    public final Connection getDBTrashOrMasterConnectionFromPool(String tableName) {
        return getDBTrashConnectionFromPool(tableName, RETURN_MASTER);
    }

    /**
     * Returns a connection to the dedicated "trash" or Slave database
     * @param tableName
     * @return the connection
     */
    public final Connection getDBTrashOrSlaveConnectionFromPool(String tableName) {
        return getDBTrashConnectionFromPool(tableName, RETURN_SLAVE);
    }


    /**
     * Returns identifiers of slave databases
     * @return the slave database identifiers
     */
    public final Set<String> getSlaveBaseId() {
        return slavePools.keySet();
    }

    /**
     * Returns identifiers of trash databases
     * @return the trash database identifiers
     */
    public final Set<String> getTrashBaseId() {
        return trashPools.keySet();
    }

    /**
     * Returns a connection to the trash database.
     * If the database ID is incorrect, returns {@code null} with all the consequences, since this
     * is used only for forced database selection in some specific service cases.
     * @param poolId - database ID
     * @return connection
     */
    public final Connection getTrashConnectionFromPool(String poolId) {
        Connection con = null;
        try {
            if (poolId != null) {
                GuardSupportedPool pool = trashPools.get(poolId);
                if (pool != null) {
                    con = pool.dataSource.getConnection();
                    con.setAutoCommit(false);
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return con;
    }

    /**
     * @return status text report for connection pools
     */
    public String poolStatus() {
        if (connectionPool == null) {
            return "";
        }

        StringBuffer sb = new StringBuffer("Connections pool to Master '" + name + "' status ");
        // Master pool status
        sb.append(poolStatus(connectionPool.pool));

        // Slave pools statuses
        for (Map.Entry<String, GuardSupportedPool> me : slavePools.entrySet()) {
            String name = me.getKey();
            GenericObjectPool<?> pool = me.getValue().pool;

            sb.append("\n");
            sb.append("Connections pool to Slave '" + name + "' status ");
            sb.append(poolStatus(pool));
        }

        // Trash pools statuses
        for (Map.Entry<String, GuardSupportedPool> me : trashPools.entrySet()) {
            String name = me.getKey();
            GenericObjectPool<?> pool = me.getValue().pool;

            sb.append("\n");
            sb.append("Connections pool to Trash '" + name + "' status ");
            sb.append(poolStatus(pool));
        }

        return sb.toString();
    }

    private String poolStatus(GenericObjectPool<?> connectionPool) {
        StringBuilder sb = new StringBuilder();
        sb.append("Idle: ");
        sb.append(connectionPool.getNumIdle());
        sb.append("; Active: ");
        sb.append(connectionPool.getNumActive());
        sb.append("; maxTotal: ");
        sb.append(connectionPool.getMaxTotal());
        sb.append("; maxIdle: ");
        sb.append(connectionPool.getMaxIdle());
        return sb.toString();
    }

    /**
     * @return pool connections borrowing stack traces text report
     */
    public String getDbTrace() {
        StringBuilder sb = new StringBuilder(100);

        if (dbTrace) {
            for (Map.Entry<Object, StackTraceElement[]> e : trace.entrySet()) {
                sb.append(e.getKey()).append('\n');

                StackTraceElement[] trace = e.getValue();

                for (int i = 2; i < trace.length; i++)
                    sb.append("\tat " + trace[i]).append('\n');

                sb.append('\n');
            }

            if (sb.isEmpty())
                sb.append("No connections to DB");
        } else
            sb.append("Pool trace is off. Check db.trace option");

        return sb.toString();
    }

    /**
     * Enables/disables the Slave database lag flag
     * @param slaveId Slave database identifier
     * @param isNotBehind {@code true} - lag disabled, {@code false} - lag enabled
     */
    @Deprecated
    public void setReplicationNotBehindMaster(String slaveId, boolean isNotBehind) {
        synchronized (repMutex) {
            if (isNotBehind) {
                behindMasterReplications.remove(slaveId);
            } else {
                behindMasterReplications.add(slaveId);
            }
        }
    }

    /**
     * Checks whether the Slave database is lagging
     * @param slaveId Slave database identifier
     * @return {@code true} if not lagging
     */
    @Deprecated
    public boolean isReplicationNotBehindMaster(String slaveId) {
        synchronized (repMutex) {
            return !behindMasterReplications.contains(slaveId);
        }
    }

    /**
     * Checks availability of the Slave database
     * @param slaveId Slave database identifier
     * @return {@code true} if available, {@code false} if not available
     */
    @Deprecated
    public boolean isReplicationAvailable(String slaveId) {
        synchronized (repMutex) {
            return !notAvailableReplications.contains(slaveId);
        }
    }
}
