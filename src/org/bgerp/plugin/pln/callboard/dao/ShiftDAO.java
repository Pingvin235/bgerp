package org.bgerp.plugin.pln.callboard.dao;

import static org.bgerp.plugin.pln.callboard.dao.Tables.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bgerp.app.exception.BGException;
import org.bgerp.cache.UserCache;
import org.bgerp.model.Pageable;
import org.bgerp.plugin.pln.callboard.model.CallboardTask;
import org.bgerp.plugin.pln.callboard.model.Shift;
import org.bgerp.plugin.pln.callboard.model.WorkShift;
import org.bgerp.plugin.pln.callboard.model.WorkTypeTime;
import org.bgerp.plugin.pln.callboard.model.config.CallboardConfig.Callboard;
import org.bgerp.util.TimeConvert;

import java.util.Set;

import ru.bgcrm.dao.CommonDAO;
import ru.bgcrm.model.Page;
import ru.bgcrm.model.user.UserGroup;
import ru.bgcrm.util.TimeUtils;
import ru.bgcrm.util.Utils;

public class ShiftDAO extends CommonDAO {
    private static Comparator<WorkTypeTime> workShiftComparator = new Comparator<>() {
        @Override
        public int compare(WorkTypeTime w1, WorkTypeTime w2) {
            return w1.getWorkTypeId() - w2.getWorkTypeId();
        }
    };

    public ShiftDAO(Connection con) {
        super(con);
    }

    public void searchShift(Pageable<Shift> searchResult, int category) {
        if (searchResult != null) {
            try {
                Page page = searchResult.getPage();
                List<Shift> list = searchResult.getList();

                ResultSet rs = null;
                PreparedStatement ps = null;
                StringBuilder query = new StringBuilder();
                query.append(SQL_SELECT_COUNT_ROWS);
                query.append("*");
                query.append(SQL_FROM);
                query.append(TABLE_SHIFT);
                query.append(SQL_WHERE);
                query.append(" category=" + category + " ");
                query.append(SQL_ORDER_BY);
                query.append("id");
                query.append(getPageLimit(page));
                ps = con.prepareStatement(query.toString());
                rs = ps.executeQuery();

                while (rs.next()) {
                    list.add(getShiftFromRs(rs));
                }

                page.setRecordCount(foundRows(ps));
                ps.close();
            } catch (SQLException e) {
                throw new BGException(e);
            }
        }
    }

    public List<Shift> getShiftList(int category) {
        List<Shift> list = new ArrayList<>();

        try {
            ResultSet rs = null;
            PreparedStatement ps = null;
            StringBuilder query = new StringBuilder();
            query.append(SQL_SELECT_COUNT_ROWS);
            query.append("*");
            query.append(SQL_FROM);
            query.append(TABLE_SHIFT);
            query.append(SQL_WHERE);
            query.append(" category=" + category + " ");
            query.append(SQL_ORDER_BY);
            query.append("id");
            ps = con.prepareStatement(query.toString());
            rs = ps.executeQuery();

            while (rs.next()) {
                list.add(getShiftFromRs(rs));
            }

            ps.close();
        } catch (SQLException e) {
            throw new BGException(e);
        }

        return list;
    }

    public List<Shift> getShiftList(Set<Integer> shiftIds) {
        List<Shift> list = new ArrayList<>();

        try {
            ResultSet rs = null;
            PreparedStatement ps = null;
            StringBuilder query = new StringBuilder();
            query.append(SQL_SELECT_COUNT_ROWS);
            query.append("*");
            query.append(SQL_FROM);
            query.append(TABLE_SHIFT);
            query.append(SQL_WHERE);
            query.append(" id IN ( " + Utils.toString(shiftIds) + " ) ");
            query.append(SQL_ORDER_BY);
            query.append("id");
            ps = con.prepareStatement(query.toString());
            rs = ps.executeQuery();

            while (rs.next()) {
                list.add(getShiftFromRs(rs));
            }

            ps.close();
        } catch (SQLException e) {
            throw new BGException(e);
        }

        return list;
    }

