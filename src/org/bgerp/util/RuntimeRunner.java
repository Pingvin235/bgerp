package org.bgerp.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Priority;
import org.bgerp.app.exception.BGException;
import org.bgerp.util.log.SessionLogAppender;
import org.bgerp.util.log.TrackedSession;

import com.google.common.collect.Lists;

/**
 * Runner of OS commands, logging STDOUT and STDERR outputs
 * with levels INFO and ERROR respectively and collecting those.
 *
 * @author Shamil Vakhitov
 */
public class RuntimeRunner {
    private static final Log log = Log.getLog();

    private static class StreamGobbler extends Thread {
        private final InputStream is;
        private final Priority logLevel;
        private final TrackedSession session;
        private final List<String> output = new ArrayList<>();

        private StreamGobbler(InputStream is, boolean error, TrackedSession session) {
            this.is = is;
            this.logLevel = error ? Level.ERROR : Level.INFO;
            this.session = session;
            setName(Thread.currentThread().getName() + "-" + (error ? "stderr" : "stdout"));
        }

        private List<String> getOutput() {
            return output;
        }

        public void run() {
            // add log output of the current thread to the sessions' ones
            if (session != null)
                SessionLogAppender.track(session.getSession(), false);
            try {
                InputStreamReader isr = new InputStreamReader(is);
                BufferedReader br = new BufferedReader(isr);
                String line = null;
                while ((line = br.readLine()) != null) {
                    output.add(line);
                    log.log(logLevel, line);
                }
            } catch (IOException e) {
                log.error(e);
            }
        }
    }

    private final String[] commands;
    private File directory;
    private volatile StreamGobbler stderr;
    private volatile StreamGobbler stdout;

    public RuntimeRunner(String... commands) {
        this.commands = commands;
    }

    /**
     * Working directory
     * @param value the directory
     * @return the runner
     */
    public RuntimeRunner directory(File value) {
        directory = value;
        return this;
    }

    /**
     * @return lines of STDERR
     */
    public List<String> stdErr() {
        return stderr.getOutput();
    }

    /**
     * @return lines of STDOUT
     */
    public List<String> stdOut() {
        return stdout.getOutput();
    }

    /**
     * Execute OS commands
     * @throws IOException
     * @throws InterruptedException
     * @throws BGException if exit code wasn't 0
     * @return the runner
     */
    public RuntimeRunner run() throws IOException, InterruptedException {
        int result = runSafe();
        if (result != 0)
            throw new BGException("Process exit code: {}", result);

        return this;
    }

    /**
     * Execute OS commands
     * @throws IOException
     * @throws InterruptedException
     * @return the exit code
     */
    public int runSafe() throws IOException, InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder();
        if (directory != null)
            processBuilder.directory(directory);

        var commands = Lists.newArrayList(this.commands);
        log.info("Running: {}", commands);
        Process proc = processBuilder.command(commands).start();

        stderr = new StreamGobbler(proc.getErrorStream(), true, SessionLogAppender.getTracked());
        stderr.start();

        stdout = new StreamGobbler(proc.getInputStream(), false, SessionLogAppender.getTracked());
        stdout.start();

        stderr.join();
        stdout.join();

        return proc.waitFor();
    }
}
