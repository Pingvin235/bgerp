package ru.bgerp.tool;

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

import ru.bgerp.util.Log;

public class PatchChanges {
    private static final Log LOG = Log.getLog();
    
    private final Pattern changesPattern = Pattern.compile("changes\\.(\\d+)\\.txt");
    private final Pattern datePattern = Pattern.compile("^\\d{2}\\.\\d{2}\\.\\d{4}");
    private final Pattern changePattern = Pattern.compile("^[FACАСBВ]:");

    // 04.11.2019 02:50:59 1334
    private final DateFormat buildDateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

    private PatchChanges(String build, String changesPath) throws Exception {
        LOG.info("Patching changes.txt for build: %s", build);
        
        File changes = new File(changesPath);
        if (!changes.exists())
            throw new FileNotFoundException(changesPath);

        // collect changes descriptions from separated files changes.<PID>.txt
        List<String> changesLines = new ArrayList<>();

        for (File file : changes.getParentFile().listFiles()) {
            var m = changesPattern.matcher(file.getName());
            if (!m.matches()) continue;

            var processId = m.group(1);
            LOG.info("Add changes file: %s, processId: %s", file.getName(), processId);

            for (String line : IOUtils.readLines(new StringReader(IOUtils.toString(file.toURI(), StandardCharsets.UTF_8).trim()))) {
                m = changePattern.matcher(line);
                if (m.find())
                    changesLines.add(m.group() + " [" + processId + "] " + line.substring(m.end() + 1));
                else
                    changesLines.add(line);
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
        new PatchChanges(args[0], args[1]);
    }

}