package org.bgerp.tool;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
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

    private final Pattern changesPattern = Pattern.compile("changes\\.(\\w+)\\.txt");
    private final Pattern datePattern = Pattern.compile("^\\d{2}\\.\\d{2}\\.\\d{4}");
    private final Pattern changePattern = Pattern.compile("^[FACАСBВ]:");

    // 04.11.2019 02:50:59 1334
    private final DateFormat buildDateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

    private ChangesFile(String build, String changesPath) throws Exception {
        log.info("Patching changes.txt for build: {}", build);

        File changes = new File(changesPath);
        if (!changes.exists())
            throw new FileNotFoundException(changesPath);

        // collect changes descriptions from separated files changes.<PID>.txt
        List<String> changesLines = new ArrayList<>();

        for (File file : changes.getParentFile().listFiles()) {
            var m = changesPattern.matcher(file.getName());
            if (!m.matches()) continue;

            var processId = m.group(1);
            // can be 'changes.lib.txt'
            if (StringUtils.isNumeric(processId)) {
                log.info("Add changes file: {}, processId: {}", file.getName(), processId);

                for (String line : IOUtils.readLines(new StringReader(IOUtils.toString(file.toURI(), StandardCharsets.UTF_8).trim()))) {
                    m = changePattern.matcher(line);
                    if (m.find())
                        changesLines.add(m.group() + " [" + processId + "] " + line.substring(m.end() + 1));
                    else
                        changesLines.add(line);
                }
            }
            file.delete();
        }

        boolean patched = false;

        StringBuilder patchedChanges = new StringBuilder();

        for (String line : IOUtils.readLines(new FileInputStream(changes), StandardCharsets.UTF_8)) {
            if (!patched) {
                boolean changesLine = changePattern.matcher(line).find();
                boolean dateLine = datePattern.matcher(line).find();
                if (changesLine || dateLine) {
                    patchedChanges
                        .append(buildDateFormat.format(new Date()))
                        .append(" ")
                        .append(build)
                        .append("\n");

                    for (String content : changesLines)
                        patchedChanges.append(content).append("\n");
                    if (dateLine)
                        patchedChanges.append("\n");

                    patched = true;
                }
            }
            patchedChanges.append(line).append("\n");
        }

        IOUtils.write(patchedChanges.toString(), new FileOutputStream(changes), StandardCharsets.UTF_8);
    }

    public static void main(String[] args) throws Exception {
        new ChangesFile(args[0], args[1]);
    }
}