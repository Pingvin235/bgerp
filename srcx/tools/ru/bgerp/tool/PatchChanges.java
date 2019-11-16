package ru.bgerp.tool;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
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
    
    // 04.11.2019 02:50:59 1334
    private DateFormat buildDateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

    private PatchChanges(String build, String changesPath) throws Exception {
        LOG.info("Patching changes.txt for build: %s", build);
        
        File changes = new File(changesPath);
        if (!changes.exists())
            throw new FileNotFoundException(changesPath);

        Pattern changesPattern = Pattern.compile("changes\\.\\d+\\.txt");

        // collect changes descriptions from separated files changes.<PID>.txt
        List<String> changesFiles = new ArrayList<>();

        for (File file : changes.getParentFile().listFiles((dir, name) -> changesPattern.matcher(name).matches())) {
            LOG.info("Add changes file: %s", file.getName());
            changesFiles.add(IOUtils.toString(file.toURI(), StandardCharsets.UTF_8).trim());
            file.delete();
        }

        Pattern datePattern = Pattern.compile("^\\d{2}\\.\\d{2}\\.\\d{4}");
        Pattern changePattern = Pattern.compile("^[FACАС]:");

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

                    for (String content : changesFiles)
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