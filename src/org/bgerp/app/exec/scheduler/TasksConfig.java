package org.bgerp.app.exec.scheduler;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.bgerp.app.cfg.Config;
import org.bgerp.app.cfg.ConfigMap;

import javassist.NotFoundException;

public class TasksConfig extends Config {
    /** Configured to run tasks. */
    private final List<TaskConfig> taskConfigs;

    protected TasksConfig(ConfigMap config) {
        super(null);
        taskConfigs = loadTaskConfigs(config);
    }

    private List<TaskConfig> loadTaskConfigs(ConfigMap config) {
        var result = new ArrayList<TaskConfig>();

        log.info("Loading tasks config.");

        for (Map.Entry<String, ConfigMap> me : config.subKeyed("scheduler.task.").entrySet()) {
            String taskId = me.getKey();
            try {
                result.add(new TaskConfig(taskId, me.getValue()));
            } catch (Throwable e) {
                log.error("Load task config: " + taskId + ", error: " + e.getMessage(), e);
            }
        }

        result.sort((tc1, tc2) -> {
            final String kernelPrefix = "Kernel";

            final String title1 = tc1.getTitle();
            final String title2 = tc2.getTitle();

            if (title1.startsWith(kernelPrefix) && !title2.startsWith(kernelPrefix))
                return -1;
            else if (!title1.startsWith(kernelPrefix) && title2.startsWith(kernelPrefix))
                return 1;
            else
                return title1.compareTo(title2);
        });

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
     * Gets a task configuration by ID.
     * @param id the ID.
     * @return a first found configuration.
     * @throws NotFoundException
     */
    public TaskConfig getTaskConfigOrThrow(String id) throws NotFoundException {
        return taskConfigs.stream()
            .filter(tc -> id.equals(tc.getId()))
            .findFirst()
            .orElseThrow(() -> new NotFoundException("Task configuration not found with ID: " + id));
    }
}