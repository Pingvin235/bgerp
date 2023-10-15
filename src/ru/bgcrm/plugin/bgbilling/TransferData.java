package ru.bgcrm.plugin.bgbilling;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.FieldPosition;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.bgerp.app.cfg.ConfigMap;
import org.bgerp.app.cfg.Preferences;
import org.bgerp.app.l10n.Localizer;
import org.bgerp.util.Log;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.util.StdDateFormat;

import ru.bgcrm.model.BGException;
import ru.bgcrm.model.BGMessageException;
import ru.bgcrm.model.user.User;
import ru.bgcrm.model.user.UserAccount;
import ru.bgcrm.plugin.bgbilling.proto.dao.PluginDAO;
import ru.bgcrm.plugin.bgbilling.proto.model.BGServerFile;
import ru.bgcrm.struts.action.BaseAction;
import ru.bgcrm.util.TimeUtils;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.XMLUtils;

public class TransferData {
    private static final Log log = Log.getLog();

    private static final int LOGGING_REQUEST_TRIM_LENGTH = 3000;
    private static final int LOGGING_RESPONSE_TRIM_LENGTH = 5000;

    private static final ExecutorService EXECUTOR = Executors.newCachedThreadPool(new NamedThreadFactory());
    private static final Pattern PATTERN_CHARSET = Pattern.compile("charset=([\\w\\d\\-]+)[\\s;]*.*$");

    private final ObjectMapper jsonMapper = new ObjectMapper();

    private DBInfo dbInfo;
    private URL url;
    private String status;
    private String message;
    private String requestEncoding;
    private String responseEncoding;
    private int timeOut;

    private static class BitelJsonDateFormat extends StdDateFormat {
        private static final TimeZone CURRENT_TIMEZONE = TimeZone.getDefault();

        // формат вида 2014-12-01T00:00:00+05:00 поддержан приоритетно,
        // парсится из него в первую очередь (потом пытается распарсить другие форматы) и в него всё сериализуется
        private static final String BITEL_FORMAT = "yyyy-MM-dd'T'HH:mm:ssXXX";

        private BitelJsonDateFormat(TimeZone tz) {
            super(tz, Locale.getDefault());
        }

        @Override
        protected Date parseAsISO8601(String dateStr, ParsePosition pos) throws ParseException {
            Date result = getDateFormat().parse(dateStr, pos);
            if (result == null) {
                return super.parseAsISO8601(dateStr, pos);
            }
            return TimeUtils.timezoneChange(result, _timezone, CURRENT_TIMEZONE);
        }

        @Override
        public StringBuffer format(Date date, StringBuffer toAppendTo, FieldPosition fieldPosition) {
            return getDateFormat().format(TimeUtils.timezoneChange(date, CURRENT_TIMEZONE, _timezone), toAppendTo,
                    fieldPosition);
        }

        private SimpleDateFormat getDateFormat() {
            SimpleDateFormat df = new SimpleDateFormat(BITEL_FORMAT);
            df.setTimeZone(_timezone);
            return df;
        }

        @Override
        public BitelJsonDateFormat clone() {
            return new BitelJsonDateFormat(_timezone);
        }
    }

    private static class NamedThreadFactory implements ThreadFactory {
        private static ThreadFactory defaultThreadFactory = Executors.defaultThreadFactory();

        @Override
        public Thread newThread(Runnable r) {
            Thread thread = defaultThreadFactory.newThread(r);
            thread.setName("bgbilling-" + thread.getName());

            return thread;
        }
    }

    public TransferData(DBInfo dbInfo) {
        this.dbInfo = dbInfo;
        this.url = dbInfo.getServerUrl();

        jsonMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        TimeZone timezone = TimeZone.getDefault();

        try {
            timezone = TimeZone.getTimeZone(dbInfo.getSetup().get("timezone"));
        } catch (Exception e) {
        }

        jsonMapper.setDateFormat(new BitelJsonDateFormat(timezone));
        jsonMapper.registerModule(JSONObjectDeserializer.toModule());

        String defaultEncoding = dbInfo.getVersion().compareTo("6.1") >= 0 ? "UTF-8" : "Cp1251";
        requestEncoding = dbInfo.getSetup().get("requestEncoding",
                System.getProperty("bgbilling.transfer.encoding", defaultEncoding));
        timeOut = dbInfo.getSetup().getInt("requestTimeOut", 5000);
    }

    public ObjectMapper getObjectMapper() {
        return jsonMapper;
    }

