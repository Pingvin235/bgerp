package ru.bgcrm.util.distr;

import java.io.File;

import ru.bgcrm.util.Setup;
import ru.bgcrm.util.distr.call.ExecuteSQL;

/**
 * Installer util, running from command line.
 * 
 * @author Shamil Vakhitov
 */
public class Installer {
    public static void main(String[] args) {
        Setup.getSetup();
        try {
            parseArgs(args);
        } catch (IllegalArgumentException ex) {
            System.out.println(ex.toString());
            System.out.println(getHelp());
            System.exit(1);
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(1);
        }
    }

    static final String K_KILLHASH = "killhash";
    static final String K_UPDATE = "update";
    static final String K_UPDATEF = "updatef";
    static final String K_INSTALL = "install";
    static final String K_INSTALLC = "installc";

    private static void parseArgs(String[] args) throws Exception {
        if (args == null || args.length == 0) {
            throw new IllegalArgumentException("No arguments!");
        } else if (args.length == 1 && args[0].equals(K_UPDATE)) {
            new InstallProcessor().update(false);
        } else if (args.length == 1 && args[0].equals(K_UPDATEF)) {
            new InstallProcessor().update(true);
        } else if (args.length == 2 && args[0].equals(K_UPDATE)) {
            new InstallProcessor(args[1]).update(false);
        } else if (args.length == 2 && args[0].equals(K_UPDATEF)) {
            new InstallProcessor(args[1]).update(true);
        } else if (args.length >= 1 && args[0].equals(K_KILLHASH)) {
            String killHashVal = args.length >= 2 ? args[1] : "";
            ExecuteSQL.clearHashById(killHashVal);
            System.out.println("Hash killing for " + killHashVal + " finished!");
        } else if (args.length == 2 && args[0].equals(K_INSTALL)) {
            var im = new InstallerModule(Setup.getSetup(), new File("."), new File(args[1]));
            System.out.println(im.getReport());
        } else if (args.length == 2 && args[0].endsWith(K_INSTALLC)) {
            var files = new UpdateProcessor(args[1]).getUpdateFiles();
            if (files.isEmpty()) {
                System.out.println("No update files found for change ID: " + args[1]);
            } else {
                System.out.println("Installing: " + files);
                new Scripts().install(files);
            }
        }
        else {
            throw new IllegalArgumentException("Argument error!");
        }
    }

    private static String getHelp() {
        StringBuffer sb = new StringBuffer();
        sb.append("\nCommands for installer:");
        sb.append("\n\t update            - update to the actual builds if they differ from currents.");
        sb.append("\n\t updatef           - update to the actual builds without comparison.");
        sb.append("\n\t update <version>  - switch to another version (not build) of the program.");
        sb.append("\n\t killhash          - clear executed queries history.");
        sb.append("\n\t install <zip>     - install a module from the zip file.");
        sb.append("\n\t installc <change> - download update files from <change> and install them.");
        return sb.toString();
    }
}