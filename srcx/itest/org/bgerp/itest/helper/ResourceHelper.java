package org.bgerp.itest.helper;

import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
import org.bgerp.util.Log;

public class ResourceHelper {
    private static final Log log = Log.getLog();

    public static String getResource(Object object, String suffix) throws Exception {
        String name = object.getClass().getSimpleName() + "." + suffix;
        log.debug("Loading resource: %s", name);
        return IOUtils.toString(object.getClass().getResourceAsStream(name), StandardCharsets.UTF_8);
    }
}