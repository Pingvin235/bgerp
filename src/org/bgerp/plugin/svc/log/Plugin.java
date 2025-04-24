package org.bgerp.plugin.svc.log;

import java.sql.Connection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bgerp.plugin.svc.log.dao.Tables;
import org.bgerp.plugin.svc.log.event.listener.ActionLog;

import ru.bgcrm.plugin.Endpoint;
import ru.bgcrm.plugin.Table;
import ru.bgcrm.plugin.Table.Type;

public class Plugin extends ru.bgcrm.plugin.Plugin {
    public static final String ID = "log";
    public static final Plugin INSTANCE = new Plugin();

    public static final String PATH_JSP_ADMIN = PATH_JSP_ADMIN_PLUGIN + "/" + ID;

    private Plugin() {
        super(ID);
    }

    @Override
    protected Map<String, List<String>> endpoints() {
       return Map.of(
            Endpoint.USER_ADMIN_MENU_ITEMS, List.of(PATH_JSP_ADMIN + "/menu_items.jsp")
       );
    }

    @Override
    public void init(Connection con) throws Exception {
        super.init(con);

        new ActionLog();
    }

    @Override
    public Set<Table> getTables() {
        return Set.of(
            new Table(Tables.TABLE_ACTION_LOG_PREFIX, Type.MONTHLY),
            // old table for action requests logs
            new Table("web_requests_log", Type.DEPRECATED, Type.MONTHLY)
        );
    }
}
