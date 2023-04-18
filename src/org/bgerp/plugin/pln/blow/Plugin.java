package org.bgerp.plugin.pln.blow;

import java.util.List;
import java.util.Map;

import ru.bgcrm.plugin.Endpoint;

public class Plugin extends ru.bgcrm.plugin.Plugin {
    public static final String ID = "blow";
    public static final Plugin INSTANCE = new Plugin();

    public static final String PATH_JSP_USER = PATH_JSP_USER_PLUGIN + "/" + ID;
    public static final String PATH_JSP_OPEN = PATH_JSP_OPEN_PLUGIN + "/" + ID;

    private Plugin() {
        super(ID);
    }

    @Override
    protected Map<String, List<String>> loadEndpoints() {
       return Map.of(
            Endpoint.JS, List.of(Endpoint.getPathPluginJS(ID)),
            Endpoint.CSS, List.of(Endpoint.getPathPluginCSS(ID)),
            Endpoint.USER_PROCESS_MENU_ITEMS, List.of(PATH_JSP_USER + "/menu_items.jsp"),
            Endpoint.OPEN_JSP, List.of(PATH_JSP_OPEN + "/url.jsp")
        );
    }
}
