package org.bgerp.app.db.sql.pool;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.NoSuchElementException;

import org.apache.commons.dbcp2.DelegatingConnection;
import org.apache.commons.dbcp2.PoolableConnection;
import org.apache.commons.dbcp2.PoolingDataSource;
import org.apache.commons.pool2.impl.GenericObjectPool;

final class GuardSupportedPool {
    final GenericObjectPool<PoolableConnection> pool;
    final PoolingDataSource<PoolableConnection> dataSource;

    public GuardSupportedPool(GenericObjectPool<PoolableConnection> pool) {
        this.pool = pool;
        this.dataSource = new PoolingDataSource<>(pool) {
            @Override
            public Connection getConnection() throws SQLException {
                try {
                    Connection conn = (Connection) pool.borrowObject();
                    if (conn != null) {
                        conn = new PoolGuardConnectionWrapper((DelegatingConnection) conn);
                    }
                    return conn;
                } catch (SQLException e) {
                    throw e;
                } catch (NoSuchElementException e) {
                    throw new SQLException("Cannot get a connection, pool error " + e.getMessage(), e);
                } catch (RuntimeException e) {
                    throw e;
                } catch (Exception e) {
                    throw new SQLException("Cannot get a connection, general error", e);
                }
            }
        };
    }

    /**
     * @return getMaxActive() <= getNumActive()
     */
    public boolean isOverload() {
        return pool.getMaxTotal() <= pool.getNumActive();
    }

    /**
     * @return pool.getNumActive() / pool.getMaxActive();
     */
    public float getLoadRatio() {
        return (float) pool.getNumActive() / pool.getMaxTotal();
    }
}