package org.bgerp.servlet.filter;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.catalina.authenticator.Constants;
import org.bgerp.event.AuthEvent;
import org.bgerp.servlet.LoginStat;
import org.bgerp.util.Log;

import ru.bgcrm.cache.UserCache;
import ru.bgcrm.event.EventProcessor;
import ru.bgcrm.model.user.User;
import ru.bgcrm.servlet.filter.SetRequestParamsFilter;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.Setup;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.sql.SingleConnectionConnectionSet;
import ru.bgerp.l10n.Localization;

/**
 * Servlet auth filter.
 *
 * @author Shamil Vakhitov
 */
public class AuthFilter implements Filter {
    private static Log log = Log.getLog();

    public static final String REQUEST_ATTRIBUTE_USER_ID_NAME = "ru.bgcrm.servlet.filter.AuthFilter.session.USER_ID";
    public static final String REQUEST_ATTRIBUTE_USER_IP_ADDRESS_NAME = "ru.bgcrm.servlet.filter.AuthFilter.session.USER_IP_ADDRESS";
    private static final String REQUEST_ATTRIBUTE_USER_NAME = "ru.bgcrm.servlet.filter.AuthFilter.request.USER";

    private static final String LOGIN_ACTION = "/login.do";
    private static final String SHELL_PAGE = "/shell.jsp";

    public void init(FilterConfig filterConfig) throws ServletException {}

    public void destroy() {}

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        User user = authUser(request, response);
        user = userInSession(request, user);

        if (user != null) {
            String requestURI = request.getRequestURI();

            if (!requestURI.endsWith(".do") && !requestURI.endsWith(".jsp")) {
                request.setAttribute(REQUEST_ATTRIBUTE_USER_NAME, user);

                for (Map.Entry<String, Object> me : SetRequestParamsFilter.getContextVariables(request).entrySet())
                    request.setAttribute(me.getKey(), me.getValue());

                String app = request.getParameter("app");
                app = Utils.notBlankString(app) ? "?app=" + app : "";

                String realm = null;
                // user or usermob
                if (requestURI.startsWith("/user")) {
                    realm = requestURI.substring(1);

                    int pos = realm.indexOf('/');
                    if (pos > 0) {
                        realm = realm.substring(0, pos);
                        forward(request, response, "/" + realm + SHELL_PAGE + app);
                    }
                    // запрос заканчивается на /user или /usermob - редирект со слешем, так как это создаёт проблемы в вызове меню
                    else
                        response.sendRedirect(requestURI + "/" + app);
                }
            } else {
                request.setAttribute(REQUEST_ATTRIBUTE_USER_NAME, user);
                filterChain.doFilter(servletRequest, servletResponse);
            }
        } else {
            var l = Localization.getSysLocalizer();
            forwardError(request, response, l.l("Ошибка авторизации"));
        }
    }

    /**
     * Authenticates user by username and password, sent in HTTP request.
     *
     * @param request
     * @param response
     * @return authenticated user or {@code null}.
     */
    private User authUser(HttpServletRequest request, HttpServletResponse response) {
        final String login = request.getParameter(Constants.FORM_USERNAME);
        final String password = request.getParameter(Constants.FORM_PASSWORD);

        if (Utils.isBlankString(login) || Utils.isBlankString(password)) {
            return null;
        }

        var user = UserCache.getUser(login);

        // local user
        if (user != null && user.getStatus() == User.STATUS_ACTIVE) {
            if (!password.equals(user.getPassword())) {
                log.debug("User ID: {} password is wrong", user.getId());
                user = null;
            }
            return user;
        }

        // if user not found or marked as external - request to plugins
        if (user == null || user.getStatus() == User.STATUS_EXTERNAL) {
            var event = new AuthEvent(login, password, user);

            try (var con = Setup.getSetup().getDBConnectionFromPool()) {
                EventProcessor.processEvent(event, new SingleConnectionConnectionSet(con));
            } catch (Exception e) {
                log.error(e);
            }

            if (event.isProcessed())
                user = event.getUser();
            else
                user = null;
        }

        return user;
    }

    /**
     * Stores user to session or restores from there.
     * @param request
     * @param user
     * @return
     */
    private User userInSession(HttpServletRequest request, User user) {
        if (user != null) {
            if (Utils.parseBoolean(request.getParameter("authToSession"), true)) {
                HttpSession session = request.getSession();
                session.setAttribute(REQUEST_ATTRIBUTE_USER_ID_NAME, user.getId());

                LoginStat.getLoginStat().userLoggedIn(session, user, DynActionForm.getHttpRequestRemoteAddr(request));
            }
        } else {
            HttpSession session = request.getSession(false);
            if (session != null) {
                Integer userId = (Integer) session.getAttribute(REQUEST_ATTRIBUTE_USER_ID_NAME);
                if (userId != null)
                    user = UserCache.getUser(userId);
            }
        }
        return user;
    }

    /**
     * Gets the current user, preserved in request's attribute.
     * @param request
     * @return
     */
    public static final User getUser(HttpServletRequest request) {
        return (User) request.getAttribute(REQUEST_ATTRIBUTE_USER_NAME);
    }

    private void forward(HttpServletRequest request, HttpServletResponse response, String page) throws IOException {
        ServletContext servletContext = request.getServletContext();
        RequestDispatcher requestDispatcher = servletContext.getRequestDispatcher(page);

        try {
            requestDispatcher.forward(request, response);
        } catch (ServletException e) {
            log.error(e);

            request.setAttribute(RequestDispatcher.ERROR_EXCEPTION, e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "ERROR");
        }
    }

    private void forwardError(HttpServletRequest request, HttpServletResponse response, String error) throws IOException {
        String responseType = request.getParameter("responseType");

        if (Utils.isBlankString(responseType)) {
            responseType = DynActionForm.RESPONSE_TYPE_HTML;
        }

        if (responseType.equals(DynActionForm.RESPONSE_TYPE_HTML)) {
            forward(request, response, LOGIN_ACTION);
        } else if (responseType.equals(DynActionForm.RESPONSE_TYPE_JSON)) {
            response.setContentType("text/plain; charset=" + StandardCharsets.UTF_8.name());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

            PrintWriter out = response.getWriter();
            out.write(error);
            out.close();
        }
    }
}
