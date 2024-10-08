package ru.bgcrm.plugin.task;

import org.bgerp.app.cfg.Setup;

import ru.bgcrm.plugin.task.dao.TaskDAO;
import ru.bgcrm.plugin.task.model.Task;

public class ExpressionObject {
    /**
     * Создаёт задачу с указанным типом. Существующая задача с таким же типом  и кодом процесса перетирается.
     * @param task
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
     * TODO: Запустить немедленно задачу.
     * @param task
     */
    public void runTask(Task task) {

    }
}