package ru.bgcrm.plugin.task;

import java.util.HashMap;
import java.util.Map;

import ru.bgcrm.plugin.task.model.TaskType;
import ru.bgcrm.util.ParameterMap;

public class Config extends ru.bgcrm.util.Config {
    private final Map<String, TaskType> typeMap = new HashMap<>();

    public Config(ParameterMap setup) {
        super(null);
        for (ParameterMap config : setup.subIndexed(Plugin.ID + ":type.").values()) {
            TaskType type = new TaskType(config);
            typeMap.put(type.getId(), type);
        }
    }

    public TaskType getType(String typeId) {
        return typeMap.get(typeId);
    }

}