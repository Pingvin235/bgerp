package ru.bgcrm.plugin.task.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.bgerp.app.cfg.Preferences;
import org.bgerp.model.Pageable;
import org.bgerp.util.TimeConvert;
import org.bgerp.util.sql.PreparedQuery;

import ru.bgcrm.dao.CommonDAO;
import ru.bgcrm.plugin.task.model.Task;

public class TaskDAO extends CommonDAO {

    private static final String TABLE = " task ";

    public TaskDAO(Connection con) {
        super(con);
    }

    /**
     * Returns a list of tasks filtered by process and type
     * @param processId the required process filter
     * @param typeId if > 0, filter by process type
     * @param onlyOpen only not executed ones
     * @throws SQLException
     */
    public void searchTasks(Pageable<Task> result, int processId, int typeId, boolean onlyOpen) throws SQLException {
        PreparedQuery pq = new PreparedQuery(con, "SELECT * FROM " + TABLE + " WHERE process_id=?");
        pq.addInt(processId);
        if (typeId > 0) {
            pq.addQuery(" AND type_id=?");
            pq.addInt(typeId);
        }
        if (onlyOpen)
            pq.addQuery(" AND executed_dt IS NULL");

        pq.addQuery(result.getPage().getLimitSql());

        ResultSet rs = pq.executeQuery();
        while (rs.next())
            result.getList().add(getTaskFromRs(rs));

        pq.close();
    }

    /**
     * Returns a list of tasks scheduled for execution
     * @param limit the max count
     * @return the scheduled tasks
     * @throws SQLException
     */
    public List<Task> getScheduledTasks(int limit) throws SQLException {
        List<Task> result = new ArrayList<>();

        String query = "SELECT * FROM " + TABLE + " WHERE executed_dt IS NULL AND scheduled_dt<=NOW() ORDER BY scheduled_dt LIMIT ?";
        PreparedStatement ps = con.prepareStatement(query);
        ps.setInt(1, limit);
        ResultSet rs = ps.executeQuery();
        while (rs.next())
            result.add(getTaskFromRs(rs));
        ps.close();

        return result;
    }

    /**
     * Adds a new task (id &lt;= 0), or updates the execution date and log of an existing one
     * @param task the task
     * @throws SQLException
     */
    public void updateTask(Task task) throws SQLException {
       if (task.getId() <= 0) {
           String query = "INSERT INTO " + TABLE + "(process_id, type_id, scheduled_dt, config) VALUES (?,?,?, ?)";
           PreparedStatement ps = con.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS);
           ps.setInt(1, task.getProcessId());
           ps.setString(2, task.getTypeId());
           ps.setTimestamp(3, TimeConvert.toTimestamp(task.getScheduledTime()));
           ps.setString(4, task.getConfig().getDataString());
           ps.executeUpdate();
           task.setId(lastInsertId(ps));
           ps.close();
       } else {
           String query = "UPDATE " + TABLE + " SET executed_dt=?, log=? WHERE id=?";
           PreparedStatement ps = con.prepareStatement(query);
           ps.setTimestamp(1, TimeConvert.toTimestamp(task.getExecutedTime()));
           ps.setString(2, task.getLog());
           ps.setInt(3, task.getId());
           ps.executeUpdate();
           ps.close();
       }
    }

    /**
     * Deletes tasks by process and type
     * @param processId the process ID
     * @param typeId the type ID
     * @throws SQLException
     */
    public void deleteTasks(int processId, String typeId) throws SQLException {
        String query = "DELETE FROM " + TABLE + " WHERE process_id=? AND type_id=?";
        PreparedStatement ps = con.prepareStatement(query);
        ps.setInt(1, processId);
        ps.setString(2, typeId);
        ps.executeUpdate();
        ps.close();
    }

    private Task getTaskFromRs(ResultSet rs) throws SQLException {
        Task task = new Task();
        task.setId(rs.getInt("id"));
        task.setProcessId(rs.getInt("process_id"));
        task.setTypeId(rs.getString("type_id"));
        task.setScheduledTime(rs.getTimestamp("scheduled_dt"));
        task.setExecutedTime(rs.getTimestamp("executed_dt"));
        task.setConfig(new Preferences(rs.getString("config")));
        task.setLog(rs.getString("log"));
        return task;
    }
}
