package org.bgerp.app.servlet.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.apache.struts.chain.commands.InvalidPathException;

public class SetCharacterEncodingFilter implements Filter {
    protected String encoding;
    protected FilterConfig filterConfig;
    protected boolean ignore;

    public void destroy() {
        this.encoding = null;
        this.filterConfig = null;
    }

    //TODO: Установка тут кодировки бессмысленна, т.к. она ставится в FormAuthenticator ранее. Разобраться и сделать!
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        // Conditionally select and set the character encoding to be used
        if (ignore || (request.getCharacterEncoding() == null)) {
            String encoding = selectEncoding(request);
            if (encoding != null)
                request.setCharacterEncoding(encoding);
        }

        // Pass control on to the next filter
        try {
            chain.doFilter(request, response);
        } catch (ServletException e) {
            // TODO: Not too correct assumption, that this filter is always running first, may be rename it to a more generic name.
            if (e.getCause() != null && e.getCause() instanceof InvalidPathException)
                throw new ServletException("Invalid path: " + ((InvalidPathException) e.getCause()).getPath(), e);
        }
    }

    public void init(FilterConfig filterConfig) throws ServletException {
        this.filterConfig = filterConfig;
        this.encoding = filterConfig.getInitParameter("encoding");
        String value = filterConfig.getInitParameter("ignore");
        if (value == null)
            this.ignore = true;
        else if (value.equalsIgnoreCase("true"))
            this.ignore = true;
        else if (value.equalsIgnoreCase("yes"))
            this.ignore = true;
        else
            this.ignore = false;
    }

    protected String selectEncoding(ServletRequest request) {
        return this.encoding;
    }
}
