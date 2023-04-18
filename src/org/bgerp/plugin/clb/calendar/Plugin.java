package org.bgerp.plugin.clb.calendar;

import java.util.List;
import java.util.Map;

import ru.bgcrm.plugin.Endpoint;

public class Plugin extends ru.bgcrm.plugin.Plugin {
    public static final String ID = "calendar";
    public static final Plugin INSTANCE = new Plugin();

    public static final String PATH_JSP_USER = PATH_JSP_USER_PLUGIN + "/" + ID;

    private Plugin() {
        super(ID);
    }

    @Override
    protected Map<String, List<String>> loadEndpoints() {
        return Map.of(
            Endpoint.USER_MENU_ITEMS, List.of(PATH_JSP_USER + "/menu_items.jsp")
        );
    }
}
