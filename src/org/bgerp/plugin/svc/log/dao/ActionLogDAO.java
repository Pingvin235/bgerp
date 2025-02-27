package org.bgerp.plugin.svc.log.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.bgerp.model.Pageable;
import org.bgerp.plugin.svc.log.model.ActionLogEntry;
import org.bgerp.util.sql.LikePattern;
import org.bgerp.util.sql.PreparedQuery;

import ru.bgcrm.dao.PeriodicDAO;
import ru.bgcrm.model.Page;
import ru.bgcrm.util.Utils;

/**
 * DAO for adding action log records and fluent search.
 *
 * @author Shamil Vakhitov
 */
public class ActionLogDAO extends PeriodicDAO {
    private final static String TABLE_NAME_PREFIX = Tables.TABLE_ACTION_LOG_PREFIX;

    private Date timeFrom;
    private Date timeTo;
    private Set<Integer> userIds;
    private String ipAddress;
    private Set<String> actions;
    private String parameter;

    public ActionLogDAO(Connection con) {
        super(con);
    }

    public void update(ActionLogEntry entry) throws SQLException {
        String table = checkAndCreateMonthTable(TABLE_NAME_PREFIX, new Date(),
            "(`id` INT NOT NULL AUTO_INCREMENT, " +
            "`user_id` INT NOT NULL, " +
            "`ip_address` VARCHAR(15) NOT NULL , " +
            "`action` VARCHAR(200) NOT NULL , " +
            "`parameters` TEXT NOT NULL , " +
            "`time` DATETIME NOT NULL, " +
            "`duration` BIGINT NOT NULL , " +
            "`error` TEXT NOT NULL, " +
            "PRIMARY KEY (`id`), " +
            "KEY `user_id` (`user_id`), " +
            "KEY `time` (`time`))");

        String query = SQL_INSERT_INTO + table
                + "(time, user_id, ip_address, action, parameters, duration, error) "
                + "VALUES (NOW(), ?, ?, ?, ?, ?, ?)";;
        var pq = new PreparedQuery(con, query);
        pq
            .addInt(entry.getUserId())
            .addString(entry.getIpAddress())
            .addString(entry.getAction())
            .addString(entry.getParameters())
            .addLong(entry.getDuration())
            .addString(entry.getError());
        pq.executeInsert();
        pq.close();
    }

    public ActionLogDAO withTimeFrom(Date value) {
        this.timeFrom = value;
        return this;
    }

    public ActionLogDAO withTimeTo(Date value) {
        this.timeTo = value;
        return this;
    }

    /**
     * Sets user IDs filter.
     * @param value user IDs.
     * @return
     */
    public ActionLogDAO withUserIds(Set<Integer> value) {
        this.userIds = value;
        return this;
    }

    /**
     * Sets IP address filter.
     * @param value user IP.
     * @return
     */
    public ActionLogDAO withIpAddress(String value) {
        this.ipAddress = value;
        return this;
    }

    /**
     * Sets actions filter.
     * @param value semicolon separated strings with action class and method names.
     * @return
     */
    public ActionLogDAO withActions(Set<String> value) {
        this.actions = value;
        return this;
    }

    public ActionLogDAO withParameter(String value) {
        this.parameter = value;
        return this;
    }

    public void search(Pageable<ActionLogEntry> result) throws SQLException {
        if (timeFrom == null)
            throw new IllegalArgumentException("timeFrom can't be null");

        String table = getMonthTableName(TABLE_NAME_PREFIX, timeFrom);
        if (!tableExists(table))
            return;

        final var list = result.getList();
        final Page page = result.getPage();

        try (var pq = new PreparedQuery(con)) {
            pq
                .addQuery("SELECT DISTINCT SQL_CALC_FOUND_ROWS * FROM ")
                .addQuery(table)
                .addQuery(" WHERE ?<=time ")
                .addDate(timeFrom);

            if (timeTo != null) {
                pq.addQuery(" AND time<? ").addDate(timeTo);
            }

            if (Utils.notBlankString(ipAddress)) {
                pq.addQuery(" AND ip_address=? ").addString(ipAddress);
            }

            if (CollectionUtils.isNotEmpty(userIds)) {
                pq.addQuery(" AND user_id IN (").addQuery(Utils.toString(userIds)).addQuery(") ");
            }

            if (CollectionUtils.isNotEmpty(actions)) {
                pq.addQuery(" AND action IN ('").addQuery(Utils.toString(actions, "", "','")).addQuery("') ");
            }

            if (Utils.notBlankString(parameter)) {
                pq.addQuery(" AND parameters LIKE '"+ LikePattern.SUB.get(parameter) +"'");
            }

            pq.addQuery(SQL_ORDER_BY).addQuery("time");

            pq.addQuery(getPageLimit(page));

            var rs = pq.executeQuery();
            while (rs.next()) {
                var entry = new ActionLogEntry();
                entry.setId(rs.getInt("id"));
                entry.setUserId(rs.getInt("user_id"));
                entry.setAction(rs.getString("action"));
                entry.setParameters(rs.getString("parameters"));
                entry.setTime(rs.getTimestamp("time"));
                entry.setIpAddress(rs.getString("ip_address"));
                entry.setDuration(rs.getLong("duration"));
                entry.setError(rs.getString("error"));

                list.add(entry);
            }

            page.setRecordCount(pq.getPrepared());
        }
    }
}