    /**
     * Отправляет запрос в биллинг, возвращает результат в т.ч. с ошибкой в виде XML документа.
     * Потенциально проблемная функция!!
     * Замалчивает ошибки, когда-то использовалась для напрямую вызова из JSP функций биллинга, теперь
     * так не делается.
     *
     * @param request
     * @param user
     * @return
     */
    @Deprecated
    public Document postDataSafe(Request request, User user) {
        Document result = null;
        try {
            result = postData(request, user);
            checkDocumentStatus(result, user);
        } catch (BGMessageException e) {
            result = createDocWithError(e.getMessage());
        } catch (Exception e) {
            result = createDocWithError(e.getMessage());
            log.error(e.getMessage(), e);
        }
        return result;
    }

    public static final UserAccount getUserAccount(String billingId, User user) {
        ConfigMap configMap = user.getConfigMap();
        return new UserAccount.Default(
                configMap.get("bgbilling:login." + billingId, configMap.get("bgbilling:login", user.getLogin())),
                configMap.get("bgbilling:password." + billingId,
                        configMap.get("bgbilling:password", user.getPassword())));
    }

    private class RequestTaskJsonRpc implements Callable<JsonNode> {
        private final RequestJsonRpc request;
        private final UserAccount user;

        public RequestTaskJsonRpc(RequestJsonRpc request, UserAccount user) {
            this.request = request;
            this.user = user;
        }

        @Override
        public JsonNode call() throws Exception {
            return postData(request, user);
        }

        private JsonNode postData(RequestJsonRpc request, UserAccount user) throws Exception {
            JsonNode result = null;

            ObjectNode rootObject = jsonMapper.createObjectNode();
            rootObject.put("method", request.getMethod());

            ObjectNode userObject = rootObject.putObject("user");
            userObject.put("user", user.getLogin());
            userObject.put("pswd", user.getPassword());

            ObjectNode paramsObject = rootObject.putObject("params");
            for (Map.Entry<String, Object> me : request.getParams().entrySet()) {
                paramsObject.putPOJO(me.getKey(), me.getValue());
            }

            URL fullUrl = new URL(url.toString() + "/json/" + request.getUrl());

            HttpURLConnection con = (HttpURLConnection) fullUrl.openConnection();
            con.setRequestMethod("POST");
            con.setDoOutput(true);
            con.setDoInput(true);

            con.setRequestProperty("Content-type", "application/json; charset=UTF-8");

            String serialized = jsonMapper.writer().writeValueAsString(rootObject);

            if (log.isDebugEnabled()) {
                log.debug(this.hashCode() + " " + fullUrl);
                log.debug(this.hashCode() + " " + (serialized.length() < LOGGING_REQUEST_TRIM_LENGTH ? serialized
                        : serialized.substring(0, LOGGING_REQUEST_TRIM_LENGTH)));
            }

            new PrintStream(con.getOutputStream(), true, requestEncoding).print(serialized);

            if (con.getResponseCode() == HttpURLConnection.HTTP_OK) {
                String response = new String(IOUtils.toByteArray(con.getInputStream()), requestEncoding);

                if (log.isDebugEnabled()) {
                    final int len = response.length();
                    log.debug(
                            this.hashCode() + " [ length = " + len + " ] JSON = " + (len > LOGGING_RESPONSE_TRIM_LENGTH
                                    ? response.substring(0, LOGGING_RESPONSE_TRIM_LENGTH) + "..." : response));
                }

                result = jsonMapper.readTree(response);

                con.disconnect();
            }

            return result;
        }
    }

    private class RequestTask implements Callable<byte[]> {
        private Request request;
        private String userName;
        private String userPswd;

        public RequestTask(Request request, String userName, String userPswd) {
            this.request = request;
            this.userName = userName;
            this.userPswd = userPswd;
        }

        @Override
        public byte[] call() throws Exception {
            return postData(request, userName, userPswd);
        }

