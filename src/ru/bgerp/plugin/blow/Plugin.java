package ru.bgerp.plugin.blow;

import java.util.Map;
import java.util.Set;

import ru.bgcrm.plugin.Endpoint;
public class Plugin extends ru.bgcrm.plugin.Plugin {
    public static final String ID = "blow";

    public static final String PATH_JSP_USER = PATH_JSP_USER_PLUGIN + "/" + ID;
    public static final String PATH_JSP_OPEN = PATH_JSP_OPEN_PLUGIN + "/" + ID;

    public Plugin() {
        super(ID);
    }

    @Override
    protected Map<String, String> loadEndpoints() {
       return Map.of(
            Endpoint.JS, Endpoint.getPathPluginJS(ID),
            Endpoint.CSS, Endpoint.getPathPluginCSS(ID),
            Endpoint.USER_MENU_ITEMS, PATH_JSP_USER + "/menu_items.jsp",
            Endpoint.OPEN_JSP, PATH_JSP_OPEN + "/url.jsp"
        );
    }

    @Override
    public Set<String> getOwnedPaths() {
        return Set.of(
            PATH_WEBAPP + "/" + PATH_JSP_USER, PATH_WEBAPP + "/" + PATH_JSP_OPEN
        );
    }
}
