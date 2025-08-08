package ru.bgcrm.struts.form;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.beanutils.DynaClass;
import org.apache.commons.beanutils.DynaProperty;
import org.apache.struts.action.ActionForm;
import org.apache.struts.upload.FormFile;
import org.bgerp.action.base.Actions;
import org.bgerp.action.base.BaseAction;
import org.bgerp.app.cfg.ConfigMap;
import org.bgerp.app.exception.BGIllegalArgumentException;
import org.bgerp.app.l10n.Localization;
import org.bgerp.app.l10n.Localizer;
import org.bgerp.app.servlet.util.ServletUtils;
import org.bgerp.util.Log;
import org.bgerp.util.TimeConvert;

import ru.bgcrm.model.Page;
import ru.bgcrm.model.user.User;
import ru.bgcrm.plugin.PluginManager;
import ru.bgcrm.util.TimeUtils;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.sql.ConnectionSet;

/**
 * HTTP request execution's context, contains: request, DB connection and response data.
 *
 * @author Shamil Vakhitov
 */
public class DynActionForm extends ActionForm implements DynaBean, DynaClass {
    private static final Log log = Log.getLog();

    public static final String KEY = "form";

    public static final String RESPONSE_TYPE_HTML = "html";
    public static final String RESPONSE_TYPE_JSON = "json";
    public static final String RESPONSE_TYPE_STREAM = "stream";

    /** System action, not real user request. */
    public static DynActionForm SYSTEM_FORM = new DynActionForm(User.USER_SYSTEM);
    @Deprecated
    public static DynActionForm SERVER_FORM = SYSTEM_FORM;

    private static final String PARAM_PAGE = "page";
    private static final String PARAM_FILE = "file";

    public static final String PARAM_ACTION_METHOD = "method";
    public static final String PARAM_ACTION_METHOD_OLD = "action";
    private static final String PARAM_ID = "id";
    private static final String PARAM_REQUEST_URL = "requestUrl";
    private static final String PARAM_RESPONSE_TYPE = "responseType";
    private static final String PARAM_RETURN_URL = "returnUrl";
    private static final String PARAM_RETURN_CHILD_UIID = "returnChildUiid";
    private static final String PARAM_FORWARD_FILE = "forwardFile";

    /** The properties are needed for allowing Struts HTML tags retrieving current request param values */
    private static final Map<String, DynaProperty> PROPERTIES = new HashMap<>();
    static {
        PROPERTIES.put(PARAM_PAGE, new DynaProperty(PARAM_PAGE, Page.class));
        PROPERTIES.put(PARAM_FILE, new DynaProperty(PARAM_FILE, FormFile.class));
        // a tiny optimization, String class for often param names
        for (String name : List.of(PARAM_ACTION_METHOD, PARAM_ACTION_METHOD_OLD, PARAM_ID, PARAM_REQUEST_URL, PARAM_RESPONSE_TYPE, PARAM_RETURN_URL,
                PARAM_RETURN_CHILD_UIID, PARAM_FORWARD_FILE))
            PROPERTIES.put(name, new DynaProperty(name, String.class));
    }

    private static final String PARAM_OVERWRITE_NAME_PREFIX = "!";

    private HttpServletRequest httpRequest;
    private HttpServletResponse httpResponse;
    private OutputStream httpResponseOutputStream;

    /** Special HTTP request params */
    private Page page = new Page();
    private FormFile file;

    /** Parsed HTTP request params */
    private ArrayHashMap param = new ArrayHashMap();

    /** DB connections. */
    private ConnectionSet connectionSet;

    private User user;
    /** Action identifier, semicolon separated class and method names */
    private String action = "???";
    private ConfigMap permission;

    public Localizer l;

    /** Response data, may be serialized to JSON. */
    private Response response = new Response();

    /** Empty constructor for Struts. */
    public DynActionForm() {}

    public DynActionForm(User user) {
        setUser(user);
        this.permission = ConfigMap.EMPTY;
        // for tests
        if (PluginManager.getInstance() != null)
            this.l = Localization.getLocalizer();
    }