        private byte[] postData(Request request, String userName, String userPswd) throws Exception {
            byte[] inBytes = null;

            byte[] userInfo = (userName + ":" + userPswd).getBytes();

            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");

            con.setRequestProperty("Content-type", "application/x-www-form-urlencoded");
            con.setRequestProperty("Authorization", "Basic " + Base64.getEncoder().encodeToString(userInfo));

            con.setDoOutput(true);
            con.setDoInput(true);

            String markerParam = dbInfo.getSetup().get("markerRequestParam");
            if (Utils.notBlankString(markerParam)) {
                String param = StringUtils.substringBefore(markerParam, ":");
                String value = Utils.maskEmpty(StringUtils.substringAfter(markerParam, ":"), "BGERP");

                request.setAttribute(param, value);
            }

            // HTTP сессия в биллинге уничтожается сразу после создания, в перспективе не будет создаваться вовсе
            request.setAttribute("authToSession", 0);

            PrintStream ps = new PrintStream(con.getOutputStream(), true);
            // параметры установленные в запросе
            for (String key : request.keys()) {
                ps.print(key);
                ps.print('=');
                ps.print(encode(request.getValue(key).toString()));
                ps.print('&');
            }

            if (log.isDebugEnabled()) {
                StringBuilder buf = new StringBuilder();
                for (String key : request.keys()) {
                    buf.append(key);
                    buf.append('=');
                    buf.append(encode(request.getValue(key).toString()));
                    buf.append('&');
                }
                log.debug(this.hashCode() + " " + url + "?" + (buf.length() < LOGGING_REQUEST_TRIM_LENGTH
                        ? buf.toString() : buf.substring(0, LOGGING_REQUEST_TRIM_LENGTH)));
                buf = null;
            }

            //request.clear();
            if (con.getResponseCode() == HttpURLConnection.HTTP_OK) {
                responseEncoding = requestEncoding;

                String key;
                for (int n = 1; (key = con.getHeaderFieldKey(n)) != null; n++) {
                    /* if (key.equalsIgnoreCase("Set-Cookie")) {
                        int i;
                        String cookie = con.getHeaderField(n);
                        if (cookie != null && (i = cookie.indexOf(';')) > -1) {
                            cookie = cookie.substring(0, i);
                        }
                        if (cookie != null && (i = cookie.indexOf('=')) > -1) {
                            String name = cookie.substring(0, i);
                            String value = cookie.substring(i + 1);
                            //cookies.put( name, value );

                            log.warn(this.hashCode() + " taked cookie: " + name + " => " + value);
                        }
                    } else  */
                    if (key.equalsIgnoreCase("Content-Type")) {
                        String contentType = con.getHeaderField(n);
                        Matcher m = PATTERN_CHARSET.matcher(contentType);
                        if (m.find()) {
                            responseEncoding = m.group(1);
                        }
                    }
                }

                inBytes = IOUtils.toByteArray(con.getInputStream());
                con.disconnect();
            }

            return inBytes;
        }
    }

    private Document getDocument(String str) throws Exception {
        int len = str.length();
        if (log.isDebugEnabled()) {
            log.debug(this.hashCode() + " [ length = " + len + " ] xml = " + (len > LOGGING_RESPONSE_TRIM_LENGTH
                    ? str.substring(0, LOGGING_RESPONSE_TRIM_LENGTH) + "..." : str.toString()));
        }

        // костыль: защита от того, что приходят кривые символы, чтобы просто парсер тупо не валился (нулевой символ может прийти в странных ситуациях HD#5692)
        str = replaceCharacterEntity(str);

        Document doc = XMLUtils.parseDocument(new InputSource(new StringReader(str.toString())));
        /* if (doc != null) {
            Element documentElement = doc.getDocumentElement();
            status = documentElement.getAttribute("status");
            if (status != null && status.equals("error")) {
                //message = documentElement.getTextContent();
                //throw new BGException( documentElement.getTextContent() );
            }
        } */

        return doc;
    }

    /**
     * Отправляет запрос в биллинг, в случае ошибки кидает исключение, при достиженнии таймаута выбрасывается также выбрасывается исключение.
     * @param request
     * @param user
     * @return
     * @throws BGException
     */
    public Document postData(Request request, User user) throws BGException {
        try {
            initSession(user);

            Document doc = getDocument(new String(postDataAsync(request, user), responseEncoding));
            checkDocumentStatus(doc, user);

            return doc;
        } catch (BGException e) {
            throw e;
        } catch (Exception e) {
            throw new BGException(e);
        }
    }

    /**
     * Отправляет запрос к Web-сервису в формате JSON-RPC.
     * Подробности по работе с форматом в документации {@link RequestJsonRpc}.
     *
     * @param request
     * @param user
     * @return елемент {@code data} из ответа.
     * @throws BGException
     */
    public JsonNode postData(RequestJsonRpc request, User user) throws BGException {
        try {
            initSession(user);

            JsonNode rootNode = postDataAsync(request, user);

            checkDocumentStatus(rootNode, user);

            return rootNode.path("data");
        } catch (BGException e) {
            throw e;
        } catch (Exception e) {
            throw new BGException(e);
        }
    }

    /**
     * Отправляет запрос к Web-сервису в формате JSON-RPC.
     * Подробности по работе с форматом в документации {@link RequestJsonRpc}.
     *
     * @param request
     * @param user
     * @return  елемент {@code return} из ответа.
     * @throws BGException
     */
    public JsonNode postDataReturn(RequestJsonRpc request, User user) throws BGException {
        return postData(request, user).path("return");
    }

