package ru.bgcrm.util.distr;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import ru.bgcrm.util.Setup;
import ru.bgcrm.util.distr.call.ExecuteSQL;

public class Installer {
    public static void main(String[] args) {
        Setup.getSetup();
        try {
            parseArgs(args);
        } catch (WrongArgumentsException ex) {
            System.out.println(ex.toString());
            System.out.println(getHelp());
            System.exit(1);
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(1);
        }
    }

    private static final String K_KILLHASH = "killhash";
    private static final String K_UPDATE = "update";
    private static final String K_UPDATEF = "updatef";
    private static final String K_INSTALL = "install";

    private static void parseArgs(String[] args) throws WrongArgumentsException {
        if (args == null || args.length == 0) {
            throw new WrongArgumentsException("No arguments!");
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
            List<String> replacedFiles = new ArrayList<>();
            new InstallerModule(new File(args[1]), replacedFiles);
            InstallerModule.replacedReport(replacedFiles);
        }
        else {
            throw new WrongArgumentsException("Argument error!");
        }
    }

    private static String getHelp() {
        StringBuffer sb = new StringBuffer();
        sb.append("\nCommands for installer:");
        sb.append("\n\t update           - update all modules.");
        sb.append("\n\t updatef          - update all modules forced.");
        sb.append("\n\t update <version> - switch to another version (not build) of the program.");
        sb.append("\n\t killhash         - clear executed queries history.");
        sb.append("\n\t install <zip>    - install a module from the zip file.");
        return sb.toString();
    }

    private static class WrongArgumentsException extends Exception {
        private String message;

        public WrongArgumentsException(String message) {
            this.message = "Wrong arguments: " + message;
        }

        @Override
        public String toString() {
            return message;
        }
    }
}