    /**
     * Constructor from string URL or only query string.
     * @param url the complete URL or only the query string after {@code ?}.
    */
    public DynActionForm(String url) {
        int pos = url.indexOf("?");
        if (pos > 0)
            url = url.substring(pos + 1);

        String params[] = url.split("&");

        Map<String, List<String>> paramsForForm = new TreeMap<>();
        for (String param : params) {
            pos = param.indexOf('=');
            if (pos < 0)
                continue;

            try {
                String key = URLDecoder.decode(param.substring(0, pos), StandardCharsets.UTF_8.name());
                final boolean overwrite = key.startsWith(PARAM_OVERWRITE_NAME_PREFIX);
                if (overwrite)
                    key = key.substring(1);
                final String value = URLDecoder.decode(param.substring(pos + 1), StandardCharsets.UTF_8.name());

                if (overwrite || paramsForForm.get(key) == null) {
                    ArrayList<String> arrayValues = new ArrayList<>(1);
                    arrayValues.add(value);
                    paramsForForm.put(key, arrayValues);
                } else {
                    paramsForForm.get(key).add(value);
                }
            } catch (UnsupportedEncodingException e) {
                log.error(e);
            }
        }

        this.param.putArrays(paramsForForm.entrySet().stream()
            .collect(Collectors.toMap(me -> me.getKey(), me -> me.getValue().toArray(new String[0])))
        );
    }

    /**
     * @return request params except {@link #PARAM_REQUEST_URL}, serialized to a query string.
     */
    public String paramsToQueryString() {
        var result = new StringBuilder(200);

        for (var me : param.entrySet()) {
            String key = me.getKey();
            if (PARAM_REQUEST_URL.equals(key))
                continue;

            for (String value : (String []) me.getValue()) {
                if (!result.isEmpty())
                    result.append("&");

                try {
                    result.append(key).append("=").append(URLEncoder.encode(value, StandardCharsets.UTF_8.name()));
                } catch (UnsupportedEncodingException e) {
                    log.error(e);
                }
            }
        }

        return result.toString();
    }

    public HttpServletRequest getHttpRequest() {
        return httpRequest;
    }

    public void setHttpRequest(HttpServletRequest httpRequest) {
        this.httpRequest = httpRequest;
    }

    /**
     * @return http request path without query string
     */
    public String getRequestURI() {
        return ServletUtils.getRequestURI(httpRequest);
    }

    /**
     * Use {@link #getRequestURI()}
     */
    @Deprecated
    public String getHttpRequestURI() {
        log.warndMethod("getHttpRequestURI", "getRequestURI");
        return getRequestURI();
    }

    public HttpServletResponse getHttpResponse() {
        return httpResponse;
    }

    public void setHttpResponse(HttpServletResponse httpResponse) {
        this.httpResponse = httpResponse;
    }

    public OutputStream getHttpResponseOutputStream() throws IOException {
        if (httpResponseOutputStream == null)
            httpResponseOutputStream = httpResponse.getOutputStream();

        return httpResponseOutputStream;
    }

    public PrintWriter getHttpResponseWriter() throws IOException {
        return new PrintWriter(new OutputStreamWriter(getHttpResponseOutputStream(), httpResponse.getCharacterEncoding()));
    }

    public Page getPage() {
        return page;
    }

    public FormFile getFile() {
        return file;
    }

    /**
     * @return request parameters
     */
    public ArrayHashMap getParam() {
        return param;
    }

    /**
     * @return request parameter {@code method}, action class method name
     */
    public String getMethod() {
        return getParam(PARAM_ACTION_METHOD);
    }

    /**
     * Use {@link #getMethod()}
     * @return request parameter {@code action}, action class method name
     */
    @Deprecated
    public String getAction() {
        String value = getMethod();
        log.warnd("Deprecated method 'getAction' was called. Use 'getMethod' instead. Value: {}, URI: {}", value, getRequestURI());
        return value;
    }

    /**
     * Builds {@link #action} as semicolon separated action ID and method name from {@link #getMethod()}
     * @param clazz the action class
     * @return the generated value
     */
    public String action(Class<? extends BaseAction> clazz) {
        return action = Actions.getByClass(clazz).getId() + ":" + Utils.maskEmpty(getMethod(), "null");
    }

    /**
     * @return request parameter {@code id}
     */
    public int getId() {
        return getParamInt(PARAM_ID);
    }

    /**
     * @return request parameter {@code requestUrl}
     */
    public String getRequestUrl() {
        return getParam(PARAM_REQUEST_URL);
    }

    public void setRequestUrl(String value) {
        setParam(PARAM_REQUEST_URL, value);
    }

