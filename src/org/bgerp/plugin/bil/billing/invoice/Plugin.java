package org.bgerp.plugin.bil.billing.invoice;

import java.sql.Connection;
import java.util.List;
import java.util.Map;

import org.bgerp.plugin.bil.billing.invoice.event.listener.Files;

import ru.bgcrm.plugin.Endpoint;

public class Plugin extends ru.bgcrm.plugin.Plugin {
    public static final String ID = "invoice";

    public static final String PATH_JSP_USER = PATH_JSP_USER_PLUGIN + "/" + ID;

    public Plugin() {
        super(ID);
    }

    @Override
    protected Map<String, List<String>> loadEndpoints() {
        return Map.of(Endpoint.USER_PROCESS_TABS, List.of(PATH_JSP_USER + "/process_tabs.jsp"));
    }

    @Override
    public void init(Connection con) throws Exception {
        super.init(con);
        new Files();
    }
}