package org.bgerp.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.google.common.collect.Lists;


import org.apache.log4j.Level;
import org.apache.log4j.Priority;
import org.bgerp.app.exception.BGException;

/**
 * Runner of OS commands, writing STDOUT and STDERR outputs
 * to a log with levels INFO and ERROR.
 *
 * @author Shamil Vakhitov
 */
public class RuntimeRunner {
    private static final Log log = Log.getLog();

    private static class StreamGobbler extends Thread {
        private final InputStream is;
        private final Priority logLevel;

        private StreamGobbler(InputStream is, Priority logLevel) {
            this.is = is;
            this.logLevel = logLevel;
        }

        public void run() {
            try {
                InputStreamReader isr = new InputStreamReader(is);
                BufferedReader br = new BufferedReader(isr);
                String line = null;
                while ((line = br.readLine()) != null)
                    log.log(logLevel, line);
            } catch (IOException e) {
                log.error(e);
            }
        }
    }

    private final String[] commands;

    public RuntimeRunner(String... commands) {
        this.commands = commands;
    }

    /**
     * Executes OS commands
     * @throws IOException
     * @throws InterruptedException
     * @throws BGException if exit code wasn't 0
     */
    public void run() throws IOException, InterruptedException {
        log.info("Running: {}", Lists.newArrayList(commands));
        Process proc = Runtime.getRuntime().exec(commands);
        new StreamGobbler(proc.getErrorStream(), Level.ERROR).start();
        new StreamGobbler(proc.getInputStream(), Level.INFO).start();
        int result = proc.waitFor();
        if (result != 0)
            throw new BGException("Process exit code: {}", result);
    }
}
