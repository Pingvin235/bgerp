package ru.bgcrm.util.distr;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import com.google.common.annotations.VisibleForTesting;

import org.apache.commons.io.FileUtils;
import org.bgerp.util.Log;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import ru.bgcrm.util.Setup;
import ru.bgcrm.util.Utils;

/**
 * Installer of update packages.
 *
 * @author Shamil Vakhitov
 */
public class InstallProcessor {
    private static final Log log = Log.getLog();

    private static final String UPDATE_URL = System.getProperty("bgerp.download.url", "https://bgerp.org/download");
    private static final String TMP_DIR_PATH = Utils.getTmpDir();

    /** App version for 'update' module. */
    private final String version;

    private final Map<String, FileInfo> remoteFileMap = new HashMap<>();
    private final List<FileInfo> listForInstall = new ArrayList<>();

    public InstallProcessor(String version) {
        this.version = version;
        loadRemoteFileList();
    }

    public InstallProcessor() {
        this(null);
    }

    /**
     * Update to the latest
     * @param force compare remote build with locally installed.
     */
    public void update(boolean force) {
        selectModulesForUpdate(force);
        installSelected();
    }

    /**
     * Load a list of remote zip-files.
     */
    private void loadRemoteFileList() {
        try {
            String kernelVersion = getVersion();

            String updateUrl = Log.format("{}/{}/", UPDATE_URL, kernelVersion);

            log.info("Loading remote file list from: {}", updateUrl);

            // connecting via http(s) and parsing the page
            Document doc = getRemoteHtml(updateUrl);

            // iterating over the all hyperlinks
            for (Element link : doc.select("a")) {
                String href = link.attr("href");
                if (FileInfo.isValidFileName(href)) {
                    var m = FileInfo.PATTERN_ZIP.matcher(href);
                    if (m.find()) {
                        var fi = new FileInfo(m.group(1), m.group(2), href, new URL(updateUrl + href));
                        remoteFileMap.put(fi.moduleName, fi);
                    }
                }
            }
        } catch (Exception e) {
            log.error(e);
            // System.exit(1);
        }
    }

    @VisibleForTesting
    protected Document getRemoteHtml(String updateUrl) throws IOException {
        return Jsoup.connect(updateUrl).get();
    }

    public Map<String, FileInfo> getRemoteFileMap() {
        return remoteFileMap;
    }

    /**
     * @return the running app version from {@link #version}, or if blank from {@link VersionInfo#getVersion()}, module 'update'.
     */
    private String getVersion() {
        String result = this.version;

        if (Utils.isBlankString(result)) {
            final var vi = VersionInfo.getVersionInfo(VersionInfo.MODULE_UPDATE);
            result = vi == null ? null : vi.getVersion();
        }

        return Utils.maskNull(result);
    }

    private void installSelected() {
        try {
            if (listForInstall.isEmpty()) {
                System.out.println("Not updates found, press Enter for exit..");
                System.in.read();
                System.exit(0);
            } else {
                for (FileInfo fi : listForInstall) {
                    System.out.print("Downloading " + fi.fileName + " to " + TMP_DIR_PATH);

                    FileUtils.copyURLToFile(fi.url, new File(TMP_DIR_PATH + "/" + fi.fileName));

                    System.out.println(" OK!");
                }

                System.out.println("Start installing..");

                for (FileInfo fi : listForInstall) {
                    File file = new File(TMP_DIR_PATH + "/" + fi.fileName);
                    var im = new InstallerModule(Setup.getSetup(), new File("."), file);
                    System.out.println(im.getReport());
                    file.deleteOnExit();
                }
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
            if (!force && fi.buildNumber.equals(buildNumber)) {
                continue;
            }

            System.out.println("Found update for '" + name + "' build " + buildNumber + " updating to build " + fi.buildNumber);
            listForInstall.add(fi);
        }
    }

    /**
     * Additional bean to store update info per each file.
     */
    public static class FileInfo {
        /**
         * Regexp for parsing zip file names.
         */
        private static final Pattern PATTERN_ZIP = java.util.regex.Pattern.compile("^(\\w+)_[\\d\\.]+_(\\d+)\\.zip$");

        final String moduleName;
        final String buildNumber;
        final String fileName;
        final URL url;

        FileInfo(String moduleName, String buildNumber, String fullName, URL url) {
            this.moduleName = moduleName;
            this.buildNumber = buildNumber;
            this.fileName = fullName;
            this.url = url;
        }

        public String getBuildNumber() {
            return buildNumber;
        }

        /**
         * Check file name starts from 'update_' and ends by '.zip'.
         * @param name
         * @return
         */
        static boolean isValidFileName(String name) {
            return name.startsWith("update_") && name.endsWith(".zip");
        }
    }
}
