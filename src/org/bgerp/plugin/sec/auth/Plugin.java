package org.bgerp.plugin.sec.auth;

import java.sql.Connection;

import org.bgerp.app.cfg.Setup;
import org.bgerp.event.AuthEvent;
import org.bgerp.plugin.sec.auth.config.Config;

import ru.bgcrm.event.EventProcessor;

public class Plugin extends ru.bgcrm.plugin.Plugin {
    public static final String ID = "auth";
    public static final Plugin INSTANCE = new Plugin();

    private Plugin() {
        super(ID);
    }

    @Override
    public void init(Connection con) throws Exception {
        super.init(con);

        EventProcessor.subscribe((event, conSet) -> {
            if (event.isProcessed())
                return;

            var config = Setup.getSetup().getConfig(Config.class);
            if (config == null)
                return;

            config.auth(event);
        }, AuthEvent.class);
    }
}
