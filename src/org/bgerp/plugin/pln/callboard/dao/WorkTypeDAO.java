package org.bgerp.plugin.pln.callboard.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bgerp.app.cfg.Preferences;
import org.bgerp.app.exception.BGException;
import org.bgerp.model.Pageable;
import org.bgerp.plugin.pln.callboard.cache.CallboardCache;
import org.bgerp.plugin.pln.callboard.model.WorkType;

import ru.bgcrm.dao.CommonDAO;
import ru.bgcrm.model.Page;
import ru.bgcrm.util.TimeUtils;
import ru.bgcrm.util.Utils;

public class WorkTypeDAO extends CommonDAO {
    public WorkTypeDAO(Connection con) {
        super(con);
    }

    public void searchWorkType(Pageable<WorkType> searchResult, int category) {
        if (searchResult != null) {
            try {
                Page page = searchResult.getPage();
                List<WorkType> list = searchResult.getList();

                ResultSet rs = null;
                PreparedStatement ps = null;
                StringBuilder query = new StringBuilder();
                query.append(SQL_SELECT_COUNT_ROWS);
                query.append("*");
                query.append(SQL_FROM);
                query.append(org.bgerp.plugin.pln.callboard.dao.Tables.TABLE_WORK_TYPE);
                query.append(" t ");
                query.append(SQL_WHERE);
                query.append(" category=" + category + " ");
                query.append(SQL_ORDER_BY);
                query.append("t.id");
                query.append(getPageLimit(page));
                ps = con.prepareStatement(query.toString());
                rs = ps.executeQuery();

                while (rs.next()) {
                    list.add(getWorkTypeFromRs(rs));
                }

                page.setRecordCount(foundRows(ps));
                ps.close();
            } catch (SQLException e) {
                throw new BGException(e);
            }
        }
    }

    public List<WorkType> getWorkTypeList() {
        return getWorkTypeList(null);
    }

    public List<WorkType> getWorkTypeList(Set<?> workTypeIds) {
        List<WorkType> result = new ArrayList<>();

        try {
            ResultSet rs = null;
            PreparedStatement ps = null;
            StringBuilder query = new StringBuilder();
            query.append(SQL_SELECT);
            query.append("*");
            query.append(SQL_FROM);
            query.append(org.bgerp.plugin.pln.callboard.dao.Tables.TABLE_WORK_TYPE);
            query.append(" t ");
            // query.append( " t LEFT JOIN " + Tables.TABLE_WORK_TYPE_DYNAMIC + " d ON
            // t.id=d.id " );
            if (workTypeIds != null && workTypeIds.size() > 0) {
                query.append(" WHERE id IN ( " + Utils.toString(workTypeIds, "", ",") + " ) ");
            }

            query.append(SQL_ORDER_BY);
            query.append("t.id");
            ps = con.prepareStatement(query.toString());
            rs = ps.executeQuery();

            while (rs.next()) {
                result.add(getWorkTypeFromRs(rs));
            }
            ps.close();
        } catch (SQLException e) {
            throw new BGException(e);
        }

        return result;
    }

    /**
     * По-возможности использовать {@link CallboardCache#getWorkTypeMap()}
     *
     * @return
     */
    public Map<Integer, WorkType> getWorkTypeMap() {
        return getWorkTypeMap(false);
    }

    public Map<Integer, WorkType> getWorkTypeMap(boolean dynamicOnly) {
        Map<Integer, WorkType> result = new HashMap<>();

        try {
            ResultSet rs = null;
            PreparedStatement ps = null;
            StringBuilder query = new StringBuilder();
            query.append(SQL_SELECT);
            query.append("*");
            query.append(SQL_FROM);
            query.append(org.bgerp.plugin.pln.callboard.dao.Tables.TABLE_WORK_TYPE);
            query.append(" t ");
            // query.append( " t LEFT JOIN " + Tables.TABLE_WORK_TYPE_DYNAMIC + " d ON
            // t.id=d.id " );

            if (dynamicOnly) {
                query.append(" WHERE d.id IS NOT NULL ");
            }

            query.append(SQL_ORDER_BY);
            query.append("t.id");
            ps = con.prepareStatement(query.toString());
            rs = ps.executeQuery();

            while (rs.next()) {
                WorkType workType = getWorkTypeFromRs(rs);
                result.put(workType.getId(), workType);
            }
            ps.close();
        } catch (SQLException e) {
            throw new BGException(e);
        }

        return result;
    }

    public void updateWorkType(WorkType workType) {
        int index = 1;
        PreparedStatement ps = null;

        try {
            if (workType.getId() > 0) {
                String query = " UPDATE " + org.bgerp.plugin.pln.callboard.dao.Tables.TABLE_WORK_TYPE
                        + " SET category=?, title=?, comment=?, config=?, non_work_hours=?, rule_config=? WHERE id=? ";
                ps = con.prepareStatement(query);
                ps.setInt(index++, workType.getCategory());
                ps.setString(index++, workType.getTitle());
                ps.setString(index++, workType.getComment());
                ps.setString(index++, workType.getConfig());
                ps.setBoolean(index++, workType.isNonWorkHours());
                ps.setString(index++, workType.getRuleConfig());
                ps.setInt(index++, workType.getId());
                ps.executeUpdate();
            } else {
                String query = " INSERT INTO " + org.bgerp.plugin.pln.callboard.dao.Tables.TABLE_WORK_TYPE
                        + " SET category=?, title=?, comment=?, config=?, non_work_hours=?, rule_config=? ";
                ps = con.prepareStatement(query.toString(), PreparedStatement.RETURN_GENERATED_KEYS);
                ps.setInt(index++, workType.getCategory());
                ps.setString(index++, workType.getTitle());
                ps.setString(index++, workType.getComment());
                ps.setString(index++, workType.getConfig());
                ps.setBoolean(index++, workType.isNonWorkHours());
                ps.setString(index++, workType.getRuleConfig());
                ps.executeUpdate();
                workType.setId(lastInsertId(ps));
            }

            ps.close();
        } catch (SQLException e) {
            throw new BGException(e);
        }
    }

