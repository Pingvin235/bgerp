package org.bgerp.plugin.git;

import java.sql.Connection;
import java.util.List;
import java.util.Map;

import ru.bgcrm.event.EventProcessor;
import ru.bgcrm.event.ParamChangingEvent;
import ru.bgcrm.event.process.ProcessChangingEvent;
import ru.bgcrm.plugin.Endpoint;
import ru.bgcrm.util.Setup;

public class Plugin extends ru.bgcrm.plugin.Plugin {
    public static final String ID = "git";
    public static final Plugin INSTANCE = new Plugin();

    public static final String PATH_JSP_USER = PATH_JSP_USER_PLUGIN + "/" + ID;

    private Plugin() {
        super(ID);
    }

    @Override
    public String getTitle() {
        return "GIT";
    }

    @Override
    public void init(Connection con) throws Exception {
        EventProcessor.subscribe((event, conSet) -> {
            var config = Setup.getSetup().getConfig(Config.class);
            if (config != null)
                config.paramChanging(event, conSet);
        }, ParamChangingEvent.class);

        EventProcessor.subscribe((event, conSet) -> {
            var config = Setup.getSetup().getConfig(Config.class);
            if (config != null)
                config.processChanging(event, conSet);
        }, ProcessChangingEvent.class);
    }

    @Override
    protected Map<String, List<String>> loadEndpoints() {
        return Map.of(Endpoint.USER_PROCESS_TABS, List.of(PATH_JSP_USER + "/process_tabs.jsp"));
    }
}
