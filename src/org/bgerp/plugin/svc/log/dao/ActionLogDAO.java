package org.bgerp.plugin.svc.log.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.bgerp.plugin.svc.log.model.ActionLogEntry;
import org.bgerp.util.sql.PreparedQuery;

import ru.bgcrm.dao.PeriodicDAO;
import ru.bgcrm.model.Page;
import ru.bgcrm.model.SearchResult;
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

        String query = SQL_INSERT + table
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

    public ActionLogDAO withUserIds(Set<Integer> value) {
        this.userIds = value;
        return this;
    }

    public ActionLogDAO withIpAddress(String value) {
        this.ipAddress = value;
        return this;
    }

    public ActionLogDAO withActions(Set<String> value) {
        this.actions = value;
        return this;
    }

    public ActionLogDAO withParameter(String value) {
        this.parameter = value;
        return this;
    }

    public void search(SearchResult<ActionLogEntry> result) throws SQLException {
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
                pq.addQuery(" AND parameters LIKE '"+ getLikePatternSub(parameter) +"'");
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

            page.setRecordCount(getFoundRows(pq.getPrepared()));
        }
    }

    /*
    public void serchRequest(SearchResult<LogEntry> searchResult, DynActionForm form)
            throws SQLException, BGMessageException, UnsupportedEncodingException {
        if (searchResult != null) {
            Page page = searchResult.getPage();

            List<LogEntry> list = searchResult.getList();

            ResultSet rs;
            PreparedStatement ps;
            StringBuilder query = new StringBuilder();

            Date dateFrom = TimeUtils.parse(form.getParam("dateFrom"), TimeUtils.FORMAT_TYPE_YMDHM);
            Date dateTo = TimeUtils.parse(form.getParam("dateTo"), TimeUtils.FORMAT_TYPE_YMDHM);

            if (dateFrom == null || dateTo == null) {
                throw new BGMessageException("Не заданы даты выборки");
            }

            query.append(" SELECT DISTINCT SQL_CALC_FOUND_ROWS ");
            query.append(" * FROM ").append(getMonthTableName(TABLE_WEB_REQUEST_LOG, dateFrom)).append(" AS entry");
            query.append(" WHERE 1=1 ");

            String dateFromValue = form.getParam("dateFrom");
            if (Utils.notBlankString(dateFromValue)) {
                query.append(" AND entry.time >= ").append(TimeUtils.formatSqlDatetime(dateFrom));
            }

            String dateToValue = form.getParam("dateTo");
            if (Utils.notBlankString(dateToValue)) {
                query.append(" AND entry.time <= ").append(TimeUtils.formatSqlDatetime(dateTo));
            }

            String ipAddress = form.getParam("ipAddress");
            if (Utils.notBlankString(ipAddress)) {
                query.append(" AND entry.ipAddress = '").append(ipAddress).append("' ");
            }

            String users = Utils.toString(form.getSelectedValuesStr("user"));
            if (Utils.notBlankString(users)) {
                query.append(" AND find_in_set( entry.uid, '").append(users.replace(" ", "")).append("') > 0");
            }

            String actionTitles = Utils.toString(form.getSelectedValuesStr("actionTitle"));
            if (Utils.notBlankString(actionTitles)) {
                query.append(" AND find_in_set( entry.action, '").append(actionTitles.replace(" ", ""))
                        .append("') > 0");
            }

            String parameter = form.getParam("parameter");
            if (Utils.notBlankString(parameter)) {
                query.append(" AND entry.parameters LIKE '").append(getLikePattern(parameter, "subs")).append("'");
            }

            String orders = Utils.toString(form.getSelectedValuesListStr("sort", "0"));
            if (Utils.notBlankString(orders)) {
                query.append(SQL_ORDER_BY);
                query.append(orders);
            }

            query.append(getPageLimit(page));

            ps = con.prepareStatement(query.toString());

            rs = ps.executeQuery();

            String prefix = "entry.";

            while (rs.next()) {
                LogEntry entry = new LogEntry();

                entry.setAction(rs.getString(prefix + "action"));
                entry.setTime(rs.getTimestamp(prefix + "time"));
                entry.setIpAddress(rs.getString(prefix + "ipAddress"));
                try {
                    entry.setParameters(URLDecoder.decode(rs.getString(prefix + "parameters"), Utils.UTF8.name()));
                } catch (Exception e) {
                    entry.setParameters(rs.getString(prefix + "parameters"));
                }

                entry.setUid(rs.getInt(prefix + "uid"));
                entry.setId(rs.getInt(prefix + "id"));

                if (SQLUtils.columnExist(con, getMonthTableName(TABLE_WEB_REQUEST_LOG, dateFrom), "duration")) {
                    entry.setDuration(rs.getLong(prefix + "duration"));
                }

                list.add(entry);
            }

            if (page != null) {
                page.setRecordCount(getFoundRows(ps));
            }
            ps.close();
        }
    }

    public void updateLogEntryDuration(int logEntryId, long duration) throws BGMessageException {
        if (SQLUtils.columnExist(con, getMonthTableName(TABLE_WEB_REQUEST_LOG, new Date()), "duration")) {
            try {
                String query = " UPDATE " + getMonthTableName(TABLE_WEB_REQUEST_LOG, new Date()) + " SET duration=? "
                        + " WHERE id=?";

                PreparedStatement ps = con.prepareStatement(query);
                ps.setLong(1, duration);
                ps.setInt(2, logEntryId);

                ps.executeUpdate();
                ps.close();
            } catch (SQLException e) {
                throw new BGMessageException(e.getMessage());
            }
        }
    }

    public void updateLogEntryResultStatus(int logEntryId, String resultStatus) throws BGMessageException {
        if (SQLUtils.columnExist(con, getMonthTableName(TABLE_WEB_REQUEST_LOG, new Date()), "result_status")) {
            try {
                String query = " UPDATE " + getMonthTableName(TABLE_WEB_REQUEST_LOG, new Date())
                        + " SET result_status=? " + " WHERE id=?";

                PreparedStatement ps = con.prepareStatement(query);

                ps.setString(1, resultStatus);
                ps.setInt(2, logEntryId);

                ps.executeUpdate();
                ps.close();
            } catch (SQLException e) {
                throw new BGMessageException(e.getMessage());
            }
        }
    } */
}