    public Map<Integer, Shift> getAllShiftMap() {
        Map<Integer, Shift> result = new HashMap<>();

        try {
            ResultSet rs = null;
            PreparedStatement ps = null;
            StringBuilder query = new StringBuilder();
            query.append(SQL_SELECT_COUNT_ROWS);
            query.append("*");
            query.append(SQL_FROM);
            query.append(TABLE_SHIFT);
            query.append(SQL_ORDER_BY);
            query.append("id");
            ps = con.prepareStatement(query.toString());
            rs = ps.executeQuery();

            while (rs.next()) {
                Shift shift = getShiftFromRs(rs);
                result.put(shift.getId(), shift);
            }

            ps.close();
        } catch (SQLException e) {
            throw new BGException(e);
        }

        return result;
    }

    public static Shift getShiftFromRs(ResultSet rs) {
        Shift result = new Shift();

        try {
            result.setId(rs.getInt("id"));
            result.setCategory(rs.getInt("category"));
            result.setTitle(rs.getString("title"));
            result.setComment(rs.getString("comment"));
            result.setColor(rs.getString("color"));
            result.setUseOwnColor(rs.getBoolean("use_own_color"));
            result.setWorkTypeTimeList(WorkTypeTime.createFromString(rs.getString("config")));
            result.setSymbol(rs.getString("symbol"));
        } catch (SQLException e) {
            throw new BGException(e);
        }

        return result;
    }

    public Shift getShift(int id, int category) {
        Shift result = null;

        try {
            ResultSet rs = null;
            PreparedStatement ps = null;
            ps = con.prepareStatement(
                    "SELECT * FROM " + TABLE_SHIFT + " WHERE id=? " + (category > 0 ? "AND category=" + category : ""));
            ps.setInt(1, id);
            rs = ps.executeQuery();

            while (rs.next()) {
                result = getShiftFromRs(rs);
            }

            ps.close();
        } catch (SQLException e) {
            throw new BGException(e);
        }

        return result;
    }

    public Shift getShift(int id) {
        return getShift(id, 0);
    }

