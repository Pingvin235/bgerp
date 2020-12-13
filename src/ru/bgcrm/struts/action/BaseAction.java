package ru.bgcrm.struts.action;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
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

import ru.bgcrm.cache.UserCache;
import ru.bgcrm.dao.Locker;
import ru.bgcrm.dao.WebRequestLogDAO;
import ru.bgcrm.dao.user.UserDAO;
import ru.bgcrm.model.BGException;
import ru.bgcrm.model.BGMessageException;
import ru.bgcrm.model.LastModify;
import ru.bgcrm.model.Lock;
import ru.bgcrm.model.Page;
import ru.bgcrm.model.user.PermissionNode;
import ru.bgcrm.model.user.User;
import ru.bgcrm.servlet.LoginStat;
import ru.bgcrm.servlet.filter.AuthFilter;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.ParameterMap;
import ru.bgcrm.util.Preferences;
import ru.bgcrm.util.SessionLogAppender;
import ru.bgcrm.util.Setup;
import ru.bgcrm.util.TimeUtils;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.sql.ConnectionSet;
import ru.bgcrm.util.sql.SQLUtils;
import ru.bgcrm.util.sql.SingleConnectionConnectionSet;
import ru.bgerp.l10n.Localizer;
import ru.bgerp.util.Log;

public class BaseAction extends DispatchAction {
    private static final Class<?>[] TYPES_CONSET_DYNFORM = { ActionMapping.class, DynActionForm.class,
            ConnectionSet.class };
    private static final Class<?>[] TYPES_CON_DYNFORM = { ActionMapping.class, DynActionForm.class,
            Connection.class };
    @Deprecated
    private static final Class<?>[] TYPES_CONSET_DYNFORM_SEVLETREQRESP = { ActionMapping.class, DynActionForm.class,
            HttpServletRequest.class, HttpServletResponse.class, ConnectionSet.class };
    @Deprecated
    private static final Class<?>[] TYPES_CON_DYNFORM_SEVLETREQRESP = { ActionMapping.class, DynActionForm.class,
            HttpServletRequest.class, HttpServletResponse.class, Connection.class };

    public static final ObjectMapper mapper = new ObjectMapper();

    static {
        // TODO: Разобраться с сериализацией дат и дата+время в JSON, узнать как
        // в биллинге.
        // при указании данной опции сериализуется в виде:
        // "2013-11-22T18:37:55.645+0011", вроде как миллисекунды и не нужны.
        // mapper.configure( SerializationFeature.WRITE_DATES_AS_TIMESTAMPS,
        // false );
        mapper.setTimeZone(TimeZone.getDefault());
    }

    protected final String FORWARD_DEFAULT = "default";

    protected final Log log = Log.getLog(this.getClass());

    protected Setup setup = Setup.getSetup();
    
    private ConcurrentMap<String, Invoker> invokerMap = new ConcurrentHashMap<>();
    
    protected Localizer l;

    protected BaseAction() {
        super();
    }
    

    /** 
     * Метод сделан, чтобы не переопределяли его, переопределять нужно с коннекшеном.
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
    
    /**
     * Акшен метод по-умолчанию, именованные делать по образцу.
     * @param mapping
     * @param form
     * @param con
     * @return
     * @throws Exception
     */
    protected ActionForward unspecified(ActionMapping mapping, DynActionForm form, Connection con) throws Exception {
        return mapping.findForward(FORWARD_DEFAULT);
    }

    @Deprecated
    protected ActionForward unspecified(ActionMapping mapping, DynActionForm form, HttpServletRequest request,
            HttpServletResponse response, ConnectionSet conSet) throws Exception {
        return mapping.findForward(FORWARD_DEFAULT);
    }
    
    /**
     * Акшен метод по-умолчанию, именованные делать по образцу.
     * @param mapping
     * @param form
     * @param conSet
     * @return
     * @throws Exception
     */
    protected ActionForward unspecified(ActionMapping mapping, DynActionForm form, ConnectionSet conSet) throws Exception {
        return mapping.findForward(FORWARD_DEFAULT);
    }

