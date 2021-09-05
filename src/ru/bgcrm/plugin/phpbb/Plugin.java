package ru.bgcrm.plugin.phpbb;

import java.util.Set;

import ru.bgcrm.plugin.phpbb.model.Topic;
import ru.bgcrm.struts.action.BaseAction;

public class Plugin extends ru.bgcrm.plugin.Plugin {
    public static final String ID = "phpbb";

    public static final String PATH_JSP_USER = BaseAction.PATH_JSP_USER_PLUGIN + "/" + ID;

    public Plugin() {
        super(ID);
    }

    @Override
    public Set<String> getObjectTypes() {
        return Set.of(Topic.OBJECT_TYPE_PREFIX);
    }

    
}
