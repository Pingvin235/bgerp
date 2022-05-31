package org.bgerp.scheduler;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.reflections.Reflections;

import ru.bgcrm.model.IdStringTitle;
import ru.bgcrm.plugin.PluginManager;
import ru.bgcrm.util.Config;
import ru.bgcrm.util.ParameterMap;

public class TasksConfig extends Config {
    private List<TaskConfig> tasks = new ArrayList<TaskConfig>();
    /** List of runnable classes, ID and titles are class names. */
    private List<IdStringTitle> runnableClasses;

    protected TasksConfig(ParameterMap config) {
        super(null);

        log.info("Reload tasks config.");

        for (Map.Entry<String, ParameterMap> me : config.subKeyed("scheduler.task.").entrySet()) {
            String taskId = me.getKey();
            try {
                tasks.add(new TaskConfig(me.getValue()));
            } catch (Exception e) {
                log.error("Load task config: " + taskId + ", error: " + e.getMessage(), e);
            }
        }
    }

    List<Runnable> getTasksToRun(Calendar time) {
        List<Runnable> result = new ArrayList<Runnable>();

        for (TaskConfig config : tasks) {
            if (!config.isEnabled() || !config.checkTime(time))
                continue;

            try {
                result.add(config.taskInstance());
            } catch (Exception e) {
                log.error("Error create task instance: " + e.getMessage(), e);
            }
        }

        return result;
    }

    /**
     * List of class names, extending {@link Task} in application {@link PluginManager#ERP_PACKAGES}.
     * @return
     */
    public List<IdStringTitle> getRunnableClasses() {
        synchronized (this) {
            if (this.runnableClasses == null) {
                var runnableClasses = new ArrayList<IdStringTitle>(100);

                var r = new Reflections(PluginManager.ERP_PACKAGES);
                for (Class<? extends Task> taskClass : r.getSubTypesOf(Task.class)) {
                    var name = taskClass.getName();
                    runnableClasses.add(new IdStringTitle(name, name));
                    log.debug("Found task class: {}", name);
                }

                Collections.sort(runnableClasses, (c1, c2) -> c1.getId().compareTo(c2.getId()));

                this.runnableClasses = Collections.unmodifiableList(runnableClasses);
            }
        }
        return runnableClasses;
    }
}