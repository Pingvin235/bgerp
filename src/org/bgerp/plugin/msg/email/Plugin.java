package org.bgerp.plugin.msg.email;

import java.util.List;
import java.util.Map;

import ru.bgcrm.util.ParameterMap;

public class Plugin extends ru.bgcrm.plugin.Plugin {
    public static final String ID = "email";

    public static final String PATH_JSP_USER = PATH_JSP_USER_PLUGIN + "/" + ID;

    static final String ENDPOINT_MESSAGE_VIEWER = PATH_JSP_USER + "/message_viewer.jsp";
    static final String ENDPOINT_MESSAGE_HEADER = PATH_JSP_USER + "/process_message_header.jsp";
    static final String ENDPOINT_MESSAGE_EDITOR = PATH_JSP_USER + "/process_message_editor.jsp";

    public Plugin() {
        super(ID);
    }

    @Override
    public boolean isEnabled(ParameterMap config, String defaultValue) {
        // TODO: Always enabled for now.
        return true;
    }

    @Override
    protected Map<String, List<String>> loadEndpoints() {
        return Map.of(
            ENDPOINT_MESSAGE_VIEWER, List.of(ENDPOINT_MESSAGE_VIEWER),
            ENDPOINT_MESSAGE_HEADER, List.of(ENDPOINT_MESSAGE_HEADER),
            ENDPOINT_MESSAGE_EDITOR, List.of(ENDPOINT_MESSAGE_EDITOR),
            "user.profile.options.jsp", List.of(PATH_JSP_USER + "/profile_options.jsp")
        );
    }
}