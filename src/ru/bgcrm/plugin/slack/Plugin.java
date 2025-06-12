package ru.bgcrm.plugin.slack;

import java.sql.Connection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bgerp.app.event.EventProcessor;
import org.bgerp.dao.expression.Expression.ContextInitEvent;

import ru.bgcrm.plugin.Endpoint;

public class Plugin extends ru.bgcrm.plugin.Plugin {
    public static final String ID = "slack";
    public static final Plugin INSTANCE = new Plugin();

    public static final String LINK_TYPE_CHANNEL = "slack-channel";

    public static final String PATH_JSP_USER = PATH_JSP_USER_PLUGIN + "/" + ID;
    public static final String PATH_JSP_ADMIN = PATH_JSP_ADMIN_PLUGIN + "/" + ID;

    public static final String ENDPOINT_MESSAGE_HEADER = PATH_JSP_USER + "/process_link_list.jsp";

    private Plugin() {
        super(ID);
    }

    @Override
    public Set<String> getObjectTypes() {
        return Set.of(LINK_TYPE_CHANNEL);
    }

    @Override
    public void init(Connection con) throws Exception {
        super.init(con);

        EventProcessor.subscribe((e, conSet) -> {
            new ExpressionObject().toContext(e.getContext());
        }, ContextInitEvent.class);
    }

    @Override
    protected Map<String, List<String>> endpoints() {
        return Map.of(
            Endpoint.USER_ADMIN_MENU_ITEMS, List.of(PATH_JSP_ADMIN + "/menu_items.jsp"),
            "user.process.link.list.jsp", List.of(PATH_JSP_USER + "/process_link_list.jsp"),
            "user.process.linkForAddCustom.jsp", List.of(PATH_JSP_USER + "/process_link_for_add_custom_list.jsp"),
            ENDPOINT_MESSAGE_HEADER, List.of(ENDPOINT_MESSAGE_HEADER)
        );
    }
}
