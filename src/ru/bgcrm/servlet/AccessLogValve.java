package ru.bgcrm.servlet;

import java.io.CharArrayWriter;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.bgerp.app.cfg.Setup;
import org.bgerp.app.servlet.filter.AuthFilter;
import org.bgerp.util.Log;

import ru.bgcrm.model.user.User;

public class AccessLogValve extends org.apache.catalina.valves.AccessLogValve {
    private static final Log log = Log.getLog();

    public static final String PARAM_HEADER_NAME_REMOTE_ADDR = "header.name.remote.addr";

    public static final String DIR = "log/access";
    private static final String PREFIX = "access.";

    private static final String DIR_OLD = "log";
    private static final String PREFIX_OLD = "access_log.";

    /**
    * %a - Remote IP address
    * %A - Local IP address
    * %b - Bytes sent, excluding HTTP headers, or '-' if zero
    * %B - Bytes sent, excluding HTTP headers
    * %h - Remote host name (or IP address if resolveHosts is false)
    * %H - Request protocol
    * %l - Remote logical username from identd (always returns '-')
    * %m - Request method (GET, POST, etc.)
    * %p - Local port on which this request was received
    * %q - Query string (prepended with a '?' if it exists)
    * %r - First line of the request (method and request URI)
    * %s - HTTP status code of the response
    * %S - User session ID
    * %t - Date and time, in Common Log Format
    * %u - Remote user that was authenticated (if any), else '-'
    * %U - Requested URL path
    * %v - Local server name
    * %D - Time taken to process the request, in millis
    * %T - Time taken to process the request, in seconds
    * %I - current request thread name (can compare later with stacktraces)
    */

    public AccessLogValve() {
        setDirectory(DIR);

        setPrefix(PREFIX);
        setFileDateFormat("yyyy-MM-dd");
        setSuffix(".log");

        setRotatable(true);
        setMaxDays(Setup.getSetup().getInt("log.access.max.days", 60));

        setPattern("%h %S %t %T %s %b \"%r\" ");

        moveOldLogs();

        setEnabled(true);
    }

    /**
     * Moves old logs to sub directory.
     * Remove later if the world will survive, 04.04.2022
     */
    private void moveOldLogs() {
        var dirOld = new File(DIR_OLD);
        var dir = new File(DIR);
        try {
            for (var file : dirOld.listFiles(file -> file.isFile() && file.getName().startsWith(PREFIX_OLD))) {
                String name = file.getName();
                log.info("Renaming and moving access log file '{}'", name);
                file.renameTo(new File(dir, PREFIX + name.substring(PREFIX_OLD.length()) + ".log"));
            }
        } catch (Exception e) {
            log.error(e);
        }
    }

    @Override
    protected AccessLogElement[] createLogElements() {
        AccessLogElement element = new AccessLogElement() {
            @Override
            public void addElement(CharArrayWriter buf, Date date, Request request, Response response, long time)
            {
                String headerNameRemoteAddr = Setup.getSetup().get(PARAM_HEADER_NAME_REMOTE_ADDR);
                if (headerNameRemoteAddr != null) {
                    buf
                        .append(headerNameRemoteAddr)
                        .append("=")
                        .append(request.getHeader(headerNameRemoteAddr))
                        .append(" ");
                }

                User user = AuthFilter.getUser(request);
                if (user != null) {
                    buf
                        .append("UID=")
                        .append(String.valueOf(user.getId()))
                        .append(" ");
                }

                Map<String, String[]> paramMap = ((HttpServletRequest) request).getParameterMap();
                for (Map.Entry<String, String[]> me : paramMap.entrySet()) {
                    String key = me.getKey();
                    String[] values = me.getValue();

                    buf.append(key).append("=");

                    boolean first = true;

                    for (String value : values) {
                        if (!first) {
                            buf.append(" ");
                        }

                        buf.append(value);
                        first = false;
                    }

                    buf.append("&");
                }
            }

        };

        List<AccessLogElement> superList = new ArrayList<>();
        for (AccessLogElement superEl : super.createLogElements()) {
            superList.add(superEl);
        }
        superList.add(element);

        return superList.toArray(new AccessLogElement[0]);
    }
}