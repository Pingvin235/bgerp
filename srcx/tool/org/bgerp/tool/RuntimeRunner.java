package org.bgerp.tool;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.io.output.WriterOutputStream;
import org.apache.commons.lang3.ArrayUtils;
import org.bgerp.util.Log;

/**
 * Runner of OS commands, collects STDOUT and STDERR output values.
 *
 * @author Shamil Vakhitov
 */
public class RuntimeRunner {
    private static final Log log = Log.getLog();

    private class StreamGobbler extends Thread {
        private final InputStream is;
        private final PrintStream out;

        private StreamGobbler(InputStream is, PrintStream out) {
            this.is = is;
            this.out = out;
        }

        public void run() {
            try {
                var br = new BufferedReader(new InputStreamReader(is));
                String line = null;
                while ((line = br.readLine()) != null) {
                    out.println(line);
                }
                out.flush();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }

    private static final AtomicInteger CMD_COUNTER = new AtomicInteger();

    /** Unique ID of an executed command. */
    private final int id = CMD_COUNTER.incrementAndGet();
    private final String[] commands;
    /** STDERR output  */
    private final StringWriter out = new StringWriter(100);
    /** STDOUT output */
    private final StringWriter err = new StringWriter(100);

    public RuntimeRunner(String... commands) {
        this.commands = commands;
    }

    public RuntimeRunner(String command, String[] args1, String... args2) {
        this.commands = ArrayUtils.addAll(ArrayUtils.addAll(new String[] { command }, args1), args2);
    }

    /**
     * Add prefix with ID to a printed out message.
     * @param msg
     * @return
     */
    private String prefix(String msg) {
        return "[" + id + "] "+ msg;
    }

    /**
     * Runs OS' process and waiting of execution end.
     * @throws Exception process' return code is not 0.
     */
    public void run() throws Exception {
        log.info(prefix("Running: " + List.of(commands)));

        long time = System.currentTimeMillis();
        Process proc = Runtime.getRuntime().exec(commands);

        new StreamGobbler(proc.getInputStream(), new PrintStream(WriterOutputStream.builder().setWriter(out).setCharset(StandardCharsets.UTF_8).get())).start();
        new StreamGobbler(proc.getErrorStream(), new PrintStream(WriterOutputStream.builder().setWriter(err).setCharset(StandardCharsets.UTF_8).get())).start();

        int code = proc.waitFor();

        String out = out();
        if (!out.isBlank())
            log.info(prefix("STDOUT: " + out));
        log.info(prefix("Execution time => " + (System.currentTimeMillis() - time) + " ms."));

        if (code != 0) {
            log.error(prefix("STDERR: " + err()));
            throw new Exception("Execution code: " + code);
        }
    }

    /**
     * @return collected STDOUT output.
     */
    public String out() {
        return out.toString();
    }

    /**
     * @return collected STDERR output.
     */
    public String err() {
        return err.toString();
    }
}
