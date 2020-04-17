package ru.bgcrm.plugin.task.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import ru.bgcrm.dao.CommonDAO;
import ru.bgcrm.model.SearchResult;
import ru.bgcrm.plugin.task.model.Task;
import ru.bgcrm.util.Preferences;
import ru.bgcrm.util.TimeUtils;
import ru.bgcrm.util.sql.PreparedDelay;

public class TaskDAO extends CommonDAO {

    private static final String TABLE = " task ";

    public TaskDAO(Connection con) {
        super(con);
    }

    /**
     * Возвращает список задач с фильтром по процессу и типу.
     * @param processId обязательный фильтр по процессу.
     * @param typeId > 0, фильтр по типу процесса.
     * @param onlyOpen только не выполненные.
     * @return
     * @throws SQLException
     */
    public void searchTasks(SearchResult<Task> result, int processId, int typeId, boolean onlyOpen) throws SQLException {
        PreparedDelay pd = new PreparedDelay(con, "SELECT * FROM " + TABLE + " WHERE process_id=?");
        pd.addInt(processId);
        if (typeId > 0) {
            pd.addQuery(" AND type_id=?");
            pd.addInt(typeId);
        }
        if (onlyOpen)
            pd.addQuery(" AND executed_dt IS NULL");
        
        pd.addQuery(getPageLimit(result.getPage()));
        
        ResultSet rs = pd.executeQuery();
        while (rs.next()) 
            result.getList().add(getTaskFromRs(rs));            
        
        pd.close();
    }

    /**
     * Возвращает список запланированных к исполнению задач.
     * @param limit
     * @return
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
     * Добавляет новую задачу (id &lt;=0 ), либо обновляет дату выполнения и лог у существующей.
     * @param task
     * @throws SQLException
     */
    public void updateTask(Task task) throws SQLException {
       if (task.getId() <= 0) {
           String query = "INSERT INTO " + TABLE + "(process_id, type_id, scheduled_dt, config) VALUES (?,?,?, ?)";
           PreparedStatement ps = con.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS);
           ps.setInt(1, task.getProcessId());
           ps.setString(2, task.getTypeId());
           ps.setTimestamp(3, TimeUtils.convertDateToTimestamp(task.getScheduledTime()));
           ps.setString(4, task.getConfig().getDataString());
           ps.executeUpdate();
           task.setId(lastInsertId(ps));
           ps.close();
       } else {
           String query = "UPDATE " + TABLE + " SET executed_dt=?, log=? WHERE id=?";
           PreparedStatement ps = con.prepareStatement(query);
           ps.setTimestamp(1, TimeUtils.convertDateToTimestamp(task.getExecutedTime()));
           ps.setString(2, task.getLog());
           ps.setInt(3, task.getId());
           ps.executeUpdate();
           ps.close();
       }        
    }
    
    /** 
     * Удаляет задачи по процессу и типу.
     * @param processId
     * @param typeId
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
