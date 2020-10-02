package ru.bgcrm.util.distr;

import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import ru.bgcrm.util.Setup;
import ru.bgcrm.util.Utils;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class InstallProcessor {
    private static VersionInfo serverVersionInfo = VersionInfo.getVersionInfo("update");

    private static Setup setup = Setup.getSetup();

    public static final String UPDATE_TO_CHANGE_URL = "https://bgerp.org/update/";
    private static final String UPDATE_URL = setup.get("bgerp.download.url", "https://bgerp.org/download");
    private static final String TMP_DIR_PATH = Utils.getTmpDir();

    private Map<String, FileInfo> remoteFileMap = new HashMap<>();
    private List<FileInfo> listForInstall = new ArrayList<>();

    private String updateVersion;

    public InstallProcessor(String updateVersion) {
        this.updateVersion = updateVersion;
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

    /**
     * Regexp to determine zip-files.
     */
    private static final Pattern pattern = java.util.regex.Pattern
            .compile("^(\\w+)_((?:[\\d\\.]+)|(?:release-[a-zA-Z]+))_(\\d+)\\.zip$");

    /**
     * Loading a list of remote zip-files.
     */
    private void loadRemoteFileList() {
        System.out.println("Update starting..");
        System.out.println("Update from " + UPDATE_URL);

        try {
            String kernelVersion = updateVersion;

            if (Utils.isBlankString(kernelVersion)) {
                kernelVersion = serverVersionInfo.getVersion();

                if (Utils.isBlankString(kernelVersion)) {
                    System.out.println("ERROR: Can't take BGERP server version, exiting");
                    System.exit(1);
                }
            }

            System.out.println("Version is " + kernelVersion);

            String updateUrl = String.format("%s/%s/", UPDATE_URL, kernelVersion);
            // connecting via http(s) and parsing the page
            Document doc = Jsoup.connect(updateUrl).get();

            // iterating over the all hyperlinks
            for (Element link : doc.select("a")) {
                String href = link.attr("href");

                // filtering only update*.zip files
                if (href.endsWith(".zip") && href.startsWith("update_") || href.startsWith("update_lib_")) {
                    Matcher m = pattern.matcher(href);

                    if (m.find()) {
                        FileInfo fi = new FileInfo(m.group(1), m.group(3), href, new URL( String.format( "%s%s", updateUrl, href ) ));
                        remoteFileMap.put(fi.name, fi);
                    }
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

                    FileUtils.copyURLToFile(fi.url, new File(TMP_DIR_PATH + "/" + fi.fileName));

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

    /**
     * Checking update necessity.
     * @param force - version check disable.
     */
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

            // remote and local builds are identical
            if (!force && fi.build.equals(buildNumber)) {
                continue;
            }

            System.out.println("Found update for '" + name + "' build " + buildNumber + " updating to build " + fi.build);
            listForInstall.add(fi);
        }
    }

    /**
     * Additional bean to store update info per each file.
     */
    private static class FileInfo {
        public String name;
        public String build;
        public String fileName;
        public URL url;

        public FileInfo(String name, String build, String fullName, URL url) {
            this.name = name;
            this.build = build;
            this.fileName = fullName;
            this.url = url;
        }
    }
}
