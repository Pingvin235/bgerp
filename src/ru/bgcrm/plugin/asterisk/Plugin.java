package ru.bgcrm.plugin.asterisk;

import java.sql.Connection;

public class Plugin extends ru.bgcrm.plugin.Plugin {
    public static final String ID = "asterisk";

    public Plugin() {
        super(ID);
    }

    @Override
    public void init(Connection con) throws Exception {
        super.init(con);

        // TODO: Run AMIManager here.
    }

}
