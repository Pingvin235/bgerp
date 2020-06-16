package ru.bgcrm.servlet.filter;

import java.io.IOException;
import java.io.PrintWriter;
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
import org.apache.catalina.connector.RequestFacade;

import ru.bgcrm.cache.UserCache;
import ru.bgcrm.event.EventProcessor;
import ru.bgcrm.event.authentication.UserAuthenticatingEvent;
import ru.bgcrm.event.authentication.UserAuthenticationEvent;
import ru.bgcrm.model.authentication.AuthenticationMode;
import ru.bgcrm.model.authentication.AuthenticationResult;
import ru.bgcrm.model.authentication.BGAuthenticationException;
import ru.bgcrm.model.user.User;
import ru.bgcrm.servlet.LoginStat;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.Setup;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.distr.VersionInfo;
import ru.bgerp.util.Log;

public class AuthFilter implements Filter {
    private static Log log = Log.getLog();

    public static final String REQUEST_ATTRIBUTE_USER_ID_NAME = "ru.bgcrm.servlet.filter.AuthFilter.session.USER_ID";
    public static final String REQUEST_ATTRIBUTE_USER_IP_ADDRESS_NAME = "ru.bgcrm.servlet.filter.AuthFilter.session.USER_IP_ADDRESS";
    private static final String REQUEST_ATTRIBUTE_USER_NAME = "ru.bgcrm.servlet.filter.AuthFilter.request.USER";

    private static final String LOGIN_ACTION = "/login.do";
    private static final String SHELL_PAGE = "/shell.jsp";

    private static final String version = Utils.maskEmpty(VersionInfo.getVersionInfo("update").getVersion(), "UNDEF");

    public void init(FilterConfig filterConfig) throws ServletException {}

