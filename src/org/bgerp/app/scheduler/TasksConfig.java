package org.bgerp.app.scheduler;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.bgerp.app.bean.Bean;

import ru.bgcrm.model.IdStringTitle;
import ru.bgcrm.plugin.PluginManager;
import ru.bgcrm.util.Config;
import ru.bgcrm.util.ParameterMap;

public class TasksConfig extends Config {
    /** Configured to run tasks. */
    private final List<TaskConfig> taskConfigs;
    /** List of runnable classes, ID and titles are class names. */
    private volatile List<IdStringTitle> taskClasses;

    protected TasksConfig(ParameterMap config) {
        super(null);
        taskConfigs = loadTaskConfigs(config);
    }

    private List<TaskConfig> loadTaskConfigs(ParameterMap config) {
        var result = new ArrayList<TaskConfig>();

        log.info("Loading tasks config.");

        for (Map.Entry<String, ParameterMap> me : config.subKeyed("scheduler.task.").entrySet()) {
            String taskId = me.getKey();
            try {
                result.add(new TaskConfig(taskId, me.getValue()));
            } catch (Exception e) {
                log.error("Load task config: " + taskId + ", error: " + e.getMessage(), e);
            }
        }

        return Collections.unmodifiableList(result);
    }

    List<TaskConfig> taskConfigsToRun(ZonedDateTime time) {
        return taskConfigs.stream()
            .filter(tc -> tc.isEnabled() && tc.checkTime(time))
            .collect(Collectors.toList());
    }

    /**
     * @return configured to run tasks.
     */
    public List<TaskConfig> getTaskConfigs() {
        return taskConfigs;
    }

    /**
     * List of class names, extending {@link Task} in application {@link PluginManager#ERP_PACKAGES}.
     * @return
     */
    public List<IdStringTitle> getTaskClasses() {
        synchronized (this) {
            if (this.taskClasses == null) {
                var taskClasses = new ArrayList<IdStringTitle>(100);

                var r = Bean.classes();
                for (Class<? extends Task> taskClass : r.getSubTypesOf(Task.class)) {
                    var name = taskClass.getName();
                    taskClasses.add(new IdStringTitle(name, name));
                    log.debug("Found task class: {}", name);
                }

                Collections.sort(taskClasses, (c1, c2) -> c1.getId().compareTo(c2.getId()));

                this.taskClasses = Collections.unmodifiableList(taskClasses);
            }
        }
        return taskClasses;
    }
}