package org.bgerp.tool.prop;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

/**
 * Build module properties class. Used in Gradle scripts and tool classes.
 *
 * @author Shamil Vakhitov
 */
public class Module {
    static final String KEY_VERSION = "version";
    static final String KEY_NAME = "name";
    static final String KEY_BUILD_TIME = "build.time";
    static final String KEY_BUILD_NUMBER = "build.number";

    final String name;
    private final String version;
    private final String build;

    public Module(String dir) throws Exception {
        Properties prop = new Properties();
        try (InputStream input = new FileInputStream(dir + "/module.properties")) {
            prop.load(input);
        }
        name = prop.getProperty(KEY_NAME);

        prop = new Properties();
        try (InputStream input = new FileInputStream(dir + "/" + propertiesName())) {
            prop.load(input);
        }
        version = prop.getProperty(KEY_VERSION);
        build = prop.getProperty(KEY_BUILD_NUMBER);
    }

    /**
     * @return properties file name.
     */
    public String propertiesName() {
        return name + ".properties";
    }

    /**
     * @return zip file name.
     */
    public String zipName() {
        return name + "_" + version + "_" + build + ".zip";
    }
}
