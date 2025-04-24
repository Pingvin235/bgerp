package org.bgerp.plugin.git;

import java.sql.Connection;
import java.util.List;
import java.util.Map;

import org.bgerp.app.cfg.Setup;
import org.bgerp.app.event.EventProcessor;

import ru.bgcrm.event.ParamChangedEvent;
import ru.bgcrm.event.process.ProcessChangingEvent;
import ru.bgcrm.plugin.Endpoint;

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
        super.init(con);

        EventProcessor.subscribe((event, conSet) -> {
            var config = Setup.getSetup().getConfig(Config.class);
            if (config != null)
                config.paramChanged(event, conSet);
        }, ParamChangedEvent.class);

        EventProcessor.subscribe((event, conSet) -> {
            var config = Setup.getSetup().getConfig(Config.class);
            if (config != null)
                config.processChanging(event, conSet);
        }, ProcessChangingEvent.class);
    }

    @Override
    protected Map<String, List<String>> endpoints() {
        return Map.of(Endpoint.USER_PROCESS_TABS, List.of(PATH_JSP_USER + "/process_tabs.jsp"));
    }
}
