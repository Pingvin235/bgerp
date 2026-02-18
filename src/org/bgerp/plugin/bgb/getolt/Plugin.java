package org.bgerp.plugin.bgb.getolt;

import java.util.List;
import java.util.Map;

import ru.bgcrm.plugin.Endpoint;

/**
 * GetOLT plugin for ONU data display in process tabs.
 * Integrates with GetOLT External API to show ONU information
 * based on contract binding (cid/contractNumber).
 */
public class Plugin extends ru.bgcrm.plugin.Plugin {
    public static final String ID = "getolt";
    public static final Plugin INSTANCE = new Plugin();

    public static final String PATH_JSP_USER = PATH_JSP_USER_PLUGIN + "/" + ID;

    private Plugin() {
        super(ID);
    }

    @Override
    public String getTitle() {
        return "Get OLT";
    }

    @Override
    protected Map<String, List<String>> endpoints() {
        return Map.of(
            Endpoint.JS, List.of(Endpoint.getPathPluginJS(ID)),
            Endpoint.CSS, List.of(Endpoint.getPathPluginCSS(ID)),
            Endpoint.USER_PROCESS_TABS, List.of(PATH_JSP_USER + "/process_tabs.jsp")
        );
    }
}
