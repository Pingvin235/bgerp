package ru.bgcrm.util.sql;

import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Statement;
import java.sql.Struct;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;

import org.apache.commons.dbcp.DelegatingConnection;

public class PoolGuardConnectionWrapper implements Connection {
    private DelegatingConnection delegate;

    PoolGuardConnectionWrapper(DelegatingConnection delegate) {
        this.delegate = delegate;
    }

    protected void checkOpen() throws SQLException {
        if (delegate == null) {
            throw new SQLException("Connection is closed.");
        }
    }

    public void close() throws SQLException {
        if (delegate != null) {
            this.delegate.close();
            this.delegate = null;
        }
    }

    public boolean isClosed() throws SQLException {
        if (delegate == null) {
            return true;
        }
        return delegate.isClosed();
    }

    public void clearWarnings() throws SQLException {
        checkOpen();
        delegate.clearWarnings();
    }

    public void commit() throws SQLException {
        checkOpen();
        delegate.commit();
    }

    public Statement createStatement() throws SQLException {
        checkOpen();
        return delegate.createStatement();
    }

    public Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException {
        checkOpen();
        return delegate.createStatement(resultSetType, resultSetConcurrency);
    }

    public boolean getAutoCommit() throws SQLException {
        checkOpen();
        return delegate.getAutoCommit();
    }

    public String getCatalog() throws SQLException {
        checkOpen();
        return delegate.getCatalog();
    }

    public DatabaseMetaData getMetaData() throws SQLException {
        checkOpen();
        return delegate.getMetaData();
    }

    public int getTransactionIsolation() throws SQLException {
        checkOpen();
        return delegate.getTransactionIsolation();
    }

    @SuppressWarnings("unchecked")
    public Map<String, Class<?>> getTypeMap() throws SQLException {
        checkOpen();
        return (Map<String, Class<?>>) delegate.getTypeMap();
    }

    public SQLWarning getWarnings() throws SQLException {
        checkOpen();
        return delegate.getWarnings();
    }

    public int hashCode() {
        if (delegate == null) {
            return 0;
        }
        return delegate.hashCode();
    }

    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        // Use superclass accessor to skip access test
        Connection conn = delegate;
        if (conn == null) {
            return false;
        }
        if (obj instanceof DelegatingConnection) {
            DelegatingConnection c = (DelegatingConnection) obj;
            return c.innermostDelegateEquals(conn);
        } else {
            return conn.equals(obj);
        }
    }

    public boolean isReadOnly() throws SQLException {
        checkOpen();
        return delegate.isReadOnly();
    }

    public String nativeSQL(String sql) throws SQLException {
        checkOpen();
        return delegate.nativeSQL(sql);
    }

    public CallableStatement prepareCall(String sql) throws SQLException {
        checkOpen();
        return delegate.prepareCall(sql);
    }

    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
        checkOpen();
        return delegate.prepareCall(sql, resultSetType, resultSetConcurrency);
    }

    public PreparedStatement prepareStatement(String sql) throws SQLException {
        checkOpen();
        return delegate.prepareStatement(sql);
    }

    public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
        checkOpen();
        return delegate.prepareStatement(sql, resultSetType, resultSetConcurrency);
    }

    public void rollback() throws SQLException {
        checkOpen();
        delegate.rollback();
    }

    public void setAutoCommit(boolean autoCommit) throws SQLException {
        checkOpen();
        delegate.setAutoCommit(autoCommit);
    }

    public void setCatalog(String catalog) throws SQLException {
        checkOpen();
        delegate.setCatalog(catalog);
    }

    public void setReadOnly(boolean readOnly) throws SQLException {
        checkOpen();
        delegate.setReadOnly(readOnly);
    }

    public void setTransactionIsolation(int level) throws SQLException {
        checkOpen();
        delegate.setTransactionIsolation(level);
    }

    public String toString() {
        if (delegate == null) {
            return "NULL";
        }
        return delegate.toString();
    }

    public int getHoldability() throws SQLException {
        checkOpen();
        return delegate.getHoldability();
    }

    public void setHoldability(int holdability) throws SQLException {
        checkOpen();
        delegate.setHoldability(holdability);
    }

    public java.sql.Savepoint setSavepoint() throws SQLException {
        checkOpen();
        return delegate.setSavepoint();
    }

    public java.sql.Savepoint setSavepoint(String name) throws SQLException {
        checkOpen();
        return delegate.setSavepoint(name);
    }

    public void releaseSavepoint(java.sql.Savepoint savepoint) throws SQLException {
        checkOpen();
        delegate.releaseSavepoint(savepoint);
    }

    public void rollback(java.sql.Savepoint savepoint) throws SQLException {
        checkOpen();
        delegate.rollback(savepoint);
    }

    public Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
        checkOpen();
        return delegate.createStatement(resultSetType, resultSetConcurrency, resultSetHoldability);
    }

    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
        checkOpen();
        return delegate.prepareCall(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
    }

    public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException {
        checkOpen();
        return delegate.prepareStatement(sql, autoGeneratedKeys);
    }

    public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
        checkOpen();
        return delegate.prepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
    }

    public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException {
        checkOpen();
        return delegate.prepareStatement(sql, columnIndexes);
    }

    public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException {
        checkOpen();
        return delegate.prepareStatement(sql, columnNames);
    }

    public <T> T unwrap(Class<T> iface) throws SQLException {
        checkOpen();
        return delegate.unwrap(iface);
    }

    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return delegate.isWrapperFor(iface);
    }

    public Clob createClob() throws SQLException {
        return delegate.createClob();
    }

    public Blob createBlob() throws SQLException {
        return delegate.createBlob();
    }

    public NClob createNClob() throws SQLException {
        return delegate.createNClob();
    }

    public SQLXML createSQLXML() throws SQLException {
        return delegate.createSQLXML();
    }

    public boolean isValid(int timeout) throws SQLException {
        return delegate.isValid(timeout);
    }

    public void setClientInfo(String name, String value) throws SQLClientInfoException {
        delegate.setClientInfo(name, value);
    }

    public void setClientInfo(Properties properties) throws SQLClientInfoException {
        delegate.setClientInfo(properties);
    }

    public String getClientInfo(String name) throws SQLException {
        return delegate.getClientInfo(name);
    }

    public Properties getClientInfo() throws SQLException {
        return delegate.getClientInfo();
    }

    public Array createArrayOf(String typeName, Object[] elements) throws SQLException {
        return delegate.createArrayOf(typeName, elements);
    }

    public Struct createStruct(String typeName, Object[] attributes) throws SQLException {
        return delegate.createStruct(typeName, attributes);
    }

    @Override
    public void setTypeMap(Map<String, Class<?>> map) throws SQLException {
        checkOpen();
        delegate.setTypeMap(map);
    }

    @Override
    public void setSchema(String schema) throws SQLException {
        // TODO Auto-generated method stub

    }

    @Override
    public String getSchema() throws SQLException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void abort(Executor executor) throws SQLException {
        // TODO Auto-generated method stub

    }

    @Override
    public void setNetworkTimeout(Executor executor, int milliseconds) throws SQLException {
        // TODO Auto-generated method stub

    }

    @Override
    public int getNetworkTimeout() throws SQLException {
        // TODO Auto-generated method stub
        return 0;
    }
}
