package org.bgerp.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Test utils.
 * 
 * @author Shamil Vakhitov
 */
public class TestUtils {

    /**
     * Creates directory.
     * @param parent parent directory path.
     * @param path dir's path.
     * @return created directory object.
     * @throws IOException
     */
    public static File addDir(File parent, String path) throws IOException {
        return addFile(parent, path, null);
    }

    /**
     * Creates file or dir, with all parents creation.
     * @param parent parent directory path.
     * @param path relative path for the file.
     * @param content null, if dir, or UTF-8 encoded string for files.
     * @return created file.
     * @throws IOException
     */
    public static File addFile(File parent, String path, String content) throws IOException {
        var result = new File(parent, path);

        if (content != null) {
            result.getParentFile().mkdirs();
            try (var fos = new FileOutputStream(result)) {
                fos.write(content.getBytes(StandardCharsets.UTF_8));
            }
        } else
            result.mkdirs();

        return result;
    }
}