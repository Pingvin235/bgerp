package ru.bgcrm.util.sql;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bgerp.app.db.sql.pool.ConnectionPool;
import org.bgerp.app.db.sql.pool.fakesql.FakeConnection;
import org.bgerp.util.Log;

/**
 * Set with DB connections, taken from pools on demand.
 *
 * @author Amir Absalilov
 * @author Shamil Vakhitov
 */
public class ConnectionSet {
    private static final Log log = Log.getLog();

    public static final String KEY = "conSet";

    public final static int TYPE_MASTER = 1;
    public final static int TYPE_SLAVE = 2;
    public final static int TYPE_TRASH = 3;
    public final static int TYPE_FAKE = 4;

    protected boolean autoCommit;

    /** Master connection is opened in the ConnectionSet and may be closed by it. */
    private final boolean internalMaster;

    private Connection masterConnection;
    private Connection slaveConnection;

    private Map<String, Connection[]> trashConnections;

    private ConnectionPool setup;

    protected ConnectionSet(Connection master) {
        this.masterConnection = master;
        this.internalMaster = false;
    }

    protected ConnectionSet(Connection master, final boolean autoCommit) {
        this.masterConnection = master;
        this.internalMaster = false;
        this.autoCommit = autoCommit;

        assert check(master, autoCommit);
    }

    public ConnectionSet(ConnectionPool setup, boolean autoCommit) {
        this.setup = setup;
        this.internalMaster = true;
        this.autoCommit = autoCommit;
    }

    private static boolean check(final Connection master, final boolean autoCommit) {
        try {
            return master == null || master.getAutoCommit() == autoCommit;
        } catch (SQLException e) {
            log.error(e);
        }

        return false;
    }

    /**
     * Provides master DB connection to the main database.
     * @return
     */
    public Connection getConnection() {
        if (masterConnection == null)
            masterConnection = newMasterConnection();

        return masterConnection;
    }

    protected Connection newMasterConnection() {
        Connection result = setup.getDBConnectionFromPool();
        try {
            if (result != null && result.getAutoCommit() != autoCommit) {
                result.setAutoCommit(autoCommit);
            }
        } catch (SQLException ex) {
            log.error(ex.getMessage(), ex);
        }

        return result;
    }

    protected Connection newSlaveConnection() {
        return setup.getDBSlaveConnectionFromPool(getConnection());
    }

    protected Connection newTrashConnection(String tableName) {
        Connection result = setup.getDBTrashConnectionFromPool(tableName, ConnectionPool.RETURN_NULL);
        try {
            if (result != null && result.getAutoCommit() != autoCommit) {
                result.setAutoCommit(autoCommit);
            }
        } catch (SQLException ex) {
            log.error(ex.getMessage(), ex);
        }

        return result;
    }

    /**
     * Gets slave connection to read-only DB replica.
     * @return
     */
    public Connection getSlaveConnection() {
        if (slaveConnection == null) {
            slaveConnection = newSlaveConnection();

            if (slaveConnection == null) {
                slaveConnection = getConnection();
            }
        }

        return slaveConnection;
    }

    /**
     * Gets connection to so-called 'trash' DB, containing non-critical data.
     * @param tableName table name.
     * @param defaultType DB type returned if no trash DB is configured, can be:
     * <pre>
     * {@link #TYPE_SLAVE} - slave DB connection;
     * {@link #TYPE_FAKE} -  instance of {@link FakeConnection}, does not do anything;
     * {@link #TYPE_MASTER} - master DB connection.
     * </pre>
     * @return
     */
    public Connection getTrashConnection(String tableName, int defaultType) {
        if (trashConnections == null) {
            trashConnections = new HashMap<>();
        }

        Connection[] connections = trashConnections.get(tableName);
        if (connections == null) {
            trashConnections.put(tableName, connections = new Connection[4]);
        }

        Connection connection = connections[0];
        if (connection == null) {
            connection = newTrashConnection(tableName);

            if (connection == null) {
                connection = connections[defaultType];

                if (connection == null) {
                    switch (defaultType) {
                    case TYPE_SLAVE:
                        connections[TYPE_SLAVE] = connection = getSlaveConnection();
                        break;

                    case TYPE_FAKE:
                        connections[TYPE_FAKE] = connection = new FakeConnection();
                        break;

                    default:
                        connections[TYPE_MASTER] = connection = getConnection();
                        break;
                    }
                }
            }
        }

        return connection;
    }