    public void deleteShift(int id) {
        try {
            PreparedStatement ps = null;

            StringBuilder query = new StringBuilder();
            query.append(SQL_DELETE_FROM);
            query.append(TABLE_SHIFT);
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

    public void updateShift(Shift shift) {
        int index = 1;
        PreparedStatement ps = null;

        try {
            if (shift.getId() > 0) {
                String query = " UPDATE " + TABLE_SHIFT
                        + " SET category=?, title=?, comment=?, config=?, color=?, use_own_color=?, symbol=? WHERE id=? ";
                ps = con.prepareStatement(query);
                ps.setInt(index++, shift.getCategory());
                ps.setString(index++, shift.getTitle());
                ps.setString(index++, shift.getComment());
                ps.setString(index++, shift.serializeToData());
                ps.setString(index++, shift.getColor());
                ps.setBoolean(index++, shift.isUseOwnColor());
                ps.setString(index++, shift.getSymbol());
                ps.setInt(index++, shift.getId());
                ps.executeUpdate();
            } else {
                String query = " INSERT INTO " + TABLE_SHIFT
                        + " SET category=?, title=?, comment=?, config=?, color=?, use_own_color=?, symbol=? ";
                ps = con.prepareStatement(query.toString(), PreparedStatement.RETURN_GENERATED_KEYS);
                ps.setInt(index++, shift.getCategory());
                ps.setString(index++, shift.getTitle());
                ps.setString(index++, shift.getComment());
                ps.setString(index++, shift.serializeToData());
                ps.setString(index++, shift.getColor());
                ps.setBoolean(index++, shift.isUseOwnColor());
                ps.setString(index++, shift.getSymbol());
                ps.executeUpdate();
                shift.setId(lastInsertId(ps));
            }

            ps.close();
        } catch (SQLException e) {
            throw new BGException(e);
        }
    }

    public WorkShift getWorkShift(int graphId, int groupId, int userId, Date date) {
        WorkShift resultWorkShift = new WorkShift();

        try {
            ResultSet rs = null;
            PreparedStatement ps = con.prepareStatement(
                    "SELECT * FROM " + TABLE_SHIFT_USER + " WHERE graph=? and `group`=? and user=? and date=?");

            ps.setInt(1, graphId);
            ps.setInt(2, groupId);
            ps.setInt(3, userId);
            ps.setDate(4, TimeUtils.convertDateToSqlDate(date));

            rs = ps.executeQuery();

            if (rs.first()) {
                resultWorkShift = getWorkShiftFromRs(rs);
                List<WorkTypeTime> workTypeTimeList = new ArrayList<>();

                do {
                    workTypeTimeList.add(new WorkTypeTime(false, rs.getInt("work_type"), rs.getInt("time_from"),
                            rs.getInt("time_to"), ""));
                } while (rs.next());

                resultWorkShift.setWorkTypeTimeList(workTypeTimeList);
            }

            ps.close();
        } catch (SQLException e) {
            throw new BGException(e);
        }

        return resultWorkShift;
    }

    private static class Key {
        private int group;
        private int userId;

        private Key(int group, int userId) {
            this.group = group;
            this.userId = userId;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + group;
            result = prime * result + userId;
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            Key other = (Key) obj;
            if (group != other.group)
                return false;
            if (userId != other.userId)
                return false;
            return true;
        }
    }

    /*
     * Возвращает мап Группа - List из Рабочих смен
     */
    public Map<Integer, List<WorkShift>> getWorkShift(Callboard callboard, Date fromDate, Date toDate,
            Map<Integer, List<Integer>> groupWithUsersSet) {
        Map<Integer, List<WorkShift>> resultMap = new LinkedHashMap<>();

        try {
            final int graphId = callboard.getId();

            Map<Key, List<WorkShift>> workShiftData = new HashMap<>();

            // убрать в далёком будущем, 03.10.2014
            // когда-то использовался формат хранения под кодом основной группы, потом был
            // заменён на 0
            String query = "UPDATE " + TABLE_SHIFT_USER + "SET `group`=0 "
                    + "WHERE graph=? AND date BETWEEN DATE_SUB(?, INTERVAL 1 DAY) AND ? AND `group`=?";
            PreparedStatement ps = con.prepareStatement(query);
            ps.setInt(1, graphId);
            ps.setDate(2, TimeUtils.convertDateToSqlDate(fromDate));
            ps.setDate(3, TimeUtils.convertDateToSqlDate(toDate));
            ps.setInt(4, callboard.getGroupId());
            ps.executeUpdate();
            ps.close();
            con.commit();
            //

            // выбор на день раньше, т.к. оттуда может перейти час
            query = "SELECT * FROM " + TABLE_SHIFT_USER
                    + "WHERE graph=? AND date BETWEEN DATE_SUB(?, INTERVAL 1 DAY) AND ? "
                    + "ORDER BY time_from, time_to";
            ps = con.prepareStatement(query);

            ps.setInt(1, graphId);
            ps.setDate(2, TimeUtils.convertDateToSqlDate(fromDate));
            ps.setDate(3, TimeUtils.convertDateToSqlDate(toDate));

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                WorkShift shift = getWorkShiftFromRs(rs);

                Key key = new Key(shift.getGroupId(), shift.getUserId());
                List<WorkShift> shiftList = workShiftData.get(key);
                if (shiftList == null) {
                    workShiftData.put(key, shiftList = new ArrayList<>());
                }

                shiftList.add(shift);
            }
            ps.close();

            // перебор групп
            for (Entry<Integer, List<Integer>> entry : groupWithUsersSet.entrySet()) {
                List<WorkShift> workShiftList = new ArrayList<>();
                // перебор пользователей
                for (Integer user : entry.getValue()) {
                    Key key = new Key(entry.getKey(), user);
                    List<WorkShift> shiftList = workShiftData.get(key);

                    if (shiftList != null) {
                        Map<Date, WorkShift> dateWorkShift = new HashMap<>();

                        for (WorkShift workShift : shiftList) {
                            /*
                             * не понял зачем, закомментировал Boolean afterCloseGroup = true;
                             *
                             * for( UserGroup userGroup : UserCache.getUserGroupList( user ) ) { //проверяем
                             * не выходят ли смены за пределы времени действия группы if(
                             * userGroup.getGroupId() == entry.getKey() && userGroup.getDateTo() == null &&
                             * userGroup.getDateFrom() != null && userGroup.getDateFrom().compareTo(
                             * workShift.getDate() ) <= 0 ) { afterCloseGroup = false; break; } else if(
                             * userGroup.getGroupId() == entry.getKey() && userGroup.getDateTo() != null &&
                             * userGroup.getDateFrom() != null && userGroup.getDateFrom().compareTo(
                             * workShift.getDate() ) <= 0 && userGroup.getDateTo().compareTo(
                             * workShift.getDate() ) >= 0 ) { afterCloseGroup = false; break; } }
                             *
                             * if( afterCloseGroup ) continue;
                             */

                            // если смена с такой датой уже есть, то нужно добавить к ней только тип работ
                            if (dateWorkShift.containsKey(workShift.getDate())) {
                                dateWorkShift.get(workShift.getDate()).getWorkTypeTimeList()
                                        .add(workShift.getWorkTypeTimeList().get(0));
                            } else {
                                dateWorkShift.put(workShift.getDate(), workShift);
                            }
                        }

                        for (WorkShift workShift : dateWorkShift.values()) {
                            if (!checkWorkShiftTimeOrder(workShift.getWorkTypeTimeList())) {
                                Collections.sort(workShift.getWorkTypeTimeList(), workShiftComparator);
                            }
                        }

                        workShiftList.addAll(dateWorkShift.values());
                    }
                }
                resultMap.put(entry.getKey(), workShiftList);
            }

            ps.close();
        } catch (SQLException e) {
            throw new BGException(e);
        }

        return resultMap;
    }

    // первый ключ - группа, второй - пользователь, далее набор дат, в которых
    // пользователь входит в группу
    public Map<Integer, Map<Integer, Set<Date>>> getAvailableDateForShift(Callboard callboard,
            Map<Integer, List<Integer>> groupWithUsersMap, Date fromDate, Date toDate) {
        Map<Integer, Map<Integer, Set<Date>>> result = new HashMap<>();

        Calendar calTo = TimeUtils.convertDateToCalendar(toDate);

        for (Map.Entry<Integer, List<Integer>> groupUsers : groupWithUsersMap.entrySet()) {
            int groupId = groupUsers.getKey();
            if (groupId == 0) {
                groupId = callboard.getGroupId();
            }

            Map<Integer, Set<Date>> groupUserDates = new HashMap<>();
            result.put(groupId, groupUserDates);

            for (Integer userId : groupUsers.getValue()) {
                Set<Date> userDates = new HashSet<>();
                groupUserDates.put(userId, userDates);

                List<UserGroup> userGroups = UserCache.getUserGroupList(userId);
                if (userGroups.size() > 0) {
                    Calendar cal = TimeUtils.convertDateToCalendar(fromDate);
                    while (TimeUtils.dateBeforeOrEq(cal, calTo)) {
                        for (UserGroup userGroup : userGroups) {
                            if (userGroup.getGroupId() == groupId && TimeUtils.dateInRange(cal,
                                    TimeUtils.convertDateToCalendar(userGroup.getDateFrom()),
                                    TimeUtils.convertDateToCalendar(userGroup.getDateTo()))) {
                                userDates.add(TimeUtils.convertCalendarToDate(cal));
                                break;
                            }
                        }
                        cal.add(Calendar.DAY_OF_YEAR, 1);
                    }
                }
            }

        }

        return result;
    }

    // первый ключ - пользователь, далее - и данные по смене
    public Map<Integer, Map<Date, WorkShift>> getUserShifts(int graphId, Date fromDate, Date toDate)
            {
        Map<Integer, Map<Date, WorkShift>> result = new HashMap<>();

        try {
            String query = "SELECT * FROM " + TABLE_SHIFT_USER
                    + "WHERE graph=? AND date BETWEEN DATE_SUB(?, INTERVAL 1 DAY) AND ? " + "ORDER BY id";

            PreparedStatement ps = con.prepareStatement(query);
            ps.setInt(1, graphId);
            ps.setDate(2, TimeUtils.convertDateToSqlDate(fromDate));
            ps.setDate(3, TimeUtils.convertDateToSqlDate(toDate));

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                WorkShift workShift = getWorkShiftFromRs(rs);

                Map<Date, WorkShift> dayUserShift = result.get(workShift.getUserId());
                if (dayUserShift == null) {
                    result.put(workShift.getUserId(), dayUserShift = new HashMap<>());
                }

                WorkShift existWorkShift = dayUserShift.get(workShift.getDate());
                if (existWorkShift != null) {
                    existWorkShift.getWorkTypeTimeList().add(workShift.getWorkTypeTimeList().get(0));
                } else {
                    dayUserShift.put(workShift.getDate(), workShift);
                }
            }

            ps.close();

            for (Map<Date, WorkShift> map : result.values()) {
                for (WorkShift workShift : map.values()) {
                    WorkTypeTime.setNextDays(workShift.getWorkTypeTimeList());
                }
            }
        } catch (SQLException e) {
            throw new BGException(e);
        }

        return result;
    }

