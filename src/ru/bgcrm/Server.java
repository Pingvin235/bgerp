package ru.bgcrm;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.sql.SQLException;
import java.util.logging.Level;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.catalina.core.StandardContext;
import org.apache.catalina.startup.Tomcat;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.PropertyConfigurator;
import org.apache.tomcat.util.IntrospectionUtils;

import ru.bgcrm.dynamic.DynamicClassManager;
import ru.bgcrm.dynamic.model.CompilationResult;
import ru.bgcrm.event.EventProcessor;
import ru.bgcrm.event.listener.KernelSystemListeners;
import ru.bgcrm.plugin.PluginManager;
import ru.bgcrm.servlet.AccessLogValve;
import ru.bgcrm.util.AdminPortListener;
import ru.bgcrm.util.AlarmSender;
import ru.bgcrm.util.Setup;
import ru.bgcrm.util.Utils;
import ru.bgerp.util.Log;

public class Server extends Tomcat {
    private final Log log;

    //private StandardHost host;
    private StandardContext context;
    protected Thread shutdownHook;

    static {
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
        } catch (Exception e) {
        }
    }

    private Server() {
        // указание для пропатченного томкэта, чтобы при инклуде не переносил параметры
        System.setProperty("ru.bgcrm.tomcat.not.copy.include.params", "true");
        
        System.setProperty("org.apache.jasper.runtime.BodyContentImpl.LIMIT_BUFFER", "true");

        PropertyConfigurator.configureAndWatch("log4j.properties");

        // подавление сообщений вида: 10-22/16:36:48  INFO [http-bio-9089-exec-4] Parameters - Invalid chunk starting at byte [0] and ending at byte [0] with a value of [null] ignored
        java.util.logging.Logger.getLogger("").setLevel(Level.WARNING);

        log = Log.getLog(Server.class);

        try {
            log.info("Starting with '%s.properties'..", Setup.getBundleName());

            var setup = Setup.getSetup();
            checkDBConnectionOrExit(setup); 

            PluginManager.init();

            String catalinaHome = (new File(".")).getAbsolutePath();
            catalinaHome = catalinaHome.substring(0, catalinaHome.length() - 2);
            setBaseDir(catalinaHome);

            String hostname = setup.get("server.host.name", "localhost");
            getEngine().setDefaultHost(hostname);

            getHost().setName(hostname);
            getHost().setAppBase(catalinaHome + "/webapps");

            log.info("catalinaHome => " + catalinaHome + "; hostname => " + hostname);

            log.debug("create user context...");

            var customJarMarker = setup.get("custom.jar.marker", "custom");

            StandardContext context = (StandardContext) addWebapp("", catalinaHome + "/webapps");
            context.setReloadable(false);
            context.setWorkDir("work");
            context.setUseNaming(false);
            context.getJarScanner().setJarScanFilter((type, name) -> {
                boolean result = name.contains("struts") || name.contains("tag") || name.contains(customJarMarker);
                log.debug("Scan type: %s, name: %s => %s", type, name, result);
                return result;
            });

            // логгер запросов
            context.addValve(new AccessLogValve());
            
            log.debug("create connector..");

            int portHttp = setup.getInt("server.port.http", 8080);
            String host = setup.get("server.listen.address", null);

            log.info("Try start server HTTP port: " + portHttp + "; listen address: " + host);

            String connectorHttpThreadMax = setup.get("connector.http.thread.max", "25");

            var connector = getConnector();

            if (StringUtils.isNotBlank(host))
                IntrospectionUtils.setProperty(connector, "address", host);
            
            connector.setPort(portHttp);
            connector.setEnableLookups(false);
            connector.setURIEncoding(Utils.UTF8.name());
            connector.setUseBodyEncodingForURI(true);
            connector.setMaxPostSize(setup.getInt("max.post.size", 10000000));
            connector.setMaxSavePostSize(1000000);
            connector.setProperty("maxThreads", connectorHttpThreadMax);

            start();

            int adminPort = setup.getInt("server.port.admin", 8005);
            new AdminPortListener(adminPort);

            new KernelSystemListeners();

            // перекомпиляция динамического кода
            CompilationResult result = DynamicClassManager.getInstance().recompileAll();

            log.info("Compile dyn classes result:");
            log.info(result.getLogString());

            for (String className : Utils.toSet(setup.get("runOnStart"))) {
                log.info("Run class on start: " + className);

                try {
                    ((Runnable) DynamicClassManager.newInstance(className)).run();
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }

            EventProcessor.subscribeDynamicClasses();

            Scheduler.getInstance();

            AlarmSender.initSender(setup);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            System.exit(1);
        }
    }

    private void checkDBConnectionOrExit(Setup setup) {
        try (var con = setup.getDBConnectionFromPool()) {
            if (con == null)
                throw new SQLException("SQL connection was null");
        } catch (SQLException e) {
            log.error("Problem with getting SQL connection, stopping..", e);
            System.exit(2);
        }
    }

    public void reloadRoot() {
        context.reload();
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
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
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