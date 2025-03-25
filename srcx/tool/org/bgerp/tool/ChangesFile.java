package org.bgerp.tool;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.bgerp.util.Log;

/**
 * Concat separated branch-based changes files to changes.txt.
 *
 * @author Shamil Vakhitov
 */
public class ChangesFile {
    private static final Log log = Log.getLog();

    private static final Pattern CHANGES_PATTERN = Pattern.compile("changes\\.(\\w+)\\.txt");
    private static final Pattern DATE_PATTERN = Pattern.compile("^\\d{2}\\.\\d{2}\\.\\d{4}");
    private static final Pattern CHANGE_PATTERN = Pattern.compile("^[FACАСBВ]:");

    // 04.11.2019 02:50:59 1334
    private final DateFormat buildDateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

    private ChangesFile(String version, String build, String changesPath) throws Exception {
        log.info("Patching changes.txt for version: {}, build: {}", version, build);

        File changes = new File(changesPath);
        if (!changes.exists())
            throw new FileNotFoundException(changesPath);

        List<String> changesLines = changesLines(changes.getParentFile());

        boolean patched = false;

        StringBuilder patchedChanges = new StringBuilder();

        for (String line : IOUtils.readLines(new FileInputStream(changes), StandardCharsets.UTF_8)) {
            // append new changes lines at the beginning of the file
            if (!patched) {
                final boolean dateLine = DATE_PATTERN.matcher(line).find();
                if (CHANGE_PATTERN.matcher(line).find() || dateLine) {
                    patchedChanges.append(buildDateFormat.format(new Date())).append(" ").append(build).append("\n");

                    final String releaseDocPath = "version/" + version + "/doc";

                    for (String content : changesLines)
                        patchedChanges.append(content.replace("change/0/doc", releaseDocPath)).append("\n");
                    if (dateLine)
                        patchedChanges.append("\n");

                    patched = true;
                }
            }
            patchedChanges.append(line).append("\n");
        }

        IOUtils.write(patchedChanges.toString(), new FileOutputStream(changes), StandardCharsets.UTF_8);
    }

    /**
     * Collects changes descriptions from separated files changes.<PID>.txt
     * @param dir the directory
     * @return
     * @throws IOException
     * @throws Exception
     */
    private List<String> changesLines(File dir) throws IOException, Exception {
        List<String> changesLines = new ArrayList<>();

        for (File file : dir.listFiles()) {
            var m = CHANGES_PATTERN.matcher(file.getName());
            if (!m.matches()) continue;

            var processId = m.group(1);
            // can be 'changes.lib.txt'
            if (StringUtils.isNumeric(processId)) {
                log.info("Add changes file: {}, processId: {}", file.getName(), processId);

                for (String line : IOUtils.readLines(new StringReader(IOUtils.toString(file.toURI(), StandardCharsets.UTF_8).trim()))) {
                    m = CHANGE_PATTERN.matcher(line);
                    if (m.find())
                        changesLines.add(m.group() + " [" + processId + "] " + line.substring(m.end() + 1));
                    else
                        throw new Exception(Log.format("Incorrect line '{}' in changes file '{}'", line, file));
                }
            }
            file.delete();
        }
        return changesLines;
    }

    public static void main(String[] args) throws Exception {
        new ChangesFile(args[0], args[1], args[2]);
    }
}