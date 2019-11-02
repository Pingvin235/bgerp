package ru.bgcrm.dao;

import static ru.bgcrm.dao.Tables.TABLE_WEB_REQUEST_LOG;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import ru.bgcrm.model.BGException;
import ru.bgcrm.model.BGMessageException;
import ru.bgcrm.model.LogEntry;
import ru.bgcrm.model.Page;
import ru.bgcrm.model.SearchResult;
import ru.bgcrm.model.user.User;
import ru.bgcrm.servlet.AccessLogValve;
import ru.bgcrm.servlet.filter.AuthFilter;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.Setup;
import ru.bgcrm.util.TimeUtils;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.sql.SQLUtils;

public class WebRequestLogDAO extends PeriodicDAO {
	static {
		tableNamePrefix = TABLE_WEB_REQUEST_LOG;

		createQuery = " CREATE TABLE " + TABLE_WEB_REQUEST_LOG + " (\n" + "	`id` INT(10) NOT NULL AUTO_INCREMENT,\n"
				+ "	`uid` INT(10) NOT NULL,\n" + "	`time` DATETIME NOT NULL,\n"
				+ "	`ipAddress` VARCHAR(15) NOT NULL ,\n" + "	`action` VARCHAR(200) NOT NULL ,\n"
				+ "	`parameters` TEXT NOT NULL ,\n" + "	`duration` BIGINT NOT NULL ,\n"
				+ " `connection_id` INT(10) NULL DEFAULT NULL, \n" + " `result_status` TEXT NULL, \n"
				+ "	PRIMARY KEY (`id`),\n" + "	INDEX `uid` (`uid`),\n" + "	INDEX `time` (`time`)\n" + " )";
	}

	public WebRequestLogDAO(Connection con) {
		super(con);
	}

	public int insertLogEntry(HttpServletRequest request, String action)
			throws BGException, UnsupportedEncodingException {
		if (!Boolean.parseBoolean(Setup.getSetup().get("isDbReadOnly"))) {
			checkAndCreatePeriodicTable();

			try {
				String headerNameRemoteAddress = Setup.getSetup().get(AccessLogValve.PARAM_HEADER_NAME_REMOTE_ADDR);

				String queryString = "";

				for (Map.Entry<String, String[]> entry : request.getParameterMap().entrySet()) {

					String[] entryValue = entry.getValue();

					for (String val : entryValue) {
						queryString += entry.getKey() + "=" + val + "\n";
					}
				}

				User user = AuthFilter.getUser(request);

				LogEntry entry = new LogEntry();
				
				if (headerNameRemoteAddress != null)
					entry.setIpAddress(request.getHeader(headerNameRemoteAddress));
				if (entry.getIpAddress() == null)
					entry.setIpAddress(request.getRemoteAddr());
				
				entry.setAction(action);
				if (Utils.notBlankString(queryString)) {
					entry.setParameters(queryString);
				}
				entry.setUid(user.getId());

				String query;

				if (SQLUtils.columnExist(con, getMonthTableName(TABLE_WEB_REQUEST_LOG, new Date()), "duration")) {

					if (SQLUtils.columnExist(con, getMonthTableName(TABLE_WEB_REQUEST_LOG, new Date()),
							"connection_id")) {
						query = "INSERT INTO " + getMonthTableName(TABLE_WEB_REQUEST_LOG, new Date())
								+ "(uid, time, ipAddress, action, parameters, duration, connection_id) "
								+ "VALUES (?,NOW(),?,?,?,-1,?)";
					} else {

						query = "INSERT INTO " + getMonthTableName(TABLE_WEB_REQUEST_LOG, new Date())
								+ "(uid, time, ipAddress, action, parameters, duration) " + "VALUES (?,NOW(),?,?,?,-1)";
					}
				} else {
					query = "INSERT INTO " + getMonthTableName(TABLE_WEB_REQUEST_LOG, new Date())
							+ "(uid, time, ipAddress, action, parameters) " + "VALUES (?,NOW(),?,?,?)";
				}

				PreparedStatement ps = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
				ps.setInt(1, entry.getUid());
				ps.setString(2, entry.getIpAddress());
				ps.setString(3, entry.getAction());

				try {
					ps.setString(4, URLDecoder.decode(entry.getParameters(), Utils.UTF8.name()));
				} catch (Exception e) {
					ps.setString(4, entry.getParameters());
				}

				if (SQLUtils.columnExist(con, getMonthTableName(TABLE_WEB_REQUEST_LOG, new Date()), "connection_id")) {
					ps.setInt(5, SQLUtils.getConnectionId(con));
				}

				ps.executeUpdate();

				int logEntryId = lastInsertId(ps);

				ps.close();

				return logEntryId;
			} catch (SQLException ex) {
				throw new BGException(ex);
			}
		}

		return -1;
	}

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

			query.append(getMySQLLimit(page));

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
	}
}
