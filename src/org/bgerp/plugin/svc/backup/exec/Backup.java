package org.bgerp.plugin.svc.backup.exec;

import org.bgerp.app.bean.annotation.Bean;
import org.bgerp.app.dist.Scripts;
import org.bgerp.app.exec.scheduler.Task;
import org.bgerp.plugin.svc.backup.Plugin;
import org.bgerp.util.Log;

import ru.bgcrm.util.ParameterMap;

@Bean
public class Backup extends Task {
    private static final Log log = Log.getLog();

    private final boolean db;

    public Backup(ParameterMap config) {
        super(null);
        this.db = config.getBoolean("db", false);
    }

    @Override
    public String getTitle() {
        var l = Plugin.INSTANCE.getLocalizer();
        return l.l(db ? "Backup With DB" : "Backup App Only");
    }

    @Override
    public void run() {
        try {
            new Scripts().backup(db);
        } catch (Exception e) {
            log.error(e);
        }
    }
}
