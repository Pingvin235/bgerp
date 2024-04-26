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
 * Set with DB connections, taken from a pool on demand.
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

    /** Pool for getting new connections */
    private final ConnectionPool pool;
    /** Master connection is opened in the ConnectionSet and may be closed by it */
    private final boolean internalMaster;
    /** Auto commit option for all the connections in the set */
    private final boolean autoCommit;
    /*
        Master, slave and trash connection references below must be volatile.
        As connection sets can be given to a separated thread, e.g. in EventProcessor,
        links to initialized there connections should not be cached.
    */
    private volatile Connection masterConnection;
    private volatile Connection slaveConnection;
    private volatile Map<String, Connection[]> trashConnections;

    public ConnectionSet(ConnectionPool setup, boolean autoCommit) {
        this.pool = setup;
        this.internalMaster = true;
        this.autoCommit = autoCommit;
    }

    protected ConnectionSet(Connection master) {
        this.pool = null;
        this.internalMaster = false;
        this.autoCommit = false;
        this.masterConnection = master;
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

    private Connection newMasterConnection() {
        Connection result = pool.getDBConnectionFromPool();
        try {
            if (result != null && result.getAutoCommit() != autoCommit) {
                result.setAutoCommit(autoCommit);
            }
        } catch (SQLException ex) {
            log.error(ex);
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

    private Connection newSlaveConnection() {
        return pool.getDBSlaveConnectionFromPool(getConnection());
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

    private Connection newTrashConnection(String tableName) {
        Connection result = pool.getDBTrashConnectionFromPool(tableName, ConnectionPool.RETURN_NULL);
        try {
            if (result != null && result.getAutoCommit() != autoCommit) {
                result.setAutoCommit(autoCommit);
            }
        } catch (SQLException ex) {
            log.error(ex);
        }

        return result;
    }

    /**
     * Commits all the connections
     * @throws SQLException
     */
    public void commit() throws SQLException {
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

    /**
     * Rolls back all the connections
     */
    public void rollback() {
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

        // slaveConnection is always autoCommit = true

        if (masterConnection != null) {
            try {
                masterConnection.rollback();
            } catch (Exception e) {
                log.error(e);
            }
        }
    }

    /**
     * Closes all the connections
     */
    public void close() {
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

        if (slaveConnection != null) {
            // if slaveConnection == masterConnection, only masterConnection will be closed later
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

        if (masterConnection != null) {
            // closing masterConnection only if it was obtained internally in the ConnectionSet
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
}