    @SuppressWarnings("unchecked")
    private Invoker getInvoker(String name) throws NoSuchMethodException {
        Invoker invoker = invokerMap.get(name);
        if (invoker == null) {
            try {
                invoker = new Invoker(clazz.getDeclaredMethod(name, TYPES_CONSET_DYNFORM));
            } catch (Exception e) {}

            if (invoker == null) {
                try {
                    invoker = new InvokerCon(clazz.getDeclaredMethod(name, TYPES_CON_DYNFORM));
                } catch (Exception e) {}
            }

            if (invoker == null) {
                try {
                    invoker = new InvokerWithRequest(clazz.getDeclaredMethod(name, TYPES_CONSET_DYNFORM_SEVLETREQRESP));
                    if (invoker != null)
                        log.warn("Deprecated action's method signature: " + name);
                } catch (Exception e) {}
            }

            if (invoker == null) {
                try {
                    invoker = new InvokerWithRequestCon(clazz.getDeclaredMethod(name, TYPES_CON_DYNFORM_SEVLETREQRESP));
                    if (invoker != null)
                        log.warn("Deprecated action's method signature: " + name);
                } catch (Exception e) {}
            }

            if (invoker == null)
                throw new NoSuchMethodException(name);

            invokerMap.putIfAbsent(name, invoker);
        }
        return invoker;
    }

