package ru.bgcrm.plugin.phpbb;

import java.util.List;
import java.util.Map;
import java.util.Set;

import ru.bgcrm.plugin.phpbb.model.Topic;
import ru.bgcrm.struts.action.BaseAction;

public class Plugin extends ru.bgcrm.plugin.Plugin {
    public static final String ID = "phpbb";

    public static final String PATH_JSP_USER = BaseAction.PATH_JSP_USER_PLUGIN + "/" + ID;

    public static final String ENDPOINT_MESSAGE_HEADER = PATH_JSP_USER + "/process_message_header.jsp";
    public static final String ENDPOINT_MESSAGE_EDITOR = PATH_JSP_USER + "/process_message_editor.jsp";

    public Plugin() {
        super(ID);
    }

    @Override
    public Set<String> getObjectTypes() {
        return Set.of(Topic.OBJECT_TYPE_PREFIX);
    }

    @Override
    protected Map<String, List<String>> loadEndpoints() {
        return Map.of(
            ENDPOINT_MESSAGE_HEADER, List.of(ENDPOINT_MESSAGE_HEADER),
            ENDPOINT_MESSAGE_EDITOR, List.of(ENDPOINT_MESSAGE_EDITOR)
        );
    }
}
