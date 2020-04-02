package ru.bgcrm.struts.form;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.beanutils.DynaClass;
import org.apache.commons.beanutils.DynaProperty;
import org.apache.struts.action.ActionForm;
import org.apache.struts.upload.FormFile;

import ru.bgcrm.model.ArrayHashMap;
import ru.bgcrm.model.Page;
import ru.bgcrm.model.user.User;
import ru.bgcrm.servlet.AccessLogValve;
import ru.bgcrm.util.ParameterMap;
import ru.bgcrm.util.Setup;
import ru.bgcrm.util.TimeUtils;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.sql.ConnectionSet;
import ru.bgerp.i18n.Localizer;
import ru.bgerp.util.Log;

/**
 * Сохраняет параметры HTTP запроса и контекст его обработки: пользователь, соединение с БД.
 * В него же устанавливаются данные ответа.
 *
 * @author Shamil
 */
public class DynActionForm extends ActionForm implements DynaBean, DynaClass {
    private static final Log log = Log.getLog();

    public static final String KEY = "form";

    private static final String PARAM_RESPONSE = "response";
    private static final String PARAM_FILE = "file";
    private static final String PARAM_PAGE = "page";
    private static final String PARAM_RETURN_URL = "returnUrl";
    private static final String PARAM_REQUEST_URL = "requestUrl";
    private static final String PARAM_RESPONSE_TYPE = "responseType";
    private static final String PARAM_RETURN_CHILD_UIID = "returnChildUiid";
    private static final String PARAM_FORWARD = "forward";
    private static final String PARAM_FORWARD_FILE = "forwardFile";

    public static final String RESPONSE_TYPE_HTML = "html";
    public static final String RESPONSE_TYPE_JSON = "json";
    public static final String RESPONSE_TYPE_STREAM = "stream";

    public static DynActionForm SERVER_FORM = new DynActionForm();
    static {
        SERVER_FORM.setUser(User.USER_SYSTEM);
    }

    private HttpServletRequest httpRequest;
    private HttpServletResponse httpResponse;
    private OutputStream httpResponseOutputStream;

    /** Набор соединений к БД. */
    private ConnectionSet connectionSet;

    /** Параметры ответа, сериализуются в JSON. */
    private Response response = new Response();

    private User user;
    private ParameterMap permission;
    private Page page = new Page();
    private FormFile file;

    /** Параметры запроса. */
    private ArrayHashMap param = new ArrayHashMap();

    public Localizer l;

    public DynActionForm() {}

