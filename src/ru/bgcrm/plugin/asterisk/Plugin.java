package ru.bgcrm.plugin.asterisk;

import java.sql.Connection;

import ru.bgcrm.event.listener.MessageTypeCallListener;
import ru.bgcrm.plugin.asterisk.event.listener.UserSessionListener;

public class Plugin extends ru.bgcrm.plugin.Plugin {
    public static final String ID = "asterisk";
    public static final Plugin INSTANCE = new Plugin();

    private Plugin() {
        super(ID);
    }

    @Override
    public void init(Connection con) throws Exception {
        super.init(con);
        new AMIManager();
        new MessageTypeCallListener();
        new UserSessionListener();
    }
}