    /**
     * Sets request parameter {@code requestUrl}.
     * @param path start of {@code requestUrl}.
     * @param queryString query string, if not blank then added to end after query char.
     */
    public void requestUrl(String path, String queryString) {
        String requestUrl = path;
        if (Utils.notBlankString(queryString))
            requestUrl += "?" + queryString;
        setRequestUrl(requestUrl);
    }

    /**
     * @return request parameter {@code responseType}
     */
    public String getResponseType() {
        return getParam(PARAM_RESPONSE_TYPE);
    }

    /**
     * Sets request parameter {@code responseType}
     * @param value the value
     */
    public void setResponseType(String value) {
        setParam(PARAM_RESPONSE_TYPE, value);
    }

    /**
     * @return request parameter {@code returnUrl}, URL for returning back from an editor
     */
    public String getReturnUrl() {
        return getParam(PARAM_RETURN_URL);
    }

    /**
     * Used in mobile interface <pre>&lt;c:set target="${form}" property="returnUrl" value="${reopenProcessUrl}"/&gt;</pre>
     */
    @Deprecated
    public void setReturnUrl(String value) {
        setParam(PARAM_RETURN_URL, value);
    }

    /**
     * @return request parameter {@code returnChildUiid}, HTML element ID, for the parent of that has to be placed result of loading {@link #getReturnUrl()}
     */
    public String getReturnChildUiid() {
        return getParam(PARAM_RETURN_CHILD_UIID);
    }

    /**
     * Used in mobile interface <pre>&lt;c:set target="${form}" property="returnChildUiid" value="${uiid}"/&gt;</pre>
     */
    @Deprecated
    public void setReturnChildUiid(String value) {
        setParam(PARAM_RETURN_CHILD_UIID, value);
    }

    /**
     * @return request parameter {@code forwardFile}, JSP template path
     */
    @Deprecated
    public String getForwardFile() {
        String result = getParam(PARAM_FORWARD_FILE);
        if (Utils.notBlankString(result))
            log.warn("Used request parameter forwardFile={}", result);
        return result;
    }

    /**
     * Sets request parameter {@code forwardFile}, JSP template path
     * @param value
     */
    @Deprecated
    public void setForwardFile(String value) {
        setParam(PARAM_FORWARD_FILE, value);
    }

    /**
     * @return request parameter {@code pageableId} or {@link #action} if it was empty or missing.
     */
    public String getPageableId() {
        return getParam(Page.PAGEABLE_ID, action);
    }

    /**
     * Area ID is used in {@link BaseAction#restoreRequestParams()} for preserving request parameters.
     * @return request parameter {@code areaId} or {@link #action} if it was empty or missing.
     */
    public String getAreaId() {
        return getParam("areaId", action);
    }

    /**
     * Gets HTTP request parameter value.
     * @param name parameter name.
     * @param defaultValue default value if not presented in request.
     * @param defaultSet set default value back in request for using in JSP.
     * @param validator optional value validator.
     * @throws BGIllegalArgumentException if validation fails.
     * @return
     */
    public String getParam(String name, String defaultValue, boolean defaultSet, Predicate<String> validator) throws BGIllegalArgumentException {
        var value = getParam(name);

        if (validator != null && !validator.test(value))
            throw new BGIllegalArgumentException(name);

        if (value != null)
            return value;

        if (defaultSet)
            setParam(name, defaultValue);
        return defaultValue;
    }

    public String getParam(String name, String defaultValue, Predicate<String> validator) throws BGIllegalArgumentException {
        return getParam(name, defaultValue, false, validator);
    }

    /**
     * Gets HTTP request parameter first value.
     * @param name the parameter's name.
     * @param defaultValue default value.
     * @return the value of parameter with {@param name} or {@param defaultValue} if not presented.
     */
    public String getParam(String name, String defaultValue) {
        var value = getParam(name);
        return value == null ? defaultValue : value;
    }

    public String getParam(String name, Predicate<String> validator) throws BGIllegalArgumentException {
        return getParam(name, null, validator);
    }

    /**
     * Gets HTTP request parameter first value.
     * @param name the parameter's name.
     * @return parameter value or null if missing or empty.
     */
    public String getParam(String name) {
        String value = param.get(name);
        if (PARAM_ACTION_METHOD_OLD.equals(name))
            log.warnd("Deprecated request parameter '{}' was gotten. Use '{}' instead. Value: {}", PARAM_ACTION_METHOD_OLD, PARAM_ACTION_METHOD, value);
        return value;
    }

