package ru.bgcrm.plugin.bgbilling;

import static java.util.Map.entry;

import java.sql.Connection;
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
import ru.bgcrm.plugin.bgbilling.proto.model.Contract;
import ru.bgcrm.util.sql.ConnectionSet;

public class Plugin extends ru.bgcrm.plugin.Plugin {
    public static final String ID = "bgbilling";

    public static final String PATH_JSP_USER = PATH_JSP_USER_PLUGIN + "/" + ID;

    public static final String ENDPOINT_MESSAGE_HEADER = PATH_JSP_USER + "/helpdesk/process_message_header.jsp";

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
    }

    @Override
    protected Map<String, List<String>> loadEndpoints() {
        // TODO: Remove the commented block later after testing.
        /* <endpoint id="user.customer.tabs.jsp" file="/WEB-INF/jspf/user/plugin/bgbilling/customer_tabs.jsp"/>
        <endpoint id="user.search.jsp" file="/WEB-INF/jspf/user/plugin/bgbilling/search.jsp"/>
        <endpoint id="user.url.jsp" file="/WEB-INF/jspf/user/plugin/bgbilling/url.jsp"/>

        <endpoint id="user.process.tabs.jsp" file="/WEB-INF/jspf/user/plugin/bgbilling/process_tabs.jsp"/>
        <endpoint id="user.process.link.list.jsp" file="/WEB-INF/jspf/user/plugin/bgbilling/process_link_list.jsp"/>
        <endpoint id="user.process.linked.list.jsp" file="/WEB-INF/jspf/user/plugin/bgbilling/process_linked_list.jsp"/>
        <endpoint id="user.process.linkForAdd.list.jsp" file="/WEB-INF/jspf/user/plugin/bgbilling/process_link_for_add_list.jsp"/>
        <endpoint id="user.process.linkForAddCustom.jsp" file="/WEB-INF/jspf/user/plugin/bgbilling/process_link_for_add_custom_list.jsp"/>

        <endpoint id="js.init" file="/WEB-INF/jspf/user/plugin/bgbilling/js_init.jsp"/>
        <endpoint id="user.process.message.attaches.jsp" file="/WEB-INF/jspf/user/plugin/bgbilling/helpdesk/process_message_attaches.jsp"/>
        <endpoint id="user.message.search.result.jsp" file="/WEB-INF/jspf/user/plugin/bgbilling/message_search_result.jsp"/>
        <endpoint id="user.profile.options.jsp" file="/WEB-INF/jspf/user/plugin/bgbilling/profile_options.jsp"/> */
        return Map.ofEntries(
            entry(Endpoint.JS, List.of(Endpoint.getPathPluginJS(ID))),
            entry(Endpoint.CSS, List.of(Endpoint.getPathPluginCSS(ID))),
            entry("user.customer.tabs.jsp", List.of(PATH_JSP_USER + "/customer_tabs.jsp")),
            entry("user.search.jsp", List.of(PATH_JSP_USER + "/search.jsp")),
            entry("user.url.jsp", List.of(PATH_JSP_USER + "/url.jsp")),
            entry(Endpoint.USER_PROCESS_TABS, List.of(PATH_JSP_USER + "/process_tabs.jsp")),
            entry("user.process.link.list.jsp", List.of(PATH_JSP_USER + "/process_link_list.jsp")),
            entry("user.process.linked.list.jsp", List.of(PATH_JSP_USER + "/process_linked_list.jsp")),
            entry("user.process.linkForAdd.list.jsp", List.of(PATH_JSP_USER + "/process_link_for_add_list.jsp")),
            entry("user.process.linkForAddCustom.jsp", List.of(PATH_JSP_USER + "/process_link_for_add_custom_list.jsp")),
            entry("js.init", List.of(PATH_JSP_USER + "/js_init.jsp")),
            entry("user.process.message.attaches.jsp", List.of(PATH_JSP_USER + "/helpdesk/process_message_attaches.jsp")),
            entry("user.message.search.result.jsp", List.of(PATH_JSP_USER + "/message_search_result.jsp")),
            entry("user.profile.options.jsp", List.of(PATH_JSP_USER + "/profile_options.jsp")),
            entry(ENDPOINT_MESSAGE_HEADER, List.of(ENDPOINT_MESSAGE_HEADER))
        );
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
