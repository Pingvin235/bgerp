package org.bgerp.tool;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.ArrayUtils;

/**
 * Runner of OS commands, redirects STDOUT and STDERR output.
 *
 * @author Shamil Vakhitov
 */
public class RuntimeRunner {
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
                    out.println(prefix(line));
                }
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }

    private static final AtomicInteger CMD_COUNTER = new AtomicInteger();

    /** Unique ID of an executed command. */
    private final int id = CMD_COUNTER.incrementAndGet();
    private final String[] commands;

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

    public void run() throws Exception {
        System.out.println(prefix("Running: " + List.of(commands)));
        long time = System.currentTimeMillis();
        Process proc = Runtime.getRuntime().exec(commands);

        new StreamGobbler(proc.getErrorStream(), System.err).start();
        new StreamGobbler(proc.getInputStream(), System.out).start();

        int code = proc.waitFor();
        System.out.println(prefix("Execution time => " + (System.currentTimeMillis() - time) + " ms."));
        if (code != 0)
            throw new Exception("Execution code: " + code);
    }
}
