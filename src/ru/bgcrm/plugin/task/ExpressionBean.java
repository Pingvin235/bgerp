package ru.bgcrm.plugin.task;

import ru.bgcrm.model.BGException;
import ru.bgcrm.plugin.task.dao.TaskDAO;
import ru.bgcrm.plugin.task.model.Task;
import ru.bgcrm.util.Setup;

public class ExpressionBean {

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
     * @throws BGException
     */
    public void runTask(Task task) throws BGException {

    }
}