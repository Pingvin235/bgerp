package org.bgerp.plugin.clb.team;

import java.util.List;
import java.util.Map;

import ru.bgcrm.plugin.Endpoint;

public class Plugin extends ru.bgcrm.plugin.Plugin {
    public static final String ID = "team";

    public static final String PATH_JSP_OPEN = PATH_JSP_OPEN_PLUGIN + "/" + ID;

    public Plugin() {
        super(ID);
    }

    @Override
    protected Map<String, List<String>> loadEndpoints() {
       return Map.of(
            Endpoint.OPEN_JSP, List.of(PATH_JSP_OPEN + "/url.jsp")
        );
    }
}
