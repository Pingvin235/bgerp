package org.bgerp.plugin.svc.backup.exec;

import java.util.Set;

import org.bgerp.app.bean.annotation.Bean;
import org.bgerp.app.cfg.ConfigMap;
import org.bgerp.app.cfg.Setup;
import org.bgerp.app.dist.Scripts;
import org.bgerp.app.exec.scheduler.Task;
import org.bgerp.plugin.svc.backup.Config;
import org.bgerp.plugin.svc.backup.Plugin;
import org.bgerp.plugin.svc.backup.action.admin.BackupAction;
import org.bgerp.util.Log;

@Bean
public class Backup extends Task {
    private static final Log log = Log.getLog();

    private final boolean db;
    private final boolean deleteOld;

    public Backup(ConfigMap config) {
        super(null);
        this.db = config.getBoolean("db", false);
        this.deleteOld = config.getBoolean("delete.old", false);
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

            if (deleteOld) {
                var config = Setup.getSetup().getConfig(Config.class);
                Set<String> candidates = config.cleanupCandidates(BackupAction.FILE_BACKUP);
                if (!candidates.isEmpty()) {
                    log.info("Delete old backups: {}", candidates);
                    BackupAction.FILE_BACKUP.list().forEach(file -> {
                        if (!candidates.contains(file.getName()))
                            return;
                        file.delete();
                    });
                }
            }
        } catch (Exception e) {
            log.error(e);
        }
    }
}
