package ru.bgcrm.plugin.bgbilling;

import java.sql.Connection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ru.bgcrm.event.EventProcessor;
import ru.bgcrm.event.SetupChangedEvent;
import ru.bgcrm.event.listener.EventListener;
import ru.bgcrm.event.user.UserChangedEvent;
import ru.bgcrm.plugin.Endpoint;
import ru.bgcrm.plugin.bgbilling.event.listener.HelpDeskListener;
import ru.bgcrm.plugin.bgbilling.event.listener.LinkChangedListener;
import ru.bgcrm.plugin.bgbilling.event.listener.LinkChangingListener;
import ru.bgcrm.plugin.bgbilling.event.listener.ProcessDoActionListener;
import ru.bgcrm.plugin.bgbilling.event.listener.RegisterExtensionListener;
import ru.bgcrm.plugin.bgbilling.proto.model.Contract;
import ru.bgcrm.util.sql.ConnectionSet;

public class Plugin extends ru.bgcrm.plugin.Plugin {
    public static final String ID = "bgbilling";

    public Plugin() {
        super(ID);
    }

    @Override
    public void init(Connection con) throws Exception {
        super.init(con);

        EventProcessor.subscribe(new EventListener<SetupChangedEvent>() {
            @Override
            public void notify(SetupChangedEvent e, ConnectionSet connectionSet) {
                DBInfoManager.flush();
            }
        }, SetupChangedEvent.class);

        EventProcessor.subscribe(new EventListener<UserChangedEvent>() {
            @Override
            public void notify(UserChangedEvent e, ConnectionSet connectionSet) {
                DBInfoManager.flush();
            }
        }, UserChangedEvent.class);

        new LinkChangingListener();

        new LinkChangedListener();

        new HelpDeskListener();

        new ProcessDoActionListener();

        // регистрация функций - расширений для XSLT генерации документов только если стоит плагин Document
        try {
            Class.forName("ru.bgcrm.plugin.document.Plugin");
            new RegisterExtensionListener();
        } catch (ClassNotFoundException e) {}
    }

    @Override
    protected Map<String, List<String>> loadEndpoints() {
        var result = new HashMap<>(super.loadEndpoints());
        result.putAll(Map.of(
            Endpoint.JS, List.of(Endpoint.getPathPluginJS(ID)),
            Endpoint.CSS, List.of(Endpoint.getPathPluginCSS(ID))
        ));
        return Collections.unmodifiableMap(result);
    }

    /**
     * Actively used in JSP pages.
     * @return
     */
    public DBInfoManager getDbInfoManager() {
        return DBInfoManager.getInstance();
    }

    @Override
    public Set<String> getObjectTypes() {
        return Set.of(Contract.OBJECT_TYPE);
    }
}
