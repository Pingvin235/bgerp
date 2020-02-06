package ru.bgcrm.util.distr;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

import ru.bgcrm.util.Setup;
import ru.bgcrm.util.Utils;

public class InstallProcessor {
    private static VersionInfo serverVersionInfo = VersionInfo.getVersionInfo("update");

    private static Setup setup = Setup.getSetup();

    public static final String UPDATE_TO_CHANGE_URL = "https://bgerp.org/update/";
    private static final String UPDATE_URL = setup.get("bgerp.update.url", "ftp://ftp.bgerp.ru/pub/bgerp");
    private static final String TMP_DIR_PATH = Utils.getTmpDir();

    private FTPClient ftp;

    private Map<String, FileInfo> remoteFileMap = new HashMap<String, FileInfo>();
    private List<FileInfo> listForInstall = new ArrayList<FileInfo>();

    private String updateVersion;

    public InstallProcessor(String updateVersion) {
        this.updateVersion = updateVersion;
        connect();
        loadRemoteFileList();
    }

    public InstallProcessor() {
        this(null);
    }

    /**
     * Обновление установленных модулей до последней версии.
     */
    public void update(boolean force) {
        selectModulesForUpdate(force);
        installSelected();
    }

    private void connect() {
        System.out.println("Update starting..");
        System.out.println("Update from " + UPDATE_URL);

        String kernelVersion = updateVersion;

        if (Utils.isBlankString(kernelVersion)) {
            kernelVersion = serverVersionInfo.getVersion();

            if (Utils.isBlankString(kernelVersion)) {
                System.out.println("ERROR: Can't take BGERP server version, exiting");
                System.exit(1);
            }
        }

        System.out.println("Version is " + kernelVersion);

        URI uri = URI.create(UPDATE_URL + "/" + kernelVersion);
        ftp = new FTPClient();

        if (uri.getPort() > 0) {
            ftp.setDefaultPort(uri.getPort());
        }

        try {
            ftp.connect(uri.getHost());

            if (System.getProperty("bginstaller.update.ftp.mode", "passive").equals("passive")) {
                ftp.enterLocalPassiveMode();
                System.out.println("Set passive mode..");
            } else {
                ftp.enterLocalActiveMode();
                System.out.println("Set active mode..");
            }

            ftp.login("anonymous", "");
            ftp.setFileType(FTP.BINARY_FILE_TYPE);

            System.out.println("Changing dir to " + uri.getPath());

            if (!ftp.changeWorkingDirectory(uri.getPath())) {
                throw new Exception("not change to " + uri.getPath());
            }
        } catch (Exception e) {
            System.out.println("ERROR: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }

        loadRemoteFileList();
    }

    private static final Pattern pattern = java.util.regex.Pattern
            .compile("^(\\w+)_((?:[\\d\\.]+)|(?:release-[a-zA-Z]+))_(\\d+)\\.zip$");

    private void loadRemoteFileList() {
        try {
            String[] files = ftp.listNames();
            for (String file : files) {
                Matcher m = pattern.matcher(file);
                if (m.find()) {
                    FileInfo fi = new FileInfo(m.group(1), m.group(3), file);
                    remoteFileMap.put(fi.name, fi);
                }
            }
        } catch (Exception e) {
            System.out.println("ERROR: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    private void installSelected() {
        try {
            if (listForInstall.size() == 0) {
                System.out.println("Not updates found, press Enter for exit..");
                System.in.read();
                System.exit(0);
            } else {
                for (FileInfo fi : listForInstall) {
                    System.out.print("Downloading " + fi.fileName);

                    FileOutputStream fos = new FileOutputStream(TMP_DIR_PATH + "/" + fi.fileName);
                    ftp.retrieveFile(fi.fileName, fos);
                    fos.close();

                    System.out.println(" OK!");
                }

                System.out.println("Start installing..");
                Thread.sleep(2000);

                List<String> replacedFiles = new ArrayList<String>();

                for (FileInfo fi : listForInstall) {
                    File file = new File(TMP_DIR_PATH + "/" + fi.fileName);
                    new InstallerModule(file, replacedFiles);
                    file.deleteOnExit();
                }

                InstallerModule.replacedReport(replacedFiles);
            }
        } catch (Exception e) {
            System.out.println("ERROR: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    private void selectModulesForUpdate(boolean force) {
        List<VersionInfo> modules = VersionInfo.getInstalledVersions();

        for (VersionInfo vi : modules) {
            String name = vi.getModuleName();
            String buildNumber = vi.getBuildNumber();

            System.out.println("Checking update for '" + name + "'..");

            FileInfo fi = remoteFileMap.get(name);
            if (fi == null) {
                continue;
            }

            // билд пакета на FTP идентичен
            if (!force && fi.build.equals(buildNumber)) {
                continue;
            }

            System.out.println("Found update for '" + name + "' build " + buildNumber + " updating to build " + fi.build);
            listForInstall.add(fi);
        }
    }

    private static class FileInfo {
        public String name;
        public String build;
        public String fileName;

        public FileInfo(String name, String build, String fullName) {
            this.name = name;
            this.build = build;
            this.fileName = fullName;
        }
    }
}