package org.bgerp.servlet.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import ru.bgerp.util.Log;

public class OpenFilter implements Filter {
    private static final Log log = Log.getLog();

    private static final String OPEN_URL_PREFIX = "/open/";
    private static final String REQUEST_ATTRIBUTE_URI = OpenFilter.class.getName() + ".URI";

    public void init(FilterConfig filterConfig) throws ServletException {}

    public void destroy() {}

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {
        var request = (HttpServletRequest) servletRequest;

        var requestURI = request.getRequestURI();
        if (requestURI.endsWith(".jsp")) {
            log.warn("Attempt to access JSP: {}", requestURI);
            return;
        }

        if (requestURI.endsWith(".do")) {
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }
        
        var requestDispatcher = request.getServletContext().getRequestDispatcher(OPEN_URL_PREFIX + "shell.jsp");
        request.setAttribute(REQUEST_ATTRIBUTE_URI, requestURI);
        requestDispatcher.forward(request, servletResponse);
    }

    /** 
     * After forwarding to shell.jsp original requestURL is lost, so it is preserved as an attribute. 
     * @return preserved URI or null
     * */
    public static final String getRequestURI(ServletRequest request) {
        return (String) request.getAttribute(REQUEST_ATTRIBUTE_URI);
    }
}
