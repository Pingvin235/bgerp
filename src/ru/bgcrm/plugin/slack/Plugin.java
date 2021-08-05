package ru.bgcrm.plugin.slack;

import java.sql.Connection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ru.bgcrm.event.EventProcessor;
import ru.bgcrm.event.listener.DefaultProcessChangeListener.DefaultProcessorChangeContextEvent;
import ru.bgcrm.plugin.Endpoint;
import ru.bgcrm.struts.action.BaseAction;

public class Plugin extends ru.bgcrm.plugin.Plugin {
    public static final String ID = "slack";

    public static final String LINK_TYPE_CHANNEL = "slack-channel";

    private static final String PATH_JSP_USER = BaseAction.PATH_JSP_USER_PLUGIN + "/" + ID;
    private static final String PATH_JSP_ADMIN = BaseAction.PATH_JSP_ADMIN_PLUGIN + "/" + ID;

    public Plugin() {
        super(ID);
    }

    @Override
    public void init(Connection con) throws Exception {
        super.init(con);

        EventProcessor.subscribe((e, conSet) -> {
            e.getContext().put(ID, new DefaultProcessorFunctions());
        }, DefaultProcessorChangeContextEvent.class);
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
            "user.process.message.header.jsp", List.of(PATH_JSP_USER + "/process_link_list.jsp")
        );
    }
}
