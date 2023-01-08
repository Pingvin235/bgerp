package org.bgerp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.logging.Level;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.catalina.LifecycleException;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.startup.Tomcat;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.PropertyConfigurator;
import org.apache.tomcat.util.IntrospectionUtils;
import org.apache.tomcat.util.http.Rfc6265CookieProcessor;
import org.bgerp.custom.Custom;
import org.bgerp.scheduler.Scheduler;
import org.bgerp.util.Log;

import ru.bgcrm.dynamic.DynamicClassManager;
import ru.bgcrm.plugin.PluginManager;
import ru.bgcrm.servlet.AccessLogValve;
import ru.bgcrm.util.AdminPortListener;
import ru.bgcrm.util.AlarmSender;
import ru.bgcrm.util.Setup;
import ru.bgcrm.util.Utils;

/**
 * Web server, the entry point of the application.
 *
 * @author Shamil Vakhitov
 */
public class Server extends Tomcat {
    private static final String WORK_DIR_NAME = "work";
    public static final String WEBAPPS_DIR_NAME = "webapps";

    private final Log log = Log.getLog();
    private final Setup setup;

    private Server() {
        installTrustManager();
        setTomcatProperties();
        configureLogging();

        log.info("Starting with '{}.properties'..", Setup.getBundleName());
        log.info(AdminPortListener.getVersionInfo());

        setup = Setup.getSetup();
        try {
            checkDBConnectionOrExit();

            PluginManager.init();

            String catalinaHome = new File(".").getAbsolutePath();
            catalinaHome = catalinaHome.substring(0, catalinaHome.length() - 2);
            setBaseDir(catalinaHome);

            log.info("catalinaHome: {}; hostname: {}", catalinaHome, hostname);

            log.info("Cleaning up {} directory.", WORK_DIR_NAME);
            FileUtils.cleanDirectory(new File(WORK_DIR_NAME));

            configureContext(catalinaHome);

            startServer();

            new AdminPortListener(setup.getInt("server.port.admin", 8005));

            // TODO: Replace by Custom logic.
            DynamicClassManager.getInstance().recompileAll();

            doOnStart();

            Scheduler.getInstance();

            AlarmSender.initSender(setup);
        } catch (Exception e) {
            log.error(e);
            System.exit(1);
        }
    }

    private void configureLogging() {
        PropertyConfigurator.configureAndWatch("log4j.properties");
        // suppress messages like: 10-22/16:36:48  INFO [http-bio-9089-exec-4] Parameters - Invalid chunk starting at byte [0] and ending at byte [0] with a value of [null] ignored
        java.util.logging.Logger.getLogger("").setLevel(Level.WARNING);
    }

    private void setTomcatProperties() {
        // for the patched Tomcat, do not inherit params to includes
        System.setProperty("org.apache.catalina.core.ApplicationHttpRequest.INHERIT_REQUEST_PARAMETERS", "false");
        // prevents incomplete HTML result in JSP
        System.setProperty("org.apache.jasper.runtime.BodyContentImpl.LIMIT_BUFFER", "true");
    }

    private void configureContext(String catalinaHome) {
        var customJarMarker = setup.get("custom.jar.marker", "custom");

        var context = (StandardContext) addWebapp("", catalinaHome + "/" + WEBAPPS_DIR_NAME);
        context.setReloadable(false);
        context.setWorkDir(WORK_DIR_NAME);
        context.setUseNaming(false);

        /* Similar to XML configuration: <CookieProcessor sameSiteCookies="strict" /> */
        var cookieProcessor = new Rfc6265CookieProcessor();
        cookieProcessor.setSameSiteCookies("strict");
        context.setCookieProcessor(cookieProcessor);

        context.getJarScanner().setJarScanFilter((type, name) -> {
            boolean result = name.contains("struts") || name.contains("tag") || name.contains(customJarMarker);
            log.debug("Scan type: {}, name: {} => {}", type, name, result);
            return result;
        });

        // TODO: Extract to secure.log plugin.
        context.addValve(new AccessLogValve());

        Custom.getInstance().webapps(catalinaHome, context);
    }

