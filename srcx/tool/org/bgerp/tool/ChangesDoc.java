package org.bgerp.tool;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import org.apache.commons.io.FileUtils;
import org.bgerp.util.Log;

/**
 * Adjusts changes link for the current build in documentation files.
 * Creates a new 0 changes directory out of a template dir.
 *
 * @author Shamil Vakhitov
 */
public class ChangesDoc {
    private static final Log log = Log.getLog();

    private ChangesDoc(String build, String path) throws Exception {
        log.info("Updating changes in doc for build: {}, path: {}", build, path);

        Path rootDir = Path.of(path);

        Path changesDir = rootDir.resolve("changes");

        File buildChangesDir = changesDir.resolve(build).toFile();
        if (buildChangesDir.exists())
            throw new Exception(Log.format("Directory or file '{}' already exists", buildChangesDir));
        changesDir.resolve("0").toFile().renameTo(buildChangesDir);

        FileUtils.copyDirectory(changesDir.resolve("0.template").toFile(), changesDir.resolve("0").toFile());

        Path index = rootDir.resolve("index.adoc");
        String data = new String(Files.readAllBytes(index), StandardCharsets.UTF_8);

        final String marker = "// changesDoc";
        int pos = data.indexOf(marker);
        if (pos <= 0)
            throw new Exception(Log.format("Not found marker '{}' in file '{}'", marker, index));
        data =
            data.substring(0, pos + marker.length()) +
            "\n** <<changes/" + build + "/index.adoc#, " + build + ">>\n" +
            data.substring(pos + marker.length() + 1);
        Files.writeString(index, data, StandardCharsets.UTF_8, StandardOpenOption.WRITE);
    }

    public static void main(String[] args) throws Exception {
        new ChangesDoc(args[0], args[1]);
    }
}