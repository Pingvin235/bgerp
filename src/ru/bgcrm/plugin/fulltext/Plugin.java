package ru.bgcrm.plugin.fulltext;

import java.sql.Connection;
import java.util.Set;

import ru.bgcrm.plugin.Table;
import ru.bgcrm.plugin.Table.Type;
import ru.bgcrm.plugin.fulltext.dao.SearchDAO;

public class Plugin extends ru.bgcrm.plugin.Plugin {
    public static final String ID = "fulltext";

    public Plugin() {
        super(ID);
    }

    @Override
    public void init(Connection con) throws Exception {
        super.init(con);

        new EventListener();
    }

    @Override
    public Set<Table> getTables() {
        return Set.of(new Table(SearchDAO.TABLE, Type.TRASH));
    }
}