    /**
     * Отправляет запрос и возвращает результат в виде массива байтов.
     * @param request
     * @param user
     * @return
     * @throws BGException
     */
    public byte[] postDataGetBytes(Request request, User user) throws BGException {
        try {
            UserAccount userAccount = getUserAccount(dbInfo.getId(), user);
            return postDataAsync(request, userAccount.getLogin(), userAccount.getPassword());
        } catch (Exception e) {
            throw new BGException(e);
        }
    }

    /**
     * Отправляет запрос и возвращает результат в виде строки, раскодированной #responseEncoding.
     * @param request
     * @param user
     * @return
     * @throws BGException
     */
    public String postDataGetString(Request request, User user) throws BGException {
        try {
            return new String(postDataGetBytes(request, user), responseEncoding);
        } catch (UnsupportedEncodingException e) {
            throw new BGException(e);
        }
    }

    /**
     * Выгружает файл на сервер биллинга.
     * @param handler - строка вида kernel/0/method, module/id/method, plugin.id/method
     * @param bgServerFile
     * @param inputStream
     * @throws IOException
     */
    public int uploadFile(String handler, BGServerFile bgServerFile, InputStream inputStream, User user) throws IOException {
        UserAccount userAccount = getUserAccount(dbInfo.getId(), user);

        String userAndPswd = userAccount.getLogin() + ":" + userAccount.getPassword();
        final HttpURLConnection con = (HttpURLConnection) (new URL(url.toString() + "/upload")).openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/octet-stream");
        con.setRequestProperty("Authorization", "Basic " + Base64.getEncoder().encodeToString(userAndPswd.getBytes(StandardCharsets.UTF_8)));
        // con.setRequestProperty( "bgbilling-client-version", BGClientInit.getClientVersion() );
        con.setRequestProperty( "bgbilling-handler", handler );
        String json = BaseAction.MAPPER.writeValueAsString(bgServerFile);
        // base64 потому что в хидерах лезет только ascii, а тут запросто русские буквы
        con.setRequestProperty( "bgbilling-file-info", Base64.getEncoder().encodeToString(json.getBytes(StandardCharsets.UTF_8)));
        con.setDoOutput(true);
        con.setDoInput(true);

        OutputStream outputStream = con.getOutputStream();
        IOUtils.copy(inputStream, outputStream);

        int id = -1;
        if (con.getResponseCode() == HttpURLConnection.HTTP_OK)
            id = Utils.parseInt( con.getHeaderField( "bgbilling-file-id" ), -1 );

        outputStream.close();
        con.disconnect();

        log.debug("{} {} => {}", handler, json, id);

        return id;
    }

    private void initSession(User user) throws BGException {
        if (dbInfo.getPluginSet() == null) {
            // т.к. PluginDAO обратится к этому же методу - то сразу
            // устанавливаем pluginSet, чтобы был не null, иначе - бесконечная рекурсия
            Set<String> pluginSet = new HashSet<String>();
            dbInfo.setPluginSet(pluginSet);

            pluginSet.addAll(new PluginDAO(user, dbInfo).getInstalledPlugins());
        }

        if (dbInfo.getGuiConfigValues() == null) {
            // сразу установка, чтобы избежать зацикливания
            Preferences prefs = new Preferences();
            dbInfo.setGuiConfigValues(prefs);

            Request request = new Request();
            request.setModule("admin");
            request.setAction("MenuAndToolBar");
            Document doc = postData(request, user);

            for (Element param : XMLUtils.selectElements(doc, "/data/params/param")) {
                prefs.put(param.getAttribute("key"), param.getAttribute("value"));
            }
        }
    }

    private byte[] postDataAsync(Request request, User user)
            throws BGMessageException, InterruptedException, ExecutionException {
        UserAccount userAccount = getUserAccount(dbInfo.getId(), user);
        return postDataAsync(request, userAccount.getLogin(), userAccount.getPassword());
    }

