package org.bgerp.plugin.sec.access;

import java.util.List;
import java.util.Map;

import ru.bgcrm.plugin.Endpoint;

public class Plugin extends ru.bgcrm.plugin.Plugin {
    public static final String ID = "access";
    public static final Plugin INSTANCE = new Plugin();

    private static final String PATH_JSP_ADMIN = PATH_JSP_ADMIN_PLUGIN + "/" + ID;

    private Plugin() {
        super(ID);
    }

    @Override
    protected Map<String, List<String>> loadEndpoints() {
       return Map.of(
            Endpoint.JS, List.of(Endpoint.getPathPluginJS(ID)),
            "admin.user.action.jsp", List.of(PATH_JSP_ADMIN + "/user_action.jsp")
       );
    }
}
