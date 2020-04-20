package ru.bgcrm.util.sql;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import ru.bgcrm.model.BGException;
import ru.bgcrm.util.sql.fakesql.FakeConnection;

/**
 * Класс с коннекшнами к базе. В данный момент использование не особо целесообразно,
 * но передаётся в событиях чтобы в будущем не менять API.
 */
public class ConnectionSet {
    private static final Logger logger = Logger.getLogger(ConnectionSet.class);

    public static final String KEY = "conSet";
    
    public final static int TYPE_MASTER = 1;
    public final static int TYPE_SLAVE = 2;
    public final static int TYPE_TRASH = 3;
    public final static int TYPE_FAKE = 4;

    protected boolean autoCommit;

    // мастер соединение открыто в этом ConnectionSet и может им же быть закрыто
    private final boolean internalMaster;

    private Connection masterConnection;
    private Connection slaveConnection;

    private Map<String, Connection[]> trashConnections;

    private volatile boolean recycled = false;

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
            logger.error(e.getMessage(), e);
        }

        return false;
    }

    public Connection getConnection() {
        if (masterConnection == null) {
            recycled = false;
            masterConnection = newMasterConnection();
        }

        return masterConnection;
    }

    protected Connection newMasterConnection() {
        Connection result = setup.getDBConnectionFromPool();
        try {
            if (result != null && result.getAutoCommit() != autoCommit) {
                result.setAutoCommit(autoCommit);
            }
        } catch (SQLException ex) {
            logger.error(ex.getMessage(), ex);
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
            logger.error(ex.getMessage(), ex);
        }

        return result;
    }

    public Connection getSlaveConnection() {
        if (slaveConnection == null) {
            recycled = false;
            slaveConnection = newSlaveConnection();

            if (slaveConnection == null) {
                slaveConnection = getConnection();
            }
        }

        return slaveConnection;
    }

    public Connection getTrashConnection(String tableName, int defaultType) {
        if (trashConnections == null) {
            trashConnections = new HashMap<String, Connection[]>();
        }

        Connection[] connections = trashConnections.get(tableName);
        if (connections == null) {
            trashConnections.put(tableName, connections = new Connection[4]);
        }

        Connection connection = connections[0];
        if (connection == null) {
            recycled = false;
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

    public void commit() throws BGException {
        try {
            if (masterConnection != null) {
                if (!masterConnection.getAutoCommit()) {
                    masterConnection.commit();
                }
            }

            if (trashConnections != null) {
                Set<Connection> commited = new HashSet<Connection>();

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
        } catch (SQLException e) {
            throw new BGException(e);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    public void recycle() {
        recycled = true;

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
                            logger.error(e.getMessage(), e);
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
                    logger.error(e.getMessage(), e);
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
                    logger.error(e.getMessage(), e);
                }
            }

            masterConnection = null;
        }
    }

    public boolean getAutoCommit() {
        return this.autoCommit;
    }

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
                            logger.error(e.getMessage(), e);
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
                logger.error(e.getMessage(), e);
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
                            logger.error(e.getMessage(), e);
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
                logger.error(e.getMessage(), e);
            }
        }
    }

    @Override
    protected void finalize() throws Throwable {
        if (!recycled) {
            logger.warn("Not recycled before finalize!");
            recycle();
        }
    }
}