package ru.bgcrm.plugin.task.exec;

import java.util.Date;
import java.util.Map;

import org.bgerp.app.cfg.Setup;
import org.bgerp.app.cfg.bean.annotation.Bean;
import org.bgerp.util.Log;

import ru.bgcrm.dao.expression.Expression;
import ru.bgcrm.dao.process.ProcessDAO;
import ru.bgcrm.model.process.Process;
import ru.bgcrm.plugin.task.Config;
import ru.bgcrm.plugin.task.Plugin;
import ru.bgcrm.plugin.task.dao.TaskDAO;
import ru.bgcrm.plugin.task.model.Task;
import ru.bgcrm.plugin.task.model.TaskType;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.sql.SingleConnectionSet;

/**
 * Tasks runner.
 *
 * @author Shamil Vakhitov
 */
@Bean(oldClasses = "ru.bgcrm.plugin.task.TaskRunner")
public class TaskRunner extends org.bgerp.app.exec.scheduler.Task {
    private static final Log log = Log.getLog();

    public TaskRunner() {
        super(null);
    }

    @Override
    public String getTitle() {
        return Plugin.INSTANCE.getLocalizer().l("Task Runner");
    }

    @Override
    public void run() {
        try (var con = Setup.getSetup().getDBConnectionFromPool()) {
            Config config = Setup.getSetup().getConfig(Config.class);
            TaskDAO taskDao = new TaskDAO(con);
            ProcessDAO processDao = new ProcessDAO(con);

            for (Task task : taskDao.getScheduledTasks(100)) {
                log.info("Task found: {}", task.getId());

                TaskType type = config.getType(task.getTypeId());
                if (type == null)
                    log.warn("Incorrect task type: {}", task.getTypeId());
                else {
                    Process process = processDao.getProcess(task.getProcessId());
                    if (process == null)
                        log.warn("Process is not found: {}", task.getProcessId());
                    else
                        try {
                            Map<String, Object> context = Expression.context(new SingleConnectionSet(con), DynActionForm.SYSTEM_FORM,
                                    null, process);
                            context.put("taskObject", task);
                            context.put("taskType", type);
                            Expression expression = new Expression(context);
                            expression.execute(type.getDoExpression());
                        } catch (Exception e) {
                            log.error("Task execution error: " + e.getMessage(), e);
                        }
                }

                task.setExecutedTime(new Date());
                taskDao.updateTask(task);
                con.commit();
            }
        } catch (Throwable e) {
            log.error(e);
        }
    }
}
