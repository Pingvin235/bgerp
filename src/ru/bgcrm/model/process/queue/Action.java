package ru.bgcrm.model.process.queue;

import java.util.Set;

import org.bgerp.app.cfg.ConfigMap;

import ru.bgcrm.util.Utils;

public class Action {
    private final String title;
    private final String shortcut;
    private final String style;
    private final Set<Integer> statusIds;
    private final String commands;

    public Action(ConfigMap config) {
        title = config.get("title", "");
        shortcut = config.get("shortcut", "*");
        style = config.get("style", "");
        statusIds = Utils.toIntegerSet(config.get("statusIds"));
        commands = config.get("commands", "");
    }

    public String getTitle() {
        return title;
    }

    public String getShortcut() {
        return shortcut;
    }

    public String getStyle() {
        return style;
    }

    public Set<Integer> getStatusIds() {
        return statusIds;
    }

    public String getCommands() {
        return commands;
    }
}
