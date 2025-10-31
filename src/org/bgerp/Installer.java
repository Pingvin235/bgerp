package org.bgerp;


import java.io.File;
import java.util.List;

import org.bgerp.app.cfg.Setup;
import org.bgerp.app.dist.inst.InstallerChanges;
import org.bgerp.app.dist.inst.InstallerModule;
import org.bgerp.app.dist.inst.InstallerModules;
import org.bgerp.app.dist.inst.call.ExecuteSQL;
import org.bgerp.util.Log;

import ru.bgcrm.util.Utils;

/**
 * Installer util, running from command line.
 *
 * @author Shamil Vakhitov
 */
public class Installer {
    private static final Log log = Log.getLog();

    public static void main(String[] args) {
        Setup.getSetup();
        try {
            execute(args);
        } catch (IllegalArgumentException ex) {
            log.error(ex.toString());
            log.info(getHelp());
            System.exit(1);
        } catch (Exception ex) {
            log.error(ex);
            System.exit(1);
        }
    }

    private static final String SKIPHASH = "skiphash";
    private static final String KILLHASH = "killhash";
    public static final String UPDATE = "update";
    public static final String UPDATEF = "updatef";
    public static final String INSTALL = "install";
    public static final String INSTALLC = "installc";

    private static void execute(String[] args) throws Exception {
        if (args == null || args.length == 0)
            throw new IllegalArgumentException("No arguments!");

        log.info("Executing {}", List.of(args));

        final String cmd = args[0];
        if (args.length == 1 && cmd.equals(UPDATE)) {
            new InstallerModules().update(false);
        } else if (args.length == 1 && args[0].equals(UPDATEF)) {
            new InstallerModules().update(true);
        } else if (args.length == 2 && args[0].equals(UPDATE)) {
            new InstallerModules(args[1]).update(false);
        } else if (args.length == 2 && args[0].equals(UPDATEF)) {
            new InstallerModules(args[1]).update(true);
        } else if (args.length == 2 && args[0].equals(SKIPHASH)) {
            ExecuteSQL.skipHash(args[1]);
            log.info("DB update hash was stored to be skipped");
        } else if (args.length >= 1 && args[0].equals(KILLHASH)) {
            ExecuteSQL.clearHashes();
            log.info("DB update hashes cleanup finished!");
        } else if (args.length == 2 && args[0].equals(INSTALL)) {
            final var im = new InstallerModule(Setup.getSetup(), new File("."), new File(args[1]));
            log.info("Report:\n{}", im.getReport());
        } else if (args.length == 2 && args[0].endsWith(INSTALLC)) {
            var files = new InstallerChanges(args[1]).getUpdateFiles();
            if (files.isEmpty()) {
                log.info("No update files found for change ID: " + args[1]);
            } else {
                final String tmpDirPath = Utils.getTmpDir();
                log.info("Installing {} from {}", files, tmpDirPath);
                for (String name : files) {
                    final var im = new InstallerModule(Setup.getSetup(), new File("."), new File(tmpDirPath + "/" + name));
                    log.info("Report:\n{}", im.getReport());
                }
            }
        }
        else {
            throw new IllegalArgumentException("Argument error!");
        }
    }

    private static String getHelp() {
        StringBuffer sb = new StringBuffer();
        sb.append("\nCommands for the Installer:");
        sb.append("\n\t update               - update to the actual builds if they differ from currents");
        sb.append("\n\t updatef              - update to the actual builds without comparison");
        sb.append("\n\t update <VERSION>     - switch to another MAJOR <VERSION> of the program");
        sb.append("\n\t skiphash <HASH>      - skip execution a DB update query with <HASH>");
        sb.append("\n\t killhash             - clear ALL executed DB update queries");
        sb.append("\n\t install <ZIP>        - install a module from the <ZIP> file");
        sb.append("\n\t installc <CHANGE_ID> - update to the <CHANGE_ID>, 0 - is for MASTER release");
        return sb.toString();
    }
}