package org.bgerp.util.lic;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
import org.bgerp.util.Log;

/**
 * Singleton object with applications license.
 *
 * @author Shamil Vakhitov
 */
public class AppLicense {
    private static final Log log = Log.getLog();

    private static License instance;

    public static void init() {
        var file = new File(License.FILE_NAME);
        var data = "";
        if (file.exists() && file.canRead()) {
            log.info("Loading license from: {}", file);
            try {
                data = IOUtils.toString(new FileInputStream(file), StandardCharsets.UTF_8);
            } catch (IOException e) {
                log.error(e);
            }
        }
        instance = new License(data);
    }

    public static License getInstance() {
        if (instance == null) {
            init();
        }
        return instance;
    }

    /** Entry point for external calls.
    public static void main(String[] args) throws Exception {
        License.sign(args[0], args[1], args[2], args[3]);
    } */
}