    /**
     * Sets HTTPS request parameter value.
     * @param name the parameter's name.
     * @param value the value.
     */
    public void setParam(String name, String value) {
        if (PARAM_ACTION_METHOD_OLD.equals(name)) {
            param.put(PARAM_ACTION_METHOD, value);
            log.warnd("Deprecated request parameter '{}' was set. Use '{}' instead. Value: {}", PARAM_ACTION_METHOD_OLD, PARAM_ACTION_METHOD, value);
        }
        param.put(name, value);
    }

    /**
     * Gets HTTP request parameter first value as type date, format {@link TimeUtils#FORMAT_TYPE_YMD}.
     * @param name parameter name.
     * @param defaultValue default value if not presented in request.
     * @param defaultSet set default value back in request for using in JSP.
     * @return
     */
    public Date getParamDate(String name, Date defaultValue, boolean defaultSet) {
        Date value = TimeUtils.parse(getParam(name), TimeUtils.FORMAT_TYPE_YMD);
        if (value != null)
            return value;
        if (defaultSet)
            setParam(name, TimeUtils.format(defaultValue, TimeUtils.FORMAT_TYPE_YMD));
        return defaultValue;
    }

    public Date getParamDate(String name, Date defaultValue) {
        return getParamDate(name, defaultValue, false);
    }

    public Date getParamDate(String name) {
        return getParamDate(name, null);
    }

    /**
     * Gets HTTP request parameter first value as type {@link YearMonth}.
     * @param name parameter name, storing the first day of month in string format {@link TimeUtils#FORMAT_TYPE_YMD}.
     * @param validator optional value validator.
     * @return parameter value or {@code null}.
     * @throws BGIllegalArgumentException when validation fails.
     */
    public YearMonth getParamYearMonth(String name, Predicate<YearMonth> validator) throws BGIllegalArgumentException {
        var result = TimeConvert.toYearMonth(getParamDate(name));

        if (validator != null && !validator.test(result))
            throw new BGIllegalArgumentException(name);

        return result;
    }

    public Date getParamDateTime(String name, String format, Date defaultValue) {
        Date value = TimeUtils.parse(getParam(name), format);
        return value != null ? value : defaultValue;
    }

    public Date getParamDateTime(String name, String format) {
        return getParamDateTime(name, format, (Date) null);
    }

    /**
     * Gets HTTP request parameter value with date and time
     * @param name parameter name
     * @param format date and time format
     * @param validator optional value validator
     * @return parameter value or {@code null}
     * @throws BGIllegalArgumentException when validation fails
     */
    public Date getParamDateTime(String name, String format, Predicate<Date> validator) throws BGIllegalArgumentException {
        var result = getParamDateTime(name, format);

        if (validator != null && !validator.test(result))
            throw new BGIllegalArgumentException(name);

        return result;
    }

    @Deprecated
    public Date getParamDateTime(String name) {
        log.warndMethod("getParamDateTime(String)", null);
        return getParamDateTime(name, TimeUtils.FORMAT_TYPE_YMDHMS, (Date) null);
    }

    public int getParamInt(String name, int defaultValue) {
        return Utils.parseInt(getParam(name), defaultValue);
    }

    public int getParamInt(String name) {
        return getParamInt(name, 0);
    }

    /**
     * Gets HTTP request parameter first value as type {@code int}.
     * @param name parameter name.
     * @param validator optional value validator.
     * @return parsed int value or {@code 0}.
     * @throws BGIllegalArgumentException
     */
    public int getParamInt(String name, Predicate<Integer> validator) throws BGIllegalArgumentException {
        String value = getParam(name);

        Integer result = Utils.isBlankString(value) ? 0 : Utils.parseInt(value);

        if (validator != null && !validator.test(result))
            throw new BGIllegalArgumentException(name);

        return result;
    }

    public long getParamLong(String name, long defaultValue) {
        return Utils.parseLong(getParam(name), defaultValue);
    }

    public long getParamLong(String name) {
        return getParamLong(name, 0);
    }

    public Boolean getParamBoolean(String name, Boolean defaultValue) {
        return Utils.parseBoolean(getParam(name), defaultValue);
    }

    public boolean getParamBoolean(String name) {
        return getParamBoolean(name, false);
    }


    /**
     * Gets HTTP request parameter values as type int.
     * @param name the parameter name.
     * @return not {@code null} set with all the parameter values parsed to integer.
     */
    public Set<Integer> getParamValues(String name) {
        Set<Integer> result = new HashSet<>();

        String[] array = param.getArray(name);
        if (array != null) {
            for (String value : array) {
                try {
                    result.add(Integer.parseInt(value.trim()));
                } catch (Exception e) {
                }
            }
        }

        return result;
    }

