package org.bgerp.plugin.pln.callboard.dao;

import static org.bgerp.plugin.pln.callboard.dao.Tables.TABLE_CALLBOARD_TASK;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.bgerp.app.exception.BGException;
import org.bgerp.plugin.pln.callboard.model.WorkTask;
import org.bgerp.plugin.pln.callboard.model.work.ShiftData;
import org.bgerp.util.TimeConvert;
import org.bgerp.util.sql.PreparedQuery;

import ru.bgcrm.dao.CommonDAO;
import ru.bgcrm.util.TimeUtils;

public class WorkTaskDAO extends CommonDAO {
    public WorkTaskDAO(Connection con) {
        super(con);
    }

    public void loadWorkTask(int graphId, Date date, Map<Integer, List<ShiftData>> dataMap) {
        try {
            String query = "SELECT * FROM " + TABLE_CALLBOARD_TASK + " WHERE graph=? AND ?<=time AND time<? " +
            // AND `group` IN(" + Utils.toString( dataMap.keySet() ) + ")
                    " ORDER BY slot_from";
            PreparedStatement ps = con.prepareStatement(query);
            ps.setInt(1, graphId);
            ps.setDate(2, TimeUtils.convertDateToSqlDate(date));
            ps.setDate(3, TimeUtils.convertDateToSqlDate(TimeUtils.getNextDay(date)));

            ResultSet rs = ps.executeQuery();

            TASK_LOOP: while (rs.next()) {
                WorkTask task = getFromRs(rs);

                List<ShiftData> dataList = dataMap.get(task.getGroupId());
                if (dataList != null) {
                    for (ShiftData dataItem : dataList) {
                        // либо по бригаде либо по пользователю
                        if ((dataItem.getTeam() > 0 && dataItem.getTeam() == task.getTeam())
                                || (dataItem.getTeam() == 0 && dataItem.getUserIds().contains(task.getUserId()))) {
                            dataItem.addTask(task);
                            continue TASK_LOOP;
                        }
                    }
                }
            }
            ps.close();
        } catch (SQLException e) {
            throw new BGException(e);
        }
    }

    public WorkTask getTaskByProcessId(int processId) {
        try {
            WorkTask result = null;

            String query = "SELECT * FROM " + TABLE_CALLBOARD_TASK + " WHERE process_id=?";
            PreparedStatement ps = con.prepareStatement(query);

            ps.setInt(1, processId);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                result = getFromRs(rs);
            }
            ps.close();

            return result;
        } catch (Exception e) {
            throw new BGException(e);
        }
    }

    public void addTask(WorkTask task) {
        try {
            String query = null;
            PreparedStatement ps = null;

            if (task.getProcessId() != WorkTask.PROCESS_ID_LOCK) {
                query = "UPDATE " + TABLE_CALLBOARD_TASK
                        + " SET graph=?, time=?, slot_from=?, `group`=?, user_id=?, team=?, duration=?, reference=? "
                        + "WHERE process_id=?";
                ps = con.prepareStatement(query);

                ps.setInt(1, task.getGraphId());
                ps.setTimestamp(2, TimeConvert.toTimestamp(task.getTime()));
                ps.setInt(3, task.getSlotFrom());
                ps.setInt(4, task.getGroupId());
                ps.setInt(5, task.getUserId());
                ps.setInt(6, task.getTeam());
                ps.setInt(7, task.getDuration());
                ps.setString(8, task.getReference());
                ps.setInt(9, task.getProcessId());

                boolean updateResult = ps.executeUpdate() > 0;
                ps.close();

                if (updateResult) {
                    return;
                }
            }

            query = "INSERT INTO " + TABLE_CALLBOARD_TASK
                    + " (graph, time, slot_from, `group`, user_id, team, duration, reference, process_id) "
                    + "VALUES (?,?,?,?,?,?,?,?,?)";
            ps = con.prepareStatement(query);

            ps.setInt(1, task.getGraphId());
            ps.setTimestamp(2, TimeConvert.toTimestamp(task.getTime()));
            ps.setInt(3, task.getSlotFrom());
            ps.setInt(4, task.getGroupId());
            ps.setInt(5, task.getUserId());
            ps.setInt(6, task.getTeam());
            ps.setInt(7, task.getDuration());
            ps.setString(8, task.getReference());
            ps.setInt(9, task.getProcessId());

            ps.executeUpdate();

            ps.close();
        } catch (SQLException e) {
            throw new BGException(e);
        }
    }

    public void removeTask(WorkTask task) {
        try {
            PreparedQuery pq = new PreparedQuery(con);
            pq.addQuery("DELETE FROM " + TABLE_CALLBOARD_TASK + " WHERE graph=? AND `group`=? AND time=? ");
            pq.addInt(task.getGraphId());
            pq.addInt(task.getGroupId());
            pq.addTimestamp(TimeConvert.toTimestamp(task.getTime()));

            if (task.getTeam() > 0) {
                pq.addQuery("AND team=?");
                pq.addInt(task.getTeam());
            } else {
                pq.addQuery("AND user_id=?");
                pq.addInt(task.getUserId());
            }

            pq.executeUpdate();
            pq.close();
        } catch (SQLException e) {
            throw new BGException(e);
        }
    }

    public void removeTaskForProcess(int processId) {
        try {
            String query = "DELETE FROM " + TABLE_CALLBOARD_TASK + " WHERE process_id=?";
            PreparedStatement ps = con.prepareStatement(query);
            ps.setInt(1, processId);
            ps.executeUpdate();
            ps.close();
        } catch (SQLException e) {
            throw new BGException(e);
        }
    }

    private WorkTask getFromRs(ResultSet rs) throws SQLException {
        WorkTask result = new WorkTask();

        result.setGraphId(rs.getInt("graph"));
        result.setTime(rs.getTimestamp("time"));
        result.setSlotFrom(rs.getInt("slot_from"));
        // result.setSlotTo( rs.getInt( "slot_to" ) );
        result.setGroupId(rs.getInt("group"));
        result.setUserId(rs.getInt("user_id"));
        result.setTeam(rs.getInt("team"));
        result.setDuration(rs.getInt("duration"));
        result.setProcessId(rs.getInt("process_id"));
        result.setReference(rs.getString("reference"));

        return result;
    }
}