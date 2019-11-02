package ru.bgcrm.plugin.task;

import java.sql.Connection;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.Logger;

import ru.bgcrm.dao.expression.Expression;
import ru.bgcrm.dao.process.ProcessDAO;
import ru.bgcrm.event.listener.DefaultProcessChangeListener;
import ru.bgcrm.plugin.task.dao.TaskDAO;
import ru.bgcrm.plugin.task.model.Task;
import ru.bgcrm.plugin.task.model.TaskType;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.Setup;
import ru.bgcrm.util.sql.SQLUtils;
import ru.bgcrm.util.sql.SingleConnectionConnectionSet;
import ru.bgcrm.model.process.Process;

/**
 * Задача проверки и рассылки уведомлений.
 * @author Shamil
 */
public class TaskRunner implements Runnable {
    private static final Logger log = Logger.getLogger(TaskRunner.class);
    
    private static final AtomicBoolean run = new AtomicBoolean();

    @Override
    public void run() {
        if (run.get()) {
            log.info("Is already running.");
            return;
        }
        
        log.info("Run tasks..");
        run.set(true);
        Connection con = Setup.getSetup().getDBConnectionFromPool();
        try {
            Config config = Setup.getSetup().getConfig(Config.class);
            TaskDAO taskDao = new TaskDAO(con);
            ProcessDAO processDao = new ProcessDAO(con);
            
            for (Task task : taskDao.getScheduledTasks(100)) {
                log.info("Task found: " + task.getId());
                
                TaskType type = config.getType(task.getTypeId());
                if (type == null)
                    log.warn("Incorrect task type: " + task.getTypeId());
                else {
                    Process process = processDao.getProcess(task.getProcessId());
                    if (process == null)
                        log.warn("Process is not found: " + task.getProcessId());
                    else 
                        try {
                            Map<String, Object> context = DefaultProcessChangeListener
                                    .getProcessJexlContext(new SingleConnectionConnectionSet(con), DynActionForm.SERVER_FORM, null, process);
                            context.put("task", task);
                            context.put("taskType", type);
                            Expression expression = new Expression(context);
                            expression.executeScript(type.getDoExpression());
                        } catch (Exception e) {
                            log.error("Task execution error: " + e.getMessage(), e);
                        }
                }
                
                task.setExecutedTime(new Date());
                taskDao.updateTask(task);
                con.commit();
            }
            
        } catch (Throwable e) {
            log.error(e.getMessage(), e);
        } finally {
            SQLUtils.closeConnection(con);
            run.set(false);
        }
    }

}
