package org.bgerp.plugin.msg.feedback;

import java.util.List;
import java.util.Map;

public class Plugin extends ru.bgcrm.plugin.Plugin {
    public static final String ID = "feedback";
    public static final Plugin INSTANCE = new Plugin();

    public static final String PATH_JSP_OPEN = PATH_JSP_OPEN_PLUGIN + "/" + ID;

    private Plugin() {
        super(ID);
    }

    @Override
    protected Map<String, List<String>> loadEndpoints() {
        return Map.of("open.process.message.add.jsp", List.of(PATH_JSP_OPEN + "/message_add.jsp"));
    }
}
