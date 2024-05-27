package org.bgerp.action;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;
import org.bgerp.action.util.Invoker;
import org.bgerp.app.cfg.ConfigMap;
import org.bgerp.app.cfg.Preferences;
import org.bgerp.app.cfg.Setup;
import org.bgerp.app.dist.lic.AppLicense;
import org.bgerp.app.event.EventProcessor;
import org.bgerp.app.exception.BGIllegalArgumentException;
import org.bgerp.app.exception.BGMessageException;
import org.bgerp.app.l10n.Localizer;
import org.bgerp.app.servlet.filter.AuthFilter;
import org.bgerp.app.servlet.user.LoginStat;
import org.bgerp.app.servlet.user.event.ActionRequestEvent;
import org.bgerp.cache.UserCache;
import org.bgerp.util.Log;
import org.bgerp.util.log.SessionLogAppender;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import javassist.NotFoundException;
import ru.bgcrm.dao.Locker;
import ru.bgcrm.dao.user.UserDAO;
import ru.bgcrm.model.LastModify;
import ru.bgcrm.model.Lock;
import ru.bgcrm.model.Page;
import ru.bgcrm.model.user.PermissionNode;
import ru.bgcrm.model.user.User;
import ru.bgcrm.servlet.filter.SetRequestParamsFilter;
import ru.bgcrm.struts.action.PoolAction;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.TimeUtils;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.sql.ConnectionSet;
import ru.bgcrm.util.sql.SingleConnectionSet;

public class BaseAction extends DispatchAction {
    public static final ObjectMapper MAPPER = new ObjectMapper();

    static {
        // TODO: Разобраться с сериализацией дат и дата+время в JSON, узнать как в биллинге.
        // при указании данной опции сериализуется в виде:
        // "2013-11-22T18:37:55.645+0011", вроде как миллисекунды и не нужны.
        // mapper.configure( SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false );
        MAPPER.setTimeZone(TimeZone.getDefault());
    }

    protected static final String PATH_JSP = "/WEB-INF/jspf";

    public static final String PATH_JSP_ADMIN = PATH_JSP + "/admin";
    public static final String PATH_JSP_USER = PATH_JSP + "/user";
    public static final String PATH_JSP_USERMOB = PATH_JSP + "/usermob";
    public static final String PATH_JSP_OPEN = PATH_JSP + "/open";

    protected final Log log = Log.getLog(this.getClass());

    protected final Setup setup = Setup.getSetup();

    /**
     * Cache for method invokers for the action class. Key - method name.
     */
    private final Map<String, Invoker> invokerMap = new ConcurrentHashMap<>();

    protected Localizer l;

    protected BaseAction() {
        super();
    }

    /**
     * Default action method if no parameter 'action' passed. Overwrite and implement.
     * @param form
     * @param conSet
     * @return
     * @throws Exception
     */
    public ActionForward unspecified(DynActionForm form, ConnectionSet conSet) throws Exception {
        throw new UnsupportedOperationException();
    }

