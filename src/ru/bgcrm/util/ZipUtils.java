package ru.bgcrm.util;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.IOUtils;
import org.bgerp.util.Log;

/**
 * ZIP utils.
 * @author Shamil Vakhitov
 */
public class ZipUtils {
    private static final Log log = Log.getLog();

    /**
     * Add named entry in ZIP, and close it after.
     * @param zos
     * @param name - name.
     * @param content - if not null content, encoded with UTF-8.
     */
    public static ZipEntry addEntry(ZipOutputStream zos, String name, String content) throws IOException {
        var ze = new ZipEntry(name);
        zos.putNextEntry(ze);
        if (content != null)
            zos.write(content.getBytes(StandardCharsets.UTF_8));
        zos.closeEntry();
        return ze;
    }

    /**
     * Extracts entries from ZIP by name prefix.
     * @param zis
     * @param prefix
     * @return
     */
    public static SortedMap<String, byte[]> getFileEntriesFromZipByPrefix(ZipInputStream zis, String prefix) {
        SortedMap<String, byte[]> result = new TreeMap<String, byte[]>();
        ZipEntry ze = null;
        try {
            while ((ze = zis.getNextEntry()) != null) {
                String name = ze.getName();
                if (name.startsWith(prefix)) {
                    byte[] data = IOUtils.toByteArray(zis);
                    result.put(name, data);
                }
            }
        } catch (Exception ex) {
            log.error(ex);
        }

        return result;
    }

    /**
     * Extracts entries from a ZIP with name's substring.
     * @param zis
     * @param subst substring.
     * @return
     */
    public static Map<String, byte[]> getEntriesFromZip(ZipInputStream zis, String subst) {
        Map<String, byte[]> result = new HashMap<String, byte[]>();
        ZipEntry ze = null;
        try {
            while ((ze = zis.getNextEntry()) != null) {
                String name = ze.getName();
                if (subst == null || name.indexOf(subst) >= 0) {
                    byte[] data = IOUtils.toByteArray(zis);
                    result.put(name, data);
                }
            }
        } catch (Exception ex) {
            log.error(ex);
        }

        return result;
    }

}
