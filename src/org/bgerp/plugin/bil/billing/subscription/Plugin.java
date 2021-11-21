package org.bgerp.plugin.bil.billing.subscription;

import java.sql.Connection;
import java.util.List;
import java.util.Map;

import org.bgerp.event.FilesEvent;

import ru.bgcrm.event.EventProcessor;
import ru.bgcrm.event.ParamChangedEvent;
import ru.bgcrm.plugin.Endpoint;
import ru.bgcrm.util.ParameterMap;
import ru.bgcrm.util.Setup;

/**
 * Subscription plugin.
 *
 * @author Shamil Vakhitov
 */
public class Plugin extends ru.bgcrm.plugin.Plugin {
    public static final String ID = "subscription";

    public static final String PATH_JSP_OPEN = PATH_JSP_OPEN_PLUGIN + "/" + ID;

    public Plugin() {
        super(ID);
    }

    @Override
    public boolean isEnabled(ParameterMap config, String defaultValue) {
        return true;
    }

    @Override
    protected Map<String, List<String>> loadEndpoints() {
        return Map.of(Endpoint.JS, List.of(Endpoint.getPathPluginJS(ID)));
    }

    @Override
    public void init(Connection con) throws Exception {
        super.init(con);

        EventProcessor.subscribe((e, conSet) -> {
            Setup.getSetup().getConfig(Config.class).paramChanged(e, conSet);
        }, ParamChangedEvent.class);

        EventProcessor.subscribe((e, conSet) -> {
            Setup.getSetup().getConfig(Config.class).files(e, conSet);
        }, FilesEvent.class);

        EventProcessor.subscribe((e, conSet) -> {
            Setup.getSetup().getConfig(Config.class).file(e, conSet);
        }, FilesEvent.Get.class);
    }
}
