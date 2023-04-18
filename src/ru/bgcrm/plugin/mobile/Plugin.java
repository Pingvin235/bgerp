package ru.bgcrm.plugin.mobile;

import ru.bgcrm.event.EventProcessor;
import ru.bgcrm.dao.expression.Expression.ContextInitEvent;
import ru.bgcrm.util.Setup;

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
