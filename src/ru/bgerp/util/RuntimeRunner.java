package ru.bgerp.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.google.common.collect.Lists;

import org.apache.log4j.Level;
import org.apache.log4j.Priority;

/**
 * Runner of OS commands, writing STDOUT and STDERR output
 * to a log with levels INFO and ERROR.
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
            } catch (IOException ioe) {
                log.error(ioe);
            }
        }
    }

    private final String[] commands;

    public RuntimeRunner(String[] commands) {
        this.commands = commands;
    }

    public void run() throws Exception {
        log.info("Running: " + Lists.newArrayList(commands));
        Process proc = Runtime.getRuntime().exec(commands);
        new StreamGobbler(proc.getErrorStream(), Level.ERROR).start();
        new StreamGobbler(proc.getInputStream(), Level.INFO).start();
        proc.waitFor();
    }
}