    /**
     * Starts Web server.
     * @throws LifecycleException
     */
    private void startServer() throws LifecycleException {
        int port = setup.getInt("server.port.http", 8080);
        var address = setup.get("server.listen.address", null);

        log.info("Starting server HTTP port: {}; listen address: {}", port, address);

        var connector = getConnector();

        if (StringUtils.isNotBlank(address))
            IntrospectionUtils.setProperty(connector, "address", address);

        connector.setPort(port);
        connector.setEnableLookups(false);
        connector.setURIEncoding(StandardCharsets.UTF_8.name());
        connector.setUseBodyEncodingForURI(true);
        connector.setMaxPostSize(setup.getInt("max.post.size", 10000000));
        connector.setMaxSavePostSize(1000000);
        connector.setProperty("maxThreads", setup.get("connector.http.thread.max", "25"));

        start();

        System.out.println(Log.format("Server URL: 'http://{}:{}', see logs in 'log' directory", hostname, port));
    }

    private void doOnStart() {
        for (String className : Utils.toSet(setup.get("runOnStart"))) {
            log.info("Run class on start: " + className);
            try {
                ((Runnable) DynamicClassManager.newInstance(className)).run();
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }

        // EventProcessor.subscribeDynamicClasses();
    }

    private void checkDBConnectionOrExit() {
        try (var con = setup.getDBConnectionFromPool()) {
            if (con == null)
                throw new SQLException("SQL connection was null");
        } catch (SQLException e) {
            log.error(e);
            Utils.errorAndExit(2, "Problem with getting SQL connection, stopping. See log for details.");
        }
    }

    /**
     * Sets trust manager, accepting all server certificates.
     */
    private void installTrustManager() {
        // Create a trust manager that does not validate certificate chains
        TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return null;
            }

            public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {}

            public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {}
        } };
        HostnameVerifier hv = new HostnameVerifier() {
            public boolean verify(String urlHostName, SSLSession session) {
                if (!urlHostName.equalsIgnoreCase(session.getPeerHost())) {
                    System.out.println("Warning: URL host '" + urlHostName + "' is different to SSLSession host '" + session.getPeerHost() + "'.");
                }
                return true;
            }
        };
        // Install the all-trusting trust manager
        try {
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier(hv);
        } catch (Exception e) {}
    }

    public static void main(String[] args) {
        int adminPort = new Setup(Setup.getBundleName(), false).getInt("server.port.admin", 8005);
        if (args.length > 0) {
            if (args[0].trim().equals("start")) {
                new Server();
            } else {
                executeCommand(args, adminPort, 1);
            }
        } else {
            showHelp();
        }
    }

    private static void executeCommand(String[] args, int port, int cnt) {
        try {
            String command = "";
            if (args.length >= 1) {
                command = args[0];

                String result = "";
                Socket socket = null;
                try {
                    socket = new Socket(InetAddress.getByName("127.0.0.1"), port);

                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);

                    out.println(command);

                    result = in.readLine().replace('$', '\n');
                    socket.close();

                    System.out.println(result);

                    if (AdminPortListener.RESPONSE_SCHEDULER_HAS_TASKS.equals(result)) {
                        Thread.sleep(5000);
                        System.out.println("RETRY: " + cnt);
                        executeCommand(args, port, ++cnt);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    Utils.errorAndExit(1, ex.getMessage());
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            Utils.errorAndExit(1, ex.getMessage());
        }
    }

    private static void showHelp() {
        StringBuffer sb = new StringBuffer();
        sb.append("\nUsage: [start|stop|status|help]");
        sb.append("\nParameters:");
        sb.append("\n\t help                       - show this help");
        sb.append("\n\t start                      - starting");
        sb.append("\n\t stop                       - stopping");
        sb.append("\n\t status                     - show status");
        sb.append("\n\t gc                         - force Garbage Collector");
        sb.append("\n\nExample: crm.sh start");
        sb.append("\nExample: crm.sh status");

        System.out.println(sb.toString());
    }

}