    //FIXME: Возможно, следует оптимизировать.
    public DynActionForm(String url) {
        String params[] = url.split("\\?")[1].split("&");

        HashMap<String, ArrayList<String>> paramsForForm = new HashMap<String, ArrayList<String>>();
        for (String param : params) {
            if (param.split("=").length < 2) {
                continue;
            }

            try {
                String key = URLDecoder.decode(param.split("=")[0], Utils.UTF8.name());
                String value = URLDecoder.decode(param.split("=")[1], Utils.UTF8.name());

                if (paramsForForm.get(key) == null) {
                    ArrayList<String> arrayValues = new ArrayList<String>();
                    arrayValues.add(value);
                    paramsForForm.put(key, arrayValues);
                } else {
                    paramsForForm.get(key).add(value);
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }

        HashMap<String, String[]> paramsForFormAsArray = new HashMap<String, String[]>();
        for (String key : paramsForForm.keySet()) {
            String[] paramsArray = new String[paramsForForm.get(key).size()];
            paramsArray = paramsForForm.get(key).toArray(paramsArray);
            paramsForFormAsArray.put(key, paramsArray);
        }

        ArrayHashMap ahm = new ArrayHashMap();
        ahm.putAll(paramsForFormAsArray);

        this.param = ahm;
    }

    public DynActionForm(User user) {
        this.user = user;
        this.permission = ParameterMap.EMPTY;
    }

    public HttpServletRequest getHttpRequest() {
        return httpRequest;
    }

    public String getHttpRequestRemoteAddr() {
        String headerNameRemoteAddress = Setup.getSetup().get(AccessLogValve.PARAM_HEADER_NAME_REMOTE_ADDR);
        String result = httpRequest.getHeader(headerNameRemoteAddress);
        if (result == null)
            result = httpRequest.getRemoteAddr();
        return result;
    }

    public void setHttpRequest(HttpServletRequest httpRequest) {
        this.httpRequest = httpRequest;
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
        // TODO: Подумать, нужно ли кэширование.
        return new PrintWriter(
                new OutputStreamWriter(getHttpResponseOutputStream(), httpResponse.getCharacterEncoding()));
    }

    public ConnectionSet getConnectionSet() {
        return connectionSet;
    }

    public void setConnectionSet(ConnectionSet value) {
        this.connectionSet = value;
    }

    public Response getResponse() {
        return response;
    }

    public void setResponseData(String key, Object value) {
        response.setData(key, value);
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public int getUserId() {
        return user != null ? user.getId() : -1;
    }

    public ParameterMap getPermission() {
        return permission;
    }

    public void setPermission(ParameterMap permission) {
        this.permission = permission;
    }

    public Page getPage() {
        return page;
    }

    public FormFile getFile() {
        return file;
    }

    public void setFile(FormFile file) {
        this.file = file;
    }

    /**
     * Возвращает доступ к мапу параметров, для получения в JSP.
     * @return
     */
    public ArrayHashMap getParam() {
        return param;
    }

    public void setParam(ArrayHashMap param) {
        this.param = param;
    }

    /**
     * Возвращает параметр запроса action.
     * @return
     */
    public String getAction() {
        return getParam("action");
    }

    /**
     * Возвращает параметр запроса id.
     * @return
     */
    public int getId() {
        return Utils.parseInt(getParam("id"));
    }

    /**
     * Возвращает параметр запроса responseType.
     * @return
     */
    public String getResponseType() {
        return getParam(PARAM_RESPONSE_TYPE);
    }

    /**
     * Устанавливает параметр запроса responseType.
     * @param responseType
     */
    public void setResponseType(String responseType) {
        setParam(PARAM_RESPONSE_TYPE, responseType);
    }

    /**
     * Возвращает параметр запроса forward.
     * @return
     */
    public String getForward() {
        return getParam(PARAM_FORWARD);
    }

    public void setForward(String value) {
        setParam(PARAM_FORWARD, value);
    }

    /**
     * Возвращает параметр запроса forwardFile.
     * @return
     */
    public String getForwardFile() {
        return getParam(PARAM_FORWARD_FILE);
    }

    public void setForwardFile(String value) {
        setParam(PARAM_FORWARD_FILE, value);
    }

    /**
     * Возвращает параметр запроса requestUrl.
     * @return
     */
    public String getRequestUrl() {
        return getParam(PARAM_REQUEST_URL);
    }

    public void setRequestUrl(String requestUrl) {
        setParam(PARAM_REQUEST_URL, requestUrl);
    }

    /**
     * Возвращает URL, который нужно загрузить для возвращения из редактора.
     * @return
     */
    public String getReturnUrl() {
        return getParam(PARAM_RETURN_URL);
    }

    public void setReturnUrl(String returnUrl) {
        setParam(PARAM_RETURN_URL, returnUrl);
    }

    /**
     * Возвращает id HTML элемента на который нужно загрузить returnUrl для возвращения из редактора.
     * @return
     */
    public String getReturnUiid() {
        return getParam("returnUiid");
    }

    /**
     * Возвращает id HTML элемента на предка которого нужно загрузить returnUrl для возвращения из редактора.
     * @return
     */
    public String getReturnChildUiid() {
        return getParam(PARAM_RETURN_CHILD_UIID);
    }

    public void setReturnChildUiid(String value) {
        setParam(PARAM_RETURN_CHILD_UIID, value);
    }

    /**
     * Возвращает параметр запроса returnScript.
     * @return
     */
    public String getReturnScript() {
        return getParam("returnScript");
    }

    /**
     * Возвращает параметр запроса pageableId либо склеенный URL запроса + action.
     * @return
     */
    public String getPageableId() {
        String result = getParam(Page.PAGEABLE_ID);
        if (Utils.isBlankString(result)) {
            result = httpRequest.getRequestURI() + "?" + getAction();
        }
        return result;
    }

    /**
     * Возвращает параметр запроса areaId либо склеенный URL запроса + action.
     * @return
     */
    public String getAreaId() {
        String result = getParam("areaId");
        if (Utils.isBlankString(result)) {
            result = httpRequest.getRequestURI() + "?" + getAction();
        }
        return result;
    }

    /**
     * Gets HTTP request parameter.
     * @param name parameter name.
     * @param defaultValue default value if not presented in request.
     * @param defaultSet  set default value back in request for using in JSP.
     * @return
     */
    public String getParam(String name, String defaultValue, boolean defaultSet) {
        String value = param.get(name);
        if (value != null)
            return value.trim();
        if (defaultSet)
            setParam(name, defaultValue);
        return defaultValue;
    }

    public String getParam(String name, String defaultValue) {
        return getParam(name, defaultValue, false);
    }

    public String getParam(String name) {
        return getParam(name, null);
    }

    public void setParam(String name, String value) {
        param.put(name, value);
    }

    /**
     * Gets HTTP request parameter with type date, format {@link TimeUtils#FORMAT_TYPE_YMD}.
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

    public Date getParamDateTime(String name, Date defaultValue) {
        Date value = TimeUtils.parse(getParam(name), TimeUtils.FORMAT_TYPE_YMDHMS);
        return value != null ? value : defaultValue;
    }

    public Date getParamDateTime(String name) {
        return getParamDateTime(name, null);
    }

    public int getParamInt(String name, int defaultValue) {
        return Utils.parseInt(getParam(name), defaultValue);
    }

    public int getParamInt(String name) {
        return getParamInt(name, 0);
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
     * Возвращает значения параметров HTTP запроса.
     * @param name имя параметра.
     * @return null, если параметр не установлен.
     */
    public String[] getParamArray(String name) {
        return param.getArray(name);
    }

    /**
     * Устанавливает значения параметров HTTP запроса.
     * @param name имя параметра.
     * @param values значения.
     */
    public void setParamArray(String name, String[] values) {
        param.put(name, values);
    }

    public void setParamArray(String name, Collection<?> values) {
        List<String> result = new ArrayList<String>(values.size());
        for (Object value : values) {
            result.add(String.valueOf(value));
        }
        param.putArray(name, result.toArray(Utils.STRING_ARRAY));
    }

    @Override
    public Object get(String name) {
        if (dynaClass.getDynaProperty(name).getType() == String.class) {
            return getParam(name);
        } else if (PARAM_PAGE.equals(name)) {
            return page;
        } else if (PARAM_FILE.equals(name)) {
            return file;
        } else if (PARAM_RESPONSE.equals(name)) {
            return response;
        } else {
            return getParam(name);
        }
    }

    @Override
    public void set(String name, Object value) {
        Class<?> type = dynaClass.getDynaProperty(name).getType();
        if (type == String.class) {
            setParam(name, (String) value);
        } else if (type == FormFile.class) {
            file = (FormFile) value;
        } else {
            param.put(name, (String[]) value);
        }
    }

    /**
     * Возвращает набор выбранных числовых значений, переданных в форме несколько значений как param(<name>)="<value>",
     * выбираются только целочисленные значения.
     *
     * @return
     */
    public Set<Integer> getSelectedValues(String name) {
        Set<Integer> result = new HashSet<Integer>();

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
     * Возвращает набор выбранных строковых значений, переданных в форме несколько значений как param(<name>)="<value>",
     * @return
     */
    public Set<String> getSelectedValuesStr(String name) {
        Set<String> result = new LinkedHashSet<String>();

        final String[] array = param.getArray(name);
        if (array != null) {
            for (String value : array) {
                result.add(value);
            }
        }

        return result;
    }

    /**
     * Возвращает список выбранных числовых значений, переданных в форме несколько значений как param(<name>)="<value>".
     * @return
     */
    public List<String> getSelectedValuesListStr(String name) {
        return getSelectedValuesListStr(name, null);
    }

    /**
     * Возвращает список выбранных числовых значений, переданных в форме несколько значений как param(<name>)="<value>",
     * из списка исключаются значения равные exclude, если != null.
     * @return
     */
    public List<String> getSelectedValuesListStr(String name, String exclude) {
        List<String> result = new ArrayList<String>();

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

    /**
     * Возвращает набор выбранных числовых значений, переданных в форме несколько значений как param(<name>)="<value>",
     * выбираются только ненулевые значения.
     *
     * @return
     */
    public List<Integer> getSelectedValuesList(String name) {
        List<Integer> result = new ArrayList<Integer>();

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

    // /////////////////////////////////////////////
    // добавлены из-за интерфейса DynBean
    // /////////////////////////////////////////////
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

    private static DynaClass dynaClass = new DynActionForm();

    @Override
    public DynaClass getDynaClass() {
        return dynaClass;
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

    ////////////////////////////////////////////////////

    // /////////////////////////////////////////////
    // добавлены из-за интерфейса DynaClass
    // /////////////////////////////////////////////
    @Override
    public String getName() {
        throw new UnsupportedOperationException();
    }

    private static final Map<String, DynaProperty> propertyMap = new HashMap<String, DynaProperty>();
    static {
        for (String name : new String[] { "action", PARAM_REQUEST_URL, PARAM_FORWARD, PARAM_FORWARD_FILE,
                PARAM_RESPONSE_TYPE }) {
            propertyMap.put(name, new DynaProperty(name, String.class));
        }
        propertyMap.put(PARAM_PAGE, new DynaProperty(PARAM_PAGE, Page.class));
        propertyMap.put(PARAM_RESPONSE, new DynaProperty(PARAM_RESPONSE, Page.class));
        propertyMap.put(PARAM_FILE, new DynaProperty(PARAM_FILE, FormFile.class));
    }

    @Override
    public DynaProperty getDynaProperty(String name) {
        DynaProperty result = propertyMap.get(name);
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
    ////////////////////////////////////////////////////
}
