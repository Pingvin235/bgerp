package org.bgerp.plugin.svc.log.event.listener;

import java.sql.SQLException;

import org.bgerp.plugin.svc.log.dao.ActionLogDAO;
import org.bgerp.plugin.svc.log.model.ActionLogEntry;
import org.bgerp.servlet.user.event.ActionRequestEvent;
import org.bgerp.util.Log;

import ru.bgcrm.event.EventProcessor;
import ru.bgcrm.util.Setup;

/**
 * Action log listener.
 *
 * @author Shamil Vakhitov
 */
public class ActionLog {
    private static final Log log = Log.getLog();

    public ActionLog() {
        EventProcessor.subscribe((e, conSet) -> {
            if (e.getPermissionNode() != null && e.getPermissionNode().isNotLogging())
                return;
            insert(e);
        }, ActionRequestEvent.class);
    }

    private void insert(ActionRequestEvent e) {
        try {
            // taking separated connection, because:
            // - the main transaction may be aborted because of error
            // - later here might be used trash DB
            try (var con = Setup.getSetup().getDBConnectionFromPool()) {
                new ActionLogDAO(con).update(new ActionLogEntry(e));
                con.commit();
            }
        } catch (SQLException ex) {
            log.error(ex);
        }
    }
}