    public WorkType getWorkType(int id) {
        WorkType result = null;

        try {
            String query = "SELECT * FROM " + org.bgerp.plugin.pln.callboard.dao.Tables.TABLE_WORK_TYPE + " t " +
                    "WHERE t.id=?";
            PreparedStatement ps = con.prepareStatement(query);
            ps.setInt(1, id);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                result = getWorkTypeFromRs(rs);
            }
            ps.close();
        } catch (SQLException e) {
            throw new BGException(e);
        }

        return result;
    }

    public void deleteWorkType(int id) {
        try {
            PreparedStatement ps = null;

            StringBuilder query = new StringBuilder();
            query.append(SQL_DELETE_FROM);
            query.append(org.bgerp.plugin.pln.callboard.dao.Tables.TABLE_WORK_TYPE);
            query.append(SQL_WHERE);
            query.append("id=?");

            ps = con.prepareStatement(query.toString());
            ps.setInt(1, id);
            ps.executeUpdate();

            ps.close();
        } catch (SQLException e) {
            throw new BGException(e);
        }
    }

    public void updateWorkDaysCalendar(int calendarId, int type, Date date) {
        PreparedStatement ps = null;

        try {
            ps = con.prepareStatement("DELETE FROM " + org.bgerp.plugin.pln.callboard.dao.Tables.TABLE_WORKDAYS_CALENDAR
                    + " WHERE id=? AND date=?");
            ps.setInt(1, calendarId);
            ps.setDate(2, TimeUtils.convertDateToSqlDate(date));

            ps.executeUpdate();
            ps.close();

            if (type > 0) {
                ps = con.prepareStatement(
                        "INSERT INTO " + org.bgerp.plugin.pln.callboard.dao.Tables.TABLE_WORKDAYS_CALENDAR
                                + " SET id=?, date=?, type=?");
                ps.setInt(1, calendarId);
                ps.setDate(2, TimeUtils.convertDateToSqlDate(date));
                ps.setInt(3, type);

                ps.executeUpdate();
                ps.close();
            }
        } catch (SQLException e) {
            throw new BGException(e);
        }
    }

    public void copyWorkDaysCalendar(int calendarId, int from, int to) {
        PreparedStatement ps = null;

        try {
            ps = con.prepareStatement("DELETE FROM " + org.bgerp.plugin.pln.callboard.dao.Tables.TABLE_WORKDAYS_CALENDAR
                    + " WHERE id=" + calendarId + " AND date BETWEEN '" + to + "-01-01' AND '" + to + "-12-31'; ");
            ps.executeUpdate();
            ps.close();

            ps = con.prepareStatement("INSERT INTO " + org.bgerp.plugin.pln.callboard.dao.Tables.TABLE_WORKDAYS_CALENDAR
                    + " " + "SELECT id, DATE_SUB( date, INTERVAL (YEAR(date)-" + to + ") YEAR ), type from "
                    + org.bgerp.plugin.pln.callboard.dao.Tables.TABLE_WORKDAYS_CALENDAR + " " + "WHERE id=" + calendarId
                    + " and YEAR(date)=" + from);
            ps.executeUpdate();
            ps.close();
        } catch (SQLException e) {
            throw new BGException(e);
        }
    }

    // TODO: Добавить период выборки.
    public Map<Date, Integer> getWorkDaysCalendarExcludes(int calendarId) {
        Map<Date, Integer> resultSet = new HashMap<>();

        try {
            ResultSet rs = null;
            PreparedStatement ps = null;
            ps = con.prepareStatement("SELECT date, type FROM "
                    + org.bgerp.plugin.pln.callboard.dao.Tables.TABLE_WORKDAYS_CALENDAR + " WHERE id=?");
            ps.setInt(1, calendarId);
            rs = ps.executeQuery();

            while (rs.next()) {
                resultSet.put(rs.getDate(1), rs.getInt(2));
            }

            ps.close();
        } catch (SQLException e) {
            throw new BGException(e);
        }

        return resultSet;
    }

    private static WorkType getWorkTypeFromRs(ResultSet rs) {
        WorkType result = new WorkType();

        try {
            result.setId(rs.getInt("id"));
            result.setTitle(rs.getString("title"));
            result.setCategory(rs.getInt("category"));
            result.setComment(rs.getString("comment"));
            result.setConfigMap(new Preferences(rs.getString("config")));
            result.setNonWorkHours(rs.getBoolean("non_work_hours"));
            result.setRuleConfig(rs.getString("rule_config"));
        } catch (SQLException e) {
            throw new BGException(e);
        }

        return result;
    }
}
