package ru.bgcrm.plugin.phpbb;

import java.util.Set;

import ru.bgcrm.plugin.phpbb.model.Topic;

public class Plugin extends ru.bgcrm.plugin.Plugin {
    public static final String ID = "phpbb";

    public Plugin() {
        super(ID);
    }

    @Override
    public Set<String> getObjectTypes() {
        return Set.of(Topic.OBJECT_TYPE_PREFIX);
    }
}
