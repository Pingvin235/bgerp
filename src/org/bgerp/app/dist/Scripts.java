package org.bgerp.app.dist;

import java.io.File;
import java.util.List;

import org.bgerp.Installer;
import org.bgerp.util.Log;
import org.bgerp.util.RuntimeRunner;

/**
 * Distribution's scripts.
 *
 * @author Shamil Vakhitov
 */
public class Scripts {
    private static final Log log = Log.getLog();

    private static final String BACKUP = "./backup.sh";
    private static final String INSTALLER = " ./installer.sh ";
    private static final String RESTART = " ./erp_restart.sh ";

    public Scripts backup(boolean db) throws Exception {
        new RuntimeRunner("bash", BACKUP, db ? "create_db" : "create").run();
        return this;
    }

    public Scripts backupRestore(String name) throws Exception {
        new RuntimeRunner("bash", BACKUP, "restore", name).run();
        return this;
    }

    public Scripts update(boolean force) throws Exception {
        new RuntimeRunner("sh", "-c", INSTALLER + (force ? Installer.K_UPDATEF : Installer.K_UPDATE)).run();
        return this;
    }

    public Scripts restart(boolean force) throws Exception {
        // the file doesn't exist when the application is running under Docker
        var pid = new File(".run/bgerp.pid");
        if (!pid.exists()) {
            log.info("Stopping container instead of restart");
            System.exit(0);
        } else {
            new RuntimeRunner("sh", "-c", RESTART + (force ? "force" : "")).run();
        }
        return this;
    }

    public Scripts install(List<String> files) throws Exception {
        for (String file : files) {
            new RuntimeRunner("sh", "-c", INSTALLER + Installer.K_INSTALL + " " + file).run();
        }
        return this;
    }

    public Scripts installc(String changeId) throws Exception {
        new RuntimeRunner("sh", "-c", INSTALLER + Installer.K_INSTALLC + " " + changeId).run();
        return this;
    }
}
