package org.bgerp.app.servlet;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.reflect.FieldUtils;

public class ServletUtils {
    /**
     * Apache Tomcat - specific field, containing  included URL
     */
    private static final String INCLUDE_URI_FIELD_NAME = "requestDispatcherPath";

    public static String getRequestURI(HttpServletRequest request) {
        String uri = request.getRequestURI();

        // hack for Tomcat includes
        try {
            uri = (String) FieldUtils.readDeclaredField(request, INCLUDE_URI_FIELD_NAME, true);
        } catch (Exception e) {}

        return uri;
    }
}
