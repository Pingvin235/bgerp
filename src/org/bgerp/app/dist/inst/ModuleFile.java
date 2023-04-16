package org.bgerp.app.dist.inst;

import java.net.URL;
import java.util.regex.Pattern;

/**
 * File with update module.
 *
 * @author Shamil Vakhitov
 */
public class ModuleFile {
    /**
     * Regexp for parsing zip file names.
     */
    static final Pattern PATTERN_ZIP = java.util.regex.Pattern.compile("^(\\w+)_[\\d\\.]+_(\\d+)\\.zip$");

    final String moduleName;
    final String buildNumber;
    final String fileName;
    final URL url;

    ModuleFile(String moduleName, String buildNumber, String fullName, URL url) {
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