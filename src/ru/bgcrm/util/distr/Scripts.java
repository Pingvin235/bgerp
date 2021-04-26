package ru.bgcrm.util.distr;

import java.util.List;

import ru.bgerp.util.RuntimeRunner;

/**
 * Distribution's scripts.
 * 
 * @author Shamil Vakhitov
 */
public class Scripts {
    private static final String BACKUP = "./backup.sh ";
    private static final String INSTALLER = " ./installer.sh ";
    private static final String RESTART = " ./erp_restart.sh ";

    public Scripts backup(boolean db) throws Exception {
        new RuntimeRunner("sh", "-c", BACKUP + (db ? "db" : "")).run();
        return this;
    }

    public Scripts update(boolean force) throws Exception {
        new RuntimeRunner("sh", "-c", INSTALLER + (force ? Installer.K_UPDATEF : Installer.K_UPDATE)).run();
        return this;
    }

    public Scripts restart(boolean force) throws Exception {
        new RuntimeRunner("sh", "-c", RESTART + (force ? "force" : "")).run();
        return this;
    }

    public Scripts install(List<String> files) throws Exception {
        for (String file : files) {
            new RuntimeRunner("sh", "-c", INSTALLER + Installer.K_INSTALL + " " + file).run();
        }
        return this;
    }
}
