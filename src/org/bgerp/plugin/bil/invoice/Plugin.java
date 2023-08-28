package org.bgerp.plugin.bil.invoice;

import java.sql.Connection;
import java.util.List;
import java.util.Map;

import org.bgerp.plugin.bil.invoice.event.listener.FilesListener;

import ru.bgcrm.plugin.Endpoint;

public class Plugin extends ru.bgcrm.plugin.Plugin {
    public static final String ID = "invoice";
    public static final Plugin INSTANCE = new Plugin();

    public static final String PATH_JSP_USER = PATH_JSP_USER_PLUGIN + "/" + ID;

    private Plugin() {
        super(ID);
    }

    @Override
    protected Map<String, List<String>> loadEndpoints() {
        return Map.of(
            Endpoint.JS, List.of(Endpoint.getPathPluginJS(ID)),
            Endpoint.USER_PROCESS_TABS, List.of(PATH_JSP_USER + "/process_tabs.jsp")
        );
    }

    @Override
    public void init(Connection con) throws Exception {
        super.init(con);
        new FilesListener();
    }
}
