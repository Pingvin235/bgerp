package ru.bgcrm.plugin.mobile;

import org.bgerp.app.cfg.Setup;
import org.bgerp.app.event.EventProcessor;

import ru.bgcrm.dao.expression.Expression.ContextInitEvent;

public class Plugin extends ru.bgcrm.plugin.Plugin {
    public static final String ID = "mobile";
    public static final Plugin INSTANCE = new Plugin();

    public static String getServerId() {
        return Setup.getSetup().get(Plugin.ID + ":serverId", "");
    }

    private Plugin() {
        super(ID);

        EventProcessor.subscribe((e, conSet) ->
            e.getContext().put(ID, new ExpressionObject()),
        ContextInitEvent.class);
    }
}