    /**
     * Default action method if no parameter 'action' passed. Overwrite and implement.
     * @param form
     * @param con
     * @return
     * @throws Exception
     */
    public ActionForward unspecified(DynActionForm form, Connection con) throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    protected ActionForward dispatchMethod(ActionMapping mapping, ActionForm actionForm, HttpServletRequest request,
            HttpServletResponse response, String name) throws Exception {
        DynActionForm form = (DynActionForm) actionForm;

        form.setHttpRequest(request);
        form.setHttpResponse(response);

        User user = AuthFilter.getUser(request);
        form.setUser(user);

        form.l = this.l = (Localizer) request.getAttribute(SetRequestParamsFilter.REQUEST_KEY_LOCALIZER);

        // shortcut
        request.setAttribute("frd", form.getResponse().getData());

        ConnectionSet conSet = new ConnectionSet(setup.getConnectionPool(), false);

        form.setConnectionSet(conSet);

        // refresh thread of tracked log
        SessionLogAppender.trackSession(request.getSession(), false);

        long timeStart = System.currentTimeMillis();

        ActionForward forward = null;
        String action = "";
        PermissionNode permissionNode = null;
        String error = "";
        try {
            AppLicense.instance().check(form);

            // redirect to login.jsp or open interface
            if (user == null) {
                form.setPermission(ConfigMap.EMPTY);
            } else {
                action = form.actionIdentifier(this.getClass());
                permissionNode = permissionCheck(form, action);
                updateUserPageSettings(conSet, form);
            }

            String requestURI = request.getRequestURI();

            String includeServletPath = (String) request.getAttribute(RequestDispatcher.INCLUDE_SERVLET_PATH);

            // так сделано, т.к. при включении вызова акшена директивой <c:import или <jsp:import
            // в requestURI приходит та JSP шка из которой был вызван импорт..
            if (includeServletPath != null)
                form.requestUrl(includeServletPath, (String) request.getAttribute(RequestDispatcher.INCLUDE_QUERY_STRING));
            else
                form.requestUrl(requestURI, request.getQueryString());

            try {
                name = Utils.maskEmpty(name, "unspecified");
                forward = (ActionForward) getInvoker(name).invoke(this, mapping, form, request, response, conSet);
            } catch (InvocationTargetException e) {
                throw e.getCause();
            }

            conSet.commit();

            String lockId = form.getParam("lockFree");
            if (Utils.notBlankString(lockId)) {
                Locker.freeLock(new Lock(lockId, form.getUserId()));
            }

            if (!(this instanceof PoolAction) && !(this instanceof LoginAction)) {
                LoginStat.instance().actionWasCalled(request.getSession());
            }
        } catch (BGMessageException ex) {
            error = ((BGMessageException) ex).getMessage(l);
            if (ex instanceof BGIllegalArgumentException)
                form.setResponseData("paramName", ((BGIllegalArgumentException) ex).getName());
            return sendError(form, error);
        } catch (Throwable ex) {
            error = ex.getMessage();

            log.error(error, ex);

            StringWriter sw = new StringWriter();
            ex.printStackTrace(new PrintWriter(sw));

            return sendError(form, sw.toString());
        } finally {
            conSet.close();

            if (Utils.notBlankString(action)) {
                EventProcessor.processEvent(new ActionRequestEvent(request, action, permissionNode,
                        System.currentTimeMillis() - timeStart, error), null);
            }
        }

        return forward;
    }

    private Invoker getInvoker(String method) throws NoSuchMethodException {
        log.trace("Looking for invoker: {}", method);

        Invoker result = invokerMap.get(method);
        if (result == null) {
            result = Invoker.find(clazz, method);
            invokerMap.putIfAbsent(method, result);
        } else {
            log.trace("Cache hit");
        }

        log.trace("Found invoker: {}", result.getClass().getSimpleName());

        return result;
    }

    /**
     * Checks action permission.
     * @param form form with user.
     * @param action semicolon separated action class and method.
     * @return permission node.
     * @throws NotFoundException permission node for {@code action} not found.
     * @throws BGMessageException permission is denied.
     */
    protected PermissionNode permissionCheck(DynActionForm form, String action) throws NotFoundException, BGMessageException {
        var permissionNode = PermissionNode.getPermissionNodeOrThrow(action);

        ConfigMap perm = UserCache.getPerm(form.getUserId(), action);
        if (perm == null && !permissionNode.isAllowAll())
            throw new BGMessageException("Action '{}' is denied.", permissionNode.getTitlePath());

        form.setPermission(perm);

        return permissionNode;
    }