    @Override
    protected ActionForward dispatchMethod(ActionMapping mapping, ActionForm actionForm, HttpServletRequest request,
            HttpServletResponse response, String name) throws Exception {
        DynActionForm form = (DynActionForm) actionForm;

        form.setHttpRequest(request);
        form.setHttpResponse(response);

        User user = AuthFilter.getUser(request);
        form.setUser(user);
        
        form.l = this.l = (Localizer) request.getAttribute("l");

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
                    // TODO: Возможно тоже нужно бросать BGMessageException?
                    log.error("PermissionNode is null for action: " + action);
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
                forward = (ActionForward) getInvoker(name).invoke(mapping, form, request, response, conSet);
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

            resultStatus = "Successful";
        } catch (BGMessageException ex) {
            resultStatus = l.l(ex.getMessage(), ex.getArgs());
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

            return status((ConnectionSet) null, form);
        } else {
            HttpServletResponse response = form.getHttpResponse();

            response.setContentType("text/plain; charset=" + Utils.UTF8.name());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

            PrintWriter out = form.getHttpResponseWriter();
            // mapper.writeValue( out, errorText );
            out.write(errorText);
            out.close();

            return null;
        }
    }

    /**
     * Returns Struts forward with name=form.getAction().
     * @param con
     * @param mapping
     * @param form
     * @return
     */
    protected ActionForward data(Connection con, ActionMapping mapping, DynActionForm form) {
        return data(con, mapping, form, form.getAction());
    }

    /**
     * Returns Struts forward by name.
     * @param con
     * @param mapping
     * @param form
     * @param name forward's name.
     * @return
     */
    protected ActionForward data(Connection con, ActionMapping mapping, DynActionForm form, String name) {
        return data(new SingleConnectionConnectionSet(con), mapping, form, name);
    }

    /**
     * Returns Struts forward with name=form.getAction().
     * @param conSet
     * @param mapping
     * @param form
     * @return
     */
    protected ActionForward data(ConnectionSet conSet, ActionMapping mapping, DynActionForm form) {
        return data(conSet, mapping, form, form.getAction());
    }

    /**
     * Returns Struts forward by name.
     * @param con
     * @param mapping
     * @param form
     * @param name forward's name.
     * @return
     */
    protected ActionForward data(ConnectionSet conSet, ActionMapping mapping, DynActionForm form, String name) {
        String responseType = form.getResponseType();
        // response requested in JSON (API call)
        if (DynActionForm.RESPONSE_TYPE_JSON.equalsIgnoreCase(responseType)) {
            return status(conSet, form);
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
        return data(new SingleConnectionConnectionSet(con), mapping, form, htmlForwardName);
    }

    @Deprecated
    protected ActionForward processUserTypedForward(Connection con, ActionMapping mapping, DynActionForm form, String htmlForwardName) {
        return data(con, mapping, form, htmlForwardName);
    }

    @Deprecated
    protected ActionForward processUserTypedForward(ConnectionSet conSet, ActionMapping mapping, DynActionForm form, HttpServletResponse response,
            String htmlForwardName) {
        return data(conSet, mapping, form, htmlForwardName);
    }

    @Deprecated
    protected ActionForward processUserTypedForward(ConnectionSet conSet, ActionMapping mapping, DynActionForm form, String htmlForwardName) {
        return data(conSet, mapping, form, htmlForwardName);
    }

    /**
     * Sends response result in JSON format.
     * @param con
     * @param form
     * @return
     */
    protected ActionForward status(Connection con, DynActionForm form) {
        return status(new SingleConnectionConnectionSet(con), form);
    }

    /**
     * Sends response result in JSON format.
     * @param conSet
     * @param form
     * @return
     */
    protected ActionForward status(ConnectionSet conSet, DynActionForm form) {
        try {
            // FIXME: Check this hack for usages and remove.
            if (Utils.notBlankString(form.getForwardFile())) {
                return new ActionForward(form.getForwardFile());
            } else {
                if (conSet != null) {
                    conSet.commit();
                }

                HttpServletResponse response = form.getHttpResponse();

                response.setContentType("application/json; charset=" + Utils.UTF8.name());
                PrintWriter out = form.getHttpResponseWriter();

                String callback = form.getParam("callback");
                if (!Utils.isEmptyString(callback)) {
                    out.write(callback + "(");
                }

                ObjectWriter objectWriter = mapper.writer();
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
        return status(conSet, form);
    }

    @Deprecated
    protected ActionForward processJsonForward(Connection con, DynActionForm form) {
        return status(con, form);
    } 

    @Deprecated
    protected ActionForward processJsonForward(Connection con, DynActionForm form, HttpServletResponse response) {
        return status(con, form);
    }

    @Deprecated
    protected ActionForward processJsonForward(ConnectionSet conSet, DynActionForm form, HttpServletResponse response) {
        return status(conSet, form);
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
            String displayUser = UserCache.getUser(form.getUserId()).getTitle();

            if (!((lastModify.getTime().equals(lastModifyTime)) && (lastModify.getUserId() == lastModifyUserId))) {
                throw new BGMessageException("Объект был изменен пользователем \"" + displayUser + "\" "
                        + TimeUtils.format(lastModify.getTime(), TimeUtils.FORMAT_TYPE_YMDHMS));
            }
        }

        lastModify.setUserId(form.getUserId());
        lastModify.setTime(new Date());
    }
    
    /**
     * Выставляет параметры запроса из сохранённых значений в случае, если они не переданы явно.
     * @param con
     * @param form
     * @param get восстанавливать
     * @param set сохранять 
     * @param params
     * @throws BGException
     */
    protected void restoreRequestParams(Connection con, DynActionForm form, 
            boolean get, boolean set,
            String... params) throws BGException {
        Preferences prefs = form.getUser().getPersonalizationMap();
        String valueBefore = prefs.getDataString();
        for (String param : params) {
            String key = "param." + Utils.getDigest(form.getAreaId());
            // параметр не пришёл в запросе - восстановление
            if (form.getParamArray(param) == null) {
                //TODO: Хранение с разделителями запятыми, может поправить потом.
                if (get && prefs.containsKey(key)) {
                    form.setParamArray(param, Utils.toList(prefs.get(key)));
                    log.debug("Restore param: %s, key: %s", param, key);
                }
            } else if (set) {
                prefs.put(key, Utils.toString(form.getSelectedValuesListStr(param)));
                log.debug("Store param: %s, key: %s", param, key);
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
    private class Invoker {
        protected final Method method;

        public Invoker(Method method) {
            this.method = method;
            method.setAccessible(true);
        }

        public Object invoke(ActionMapping mapping, DynActionForm actionForm, HttpServletRequest request,
                HttpServletResponse response, ConnectionSet conSet)
                        throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
            return method.invoke(BaseAction.this, new Object[] { mapping, actionForm, conSet });
        }
    }
    
    private class InvokerCon extends Invoker {
        public InvokerCon(Method method) {
            super(method);
        }

        @Override
        public Object invoke(ActionMapping mapping, DynActionForm actionForm, HttpServletRequest request,
                HttpServletResponse response, ConnectionSet conSet)
                        throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
            return method.invoke(BaseAction.this,
                    new Object[] { mapping, actionForm, conSet.getConnection() });
        }
    }

    private class InvokerWithRequest extends Invoker {
        public InvokerWithRequest(Method method) {
            super(method);
        }

        @Override
        public Object invoke(ActionMapping mapping, DynActionForm actionForm, HttpServletRequest request,
                HttpServletResponse response, ConnectionSet conSet)
                        throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
            return method.invoke(BaseAction.this, new Object[] { mapping, actionForm, request, response, conSet });
        }
    }

    private class InvokerWithRequestCon extends Invoker {
        public InvokerWithRequestCon(Method method) {
            super(method);
        }

        @Override
        public Object invoke(ActionMapping mapping, DynActionForm actionForm, HttpServletRequest request,
                HttpServletResponse response, ConnectionSet conSet)
                        throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
            return method.invoke(BaseAction.this,
                    new Object[] { mapping, actionForm, request, response, conSet.getConnection() });
        }
    }
}