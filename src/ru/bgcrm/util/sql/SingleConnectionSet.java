package ru.bgcrm.util.sql;

import java.sql.Connection;

/**
 * Implementation of {@link ConnectionSet} returning always the same master connection.
 * Eliminates advantages of real connection set, can be used as quick adapter.
 *
 * @author Shamil Vakhitov
 */
public class SingleConnectionSet extends ConnectionSet {
    public SingleConnectionSet(Connection master) {
        super(master);
    }

    @Override
    public Connection getSlaveConnection() {
        return getConnection();
    }

    @Override
    public Connection getTrashConnection(String tableName, int defaultType) {
        return getConnection();
    }

    @Override
    protected void finalize() throws Throwable {}
}