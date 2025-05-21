package org.bgerp.app.dist.inst;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.bgerp.app.cfg.Setup;
import org.bgerp.app.dist.App;
import org.bgerp.util.Log;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.google.common.annotations.VisibleForTesting;

import ru.bgcrm.util.Utils;

/**
 * Installer of update modules.
 *
 * @author Shamil Vakhitov
 */
public class InstallerModules {
    private static final Log log = Log.getLog();

    private static final String VERSION_URL = App.UPDATE_URL + "/version";
    private static final String TMP_DIR_PATH = Utils.getTmpDir();

    /** App version for 'update' module. */
    private final String version;

    private final Map<String, ModuleFile> remoteFileMap = new HashMap<>();
    private final List<ModuleFile> listForInstall = new ArrayList<>();

    public InstallerModules(String version) {
        this.version = version;
        loadRemoteFileList();
    }

    public InstallerModules() {
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

            String updateUrl = Log.format("{}/{}/", VERSION_URL, kernelVersion);

            log.info("Loading remote file list from: {}", updateUrl);

            // connecting via http(s) and parsing the page
            Document doc = getRemoteHtml(updateUrl);

            // iterating over the all hyperlinks
            for (Element link : doc.select("a")) {
                String href = link.attr("href");
                if (ModuleFile.isValidFileName(href)) {
                    var m = ModuleFile.PATTERN_ZIP.matcher(href);
                    if (m.find()) {
                        var fi = new ModuleFile(m.group(1), m.group(2), href, new URI(updateUrl + href).toURL());
                        remoteFileMap.put(fi.moduleName, fi);
                    }
                }
            }
        } catch (Exception e) {
            log.error(e);
        }
    }

    @VisibleForTesting
    protected Document getRemoteHtml(String updateUrl) throws IOException {
        return Jsoup.connect(updateUrl).get();
    }

    public Map<String, ModuleFile> getRemoteFileMap() {
        return remoteFileMap;
    }

    /**
     * @return the running app version from {@link #version}, or if blank from {@link InstalledModule#getVersion()}, module 'update'.
     */
    private String getVersion() {
        String result = this.version;

        if (Utils.isBlankString(result)) {
            final var vi = InstalledModule.get(InstalledModule.MODULE_UPDATE);
            result = vi == null ? null : vi.getVersion();
        }

        return Utils.maskNull(result);
    }

    private void installSelected() {
        try {
            if (listForInstall.isEmpty()) {
                log.info("Not updates found, press Enter for exit..");
                System.in.read();
                System.exit(0);
            } else {
                for (ModuleFile fi : listForInstall) {
                    log.info("Downloading {} to {}", fi.fileName, TMP_DIR_PATH);

                    FileUtils.copyURLToFile(fi.url, new File(TMP_DIR_PATH + "/" + fi.fileName));

                    log.info(" OK!");
                }

                log.info("Start installing..");

                for (ModuleFile fi : listForInstall) {
                    File file = new File(TMP_DIR_PATH + "/" + fi.fileName);
                    var im = new InstallerModule(Setup.getSetup(), new File("."), file);
                    log.info(im.getReport().toString());
                    file.deleteOnExit();
                }
            }
        } catch (Exception e) {
            log.error(e);
        }
    }

    /**
     * Checking update necessity.
     * @param force - version check disable.
     */
    private void selectModulesForUpdate(boolean force) {
        List<InstalledModule> modules = InstalledModule.getInstalled();

        for (InstalledModule vi : modules) {
            String name = vi.getModuleName();
            String buildNumber = vi.getBuildNumber();

            log.info("Checking update for '{}'..", name);

            ModuleFile fi = remoteFileMap.get(name);
            if (fi == null) {
                continue;
            }

            // remote and local builds are identical
            if (!force && fi.buildNumber.equals(buildNumber)) {
                continue;
            }

            log.info("Found update for '{}' build {} updating to build {}", name, buildNumber, fi.buildNumber);
            listForInstall.add(fi);
        }
    }
}
