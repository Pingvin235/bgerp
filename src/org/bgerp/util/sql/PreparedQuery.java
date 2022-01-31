package org.bgerp.util.sql;

import java.io.Closeable;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.bgerp.util.Log;
import org.bgerp.util.TimeConvert;

import ru.bgcrm.util.TimeUtils;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.sql.SQLUtils;

/**
 * Dynamically builds of {@link PreparedStatement}.
 * Allows adding params without definition of position number.
 *
 * @author Shamil Vakhitov
 */
public class PreparedQuery implements Closeable {
    private static final Log log = Log.getLog();

    private Connection con;
    private StringBuilder query;

    private List<Object> parameters = new ArrayList<>(20);
    private PreparedStatement ps;

    public PreparedQuery(Connection con) {
        this.con = con;
    }

    public PreparedQuery(Connection con, String query) {
        this.con = con;
        addQuery(query);
    }

    /**
     * @return {@link PreparedStatement} or {@code null} if not created.
     */
    public PreparedStatement getPrepared() {
        return ps;
    }

    /**
     * @return current query.
     */
    @Deprecated
    public StringBuilder getQuery() {
        return query;
    }

    /**
     * Adds SQL string to the query.
     * @param value
     * @return the current object.
     */
    public PreparedQuery addQuery(String value) {
        if (Utils.isBlankString(value)) {
            return this;
        }

        if (query == null) {
            query = new StringBuilder();
        }
        query.append(value);

        return this;
    }

    /**
     * Replaces the current query.
     * @param value
     */
    public void setQuery(String value) {
        query.setLength(0);
        query.append(value);
    }

    /**
     * Changes position if the last parameter.
     * Adding new parameters will be continued after this.
     * Closes existing {@link PreparedStatement} if exists and assigns it to {@code null}.
     * @param pos 1 based position.
     * @throws SQLException
     */
    public void setPos(int pos) throws SQLException {
        parameters = parameters.subList(0, pos);
        if (ps != null)
            ps.close();
        ps = null;
    }

    /**
     * Adds int parameter in the prepared statement.
     * @param value
     * @return the current instance.
     */
    public PreparedQuery addInt(int value) {
        parameters.add(value);
        return this;
    }

    /** Adds long parameter in the prepared statement
     * @param value
     * @return the current instance.
     */
    public PreparedQuery addLong(long value) {
        parameters.add(value);
        return this;
    }

    /** Adds Decimal parameter in the prepared statement.
     * @param value
     * @return the current instance.
     */
    public PreparedQuery addBigDecimal(BigDecimal value) {
        parameters.add(value);
        return this;
    }

    /**
     * Adds int parameter in the prepared statement.
     * @param value
     * @return the current instance.
     */
    public PreparedQuery addString(String value) {
        parameters.add(value);
        return this;
    }

    /**
     * Adds Date parameter in the prepared statement.
     * @param value
     * @return the current instance.
     */
    public PreparedQuery addDate(Date value) {
        parameters.add(TimeUtils.convertDateToSqlDate(value));
        return this;
    }

    /**
     * Adds Timestamp parameter in the prepared statement.
     * @param value
     * @return the current instance.
     */
    public PreparedQuery addTimestamp(Timestamp value) {
        parameters.add(value);
        return this;
    }

    /**
     * Adds Timestamp parameter in the prepared statement.
     * @param value
     * @return the current instance.
     */
    public PreparedQuery addTimestamp(Date value) {
        parameters.add(TimeConvert.toTimestamp(value));
        return this;
    }

    /**
     * Adds Boolean parameter in the prepared statement.
     * @param value
     * @return the current instance.
     */
    public PreparedQuery addBoolean(Boolean value) {
        parameters.add(value);
        return this;
    }

    /**
     * Adds parameters with arbitrary types in the prepared statement.
     * @param values
     * @return the current instance.
     */
    public PreparedQuery addObjects(Object... values) {
        for (Object value : values)
            parameters.add(value);
        return this;
    }

    /**
     * Executes the prepared statement for select.
     * @return
     * @throws SQLException
     */
    public ResultSet executeQuery() throws SQLException {
        prepareStatementAndSetParameters();
        return ps.executeQuery();
    }

    /**
     * Executes the prepared statement for update.
     * @return
     * @throws SQLException
     */
    public int executeUpdate() throws SQLException {
        prepareStatementAndSetParameters();
        return ps.executeUpdate();
    }

    /**
     * Executes the prepared insert statement.
     * @return generated ID.
     * @throws SQLException
     */
    public int executeInsert() throws SQLException {
        if (ps == null) {
            ps = con.prepareStatement(query.toString(), PreparedStatement.RETURN_GENERATED_KEYS);
        }
        ps.executeUpdate();
        return SQLUtils.lastInsertId(ps);
    }

    private void prepareStatementAndSetParameters() throws SQLException {
        if (ps == null) {
            ps = con.prepareStatement(query.toString());
        }
        setParameters();
    }

    private void setParameters() throws SQLException {
        final int size = parameters.size();
        for (int i = 0; i < size; i++) {
            ps.setObject(i + 1, parameters.get(i));
        }
    }

    @Override
    public void close() {
        try {
            if (ps != null)
                ps.close();
            ps = null;
            parameters.clear();
        } catch (Exception e) {
            log.error(e);
        }
    }

    @Override
    public String toString() {
        return Log.format("Prepared query: {}, params: {}", query, parameters);
    }
}