    /**
     * Gets HTTP request parameter values.
     * @param name the parameter name.
     * @return not {@code null} set with all the parameter values excluding empty strings.
     */
    public Set<String> getParamValuesStr(String name) {
        final String[] array = param.getArray(name);
        if (array != null)
            return Stream.of(array).filter(Utils::notBlankString).collect(Collectors.toSet());
        return Collections.emptySet();
    }

    /**
     * Gets HTTP request parameter values as an ordered list of type int.
     * @param name the parameter name.
     * @return not {@code null} list with all the parameter values parsed to integer.
     */
    public List<Integer> getParamValuesList(String name) {
        List<Integer> result = new ArrayList<>();

        String[] array = param.getArray(name);
        if (array != null) {
            for (String value : array) {
                int valInt = Utils.parseInt(value);
                if (valInt == 0) {
                    continue;
                }
                result.add(valInt);
            }
        }

        return result;
    }

    /**
     * Gets HTTP request parameter values as an ordered list.
     * @param name the parameter name.
     * @return not {@code null} list with all the parameter values.
     */
    public List<String> getParamValuesListStr(String name) {
        return getParamValuesListStr(name, null);
    }

    /**
     * Gets HTTP request parameter values as an ordered list.
     * @param name the parameter name.
     * @param exclude excluded value.
     * @return not {@code null} list with all the parameter values.
     */
    public List<String> getParamValuesListStr(String name, String exclude) {
        List<String> result = new ArrayList<>();

        String[] array = param.getArray(name);
        if (array != null) {
            for (String value : array) {
                if (exclude == null || !exclude.equals(value)) {
                    result.add(value);
                }
            }
        }

        return result;
    }

    // processing context

    public ConnectionSet getConnectionSet() {
        return connectionSet;
    }

    public void setConnectionSet(ConnectionSet value) {
        this.connectionSet = value;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
        if (user != null)
            param.setLogTrackingId("UID: " + user.getId());
    }

    public int getUserId() {
        return user != null ? user.getId() : -1;
    }

    public ConfigMap getPermission() {
        return permission;
    }

    public void setPermission(ConfigMap permission) {
        this.permission = permission;
    }

    // response

    public Response getResponse() {
        return response;
    }

    /**
     * Set response object data.
     * @param key
     * @param value
     */
    public void setResponseData(String key, Object value) {
        response.setData(key, value);
    }

    /**
     * Set HTTP request attribute. Unlike response data, not serialized to JSON.
     * @param key
     * @param value
     */
    public void setRequestAttribute(String key, Object value) {
        httpRequest.setAttribute(key, value);
    }

    // DynBean
    @Override
    public Object get(String name) {
        if (PARAM_PAGE.equals(name))
            return page;
        if (PARAM_FILE.equals(name))
            return file;
        if ("response".equals(name))
            return response;
        return getParam(name);
    }

    @Override
    public void set(String name, Object value) {
        Class<?> type = getDynaProperty(name).getType();
        if (type == String.class) {
            setParam(name, (String) value);
        } else if (type == FormFile.class) {
            file = (FormFile) value;
        } else {
            String[] values = (String[]) value;
            if (name.startsWith(PARAM_OVERWRITE_NAME_PREFIX))
                setParam(name.substring(1), values[values.length - 1]);
            else
                param.putArray(name, values);
        }
    }

    @Override
    public boolean contains(String name, String key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object get(String name, int index) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object get(String name, String key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public DynaClass getDynaClass() {
        return this;
    }

    @Override
    public void remove(String name, String key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void set(String name, int index, Object value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void set(String name, String key, Object value) {
        throw new UnsupportedOperationException();
    }

    // DynaClass
    @Override
    public String getName() {
        throw new UnsupportedOperationException();
    }

    @Override
    public DynaProperty getDynaProperty(String name) {
        DynaProperty result = PROPERTIES.get(name);
        if (result == null) {
            result = new DynaProperty(name, String[].class);
        }
        return result;
    }

    @Override
    public DynaProperty[] getDynaProperties() {
        throw new UnsupportedOperationException();
    }

    @Override
    public DynaBean newInstance() throws IllegalAccessException, InstantiationException {
        return new DynActionForm();
    }
}
