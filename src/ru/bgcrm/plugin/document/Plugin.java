package ru.bgcrm.plugin.document;

import java.util.Set;

import ru.bgcrm.struts.action.BaseAction;


public class Plugin extends ru.bgcrm.plugin.Plugin {
    public static final String ID = "document";

    public static final String PATH_JSP_USER = BaseAction.PATH_JSP_USER_PLUGIN + "/" + ID;

    public Plugin() {
        super(ID);
    }

    @Override
    public Set<String> getUnusedPaths() {
        return Set.of("docpattern");
    }
}
