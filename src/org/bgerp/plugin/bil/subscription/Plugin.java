package org.bgerp.plugin.bil.subscription;

import java.sql.Connection;

import org.bgerp.app.cfg.Setup;
import org.bgerp.plugin.bil.subscription.event.listener.PaidInvoicesListener;

import ru.bgcrm.event.EventProcessor;
import ru.bgcrm.event.ParamChangedEvent;

/**
 * Subscription plugin.
 *
 * @author Shamil Vakhitov
 */
public class Plugin extends ru.bgcrm.plugin.Plugin {
    public static final String ID = "subscription";
    public static final Plugin INSTANCE = new Plugin();

    public static final String PATH_JSP_OPEN = PATH_JSP_OPEN_PLUGIN + "/" + ID;
    public static final String PATH_JSP_USER = PATH_JSP_USER_PLUGIN + "/" + ID;

    private Plugin() {
        super(ID);
    }

    @Override
    public void init(Connection con) throws Exception {
        super.init(con);

        EventProcessor.subscribe((e, conSet) -> {
            var config = Setup.getSetup().getConfig(Config.class);
            if (config != null)
                config.paramChanged(e, conSet);
        }, ParamChangedEvent.class);

        new PaidInvoicesListener();
    }
}
