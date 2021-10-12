package ru.bgcrm.plugin.task;

import java.sql.Connection;
import java.util.List;
import java.util.Map;

import ru.bgcrm.dao.expression.Expression.ContextInitEvent;
import ru.bgcrm.event.EventProcessor;
import ru.bgcrm.plugin.Endpoint;

public class Plugin extends ru.bgcrm.plugin.Plugin {
    public static final String ID = "task";

    public static final String PATH_JSP_USER = PATH_JSP_USER_PLUGIN + "/" + ID;

    public Plugin() {
        super(ID);
    }

    @Override
    public void init(Connection con) throws Exception {
        super.init(con);

        EventProcessor.subscribe((e, conSet) -> {
            e.getContext().put(ID, new ExpressionBean());
        }, ContextInitEvent.class);
    }

    @Override
    protected Map<String, List<String>> loadEndpoints() {
        return Map.of(Endpoint.USER_PROCESS_TABS, List.of(PATH_JSP_USER + "/process_tabs.jsp"));
    }
}
