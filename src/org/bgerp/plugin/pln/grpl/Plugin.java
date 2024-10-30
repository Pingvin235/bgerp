package org.bgerp.plugin.pln.grpl;

import java.sql.Connection;
import java.util.List;
import java.util.Map;

import org.bgerp.app.cfg.Setup;
import org.bgerp.util.Dynamic;

import ru.bgcrm.plugin.Endpoint;

public class Plugin extends ru.bgcrm.plugin.Plugin {
    public static final String ID = "grpl";
    public static final Plugin INSTANCE = new Plugin();

    public static final String PATH_JSP_USER = PATH_JSP_USER_PLUGIN + "/" + ID;

    private Plugin() {
        super(ID);
    }

    @Override
    public String getTitle() {
        return "Group Plan";
    }

    @Override
    protected Map<String, List<String>> loadEndpoints() {
       return Map.of(
            Endpoint.JS, List.of(Endpoint.getPathPluginJS(ID)),
            Endpoint.CSS, List.of(Endpoint.getPathPluginCSS(ID)),
            Endpoint.USER_PROCESS_MENU_ITEMS, List.of(PATH_JSP_USER + "/menu_items.jsp"),
            Endpoint.USER_PROCESS_TABS, List.of(PATH_JSP_USER + "/process_tabs.jsp")
        );
    }

    @Override
    public void init(Connection con) throws Exception {
        super.init(con);

        /* EventProcessor.subscribe((e, conSet) -> {
            if (!e.isStatus())
                return;

            Process process = e.getProcess();

            var typeConfig = process.getType().getProperties().getConfigMap().getConfig(ProcessTypeConfig.class);
            if (typeConfig == null)
                return;

            log.debug("Agree status changing for process {}", process.getId());

            typeConfig.statusChanged(e, conSet, process);
        }, ProcessChangedEvent.class, 1); */
    }

    @Dynamic
    public Config getConfig(Setup setup) {
        return setup.getConfig(Config.class);
    }
}