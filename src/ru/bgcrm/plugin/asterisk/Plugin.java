package ru.bgcrm.plugin.asterisk;

import java.sql.Connection;
import java.util.List;
import java.util.Map;

import ru.bgcrm.event.listener.MessageTypeCallListener;
import ru.bgcrm.plugin.Endpoint;
import ru.bgcrm.plugin.asterisk.event.listener.UserSessionListener;

public class Plugin extends ru.bgcrm.plugin.Plugin {
    public static final String ID = "asterisk";
    public static final Plugin INSTANCE = new Plugin();

    public static final String PATH_JSP_USER = PATH_JSP_USER_PLUGIN + "/" + ID;

    private Plugin() {
        super(ID);
    }

    @Override
    protected Map<String, List<String>> endpoints() {
        return Map.of(
            Endpoint.USER_PARAM_MENU_ITEMS, List.of(PATH_JSP_USER + "/param_menu_items.jsp")
        );
    }

    @Override
    public void init(Connection con) throws Exception {
        super.init(con);

        new AMIManager();
        new MessageTypeCallListener();
        new UserSessionListener();
    }
}
