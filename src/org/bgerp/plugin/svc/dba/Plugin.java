package org.bgerp.plugin.svc.dba;

import java.util.List;
import java.util.Map;

import ru.bgcrm.plugin.Endpoint;

public class Plugin extends ru.bgcrm.plugin.Plugin {
    public static final String ID = "dba";
    public static final Plugin INSTANCE = new Plugin();

    public static final String PATH_JSP_ADMIN = PATH_JSP_ADMIN_PLUGIN + "/" + ID;

    private Plugin() {
        super(ID);
    }

    @Override
    public String getTitle() {
        return "DBA";
    }

    @Override
    protected Map<String, List<String>> loadEndpoints() {
        return Map.of(
            Endpoint.JS, List.of(Endpoint.getPathPluginJS(ID)),
            Endpoint.USER_ADMIN_MENU_ITEMS, List.of(PATH_JSP_ADMIN + "/menu_items.jsp")
        );
    }
}
