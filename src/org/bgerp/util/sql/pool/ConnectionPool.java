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
     * предотвращение перерасхода slave-соединений и залипания сервера, если
     * кончаются slave-соединения (например, при подвешивании реплики), берутся master-соединения.
     */
    private boolean disablePreventionSlaveOverrun = false;

    private final ConcurrentMap<Object, StackTraceElement[]> trace = new ConcurrentHashMap<>();

    private GuardSupportedPool connectionPool;
    private DataSource dataSource;

    // последнее время, когда попытка соединения с Мастер БД окончилась ошибкой
    private final AtomicLong lastMasterErrorTime = new AtomicLong();
    // через какое количество миллисекунд после ошибки можно попробовать снова установить соединений
    private static final long MASTER_RETEST_INTERVAL = 5000;

    // пулы соединений к Slave - базам
    private final ConcurrentHashMap<String, GuardSupportedPool> slavePools = new ConcurrentHashMap<>();
    // времена, когда из слейв пула была ошибка получения коннекта
    private ConcurrentHashMap<String, Long> slaveErrorTimes = new ConcurrentHashMap<>();
    // если из слейв пула была ошибка получения коннекта - минимальное время, через которое попытка будет повторена
    private static final long MIN_TIME_FOR_SLAVE_USE = 10000;

    // пулы соединений к "мусорным" базам
    private final ConcurrentHashMap<String, GuardSupportedPool> trashPools = new ConcurrentHashMap<>();
    // селектор нужной "мусорной" базы
    private TrashDatabaseSelector trashSelector;

    // управление репликацией
    private final Object repMutex = new Object();
    // флаг отставания слейва от мастера
    private final Set<String> behindMasterReplications = new TreeSet<>();
    // флаг отключенных слейвов
    private final Set<String> notAvailableReplications = new TreeSet<>();

    public ConnectionPool(String name, ConfigMap map) {
        if (!name.endsWith(" ")) {
            name += " ";
        }

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
     * Инициализация пула соединений к базе данных
     * @param prefix - префикс к переменным конфигурации, содержащим опция соединения
     * @return
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

            /* Previously here was an extension of PoolableConnectionFactory class with such a logic:
            public Object makeObject() throws Exception {
                ... copied blocks from PoolableConnectionFactory
                return new PoolableConnection(conn, _pool, _config) {
                    @Override
                    public synchronized void close() throws SQLException {
                        try {
                            final List<?> traceList = super.getTrace();
                            if (traceList != null && traceList.size() > 80) {
                                StringBuilder sb = new StringBuilder(300);
                                sb.append("Many statements was open at connection close:\n");

                                int count = 0;

                                for (Object o : traceList) {
                                    if (++count > 100) {
                                        break;
                                    }

                                    sb.append(o).append('\n');
                                }

                                log.error(name + sb.toString(), new RuntimeException());
                            }
                        } finally {
                            super.close();
                        }
                    }
                };
            */

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
                //TODO: Сделать закрытие различных соединений реплик и мусорных баз.
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    /**
     * Возвращает соединение с Master БД из пула.
     * @return соединение с Master БД либо null в случае недоступности.
     */
    public final Connection getDBConnectionFromPool() {
        if (connectionPool == null) {
            return null;
        }

        Connection con = null;

        // т.к. сбой соединения определяется по таймауту в 1 с выполннения запроса, то чтобы не было задержек
        // после сбоя попытки получения соединения в течении MASTER_RETEST_INTERVAL возвращаются null без фактической попытки реконнекта
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
                // выбор наименнее загруженной базы по минимальному отношению активных коннектов
                // к максимальному числу активных
                for (Map.Entry<String, GuardSupportedPool> me : slavePools.entrySet()) {
                    String key = me.getKey();
                    GuardSupportedPool pool = me.getValue();

                    //отстающая база
                    if (!isReplicationNotBehindMaster(key)) {
                        continue;
                    }

                    // была ошибка получения коннекта со слейва и не прошло необходимое время
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
                    // null может получится, если все Slave базы отстали
                    if (prefPool != null) {
                        boolean slaveOk = true;

                        if (prefPool.isOverload()) {
                            log.error("Slave pool '{}' connections limit is over", prefId);

                            AlarmSender.send("db.slave.connection.limit.over",  30 * 1000, "Slave DB connections limit is over", () ->
                                "That can slow down the app instance." +
                                "\nSomething has to be done to speed up work of Slave DB." +
                                "\n\n" + poolStatus());

                            // можно использоать slave только если не включена защита от зависания БД
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

        // Slave база не выдана
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
     * Возвращает соединение с Slave БД из пула. Если Slave БД не определены в
     * конфигурации - возвращает из Master пула.
     * @return
     */
    public final Connection getDBSlaveConnectionFromPool() {
        return getDBSlaveConnectionFromPool(null);
    }

    /**
     * Возвращает соединение с Slave БД из пула. Если Slave БД не определены в
     * конфигурации - возвращает из Master пула.
     * @param masterOnNull если false то при отсутсвии slave баз вернет null.
     * @return
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

        // slave база не выдана
        if (con == null) {
            log.warn("Не удалось получить подключение к slave базе данных!\n" + poolStatus());

            if (master == null) {
                con = getDBConnectionFromPool();
            } else {
                con = master;
            }
        }

        return con;
    }

    /**
     * Возвращает соединение с мусорной БД если она описана для таблицы в конфиге либо в зависимости от retType.
     * @param tableName имя таблицы.
     * @param retType {@link #RETURN_FAKE}, {@link #RETURN_SLAVE} либо {@link #RETURN_MASTER}.
     * @return если не указана конфигурация мусорной БД может быть возвращен коннект к мастер БД, фейковый коннект либо коннект Slave БД.
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

            // конфигурация Trash базы не определена
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
            //конекшн заглушка, ничего не делает, возвращает пустые имплементации интерфейсов
            result = new FakeConnection();
            log.error(e.getMessage(), e);
        }
        return result;
    }

    /**
     * Возвращение соединения к выделенной "мусорной" либо Master-базе (если
     * треш-база не найдена).
     * @param tableName
     * @return
     */
    public final Connection getDBTrashOrMasterConnectionFromPool(String tableName) {
        return getDBTrashConnectionFromPool(tableName, RETURN_MASTER);
    }

    /**
     * Возвращение соединения к выделенной "мусорной" либо Slave-базе
     * @param tableName
     * @return
     */
    public final Connection getDBTrashOrSlaveConnectionFromPool(String tableName) {
        return getDBTrashConnectionFromPool(tableName, RETURN_SLAVE);
    }


    /**
     * Возвращает идентификаторы slave баз.
     * @return
     */
    public final Set<String> getSlaveBaseId() {
        return slavePools.keySet();
    }

    /**
     * Возвращает идентификаторы trash баз.
     * @return
     */
    public final Set<String> getTrashBaseId() {
        return trashPools.keySet();
    }

    /**
     * Возвращает соединение с trash базой.
     * если неверная база то null со всем вытекающим, так как
     * применяется только при принудительном выборе базы в некоторых специфичных
     * служебных случаях.
     * @param poolId - ид базы
     * @return коннекшен
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
     * @return status text report for connection pools.
     */
    public String poolStatus() {
        if (connectionPool == null) {
            return "";
        }

        StringBuffer sb = new StringBuffer("Connections pool to Master '" + name + "' status ");
        // статус Master пула
        sb.append(poolStatus(connectionPool.pool));

        // статусы Slave пулов
        for (Map.Entry<String, GuardSupportedPool> me : slavePools.entrySet()) {
            String name = me.getKey();
            GenericObjectPool<?> pool = me.getValue().pool;

            sb.append("\n");
            sb.append("Connections pool to Slave '" + name + "' status ");
            sb.append(poolStatus(pool));
        }

        // статусы Trash пулов
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
     * @return pool connections borowing stack traces text report.
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
     * Включение/Отключение флага отставания Slave базы
     * @param slaveId идентификатор Slave базы
     * @param isNotBehind true - отставание выключено, false - отставание включено
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
     * Проверяет есть ли отставание Slave базы
     * @param slaveId идентификатор Slave базы
     * @return
     */
    @Deprecated
    public boolean isReplicationNotBehindMaster(String slaveId) {
        synchronized (repMutex) {
            return !behindMasterReplications.contains(slaveId);
        }
    }

    /**
     * Проверяет доступность Slave базы
     * @param slaveId идентификатор Slave базы
     * @return true - если доступна, false - если не доступна
     */
    @Deprecated
    public boolean isReplicationAvailable(String slaveId) {
        synchronized (repMutex) {
            return !notAvailableReplications.contains(slaveId);
        }
    }
}
