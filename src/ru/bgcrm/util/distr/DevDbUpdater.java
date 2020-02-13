package ru.bgcrm.util.distr;

import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.util.Arrays;

import org.apache.commons.io.IOUtils;

import ru.bgcrm.util.Setup;
import ru.bgcrm.util.distr.call.ExecuteSQL;
import ru.bgerp.util.Log;

/**
 * Updates DB structure for running in IDE.
 */
public class DevDbUpdater implements Runnable {
    private static final Log log = Log.getLog();

    @Override
    public void run() {
        log.info("Running developer's DB update");

        ExecuteSQL sqlCall = new ExecuteSQL();
        try (Connection con = Setup.getSetup().getDBConnectionFromPool()) {
            File[] files = new File("build/update").listFiles(f -> f.getName().endsWith(".sql"));
            Arrays.sort(files, (f1, f2) -> f1.getName().length() - f2.getName().length());
            for (File file : files) {
                log.info("Applying: %s", file.getName());
                sqlCall.call(con, IOUtils.toString(new FileInputStream(file), StandardCharsets.UTF_8));
            }
        } catch (Exception e) {
            log.error(e);
        }
    }

}
