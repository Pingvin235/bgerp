package ru.bgcrm.util.distr;

import java.util.List;

import ru.bgerp.util.RuntimeRunner;

/**
 * Distribution's scripts.
 * @author Shamil Vakhitov
 */
public class Scripts {
    private static final String BACKUP = "./backup.sh ";
    private static final String INSTALLER = " ./installer.sh ";
    private static final String RESTART = " ./erp_restart.sh";

    public static void backupUpdateRestart(boolean updateForce) throws Exception {
        new RuntimeRunner(new String[] { 
            "bash", "-c", BACKUP + "&&" + INSTALLER + (updateForce ? Installer.K_UPDATEF : Installer.K_UPDATE) + " &&" + RESTART })
            .run();
    }

    public static void backupInstallRestart(List<String> updateFiles) throws Exception {
        var installerCommand = new StringBuilder(100);
        for (String file : updateFiles) {
            installerCommand.append("&&" + INSTALLER + Installer.K_INSTALL);
            installerCommand.append(file);
        }
    
        new RuntimeRunner(new String[] { 
            "bash", "-c", BACKUP + " " + installerCommand.toString() + " &&" + RESTART })
            .run();
    }

}
