package org.bgerp.app.servlet;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.bgerp.app.cfg.Setup;
import org.bgerp.app.servlet.util.AccessLogValve;

public class ServletUtils {
    /**
     * Apache Tomcat - specific field, containing  included URL
     */
    private static final String INCLUDE_URI_FIELD_NAME = "requestDispatcherPath";

    /**
     * Retrieves request URI with support of includes in Tomcat
     * @param request
     * @return
     */
    public static String getRequestURI(HttpServletRequest request) {
        String uri = request.getRequestURI();

        // hack for Tomcat includes
        try {
            uri = (String) FieldUtils.readDeclaredField(request, INCLUDE_URI_FIELD_NAME, true);
        } catch (Exception e) {}

        return uri;
    }

    /**
     * Gets IP address of request from
     * HTTP header 'X-Real-IP' or another defined in configuration param {@link AccessLogValve#PARAM_HEADER_NAME_REMOTE_ADDR}
     * or {@link ServletRequest#getRemoteAddr()}
     * @return
     */
    public static String getHttpRequestRemoteAddr(HttpServletRequest httpRequest) {
        String headerNameRemoteAddress = Setup.getSetup().get(AccessLogValve.PARAM_HEADER_NAME_REMOTE_ADDR, "X-Real-IP");
        String result = httpRequest.getHeader(headerNameRemoteAddress);
        if (result == null)
            result = httpRequest.getRemoteAddr();
        return result;
    }
}
