package org.bgerp.plugin.msg.feedback;

import java.sql.Connection;
import java.util.List;
import java.util.Map;

public class Plugin extends ru.bgcrm.plugin.Plugin {
    public static final String ID = "feedback";

    public static final String PATH_JSP_OPEN = PATH_JSP_OPEN_PLUGIN + "/" + ID;

    public Plugin() {
        super(ID);
    }

    @Override
    public void init(Connection con) throws Exception {
        super.init(con);
    }

    @Override
    protected Map<String, List<String>> loadEndpoints() {
        return Map.of("open.process.message.add.jsp", List.of(PATH_JSP_OPEN + "/message_add.jsp"));
    }
}
