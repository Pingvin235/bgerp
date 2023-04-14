package ru.bgcrm.util.distr;

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

import ru.bgcrm.util.ParameterMap;
import ru.bgcrm.util.Preferences;

/**
 * Installation module's properties, stored in version.properties file.
 *
 * @author Shamil Vakhitov
 */
public class VersionInfo {
    /** The application's classes. */
    public static final String MODULE_UPDATE = "update";
    /** External jars. */
    public static final String MODULE_UPDATE_LIB = "update_lib";

    private static final String LIB_APP_DIR = "lib/app";
    private static final String VERSION_INFO_PACKAGE = "ru/bgcrm/version/";

    private final ParameterMap properties;

    public VersionInfo(ParameterMap properties) {
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
    public static final VersionInfo getVersionInfo(String module) {
        VersionInfo result = null;

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        if (classLoader != null) {
            InputStream is = classLoader.getResourceAsStream(VERSION_INFO_PACKAGE + module + ".properties");
            if (is != null) {
                try {
                    result = new VersionInfo(new Preferences(IOUtils.toString(is, StandardCharsets.UTF_8)));
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return result;
    }

    /**
     * @return version infos of currently existing in the running app modules.
     */
    public static final List<VersionInfo> getInstalledVersions() {
        List<VersionInfo> result = new ArrayList<>();

        try {
            File libExtDir = new File(LIB_APP_DIR);
            for (File file : libExtDir.listFiles()) {
                if (!file.getName().endsWith(".jar")) {
                    continue;
                }

                ZipInputStream zis = new ZipInputStream(new FileInputStream(file));

                ZipEntry entry = null;
                while ((entry = zis.getNextEntry()) != null) {
                    final String entryName = entry.getName();

                    if (entryName.startsWith(VERSION_INFO_PACKAGE) && entryName.endsWith(".properties"))
                        result.add(new VersionInfo(new Preferences(IOUtils.toString(zis, StandardCharsets.UTF_8))));
                }

                zis.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }
}