package ru.bgcrm.plugin.task;

import java.util.Map;

import org.bgerp.app.cfg.Setup;

import ru.bgcrm.plugin.task.dao.TaskDAO;
import ru.bgcrm.plugin.task.model.Task;

public class ExpressionObject implements org.bgerp.dao.expression.ExpressionObject {
    ExpressionObject() {}

    @Override
    public void toContext(Map<String, Object> context) {
       context.put(Plugin.ID, this);
    }

    /**
     * Creates a task with the specified type. An existing task with the same type and process ID gets overwritten.
     * @param task the task
     * @throws Exception
     */
    public void setTask(Task task) throws Exception {
        try (var con = Setup.getSetup().getDBConnectionFromPool()) {
            TaskDAO dao = new TaskDAO(con);
            dao.deleteTasks(task.getProcessId(), task.getTypeId());

            dao.updateTask(task);

            con.commit();
        }
    }

    /**
     * TODO: Run the task immediately
     * @param task the task
     */
    public void runTask(Task task) {

    }
}