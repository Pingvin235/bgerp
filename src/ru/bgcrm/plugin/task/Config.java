package ru.bgcrm.plugin.task;

import java.util.HashMap;
import java.util.Map;

import org.bgerp.app.cfg.ConfigMap;

import ru.bgcrm.plugin.task.model.TaskType;

public class Config extends org.bgerp.app.cfg.Config {
    private final Map<String, TaskType> typeMap = new HashMap<>();

    public Config(ConfigMap setup) {
        super(null);
        for (ConfigMap config : setup.subIndexed(Plugin.ID + ":type.").values()) {
            TaskType type = new TaskType(config);
            typeMap.put(type.getId(), type);
        }
    }

    public TaskType getType(String typeId) {
        return typeMap.get(typeId);
    }

}