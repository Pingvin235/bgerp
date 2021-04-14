package org.bgerp.plugin.report;

import java.util.List;
import java.util.Map;

import ru.bgcrm.plugin.Endpoint;
import ru.bgcrm.struts.action.BaseAction;

public class Plugin extends ru.bgcrm.plugin.Plugin {
    public static final String ID = "report";

    public static final String PATH_JSP_USER = BaseAction.PATH_JSP_USER_PLUGIN + "/" + ID;

    public Plugin() {
        super(ID);
    }

    @Override
    protected Map<String, List<String>> loadEndpoints() {
       return Map.of(
            Endpoint.JS, List.of(
                Endpoint.getPathPluginJS(ID),
                PATH_LIB + "/echarts-5.0.2/echarts.js"
            ),
            Endpoint.USER_MENU_ITEMS, List.of(PATH_JSP_USER + "/menu_items.jsp")
       );
    }
}