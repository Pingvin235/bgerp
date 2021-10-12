package ru.bgcrm.plugin.slack;

import java.sql.Connection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ru.bgcrm.dao.expression.Expression.ContextInitEvent;
import ru.bgcrm.event.EventProcessor;
import ru.bgcrm.plugin.Endpoint;

public class Plugin extends ru.bgcrm.plugin.Plugin {
    public static final String ID = "slack";

    public static final String LINK_TYPE_CHANNEL = "slack-channel";

    public static final String PATH_JSP_USER = PATH_JSP_USER_PLUGIN + "/" + ID;
    public static final String PATH_JSP_ADMIN = PATH_JSP_ADMIN_PLUGIN + "/" + ID;

    public static final String ENDPOINT_MESSAGE_HEADER = PATH_JSP_USER + "/process_link_list.jsp";

    public Plugin() {
        super(ID);
    }

    @Override
    public void init(Connection con) throws Exception {
        super.init(con);

        EventProcessor.subscribe((e, conSet) -> {
            e.getContext().put(ID, new DefaultProcessorFunctions());
        }, ContextInitEvent.class);
    }

    @Override
    public Set<String> getObjectTypes() {
        return Set.of(LINK_TYPE_CHANNEL);
    }

    @Override
    protected Map<String, List<String>> loadEndpoints() {
        return Map.of(
            Endpoint.USER_ADMIN_MENU_ITEMS, List.of(PATH_JSP_ADMIN + "/menu_items.jsp"),
            "user.process.link.list.jsp", List.of(PATH_JSP_USER + "/process_link_list.jsp"),
            "user.process.linkForAddCustom.jsp", List.of(PATH_JSP_USER + "/process_link_for_add_custom_list.jsp"),
            ENDPOINT_MESSAGE_HEADER, List.of(ENDPOINT_MESSAGE_HEADER)
        );
    }
}
