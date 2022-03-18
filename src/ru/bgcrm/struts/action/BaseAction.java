package ru.bgcrm.struts.action;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.util.Date;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;
import org.bgerp.servlet.LoginStat;
import org.bgerp.servlet.filter.AuthFilter;
import org.bgerp.util.Log;

import ru.bgcrm.cache.UserCache;
import ru.bgcrm.dao.Locker;
import ru.bgcrm.dao.WebRequestLogDAO;
import ru.bgcrm.dao.user.UserDAO;
import ru.bgcrm.model.BGException;
import ru.bgcrm.model.BGIllegalArgumentException;
import ru.bgcrm.model.BGMessageException;
import ru.bgcrm.model.LastModify;
import ru.bgcrm.model.Lock;
import ru.bgcrm.model.Page;
import ru.bgcrm.model.user.PermissionNode;
import ru.bgcrm.model.user.User;
import ru.bgcrm.servlet.filter.SetRequestParamsFilter;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.ParameterMap;
import ru.bgcrm.util.Preferences;
import ru.bgcrm.util.SessionLogAppender;
import ru.bgcrm.util.Setup;
import ru.bgcrm.util.TimeUtils;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.sql.ConnectionSet;
import ru.bgcrm.util.sql.SQLUtils;
import ru.bgcrm.util.sql.SingleConnectionSet;
import ru.bgerp.l10n.Localizer;

public class BaseAction extends DispatchAction {
    private static final Class<?>[] TYPES_CONSET_DYNFORM = { DynActionForm.class,
        ConnectionSet.class };
    private static final Class<?>[] TYPES_CON_DYNFORM = { DynActionForm.class,
        Connection.class };
    @Deprecated
    private static final Class<?>[] TYPES_MAPPING_CONSET_DYNFORM = { ActionMapping.class, DynActionForm.class,
            ConnectionSet.class };
    @Deprecated
    private static final Class<?>[] TYPES_MAPPING_CON_DYNFORM = { ActionMapping.class, DynActionForm.class,
            Connection.class };
    @Deprecated
    private static final Class<?>[] TYPES_MAPPING_CONSET_DYNFORM_SEVLETREQRESP = { ActionMapping.class, DynActionForm.class,
            HttpServletRequest.class, HttpServletResponse.class, ConnectionSet.class };
    @Deprecated
    private static final Class<?>[] TYPES_MAPPING_CON_DYNFORM_SEVLETREQRESP = { ActionMapping.class, DynActionForm.class,
            HttpServletRequest.class, HttpServletResponse.class, Connection.class };

    public static final ObjectMapper MAPPER = new ObjectMapper();

    static {
        // TODO: Разобраться с сериализацией дат и дата+время в JSON, узнать как
        // в биллинге.
        // при указании данной опции сериализуется в виде:
        // "2013-11-22T18:37:55.645+0011", вроде как миллисекунды и не нужны.
        // mapper.configure( SerializationFeature.WRITE_DATES_AS_TIMESTAMPS,
        // false );
        MAPPER.setTimeZone(TimeZone.getDefault());
    }

    protected static final String PATH_JSP = "/WEB-INF/jspf";

    public static final String PATH_JSP_ADMIN = PATH_JSP + "/admin";
    public static final String PATH_JSP_USER = PATH_JSP + "/user";
    public static final String PATH_JSP_OPEN = PATH_JSP + "/open";

    protected final String FORWARD_DEFAULT = "default";

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
     * Standard Struts method, shouldn't be used.
     */
    @Override
    protected final ActionForward unspecified(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        return super.unspecified(mapping, form, request, response);
    }

    @Deprecated
    protected ActionForward unspecified(ActionMapping mapping, DynActionForm form, HttpServletRequest request,
            HttpServletResponse response, Connection con) throws Exception {
        return mapping.findForward(FORWARD_DEFAULT);
    }

    @Deprecated
    protected ActionForward unspecified(ActionMapping mapping, DynActionForm form, Connection con) throws Exception {
        return mapping.findForward(FORWARD_DEFAULT);
    }

    @Deprecated
    protected ActionForward unspecified(ActionMapping mapping, DynActionForm form, HttpServletRequest request,
            HttpServletResponse response, ConnectionSet conSet) throws Exception {
        return mapping.findForward(FORWARD_DEFAULT);
    }

