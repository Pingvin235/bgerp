package ru.bgerp.plugin.workload;

import java.util.List;
import java.util.Map;

import ru.bgcrm.plugin.Endpoint;

public class Plugin extends ru.bgcrm.plugin.Plugin {
    public static final String ID = "workload";

    private static final String PATH_JSP_USER = PATH_JSP_USER_PLUGIN + "/" + ID;

    public Plugin() {
        super(ID);
    }

    @Override
    protected Map<String, List<String>> loadEndpoints() {
        return Map.of(
            Endpoint.USER_PROCESS_TABS, List.of(PATH_JSP_USER + "/process_tabs.jsp")
        );
    }
}
