package ru.bgcrm.util.distr;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Parser of version.properties file, stored in jar.
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

    private Properties properties;

    private VersionInfo() {}

    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    private String getProperty(String name) {
        String result = "";

        if (properties != null) {
            result = properties.getProperty(name);
        }

        return result;
    }

    public String getModuleName() {
        return getProperty("name");
    }

    public String getVersion() {
        return getProperty("version");
    }

    public String getBuildNumber() {
        return getProperty("build.number");
    }

    public String getChangeId() {
        return getProperty("change.id");
    }

    public String getBuildTime() {
        return getProperty("build.time");
    }

    /**
     * Gets module version info.
     * @param module module name, {@link #MODULE_UPDATE} or {@link #MODULE_UPDATE_LIB}.
     * @return
     */
    public static final VersionInfo getVersionInfo(String module) {
        VersionInfo result = new VersionInfo();

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        if (classLoader != null) {
            InputStream is = classLoader.getResourceAsStream(VERSION_INFO_PACKAGE + module + ".properties");
            if (is != null) {
                try {
                    Properties p = new Properties();
                    p.load(is);
                    result.setProperties(p);
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return result;
    }

    /**
     * Получение версий текущих установленных модулей.
     * @return
     */
    public static final List<VersionInfo> getInstalledVersions() {
        List<VersionInfo> result = new ArrayList<VersionInfo>();

        try {
            File libExtDir = new File(LIB_APP_DIR);
            for (File file : libExtDir.listFiles()) {
                if (!file.getName().endsWith(".jar")) {
                    continue;
                }

                ZipInputStream zis = new ZipInputStream(new FileInputStream(file));

                ZipEntry entry = null;
                while ((entry = zis.getNextEntry()) != null) {
                    String entryName = entry.getName();

                    if (entryName.startsWith(VERSION_INFO_PACKAGE) && entryName.endsWith(".properties")) {
                        Properties props = new Properties();
                        props.load(zis);

                        VersionInfo vi = new VersionInfo();
                        vi.setProperties(props);

                        result.add(vi);
                    }
                }

                zis.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }
}