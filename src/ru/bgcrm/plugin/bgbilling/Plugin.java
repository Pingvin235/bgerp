package ru.bgcrm.plugin.bgbilling;

import static java.util.Map.entry;

import java.sql.Connection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bgerp.app.event.EventProcessor;
import org.bgerp.dao.expression.Expression.ContextInitEvent;
import org.bgerp.util.Dynamic;

import ru.bgcrm.event.SetupChangedEvent;
import ru.bgcrm.event.user.UserChangedEvent;
import ru.bgcrm.plugin.Endpoint;
import ru.bgcrm.plugin.bgbilling.event.listener.HelpDeskListener;
import ru.bgcrm.plugin.bgbilling.event.listener.LinkChangedListener;
import ru.bgcrm.plugin.bgbilling.event.listener.LinkChangingListener;
import ru.bgcrm.plugin.bgbilling.event.listener.ProcessDoActionListener;
import ru.bgcrm.plugin.bgbilling.proto.model.Contract;

public class Plugin extends ru.bgcrm.plugin.Plugin {
    public static final String ID = "bgbilling";
    public static final Plugin INSTANCE = new Plugin();

    public static final String PATH_JSP_USER = PATH_JSP_USER_PLUGIN + "/" + ID;

    public static final String ENDPOINT_HD_MESSAGE_HEADER = PATH_JSP_USER + "/helpdesk/process_message_header.jsp";
    public static final String ENDPOINT_HD_MESSAGE_EDITOR = PATH_JSP_USER + "/helpdesk/process_message_editor.jsp";

    private Plugin() {
        super(ID);
    }

    @Override
    public String getTitle() {
        return "BGBilling";
    }

    /**
     * @return BGBilling servers.
     */
    @Dynamic
    public DBInfoManager getDbInfoManager() {
        return DBInfoManager.getInstance();
    }

    @Override
    public Set<String> getObjectTypes() {
        return Set.of(Contract.OBJECT_TYPE);
    }

    @Override
    public void init(Connection con) throws Exception {
        super.init(con);

        EventProcessor.subscribe((e, conSet) -> {
            DBInfoManager.flush();
        }, SetupChangedEvent.class);

        EventProcessor.subscribe((e, conSet) -> {
            DBInfoManager.flush();
        }, UserChangedEvent.class);

        new LinkChangingListener();

        new LinkChangedListener();

        new HelpDeskListener();

        new ProcessDoActionListener();

        EventProcessor.subscribe((e, conSet) -> {
            new ExpressionObject().toContext(e.getContext());
        }, ContextInitEvent.class);
    }

    @Override
    protected Map<String, List<String>> endpoints() {
        return Map.ofEntries(
            entry(Endpoint.JS, List.of(Endpoint.getPathPluginJS(ID), Plugin.PATH_JS + "/pl." + ID + ".inet.js")),
            entry(Endpoint.CSS, List.of(Endpoint.getPathPluginCSS(ID))),
            entry("user.customer.tabs.jsp", List.of(PATH_JSP_USER + "/customer_tabs.jsp")),
            entry("user.search.jsp", List.of(PATH_JSP_USER + "/search.jsp")),
            entry("user.url.jsp", List.of(PATH_JSP_USER + "/url.jsp")),
            entry("user.process.link.list.jsp", List.of(PATH_JSP_USER + "/process_link_list.jsp")),
            entry("user.process.linked.list.jsp", List.of(PATH_JSP_USER + "/process_linked_list.jsp")),
            entry("user.process.linkForAdd.list.jsp", List.of(PATH_JSP_USER + "/process_link_for_add_list.jsp")),
            entry("user.process.linkForAddCustom.jsp", List.of(PATH_JSP_USER + "/process_link_for_add_custom_list.jsp")),
            entry("js.init", List.of(PATH_JSP_USER + "/js_init.jsp")),
            entry("user.message.search.result.jsp", List.of(PATH_JSP_USER + "/message_search_result.jsp")),
            entry(ENDPOINT_HD_MESSAGE_HEADER, List.of(ENDPOINT_HD_MESSAGE_HEADER)),
            entry("user.process.message.attachments.jsp", List.of(PATH_JSP_USER + "/helpdesk/process_message_attachments.jsp")),
            entry(ENDPOINT_HD_MESSAGE_EDITOR, List.of(ENDPOINT_HD_MESSAGE_EDITOR))
        );
    }
}