    public Set<WorkShift> getWorkShiftSetFor(Date date, int userId) {
        Set<WorkShift> result = new HashSet<>();

        try {
            ResultSet rs = null;
            PreparedStatement ps = con
                    .prepareStatement("SELECT * FROM " + TABLE_SHIFT_USER + " WHERE user=? AND date=? AND shift <> 0");

            ps.setInt(1, userId);
            ps.setDate(2, TimeUtils.convertDateToSqlDate(date));

            rs = ps.executeQuery();

            while (rs.next()) {
                result.add(getWorkShiftFromRs(rs));
            }

            ps.close();
        } catch (SQLException e) {
            throw new BGException(e);
        }

        return result;
    }

    public Map<Date, Set<WorkShift>> getMonthWorkShift(Date date, int groupId) {
        Map<Date, Set<WorkShift>> resultMap = new LinkedHashMap<>();

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.DAY_OF_MONTH, 1);

        try {
            ResultSet rs = null;
            PreparedStatement ps = con.prepareStatement(
                    "SELECT * FROM " + TABLE_SHIFT_USER + " WHERE team <> 0 AND `group`=? AND date BETWEEN ? AND ?");

            ps.setInt(1, groupId);
            ps.setDate(2, TimeUtils.convertDateToSqlDate(calendar.getTime()));

            calendar.add(Calendar.MONTH, 1);

            ps.setDate(3, TimeUtils.convertDateToSqlDate(calendar.getTime()));

            rs = ps.executeQuery();

            while (rs.next()) {
                WorkShift workShift = getWorkShiftFromRs(rs);

                if (resultMap.containsKey(workShift.getDate())) {
                    resultMap.get(workShift.getDate()).add(workShift);
                } else {
                    Set<WorkShift> workShiftSet = new LinkedHashSet<>();
                    workShiftSet.add(workShift);

                    resultMap.put(workShift.getDate(), workShiftSet);
                }
            }

            ps.close();
        } catch (SQLException ex) {
            throw new BGException(ex);
        }

