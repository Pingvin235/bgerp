package org.bgerp.plugin.msg.email;

import java.sql.Connection;
import java.util.List;
import java.util.Map;

import org.bgerp.plugin.msg.email.event.listener.ProcessNotificationListener;

import ru.bgcrm.dao.expression.Expression.ContextInitEvent;
import ru.bgcrm.event.EventProcessor;

public class Plugin extends ru.bgcrm.plugin.Plugin {
    public static final String ID = "email";
    public static final Plugin INSTANCE = new Plugin();

    public static final String PATH_JSP_USER = PATH_JSP_USER_PLUGIN + "/" + ID;

    static final String ENDPOINT_MESSAGE_VIEWER = PATH_JSP_USER + "/message_viewer.jsp";
    static final String ENDPOINT_MESSAGE_HEADER = PATH_JSP_USER + "/process_message_header.jsp";
    static final String ENDPOINT_MESSAGE_EDITOR = PATH_JSP_USER + "/process_message_editor.jsp";

    private Plugin() {
        super(ID);
    }

    @Override
    public String getTitle() {
        return "E-Mail";
    }

    @Override
    protected Map<String, List<String>> loadEndpoints() {
        return Map.of(
            ENDPOINT_MESSAGE_VIEWER, List.of(ENDPOINT_MESSAGE_VIEWER),
            ENDPOINT_MESSAGE_HEADER, List.of(ENDPOINT_MESSAGE_HEADER),
            ENDPOINT_MESSAGE_EDITOR, List.of(ENDPOINT_MESSAGE_EDITOR),
            "user.process.message.attachments.jsp", List.of(PATH_JSP_USER + "/process_message_attachments.jsp"),
            "user.profile.options.jsp", List.of(PATH_JSP_USER + "/profile_options.jsp")
        );
    }

    @Override
    public void init(Connection con) throws Exception {
        new ProcessNotificationListener();

        EventProcessor.subscribe((e, conSet) -> {
            e.getContext().put(ID, new ExpressionObject());
        }, ContextInitEvent.class);
    }
}