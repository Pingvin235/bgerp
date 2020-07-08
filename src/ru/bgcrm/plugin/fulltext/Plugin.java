package ru.bgcrm.plugin.fulltext;

import java.sql.Connection;

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
}