    /**
     * Updates page setting for user.
     * @param conSet DB connections.
     * @param form for taking {@link DynActionForm#getPageableId()} and {@link DynActionForm#getPage()}.
     * @throws SQLException
     */
    private void updateUserPageSettings(ConnectionSet conSet, DynActionForm form) throws SQLException {
        String pageableId = form.getPageableId();
        int pageSize = form.getPage().getPageSize();

        if (Utils.notBlankString(pageableId) && pageSize > 0) {
            String key = Page.PAGE_SIZE + "." + pageableId;
            User user = form.getUser();

            int currentValue = user.getPersonalizationMap().getInt(key, -1);
            if (currentValue != pageSize) {
                user.getPersonalizationMap().put(key, String.valueOf(pageSize));
                new UserDAO(conSet.getConnection()).updatePersonalization(null, user);
            }
        }
    }

    private ActionForward sendError(DynActionForm form, String errorText)
            throws IOException, JsonGenerationException, JsonMappingException {
        String responseType = form.getResponseType();
        if (DynActionForm.RESPONSE_TYPE_JSON.equalsIgnoreCase(responseType)) {
            form.getResponse().setStatus("error");
            form.getResponse().setMessage(errorText);

            return json((ConnectionSet) null, form);
        } else {
            HttpServletResponse response = form.getHttpResponse();

            response.setContentType("text/plain; charset=" + StandardCharsets.UTF_8.name());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

            PrintWriter out = form.getHttpResponseWriter();
            out.write(errorText);
            out.close();

            return null;
        }
    }

    /**
     * JSP forward.
     * @param con SQL connection.
     * @param form form object.
     * @param path JSP path.
     * @return
     */
    protected ActionForward html(Connection con, DynActionForm form, String path) {
        String forwardFile = form != null ? form.getForwardFile() : null;
        if (Utils.notBlankString(forwardFile))
            path = forwardFile;
        return html(new SingleConnectionSet(con), form, path);
    }

    /**
     * JSP forward.
     * @param conSet set of SQL connections.
     * @param form form object.
     * @param path JSP path.
     * @return
     */
    protected ActionForward html(ConnectionSet conSet, DynActionForm form, String path) {
        // response requested in JSON (API call)
        if (form != null && DynActionForm.RESPONSE_TYPE_JSON.equalsIgnoreCase(form.getResponseType())){
            if (AuthFilter.getUser(form.getHttpRequest()) == null) {
                throw new IllegalArgumentException("For open interface JSON response isn't allowed");
            }
            return json(conSet, form);
        } else {
            return new ActionForward(path);
        }
    }

    /**
     * JSP forward path.
     * @param form    complete path may be defined in
     *                {@link DynActionForm#getForwardFile()} or
     *                {@link DynActionForm#getForward()} used as key to
     *                mapping
     * @param mapping mapping forward names from {@link DynActionForm#getForward()}
     *                to JSP paths, default mapping key - empty string.
     * @return JSP path.
     * @throws IllegalArgumentException no JSP found in mapping.
     */
    protected String getForwardJspPath(DynActionForm form, Map<String, String> mapping) throws IllegalArgumentException {
        var forwardFile = form.getForwardFile();
        if (Utils.notBlankString(forwardFile))
            return forwardFile;

        var forward = Utils.maskNull(form.getForward());
        if (!mapping.containsKey(forward))
            throw new IllegalArgumentException("Not found JSP for forward: " + forward);

        return mapping.get(forward);
    }

    /**
     * Sends response result in JSON format.
     * @param con
     * @param form
     * @return
     */
    protected ActionForward json(Connection con, DynActionForm form) {
        return json(new SingleConnectionSet(con), form);
    }

    /**
     * Sends response result in JSON format.
     * @param conSet
     * @param form
     * @return
     */
    protected ActionForward json(ConnectionSet conSet, DynActionForm form) {
        try {
            // FIXME: Check this hack for usages and remove.
            if (Utils.notBlankString(form.getForwardFile())) {
                return new ActionForward(form.getForwardFile());
            } else {
                if (conSet != null) {
                    conSet.commit();
                }

                HttpServletResponse response = form.getHttpResponse();

                response.setContentType("application/json; charset=" + StandardCharsets.UTF_8.name());
                PrintWriter out = form.getHttpResponseWriter();

                // TODO: Remove the callback magic together with sendAJAXCommandAsync JS function on FE.
                String callback = form.getParam("callback");
                if (!Utils.isEmptyString(callback)) {
                    out.write(callback + "(");
                }

                ObjectWriter objectWriter = MAPPER.writer();
                out.write(objectWriter.writeValueAsString(form.getResponse()));

                if (!Utils.isEmptyString(callback)) {
                    out.write(");");
                }

                out.close();
            }
        } catch (Exception e) {
            log.error(e);
        }

        return null;
    }

