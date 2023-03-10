package ru.bgcrm.util.io;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class IOUtils {
    /**
     * Reads byte array out of file with {@code path} case if exists,
     * otherwise treats {@code path} as Java resource and reads it.
     * @param path file path or Java resource identifier, examples: {@code /org/bgerp/util/SomeFile.data} or {@code /tmp/SomeFile.data}.
     * @return
     * @throws IOException
     */
    public static final byte[] read(String path) throws IOException {
        var file = new File(path);
        if (file.exists() && file.isFile() && file.canRead())
            return Files.readAllBytes(file.toPath());
        return org.apache.commons.io.IOUtils.resourceToByteArray(path);
    }
}