        return resultMap;
    }

    private WorkShift getWorkShiftFromRs(ResultSet rs) {
        WorkShift result = new WorkShift();

        try {
            result.setId(rs.getInt("id"));
            result.setUserId(rs.getInt("user"));
            result.setGraphId(rs.getInt("graph"));
            result.setGroupId(rs.getInt("group"));
            result.setDate(rs.getDate("date"));
            result.setTeam(rs.getInt("team"));
            result.setShiftId(rs.getInt("shift"));

            List<WorkTypeTime> workTypeTimeList = new ArrayList<>();
            workTypeTimeList.add(new WorkTypeTime(rs.getBoolean("is_dynamic"), rs.getInt("work_type"),
                    rs.getInt("time_from"), rs.getInt("time_to"), rs.getString("comment")));

            result.setWorkTypeTimeList(workTypeTimeList);
        } catch (SQLException e) {
            throw new BGException(e);
        }

        return result;
    }

    public void updateWorkShift(WorkShift workShift) {
        PreparedStatement ps = null;

        try {
            String query = " INSERT INTO " + TABLE_SHIFT_USER
                    + " ( user, graph, `group`, date, team, shift, work_type, time_from, time_to, is_dynamic, comment ) VALUES( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? )";
            ps = con.prepareStatement(query.toString(), PreparedStatement.RETURN_GENERATED_KEYS);

            for (WorkTypeTime workTypeTime : workShift.getWorkTypeTimeList()) {
                int index = 1;

                ps.setInt(index++, workShift.getUserId());
                ps.setInt(index++, workShift.getGraphId());
                ps.setInt(index++, workShift.getGroupId());
                ps.setDate(index++, TimeUtils.convertDateToSqlDate(workShift.getDate()));
                ps.setInt(index++, workShift.getTeam());
                ps.setInt(index++, workShift.getShiftId());
                ps.setInt(index++, workTypeTime.getWorkTypeId());
                ps.setInt(index++, workTypeTime.getDayMinuteFrom());
                ps.setInt(index++, workTypeTime.getDayMinuteTo());
                ps.setBoolean(index++, workTypeTime.isDynamic());
                ps.setString(index++, workTypeTime.getComment() == null ? "" : workTypeTime.getComment());

                ps.executeUpdate();
            }

            workShift.setId(lastInsertId(ps));

            ps.close();
        } catch (SQLException e) {
            throw new BGException(e);
        }
    }

    public void deleteWorkShift(int graphId, int groupId, int userId, Date date) {
        try {
            PreparedStatement ps = con.prepareStatement(
                    "DELETE FROM " + TABLE_SHIFT_USER + " WHERE graph=? AND `group`=? AND user=? AND date=? ");

            ps.setInt(1, graphId);
            ps.setInt(2, groupId);
            ps.setInt(3, userId);
            ps.setDate(4, TimeUtils.convertDateToSqlDate(date));

            ps.executeUpdate();
            ps.close();
        } catch (SQLException e) {
            throw new BGException(e);
        }
    }

    public void addCallboardTask(int process_id, int group, int team, int graph, Date date) {
        PreparedStatement ps = null;

        try {
            String query = " INSERT INTO " + org.bgerp.plugin.pln.callboard.dao.Tables.TABLE_CALLBOARD_TASK
                    + " (`group`, team, date, graph, process_id) VALUES( ?, ?, ?, ?, ? )";
            ps = con.prepareStatement(query.toString(), PreparedStatement.RETURN_GENERATED_KEYS);

            ps.setInt(1, group);
            ps.setInt(2, team);
            ps.setTimestamp(3, TimeConvert.toTimestamp(date));
            ps.setInt(4, graph);
            ps.setInt(5, process_id);

            ps.executeUpdate();
            ps.close();
        } catch (SQLException e) {
            throw new BGException(e);
        }
    }

    public void deleteCallboardTask(int process_id, int group, int team, int graph, Date date) {
        try {
            PreparedStatement ps = con
                    .prepareStatement("DELETE FROM " + org.bgerp.plugin.pln.callboard.dao.Tables.TABLE_CALLBOARD_TASK
                            + " WHERE process_id=? `group`=? AND team=? AND date=? AND graph=? ");

            ps.setInt(1, process_id);
            ps.setInt(2, group);
            ps.setInt(3, team);
            ps.setDate(4, TimeUtils.convertDateToSqlDate(date));
            ps.setInt(5, graph);

            ps.executeUpdate();
            ps.close();
        } catch (SQLException e) {
            throw new BGException(e);
        }
    }

    public void deleteCallboardTask(int process_id) {
        try {
            PreparedStatement ps = con.prepareStatement("DELETE FROM "
                    + org.bgerp.plugin.pln.callboard.dao.Tables.TABLE_CALLBOARD_TASK + " WHERE process_id=? ");

            ps.setInt(1, process_id);
            ps.executeUpdate();
            ps.close();
        } catch (SQLException e) {
            throw new BGException(e);
        }
    }

    public boolean isTimeOccupied(int group, int team, int graph, Date date) {
        boolean result = false;

        try {
            ResultSet rs = null;
            PreparedStatement ps = con
                    .prepareStatement("SELECT * FROM " + org.bgerp.plugin.pln.callboard.dao.Tables.TABLE_CALLBOARD_TASK
                            + " WHERE `group`=? AND date=? AND team=? AND graph=?");

            ps.setInt(1, group);
            ps.setDate(2, TimeUtils.convertDateToSqlDate(date));
            ps.setInt(3, team);
            ps.setInt(4, graph);

            rs = ps.executeQuery();

            if (rs.first()) {
                result = true;
            }

            ps.close();
        } catch (SQLException e) {
            throw new BGException(e);
        }

        return result;
    }

    public List<CallboardTask> getDateTaskList(Date date) {
        List<CallboardTask> result = new ArrayList<>();

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        try {
            ResultSet rs = null;
            PreparedStatement ps = con.prepareStatement("SELECT * FROM "
                    + org.bgerp.plugin.pln.callboard.dao.Tables.TABLE_CALLBOARD_TASK + " WHERE date BETWEEN ? AND ?");

            ps.setTimestamp(1, TimeUtils.convertCalendarToTimestamp(calendar));

            calendar.add(Calendar.DAY_OF_MONTH, 1);
            ps.setTimestamp(2, TimeUtils.convertCalendarToTimestamp(calendar));

            rs = ps.executeQuery();

            while (rs.next()) {
                result.add(new CallboardTask(rs.getInt("process_id"), rs.getInt("group"), rs.getInt("team"), rs.getInt("graph"),
                        rs.getTimestamp("date")));
            }

            ps.close();
        } catch (SQLException e) {
            throw new BGException(e);
        }

        return result;
    }

    /*
     * Находит пользователей с рабочими сменами, которые состоят в одной бригаде в
     * один и тот же день, в одной группе вместе с переданной сменой
     */
    public List<WorkShift> findSameWorkShift(WorkShift workShift) {
        List<WorkShift> resultList = new ArrayList<>();

        try {
            ResultSet rs = null;
            PreparedStatement ps = con.prepareStatement("SELECT DISTINCT(user) FROM " + TABLE_SHIFT_USER
                    + " WHERE graph=? and `group`=? and team=? and date=? and user<>?");

            ps.setInt(1, workShift.getGraphId());
            ps.setInt(2, workShift.getGroupId());
            ps.setInt(3, workShift.getTeam());
            ps.setDate(4, TimeUtils.convertDateToSqlDate(workShift.getDate()));
            ps.setInt(5, workShift.getUserId());

            rs = ps.executeQuery();

            if (rs.next()) {
                WorkShift sameWorkShift = workShift;
                sameWorkShift.setUserId(rs.getInt("user"));

                resultList.add(sameWorkShift);
            }

            ps.close();
        } catch (SQLException e) {
            throw new BGException(e);
        }

        return resultList;
    }

    public void updateShiftOrder(int graphId, int groupId, Map<Integer, Integer> orderMap) {
        try {
            PreparedStatement ps = con
                    .prepareStatement("DELETE FROM " + TABLE_SHIFT_ORDER + " WHERE graph_id=? AND group_id=? ");

            ps.setInt(1, graphId);
            ps.setInt(2, groupId);

            ps.executeUpdate();
            ps.close();

            String query = " INSERT INTO " + TABLE_SHIFT_ORDER
                    + " ( graph_id, group_id, user_id, `order` ) VALUES( ?, ?, ?, ? )";
            ps = con.prepareStatement(query.toString());

            ps.setInt(1, graphId);
            ps.setInt(2, groupId);

            for (Entry<Integer, Integer> entry : orderMap.entrySet()) {
                ps.setInt(3, entry.getKey());
                ps.setInt(4, entry.getValue());

                ps.executeUpdate();
            }

            ps.close();
        } catch (SQLException e) {
            throw new BGException(e);
        }
    }

    public Map<Integer, Integer> getShiftOrder(int graphId, int groupId) {
        Map<Integer, Integer> result = new HashMap<>();

        try {
            ResultSet rs = null;
            PreparedStatement ps = con
                    .prepareStatement("SELECT * FROM " + TABLE_SHIFT_ORDER + " WHERE graph_id=? AND group_id=? ");

            ps.setInt(1, graphId);
            ps.setInt(2, groupId);
            rs = ps.executeQuery();

            while (rs.next()) {
                result.put(rs.getInt("user_id"), rs.getInt("order"));
            }

            ps.close();
        } catch (SQLException e) {
            throw new BGException(e);
        }

        return result;
    }

    public void setDynamicShiftTime(int workShiftId, int timeBegin, int timeEnd) {
        try {
            PreparedStatement ps = con.prepareStatement("UPDATE " + TABLE_SHIFT_USER
                    + " SET is_dynamic=0, time_from=?, time_to=? WHERE id=? AND is_dynamic=1  ");

            ps.setInt(1, timeBegin);
            ps.setInt(2, timeEnd);
            ps.setInt(3, workShiftId);

            ps.executeUpdate();
            ps.close();
        } catch (SQLException e) {
            throw new BGException(e);
        }
    }

    public int getSameWorkTypeShiftCount(int workTypeId, int workShiftId, int time_from, int time_to)
            {
        int result = 0;

        try {
            PreparedStatement ps1 = con
                    .prepareStatement("SELECT date FROM " + TABLE_SHIFT_USER + " WHERE id=" + workShiftId);
            ResultSet rs = ps1.executeQuery();

            if (rs.next()) {
                Date date = rs.getDate("date");

                PreparedStatement ps = con.prepareStatement("SELECT COUNT(*) as cnt FROM " + TABLE_SHIFT_USER
                        + " WHERE is_dynamic=0 AND work_type=? AND date=? AND time_from=? AND time_to=? ");

                ps.setInt(1, workTypeId);
                ps.setDate(2, TimeUtils.convertDateToSqlDate(date));
                ps.setInt(3, time_from);
                ps.setInt(4, time_to);

                rs = ps.executeQuery();

                if (rs.next()) {
                    result = rs.getInt("cnt");
                }

                ps.close();
            }

            ps1.close();
        } catch (SQLException e) {
            throw new BGException(e);
        }

        return result;
    }

    // FIXME: Неведомая функция со сложной логикой, оставлена со старой версии!!!
    // Разобраться, что делает, т.к. возможно не нужна..
    private boolean checkWorkShiftTimeOrder(List<WorkTypeTime> workShiftList) {
        Set<Integer> beginTimeSet = new HashSet<>();
        Set<Integer> endTimeSet = new HashSet<>();
        Set<Integer> copyTimeSet = new HashSet<>();

        for (WorkTypeTime workTypeTime : workShiftList) {
            beginTimeSet.add(workTypeTime.getDayMinuteFrom());
            endTimeSet.add(workTypeTime.getDayMinuteTo());
        }

        copyTimeSet = new HashSet<>(beginTimeSet);
        beginTimeSet.removeAll(endTimeSet);
        endTimeSet.removeAll(copyTimeSet);

        if (beginTimeSet.size() == 1 && endTimeSet.size() == 1) {
            int count = 0;
            int lastTime = new ArrayList<>(beginTimeSet).get(0);
            List<WorkTypeTime> sortedShiftList = new ArrayList<>();

            while (sortedShiftList.size() < workShiftList.size()) {
                for (WorkTypeTime workTypeTime : workShiftList) {
                    if (workTypeTime.getDayMinuteFrom() == lastTime) {
                        sortedShiftList.add(workTypeTime);
                        lastTime = workTypeTime.getDayMinuteTo();
                        break;
                    }
                }

                if (count > workShiftList.size()) {
                    break;
                }

                count++;
            }

            if (sortedShiftList.size() == workShiftList.size()) {
                workShiftList.clear();
                workShiftList.addAll(sortedShiftList);
                return true;
            }
        }

        return false;
    }
}