    /**
     * Checks concurrent modifications by different users.
     * @param lastModify initial state of modified item.
     * @param form request with parameters {@code lastModifyUserId} and {@code lastModifyTime} for comparing to {@code lastModify}.
     * @throws BGMessageException entity was stored by another user in between.
     */
    protected void checkModified(LastModify lastModify, DynActionForm form) throws BGMessageException {
        if (lastModify.getTime() != null) {
            int lastModifyUserId = Utils.parseInt(form.getParam("lastModifyUserId"), 0);
            Date lastModifyTime = TimeUtils.parse(form.getParam("lastModifyTime"), TimeUtils.PATTERN_YYYYMMDDHHMMSS);

            // equals doesn't work here, because lastModify.getTime() can be java.sql.Timestamp
            if (lastModify.getTime().getTime() != lastModifyTime.getTime() || lastModify.getUserId() != lastModifyUserId) {
                throw new BGMessageException("Объект был изменен пользователем \"{}\" {}", UserCache.getUser(form.getUserId()).getTitle(),
                        TimeUtils.format(lastModify.getTime(), TimeUtils.FORMAT_TYPE_YMDHMS));
            }
        }

        lastModify.setUserId(form.getUserId());
        lastModify.setTime(new Date());
    }

    /**
     * Saves and restores HTTP request parameters.
     * As storage used {@link User#getPersonalizationMap()}, key is 'param.' + {@link DynActionForm#getAreaId()}.
     * @param con DB connection.
     * @param form there params are taken and restored, also contains 'areaId' param.
     * @param get restore values.
     * @param set save values.
     * @param params parameter names.
     * @throws SQLException
     */
    protected void restoreRequestParams(Connection con, DynActionForm form, boolean get, boolean set, String... params) throws SQLException {
        final Preferences map = form.getUser().getPersonalizationMap();
        final String mapDataBefore = map.getDataString();

        for (String param : params) {
            final String key = "param." + form.getAreaId() + "." + param;
            // param doesn't present in the request - restoring
            if (form.getParam().getArray(param) == null) {
                // storing values comma-separated
                if (get && map.containsKey(key)) {
                    List<String> values = Utils.toList(map.get(key));
                    if (!values.isEmpty()) {
                        if (values.size() > 1)
                            form.getParam().putArray(param, values.toArray(new String[0]));
                        else if (values.size() == 1)
                            form.setParam(param, values.get(0));

                        log.debug("Restoring param: {}, key: {}", param, key);
                    }
                }
            } else if (set) {
                String values = Utils.toString(form.getParamValuesListStr(param));
                if (Utils.notBlankString(values)) {
                    map.put(key, values);
                    log.debug("Storing param: {}, key: {}", param, key);
                }
            }
        }

        new UserDAO(con).updatePersonalization(mapDataBefore, form.getUser());
    }

    /**
     * Stores new values in personalization map and update it if changed.
     * @param form the form for obtaining the user.
     * @param con DB connection.
     * @param setFunction function for setting map parameters.
     * @throws Exception
     */
    protected void updatePersonalization(DynActionForm form, Connection con, Consumer<Preferences> setFunction) throws Exception {
        User user = form.getUser();

        Preferences map = user.getPersonalizationMap();
        String mapDataBefore = map.getDataString();

        setFunction.accept(map);

        new UserDAO(con).updatePersonalization(mapDataBefore, user);
    }
}