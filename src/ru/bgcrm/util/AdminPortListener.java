package ru.bgcrm.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.ThreadMXBean;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.bgerp.util.Log;

import ru.bgcrm.Scheduler;
import ru.bgcrm.dynamic.DynamicClassManager;
import ru.bgcrm.model.BGException;
import ru.bgcrm.util.distr.VersionInfo;

public class AdminPortListener implements Runnable {
    private Log log = Log.getLog();

    private static final java.util.Date START_TIME = new java.util.Date();
    public static final String RESPONSE_SCHEDULER_HAS_TASKS = "Scheduler has running tasks, need wait to stop.";

    protected ServerSocket s = null;
    protected boolean run = true;

    public AdminPortListener(int port) {
        log.info("Starting listen admin port " + port);
        try {
            s = new ServerSocket();
            s.bind(new InetSocketAddress(InetAddress.getLoopbackAddress(), port));
        } catch (Exception ex) {
            log.error("Port " + port + " is busy! [" + ex.getMessage() + "]");
            System.exit(1);
        }

        start();
    }

    public void run() {
        while (run && s != null) {
            Socket socket = null;
            try {
                // ждемс.....
                socket = s.accept();

                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);

                try {
                    final String command = in.readLine().trim();

                    log.info("Executing " + command);

                    if (command.equals("stop")) {
                        if (Scheduler.getInstance().getActiveTaskCount() > 0) {
                            out.println(RESPONSE_SCHEDULER_HAS_TASKS);
                        } else {
                            out.println("OK stopping..");
                            System.exit(0);
                        }
                    } else if (command.equals("status")) {
                        out.println(getStatus().replace('\n', '$'));
                    } else if (command.equals("gc")) {
                        System.gc();
                        out.println("GC forced..");
                    } else if (command.startsWith("runclass")) {
                        final String className = StringUtils.substringAfter(command, " ").trim();

                        Class<?> clazz = null;
                        try {
                            clazz = DynamicClassManager.getClass(className);
                        } catch (ClassNotFoundException e) {
                            throw new BGException("Class not found: " + className, e);
                        }

                        if (Runnable.class.isAssignableFrom(clazz)) {
                            new Thread((Runnable) clazz.getDeclaredConstructor().newInstance()).start();
                        } else {
                            out.println("Incorrect Runnable class.");
                        }

                        out.println("Dynamic class started..");
                    } else {
                        out.println("Unknown admin command: " + command);
                    }
                } catch (Exception ex) {
                    out.println(ex.getMessage());
                    log.error(ex.getMessage(), ex);
                } finally {
                    socket.close();
                }
            } catch (Exception ex) {
                log.error(ex.getMessage(), ex);
            }
        }
    }

    public static String getVersionInfo() {
        var result = new StringBuilder(200);

        var vi = VersionInfo.getVersionInfo(VersionInfo.MODULE_UPDATE);
        var viLib = VersionInfo.getVersionInfo(VersionInfo.MODULE_UPDATE_LIB);

        result
            .append("BGERP ")
            .append(vi.getVersion())
            .append(".")
            .append(vi.getBuildNumber())
            .append(" from ")
            .append(vi.getBuildTime())
            .append("; lib set ")
            .append(viLib.getBuildNumber())
            .append(" from ")
            .append(viLib.getBuildTime());

        return result.toString();
    }

    public static String getStatus() {
        var result = new StringBuilder(1000);

        result
            .append(getVersionInfo())
            .append("\n")
            .append(uptimeStatus())
            .append("\n")
            .append(memoryStatus())
            .append("\n")
            .append(Setup.getSetup().getPoolStatus());

        return result.toString();
    }

    /**
     * Возвращает строковый статус uptime чего либо (с момента инициации Java-приложения).
     * Используется в отдельнозапущенных серверах для информации в статусе.
     * @return
     */
    public static final String uptimeStatus() {
        StringBuilder report = new StringBuilder(100);

        report.append("Started: ");
        report.append(TimeUtils.format(START_TIME, TimeUtils.FORMAT_TYPE_YMDHMS));
        report.append("\t");
        report.append("Now: ");
        report.append(new Date());
        report.append("\t");

        long delta = (System.currentTimeMillis() - START_TIME.getTime()) / 1000L;

        int days = (int) (delta / 86400);
        delta -= days * 86400;
        int hours = (int) (delta / 3600);
        delta -= hours * 3600;
        int min = (int) (delta / 60);
        delta -= min * 60;
        int sec = (int) (delta);

        DecimalFormat dfTime = new DecimalFormat("00");
        report.append("Uptime: ");
        report.append(days);
        report.append(" d ");
        report.append(dfTime.format(hours));
        report.append(":");
        report.append(dfTime.format(min));
        report.append(":");
        report.append(dfTime.format(sec));

        return report.toString();
    }

    /**
     * Возвращает строку с состоянием памяти приложения.
     * @return
     */
    public static final String memoryStatus() {
        StringBuilder report = new StringBuilder(50);

        DecimalFormat df = new DecimalFormat("###,###,###,###");
        DecimalFormatSymbols dfs = new DecimalFormatSymbols();
        dfs.setGroupingSeparator(' ');

        df.setDecimalFormatSymbols(dfs);

        Runtime r = Runtime.getRuntime();
        report.append("Memory total: ");
        report.append(df.format(r.totalMemory()));
        report.append("; max: ");
        report.append(df.format(r.maxMemory()));
        report.append("; free: ");
        report.append(df.format(r.freeMemory()));

        report.append("\nMemory pools:");
        for (final MemoryPoolMXBean pool : ManagementFactory.getMemoryPoolMXBeans()) {
            report.append("\n  ");
            report.append(pool.getType() + "[" + pool.getName() + "]: ");
            report.append("max: ");
            report.append(df.format(pool.getUsage().getMax()));
            report.append("; used: ");
            report.append(df.format(pool.getUsage().getUsed()));
            report.append("; peek: ");
            report.append(df.format(pool.getPeakUsage().getUsed()));
        }

        ThreadMXBean threads = ManagementFactory.getThreadMXBean();
        report.append("\nThread count: ");
        report.append(threads.getThreadCount());

        return report.toString();
    }

    public void start() {
        new Thread(this, "AdminPortListener").start();
    }
}
