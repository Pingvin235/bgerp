package ru.bgcrm.plugin.asterisk;

import java.sql.Connection;

import ru.bgcrm.event.listener.MessageTypeCallListener;

public class Plugin extends ru.bgcrm.plugin.Plugin {
    public static final String ID = "asterisk";

    public Plugin() {
        super(ID);
    }

    @Override
    public void init(Connection con) throws Exception {
        super.init(con);
        new AMIManager();
        new MessageTypeCallListener();
    }

}
