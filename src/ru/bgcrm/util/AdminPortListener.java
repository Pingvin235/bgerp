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

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import ru.bgcrm.Scheduler;
import ru.bgcrm.dynamic.DynamicClassManager;
import ru.bgcrm.model.BGException;
import ru.bgcrm.util.distr.VersionInfo;

public class AdminPortListener implements Runnable {
    private Logger logger = Logger.getLogger(AdminPortListener.class);
    
    public static final String RESPONSE_SCHEDULER_HAS_TASKS = "Scheduler has running tasks, need wait to stop.";

    protected ServerSocket s = null;
    protected boolean run = true;

    public AdminPortListener(int port) {
        logger.info("Starting listen admin port " + port);
        try {
            s = new ServerSocket();
            s.bind(new InetSocketAddress(InetAddress.getLoopbackAddress(), port));
        } catch (Exception ex) {
            logger.error("Port " + port + " is busy! [" + ex.getMessage() + "]");
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

                    logger.info("Executing " + command);

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
                    logger.error(ex.getMessage(), ex);
                } finally {
                    socket.close();
                }
            } catch (Exception ex) {
                logger.error(ex.getMessage(), ex);
            }
        }
    }

    public static String getStatus() {
        StringBuilder status = new StringBuilder(300);

        VersionInfo vi = VersionInfo.getVersionInfo("update");
        VersionInfo viLib = VersionInfo.getVersionInfo("update_lib");

        status.append("BGERP v ");
        status.append(vi.getVersion());
        status.append(" build ");
        status.append(vi.getBuildNumber());
        status.append(" from ");
        status.append(vi.getBuildTime());
        status.append("; update_lib build ");
        status.append(viLib.getBuildNumber());
        status.append(" from ");
        status.append(viLib.getBuildTime());
        status.append("\n");
        status.append(uptimeStatus());
        status.append("\n");
        status.append(memoryStatus());
        status.append("\n");
        status.append(Setup.getSetup().getPoolStatus());

        return status.toString();
    }

    private static java.util.Date startTime = new java.util.Date();

    /**
     * Возвращает строковый статус uptime чего либо (с момента инициации Java-приложения). 
     * Используется в отдельнозапущенных серверах для информации в статусе. 
     * @return
     */
    public static final String uptimeStatus() {
        StringBuilder report = new StringBuilder(100);

        report.append("Started: ");
        report.append(TimeUtils.format(startTime, TimeUtils.FORMAT_TYPE_YMDHMS));
        report.append("\t");

        long delta = (System.currentTimeMillis() - startTime.getTime()) / 1000L;

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
