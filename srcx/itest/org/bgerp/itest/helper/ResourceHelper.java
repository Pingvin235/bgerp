package org.bgerp.itest.helper;

import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
import org.bgerp.util.Log;

public class ResourceHelper {
    private static final Log log = Log.getLog();

    public static String getResource(Object object, String suffix) throws Exception {
        return new String(getResourceBytes(object, suffix), StandardCharsets.UTF_8).replace("\r", "");
    }

    public static byte[] getResourceBytes(Object object, String suffix) throws Exception {
        String name = object.getClass().getSimpleName() + "." + suffix;
        log.debug("Loading resource: {}", name);
        return IOUtils.toByteArray(object.getClass().getResourceAsStream(name));
    }
}