    public void commit() throws Exception {
        if (masterConnection != null) {
            if (!masterConnection.getAutoCommit()) {
                masterConnection.commit();
            }
        }

        if (trashConnections != null) {
            Set<Connection> commited = new HashSet<>();

            for (Connection[] connections : trashConnections.values()) {
                for (int i = 0, size = connections.length; i < size; i++) {
                    Connection connection = connections[i];
                    if (connection != null && connection != slaveConnection && connection != masterConnection
                            && !connection.getAutoCommit() && commited.add(connection)) {
                        connection.commit();
                    }
                }
            }
        }
    }

    public void recycle() {
        // закрываем трэш коннекшны, если они есть
        if (trashConnections != null) {
            for (Connection[] connections : trashConnections.values()) {
                for (int i = 0, size = connections.length; i < size; i++) {
                    Connection connection = connections[i];
                    if (connection != null && connection != slaveConnection && connection != masterConnection) {
                        try {
                            if (!connection.isClosed()) {
                                connection.close();
                            }
                        } catch (Exception e) {
                            log.error(e);
                        }

                        connections[i] = null;
                    }
                }
            }

            trashConnections.clear();
        }

        // закрываем слейв коннекшн, если он есть
        if (slaveConnection != null) {
            // если slaveConnection и masterConnection - одно и тоже - закроем ниже только masterConnection
            if (slaveConnection != masterConnection) {
                try {
                    if (!slaveConnection.isClosed()) {
                        slaveConnection.close();
                    }
                } catch (Exception e) {
                    log.error(e);
                }
            }

            slaveConnection = null;
        }

        // закрываем мастер коннекшн, если он есть и если он был открыт внутри этого ConnectionSet
        if (masterConnection != null) {
            if (internalMaster) {
                try {
                    if (!masterConnection.isClosed()) {
                        masterConnection.close();
                    }
                } catch (Exception e) {
                    log.error(e);
                }
            }

            masterConnection = null;
        }
    }

    public boolean getAutoCommit() {
        return this.autoCommit;
    }

    /**
     * Sets autocommit property to all connections.
     * @param autoCommit wanted value.
     */
    public void setAutoCommit(final boolean autoCommit) {
        // трэш коннекшны, если они есть
        if (trashConnections != null) {
            for (Connection[] connections : trashConnections.values()) {
                for (int i = 0, size = connections.length; i < size; i++) {
                    Connection connection = connections[i];
                    if (connection != null && connection != slaveConnection && connection != masterConnection) {
                        try {
                            connection.setAutoCommit(autoCommit);
                        } catch (Exception e) {
                            log.error(e);
                        }
                    }
                }
            }
        }

        // слейв коннекшн всегда autoCommit = true

        // мастер коннекшн, если он есть
        if (masterConnection != null) {
            try {
                masterConnection.setAutoCommit(autoCommit);
            } catch (Exception e) {
                log.error(e);
            }
        }

        this.autoCommit = autoCommit;
    }

    public void rollback() {
        // трэш коннекшны, если они есть
        if (trashConnections != null) {
            for (Connection[] connections : trashConnections.values()) {
                for (int i = 0, size = connections.length; i < size; i++) {
                    Connection connection = connections[i];
                    if (connection != null && connection != slaveConnection && connection != masterConnection) {
                        try {
                            connection.rollback();
                        } catch (Exception e) {
                            log.error(e);
                        }
                    }
                }
            }
        }

        // слейв коннекшн всегда autoCommit = true

        // мастер коннекшн, если он есть
        if (masterConnection != null) {
            try {
                masterConnection.rollback();
            } catch (Exception e) {
                log.error(e);
            }
        }
    }
}