    public void destroy() {}

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {
        HttpServletRequest request = (RequestFacade) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        User user = null;

        try {
            processUserAuthenticatingEvent(request, response);

            // аутентификация пользователя по имени и паролю, выброс исключения по ошибке
            user = authenticateUser(request, response);

            if (Utils.parseBoolean(request.getParameter("authToSession"), true)) {
                HttpSession session = request.getSession();
                session.setAttribute(REQUEST_ATTRIBUTE_USER_ID_NAME, user.getId());

                LoginStat.getLoginStat().userLoggedIn(session, user);
            }

            processUserAuthenticationEvent(user, request, response, AuthenticationMode.REQUEST, AuthenticationResult.SUCCESS);
        } catch (BGAuthenticationException exception) {
            request.setAttribute("authenticationException", exception);
            AuthenticationResult authenticationResult = exception.getAuthenticationResult();

            if (!authenticationResult.equals(AuthenticationResult.USERNAME_AND_PASSWORD_NOT_DEFINED)) {
                processUserAuthenticationEvent(user, request, response, AuthenticationMode.REQUEST, authenticationResult);
            } else {
                HttpSession session = request.getSession(false);
                if (session != null) {
                    Integer userId = (Integer) session.getAttribute(REQUEST_ATTRIBUTE_USER_ID_NAME);
                    if (userId != null)
                        user = UserCache.getUser(userId);
                }
            }
        }

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
                        response.sendRedirect(requestURI + "/");
                }
            } else {
                request.setAttribute(REQUEST_ATTRIBUTE_USER_NAME, user);
                filterChain.doFilter(servletRequest, servletResponse);
            }
        } else {
            forwardException(request, response);
        }
    }

    public void forward(HttpServletRequest request, HttpServletResponse response, String page) throws IOException {
        ServletContext servletContext = request.getServletContext();
        RequestDispatcher requestDispatcher = servletContext.getRequestDispatcher(page);

        try {
            requestDispatcher.forward(request, response);
        } catch (ServletException e) {
            log.error(e.getMessage(), e);

            request.setAttribute(RequestDispatcher.ERROR_EXCEPTION, e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "ERROR");
        }
    }

    public static final User getUser(HttpServletRequest request) {
        return (User) request.getAttribute(REQUEST_ATTRIBUTE_USER_NAME);
    }

    /**
     * Процедура аутентификации пользователя по имени и паролю, переданных в
     * параметрах HTTP запроса.
     *
     * @param request
     * @param response
     * @return Объект аутентифицированного пользователя
     * @throws BGAuthenticationException
     */
    private User authenticateUser(HttpServletRequest request, HttpServletResponse response)
            throws BGAuthenticationException {
        String username = request.getParameter(Constants.FORM_USERNAME);
        String password = request.getParameter(Constants.FORM_PASSWORD);

        if (username == null && password == null) {
            throw new BGAuthenticationException(AuthenticationResult.USERNAME_AND_PASSWORD_NOT_DEFINED);
        }

        if (username == null) {
            throw new BGAuthenticationException(AuthenticationResult.USERNAME_IS_NOT_DEFINED);
        }

        if (password == null) {
            throw new BGAuthenticationException(AuthenticationResult.PASSWORD_IS_NOT_DEFINED);
        }

        if ("".equals(username)) {
            throw new BGAuthenticationException(AuthenticationResult.USERNAME_IS_EMPTY);
        }

        if ("".equals(password)) {
            throw new BGAuthenticationException(AuthenticationResult.PASSWORD_IS_EMPTY);
        }

        User user = UserCache.getUser(username);

        if (user == null) {
            throw new BGAuthenticationException(AuthenticationResult.USER_NOT_FOUND);
        }

        if (user.getStatus() != User.STATUS_ENABLE) {
            throw new BGAuthenticationException(AuthenticationResult.USER_NOT_ENABLED);
        }

        if (!password.equals(user.getPassword())) {
            throw new BGAuthenticationException(AuthenticationResult.PASSWORD_INCORRECT);
        }

        if (Utils.toSet(user.getConfigMap().get("denyAuthInVersion")).contains(version)) {
            throw new BGAuthenticationException(AuthenticationResult.DENY_AUTH_IN_VERSION);
        }

        return user;
    }

    private void processUserAuthenticatingEvent(HttpServletRequest request, HttpServletResponse response) {
        try {
            UserAuthenticatingEvent userAuthenticatingEvent = new UserAuthenticatingEvent(request, response);
            String userAuthenticatingEventListener = Setup.getSetup().get("userAuthenticatingEventListener");

            EventProcessor.processEvent(userAuthenticatingEvent, userAuthenticatingEventListener, null);
        } catch (Exception exception) {
            log.error(exception);
        }
    }

    private void processUserAuthenticationEvent(
            User user, HttpServletRequest request, HttpServletResponse response,
            AuthenticationMode authenticationMode, AuthenticationResult authenticationResult) {
        try {
            UserAuthenticationEvent userAuthenticationEvent = new UserAuthenticationEvent(
                    user, request, response,
                    authenticationMode, authenticationResult);
            String userAuthenticationEventListener = Setup.getSetup().get("userAuthenticationEventListener");

            EventProcessor.processEvent(userAuthenticationEvent, userAuthenticationEventListener, null);
        } catch (Exception exception) {
            log.error(exception);
        }
    }

    private void forwardException(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String responseType = request.getParameter("responseType");

        if (Utils.isBlankString(responseType)) {
            responseType = DynActionForm.RESPONSE_TYPE_HTML;
        }

        if (responseType.equals(DynActionForm.RESPONSE_TYPE_HTML)) {
            forward(request, response, LOGIN_ACTION);
        } else if (responseType.equals(DynActionForm.RESPONSE_TYPE_JSON)) {
            writeException(request, response);
        }
    }

    private void writeException(HttpServletRequest request, HttpServletResponse response) throws IOException {
        BGAuthenticationException authenticationException = (BGAuthenticationException) request
                .getAttribute("authenticationException");

        response.setContentType("text/plain; charset=" + Utils.UTF8.name());
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        PrintWriter out = response.getWriter();
        out.write(authenticationException.getMessage());
        out.close();
    }
}
