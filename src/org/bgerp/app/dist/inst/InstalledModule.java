package org.bgerp.app.dist.inst;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.IOUtils;
import org.bgerp.app.cfg.ConfigMap;
import org.bgerp.app.cfg.Preferences;
import org.bgerp.util.Log;

/**
 * Installed module's properties, stored in version.properties files in application JAR.
 *
 * @author Shamil Vakhitov
 */
public class InstalledModule {
    private static final Log log = Log.getLog();

    /** The application's classes. */
    public static final String MODULE_UPDATE = "update";
    /** External jars. */
    public static final String MODULE_UPDATE_LIB = "update_lib";

    private static final String LIB_APP_DIR = "lib/app";
    private static final String INSTALLED_MODULE_PACKAGE = InstalledModule.class.getPackageName().replace(".", "/") + "/module/";
    private static final String INSTALLED_MODULE_PACKAGE_OLD = "ru/bgcrm/version/";

    private final ConfigMap properties;

    public InstalledModule(ConfigMap properties) {
        this.properties = properties;
    }

    public String getModuleName() {
        return properties.get("name");
    }

    public String getVersion() {
        return properties.get("version");
    }

    public String getBuildNumber() {
        return properties.get("build.number");
    }

    public String getChangeId() {
        return properties.get("change.id");
    }

    public String getBuildTime() {
        // build.time is generated in Java Properties format
        return properties.get("build.time").replace("\\:", ":");
    }

    /**
     * Gets module version info.
     * @param module module name, {@link #MODULE_UPDATE} or {@link #MODULE_UPDATE_LIB}.
     * @return
     */
    public static final InstalledModule get(String module) {
        InstalledModule result = null;

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        if (classLoader != null) {
            InputStream is = classLoader.getResourceAsStream(INSTALLED_MODULE_PACKAGE + module + ".properties");
            if (is == null)
                is = classLoader.getResourceAsStream(INSTALLED_MODULE_PACKAGE_OLD + module + ".properties");
            if (is != null) {
                try {
                    result = new InstalledModule(new Preferences(IOUtils.toString(is, StandardCharsets.UTF_8)));
                    is.close();
                } catch (IOException e) {
                    log.error(e);
                }
            }
        }

        return result;
    }

    /**
     * @return version infos of currently existing in the running app modules.
     */
    public static final List<InstalledModule> getInstalled() {
        List<InstalledModule> result = new ArrayList<>();

        try {
            File libExtDir = new File(LIB_APP_DIR);
            for (File file : libExtDir.listFiles()) {
                if (!file.getName().endsWith(".jar")) {
                    continue;
                }

                try (ZipInputStream zis = new ZipInputStream(new FileInputStream(file))) {
                    ZipEntry entry = null;
                    while ((entry = zis.getNextEntry()) != null) {
                        final String entryName = entry.getName();

                        if ((entryName.startsWith(INSTALLED_MODULE_PACKAGE) || entryName.startsWith(INSTALLED_MODULE_PACKAGE_OLD))&& entryName.endsWith(".properties"))
                            result.add(new InstalledModule(new Preferences(IOUtils.toString(zis, StandardCharsets.UTF_8))));
                    }
                }
            }
        } catch (Exception e) {
            log.error(e);
        }

        return result;
    }
}