    private byte[] postDataAsync(Request request, String userName, String userPswd)
            throws InterruptedException, ExecutionException, BGMessageException {
        try {
            // "асинхронность" тут весьма условна, положительно то, что можно установить таймаут
            // + есть ограничение на количество параллельных запросов в биллинги
            Future<byte[]> future = EXECUTOR.submit(new RequestTask(request, userName, userPswd));
            return future.get(timeOut, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            throw new BGMessageException(Localizer.TRANSPARENT, Log.format("Время ожидания ответа от биллинга истекло! ({}} мс).", timeOut));
        }
    }

    /**
     * Отправляет запрос к Web-сервису в формате JSON-RPC.
     * Подробности по работе с форматом в документации {@link RequestJsonRpc}.
     *
     * @param request
     * @param user
     * @return
     * @throws BGMessageException
     * @throws InterruptedException
     * @throws ExecutionException
     */
    private JsonNode postDataAsync(RequestJsonRpc request, User user)
            throws BGMessageException, InterruptedException, ExecutionException {
        try {
            // "асинхронность" тут весьма условна, положительно то, что можно установить таймаут
            // + есть ограничение на количество параллельных запросов в биллинги
            Future<JsonNode> future = EXECUTOR
                    .submit(new RequestTaskJsonRpc(request, getUserAccount(dbInfo.getId(), user)));
            return future.get(timeOut, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            throw new BGMessageException("Время ожидания ответа от биллинга истекло! (" + timeOut + "мс).");
        }
    }

    public void setUrl(URL url) {
        this.url = url;
    }

    public String getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    private String encode(String inValue) {
        String outValue = "";
        try {
            outValue = URLEncoder.encode(inValue, requestEncoding);
        } catch (UnsupportedEncodingException e) {
        }
        return outValue;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("url = ");
        builder.append(url);
        return builder.toString();
    }

    public static final Document createDocWithError(String error) {
        Document doc = XMLUtils.newDocument();
        Element rootNode = XMLUtils.newElement(doc, "data");
        rootNode.setAttribute("status", "error");
        XMLUtils.createTextNode(rootNode, error);
        return doc;
    }

    private void checkDocumentStatus(Document doc, User user) throws BGMessageException {
        String status = XMLUtils.selectText(doc, "/data/@status");
        if (!"ok".equals(status)) {
            throw new BGMessageException(Localizer.TRANSPARENT, Log.format(
                "На запрос пользователя {} биллинг {} вернул ошибку {}", user.getTitle(),
                    dbInfo.getId(), XMLUtils.selectText(doc, "/data/text()")));
        }
    }

    private void checkDocumentStatus(JsonNode rootNode, User user) throws BGMessageException {
        String status = rootNode.path("status").textValue();
        if (!"ok".equals(status)) {
            String exceptionType = rootNode.path("exception").textValue();
            if (exceptionType != null && exceptionType.equals("ru.bitel.bgbilling.common.BGMessageException")) {
                throw new BGMessageException(Localizer.TRANSPARENT, Log.format(
                    "На запрос пользователя {} биллинг {} вернул ошибку {}",
                    user.getTitle(), dbInfo.getId(), rootNode.path("message").textValue()));
            } else {
                throw new BGException(rootNode.path("message").textValue());
            }
        }
    }

    private final static Pattern CHARACTER_ENTITY_INVALID_REGEXP = Pattern.compile(
            "&#0;|&#1;|&#2;|&#3;|&#4;|&#5;|&#6;|&#7;|&#8;|&#11;|&#12;|&#14;|&#15;|&#16;|&#17;|&#18;|&#19;|&#20;|&#21;|&#22;|&#23;|&#24;|&#25;|&#26;|&#27;|&#28;|&#29;|&#30;|&#31;");

    /**
    * Заменяет character entity вида &#XX; на вопросики, т.к. парсер не есть их. Хотя на сервере может такие символы сделать.
    * Допустимые такие:
    * Char       ::=      #x9 | #xA | #xD | [#x20-#xD7FF] | [#xE000-#xFFFD] | [#x10000-#x10FFFF]  / any Unicode character, excluding the surrogate blocks, FFFE, and FFFF. /
    * Т.е. нельзя в десятичном: 0-8, 11, 12, 14-31
    * Почему сервер генерит недопустимые - тут уже вопрос, пока костыль. TODO по-хорошему надо на сервере с этим как-то бороться, только пока недоразобрался в каком именно месте.
    */
    private static String replaceCharacterEntity(String xml) {
        return CHARACTER_ENTITY_INVALID_REGEXP.matcher(xml).replaceAll("?");
    }


    private static class JSONObjectDeserializer extends StdDeserializer<JSONObject> {

        public JSONObjectDeserializer() {
            this(JSONObject.class);
        }

        protected JSONObjectDeserializer(Class<?> vc) {
            super(vc);
        }

        @Override
        public JSONObject deserialize(JsonParser p, DeserializationContext ctxt)
                throws IOException, JsonProcessingException {
            JsonNode node = p.getCodec().readTree(p);
            JSONObject jsonObject = new JSONObject(node.get("json").asText());
            return jsonObject;
        }

        private static SimpleModule toModule() {
            return new SimpleModule().addDeserializer(JSONObject.class, new JSONObjectDeserializer());
        }

    }
}