    @Deprecated
    protected ActionForward unspecified(ActionMapping mapping, DynActionForm form, ConnectionSet conSet) throws Exception {
        return mapping.findForward(FORWARD_DEFAULT);
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

    @SuppressWarnings("unchecked")
    private Invoker getInvoker(String method) throws NoSuchMethodException {
        log.debug("Looking for invoker: {}", method);

        Invoker result = invokerMap.get(method);
        if (result == null) {
            try {
                result = new Invoker(clazz.getDeclaredMethod(method, TYPES_CONSET_DYNFORM));
            } catch (Exception e) {}

            if (result == null) {
                try {
                    result = new InvokerCon(clazz.getDeclaredMethod(method, TYPES_CON_DYNFORM));
                } catch (Exception e) {}
            }

            if (result == null) {
                try {
                    result = new InvokerMapping(clazz.getDeclaredMethod(method, TYPES_MAPPING_CONSET_DYNFORM));
                } catch (Exception e) {}
            }

            if (result == null) {
                try {
                    result = new InvokerMappingCon(clazz.getDeclaredMethod(method, TYPES_MAPPING_CON_DYNFORM));
                } catch (Exception e) {}
            }

            if (result == null) {
                try {
                    result = new InvokerWithRequest(clazz.getDeclaredMethod(method, TYPES_MAPPING_CONSET_DYNFORM_SEVLETREQRESP));
                    if (result != null)
                        log.warn("Deprecated action's method signature: " + method);
                } catch (Exception e) {}
            }

            if (result == null) {
                try {
                    result = new InvokerWithRequestCon(clazz.getDeclaredMethod(method, TYPES_MAPPING_CON_DYNFORM_SEVLETREQRESP));
                    if (result != null)
                        log.warn("Deprecated action's method signature: " + method);
                } catch (Exception e) {}
            }

            if (result == null)
                throw new NoSuchMethodException(method);

            invokerMap.putIfAbsent(method, result);
        } else {
            log.debug("Cache hit");
        }

        log.debug("Found invoker: {}", result.getClass().getSimpleName());

        return result;
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

        ActionForward forward = null;
        ConnectionSet conSet = new ConnectionSet(setup.getConnectionPool(), false);

        form.setConnectionSet(conSet);

        // обновляется поток отслеживаемого лога
        SessionLogAppender.trackSession(request.getSession(), false);

        long timeStart = System.currentTimeMillis();
        int logEntryId = 0;
        String resultStatus = "";
        try {
            // redirect to login.jsp or open interface
            if (user == null) {
                form.setPermission(ParameterMap.EMPTY);
            } else {
                // permission check
                String action = this.getClass().getName() + ":" + form.getAction();
                PermissionNode permissionNode = PermissionNode.getPermissionNode(action);

                ParameterMap perm = UserCache.getPerm(user.getId(), action);

                if (perm == null) {
                    if (permissionNode == null) {
                        throw new BGMessageException("Действие " + action + " не найдено.");
                    }
                    if (!permissionNode.isAllowAll()) {
                        throw new BGMessageException("Действие " + permissionNode.getTitlePath() + " запрещено.");
                    }
                }
                form.setPermission(perm);

                if (permissionNode == null) {
                    throw new BGException("PermissionNode is null for action: " + action);
                }

                // логирование запроса
                if (!permissionNode.isNotLogging()) {
                    Connection con = null;
                    try {
                        con = Setup.getSetup().getDBConnectionFromPool();

                        WebRequestLogDAO webRequestLogDAO = new WebRequestLogDAO(con);
                        logEntryId = webRequestLogDAO.insertLogEntry(request, action);

                        con.commit();
                    } finally {
                        SQLUtils.closeConnection(con);
                    }
                }

                // сохранение изменившегося размера страницы
                String pageableId = form.getPageableId();
                int pageSize = form.getPage().getPageSize();

                if (Utils.notBlankString(pageableId) && pageSize > 0) {
                    String key = Page.PAGE_SIZE + "." + pageableId;

                    int currentValue = user.getPersonalizationMap().getInt(key, -1);
                    if (currentValue != pageSize) {
                        user.getPersonalizationMap().put(key, String.valueOf(pageSize));
                        new UserDAO(conSet.getConnection()).updatePersonalization(null, user);
                    }
                }
            }

            String requestURI = request.getRequestURI();

            String includeSevletPath = (String) request.getAttribute(RequestDispatcher.INCLUDE_SERVLET_PATH);

            // так сделано, т.к. при включении вызова акшена директивой
            // <c:import или <jsp:import
            // в requestURI приходит та JSP шка из которой был вызван импорт..
            if (includeSevletPath != null) {
                form.setRequestUrl(
                        includeSevletPath + "?" + request.getAttribute(RequestDispatcher.INCLUDE_QUERY_STRING));
            } else {
                form.setRequestUrl(requestURI + "?" + request.getQueryString());
            }

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
                LoginStat.getLoginStat().actionWasCalled(request.getSession());
            }

            resultStatus = "Successfully";
        } catch (BGMessageException ex) {
            resultStatus = ((BGMessageException) ex).getMessage(l);
            if (ex instanceof BGIllegalArgumentException)
                form.setResponseData("paramName", ((BGIllegalArgumentException) ex).getName());
            return sendError(form, resultStatus);
        } catch (Throwable ex) {
            resultStatus = ex.getMessage();

            log.error(resultStatus, ex);

            StringWriter sw = new StringWriter();
            ex.printStackTrace(new PrintWriter(sw));

            return sendError(form, sw.toString());
        } finally {
            conSet.recycle();

            if (logEntryId > 0) {
                Connection con = null;
                try {
                    con = Setup.getSetup().getDBConnectionFromPool();

                    WebRequestLogDAO webRequestLogDAO = new WebRequestLogDAO(con);
                    webRequestLogDAO.updateLogEntryDuration(logEntryId, System.currentTimeMillis() - timeStart);
                    webRequestLogDAO.updateLogEntryResultStatus(logEntryId, resultStatus);

                    con.commit();
                } finally {
                    SQLUtils.closeConnection(con);
                }
            }
        }

        return forward;
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
            // mapper.writeValue( out, errorText );
            out.write(errorText);
            out.close();

            return null;
        }
    }

    /**
     * JSP forward file path calculated using {@link #getForwardJspPath(DynActionForm, Map)} function.
     * @param con
     * @param form
     * @param mapping
     * @return
     */
    protected ActionForward html(Connection con, DynActionForm form, Map<String, String> mapping) {
        return html(con, form, getForwardJspPath(form, mapping));
    }

    /**
     * JSP forward file path.
     * @param con
     * @param form must be 'null' for open interface.
     * @param path JSP path.
     * @return
     */
    protected ActionForward html(Connection con, DynActionForm form, String path) {
        return html(new SingleConnectionSet(con), form, path);
    }

    /**
     * JSP forward file path.
     * @param conSet
     * @param form
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
    private String getForwardJspPath(DynActionForm form, Map<String, String> mapping) throws IllegalArgumentException {
        var forwardFile = form.getForwardFile();
        if (Utils.notBlankString(forwardFile))
            return forwardFile;

        var forward = Utils.maskNull(form.getForward());
        if (!mapping.containsKey(forward))
            throw new IllegalArgumentException("Not found JSP for forward: " + forward);

        return mapping.get(forward);
    }

    /**
     * Use {@link #html(Connection, DynActionForm, String)}.
     *
     * Returns Struts forward with name=form.getAction().
     * @param con
     * @param mapping
     * @param form
     * @return
     */
    @Deprecated
    protected ActionForward html(Connection con, ActionMapping mapping, DynActionForm form) {
        return html(con, mapping, form, form.getAction());
    }

    /**
     * Use {@link #html(Connection, DynActionForm, String)}.
     *
     * Returns Struts forward by name.
     * @param con
     * @param mapping
     * @param form
     * @param name forward's name.
     * @return
     */
    @Deprecated
    protected ActionForward html(Connection con, ActionMapping mapping, DynActionForm form, String name) {
        return html(new SingleConnectionSet(con), mapping, form, name);
    }

    /**
     * Use {@link #html(ConnectionSet, DynActionForm, String)}.
     *
     * Returns Struts forward with name=form.getAction().
     * @param conSet
     * @param mapping
     * @param form
     * @return
     */
    @Deprecated
    protected ActionForward html(ConnectionSet conSet, ActionMapping mapping, DynActionForm form) {
        return html(conSet, mapping, form, form.getAction());
    }

    /**
     * Use {@link #html(ConnectionSet, DynActionForm, String)}.
     *
     * Returns Struts forward by name.
     * @param conSet
     * @param mapping
     * @param form
     * @param name forward's name.
     * @return
     */
    @Deprecated
    protected ActionForward html(ConnectionSet conSet, ActionMapping mapping, DynActionForm form, String name) {
        String responseType = form.getResponseType();
        // response requested in JSON (API call)
        if (DynActionForm.RESPONSE_TYPE_JSON.equalsIgnoreCase(responseType)) {
            return json(conSet, form);
        } else {
            // wanted forward defined in form
            if (Utils.notBlankString(form.getForward())) {
                name = form.getForward();
            } else if (Utils.notBlankString(form.getForwardFile())) {
                return new ActionForward(form.getForwardFile());
            }
            return mapping.findForward(name);
        }
    }

    @Deprecated
    protected ActionForward processUserTypedForward(Connection con, ActionMapping mapping, DynActionForm form, HttpServletResponse response,
            String htmlForwardName) {
        return html(new SingleConnectionSet(con), mapping, form, htmlForwardName);
    }

    @Deprecated
    protected ActionForward processUserTypedForward(Connection con, ActionMapping mapping, DynActionForm form, String htmlForwardName) {
        return html(con, mapping, form, htmlForwardName);
    }

    @Deprecated
    protected ActionForward processUserTypedForward(ConnectionSet conSet, ActionMapping mapping, DynActionForm form, HttpServletResponse response,
            String htmlForwardName) {
        return html(conSet, mapping, form, htmlForwardName);
    }

    @Deprecated
    protected ActionForward processUserTypedForward(ConnectionSet conSet, ActionMapping mapping, DynActionForm form, String htmlForwardName) {
        return html(conSet, mapping, form, htmlForwardName);
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
            log.error(e.getMessage(), e);
        }

        return null;
    }

    @Deprecated
    protected ActionForward processJsonForward(ConnectionSet conSet, DynActionForm form) {
        return json(conSet, form);
    }

    @Deprecated
    protected ActionForward processJsonForward(Connection con, DynActionForm form) {
        return json(con, form);
    }

    @Deprecated
    protected ActionForward processJsonForward(Connection con, DynActionForm form, HttpServletResponse response) {
        return json(con, form);
    }

    @Deprecated
    protected ActionForward processJsonForward(ConnectionSet conSet, DynActionForm form, HttpServletResponse response) {
        return json(conSet, form);
    }

    protected boolean getAccess(String accessList, String accessItemKey, int value) {
        boolean result = false;
        if (accessList != null && accessItemKey != null) {
            for (String accessItem : accessList.split(";")) {
                if (accessItem.startsWith(accessItemKey + ":")) {
                    String accessValue = accessItem.substring(accessItemKey.length() + 1).trim();
                    result = "*".equals(accessValue);
                    if (!result) {
                        String valueString = String.valueOf(value);
                        for (String accessValueItem : accessValue.split(",")) {
                            if (accessValueItem.matches("^-?[0-9]+$") && valueString.equals(accessValueItem)) {
                                result = true;
                                break;
                            } else if (accessValueItem.matches("^-?[0-9]+-[0-9]+$")) {
                                int pos = accessValueItem.indexOf('-', 1);
                                int v1 = Integer.parseInt(accessValueItem.substring(0, pos));
                                int v2 = Integer.parseInt(accessValueItem.substring(pos + 1));
                                if (v1 <= value && value <= v2) {
                                    result = true;
                                    break;
                                }
                            }
                        }
                    }
                    break;
                }
            }
        }
        return result;
    }

    protected void checkModified(LastModify lastModify, DynActionForm form) throws BGException {
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
     * As storage used {@link User#getPersonalizationMap()}, key is 'param.' + digest from {@link DynActionForm#getAreaId()}.
     * @param con
     * @param form there params are taken and restored, also contains 'areaId' param.
     * @param get restore
     * @param set saves
     * @param params parameter names
     * @throws BGException
     */
    protected void restoreRequestParams(Connection con, DynActionForm form,
            boolean get, boolean set,
            String... params) throws BGException {
        Preferences prefs = form.getUser().getPersonalizationMap();
        String valueBefore = prefs.getDataString();
        for (String param : params) {
            String key = "param." + Utils.getDigest(form.getAreaId());
            // param doesn't present in the request - restoring
            if (form.getParamArray(param) == null) {
                // storing values comma-separated
                if (get && prefs.containsKey(key)) {
                    form.setParamArray(param, Utils.toList(prefs.get(key)));
                    log.debug("Restore param: {}, key: {}", param, key);
                }
            } else if (set) {
                prefs.put(key, Utils.toString(form.getSelectedValuesListStr(param)));
                log.debug("Store param: {}, key: {}", param, key);
            }
        }
        new UserDAO(con).updatePersonalization(valueBefore, form.getUser());
    }

    /**
     * Stores new values in personalization map and update it if changed.
     * @param form
     * @param con
     * @param setFunction
     * @throws Exception
     */
    protected void updatePersonalization(DynActionForm form, Connection con, Consumer<Preferences> setFunction) throws Exception {
        User user = form.getUser();
        Preferences personalizationMap = user.getPersonalizationMap();
        String persConfigBefore = personalizationMap.getDataString();

        setFunction.accept(personalizationMap);

        new UserDAO(con).updatePersonalization(persConfigBefore, user);
    }

    private static class Invoker {
        protected final Method method;

        public Invoker(Method method) {
            this.method = method;
            method.setAccessible(true);
            if (Modifier.isStatic(method.getModifiers()))
                throw new IllegalArgumentException("Action method can't be static");
        }

        public Object invoke(BaseAction action, ActionMapping mapping, DynActionForm actionForm, HttpServletRequest request,
                HttpServletResponse response, ConnectionSet conSet)
                        throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
            return method.invoke(action, actionForm, conSet);
        }
    }

    private static class InvokerCon extends Invoker {
        public InvokerCon(Method method) {
            super(method);
        }

        @Override
        public Object invoke(BaseAction action, ActionMapping mapping, DynActionForm actionForm, HttpServletRequest request,
                HttpServletResponse response, ConnectionSet conSet)
                        throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
            return method.invoke(action, actionForm, conSet.getConnection());
        }
    }

    private static class InvokerMapping extends Invoker {
        public InvokerMapping(Method method) {
            super(method);
        }

        @Override
        public Object invoke(BaseAction action, ActionMapping mapping, DynActionForm actionForm, HttpServletRequest request,
                HttpServletResponse response, ConnectionSet conSet)
                        throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
            return method.invoke(action, mapping, actionForm, conSet);
        }
    }

    private static class InvokerMappingCon extends Invoker {
        public InvokerMappingCon(Method method) {
            super(method);
        }

        @Override
        public Object invoke(BaseAction action, ActionMapping mapping, DynActionForm actionForm, HttpServletRequest request,
                HttpServletResponse response, ConnectionSet conSet)
                        throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
            return method.invoke(action, mapping, actionForm, conSet.getConnection());
        }
    }

    private static class InvokerWithRequest extends Invoker {
        public InvokerWithRequest(Method method) {
            super(method);
        }

        @Override
        public Object invoke(BaseAction action, ActionMapping mapping, DynActionForm actionForm, HttpServletRequest request,
                HttpServletResponse response, ConnectionSet conSet)
                        throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
            return method.invoke(action, mapping, actionForm, request, response, conSet);
        }
    }

    private static class InvokerWithRequestCon extends Invoker {
        public InvokerWithRequestCon(Method method) {
            super(method);
        }

        @Override
        public Object invoke(BaseAction action, ActionMapping mapping, DynActionForm actionForm, HttpServletRequest request,
                HttpServletResponse response, ConnectionSet conSet)
                        throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
            return method.invoke(action, mapping, actionForm, request, response, conSet.getConnection());
        }
    }
}