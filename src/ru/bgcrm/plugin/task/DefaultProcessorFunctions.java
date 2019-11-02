package ru.bgcrm.plugin.task;

import java.sql.Connection;

import ru.bgcrm.dao.expression.ExpressionBasedFunction;
import ru.bgcrm.model.BGException;
import ru.bgcrm.plugin.task.dao.TaskDAO;
import ru.bgcrm.plugin.task.model.Task;
import ru.bgcrm.util.Setup;
import ru.bgcrm.util.sql.SQLUtils;

public class DefaultProcessorFunctions extends ExpressionBasedFunction {

	public DefaultProcessorFunctions() {}
		
	/**
	 * Создаёт задачу с указанным типом. Существующая задача с таким же типом  и кодом процесса перетирается.
	 * @param process
	 * @param typeId
	 * @throws BGException
	 */
	public void setTask(Task task) throws BGException {
	    Connection con = Setup.getSetup().getDBConnectionFromPool();
        try {
            TaskDAO dao = new TaskDAO(con);
            dao.deleteTasks(task.getProcessId(), task.getTypeId());
            
            dao.updateTask(task);
            
            con.commit();
        } catch (Exception ex) {
            throw new BGException(ex);
        } finally {
            SQLUtils.closeConnection(con);
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