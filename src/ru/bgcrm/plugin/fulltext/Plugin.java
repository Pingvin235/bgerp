package ru.bgcrm.plugin.fulltext;

import java.sql.Connection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ru.bgcrm.plugin.Table;
import ru.bgcrm.plugin.Table.Type;
import ru.bgcrm.plugin.fulltext.dao.SearchDAO;

public class Plugin extends ru.bgcrm.plugin.Plugin {
    public static final String ID = "fulltext";
    public static final Plugin INSTANCE = new Plugin();

    public static final String PATH_JSP_USER = PATH_JSP_USER_PLUGIN + "/" + ID;

    private Plugin() {
        super(ID);
    }

    @Override
    public Set<Table> getTables() {
        return Set.of(new Table(SearchDAO.TABLE, Type.TRASH));
    }

    @Override
    public void init(Connection con) throws Exception {
        super.init(con);

        new EventListener();
    }

    @Override
    protected Map<String, List<String>> endpoints() {
        return Map.of(
            "user.search.jsp", List.of(PATH_JSP_USER + "/search.jsp"),
            "user.process.linkForAddCustom.jsp", List.of(PATH_JSP_USER + "/process_link_for_add_custom_list.jsp")
        );
    }
}
