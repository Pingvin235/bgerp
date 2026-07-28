package ru.bgcrm.plugin.bgbilling;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URISyntaxException;
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
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.bgerp.action.base.BaseAction;
import org.bgerp.app.cfg.Preferences;
import org.bgerp.app.exception.BGException;
import org.bgerp.app.exception.BGMessageException;
import org.bgerp.util.Log;
import org.bgerp.util.xml.XMLUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.util.StdDateFormat;

import ru.bgcrm.model.user.User;
import ru.bgcrm.plugin.bgbilling.proto.dao.PluginDAO;
import ru.bgcrm.plugin.bgbilling.proto.model.BGServerFile;
import ru.bgcrm.plugin.bgbilling.transfer.UserAccount;
import ru.bgcrm.util.TimeUtils;
import ru.bgcrm.util.Utils;

public class TransferData {
    private static final Log log = Log.getLog();

    private static final String REQUEST_ENCODING = "UTF-8";
    private static final String RESPONSE_ENCODING = "UTF-8";

    private static final int LOGGING_REQUEST_TRIM_LENGTH = 3000;
    private static final int LOGGING_RESPONSE_TRIM_LENGTH = 5000;

    private static final Pattern CHARACTER_ENTITY_INVALID_REGEXP = Pattern.compile(
            "&#0;|&#1;|&#2;|&#3;|&#4;|&#5;|&#6;|&#7;|&#8;|&#11;|&#12;|&#14;|&#15;|&#16;|&#17;|&#18;|&#19;|&#20;|&#21;|&#22;|&#23;|&#24;|&#25;|&#26;|&#27;|&#28;|&#29;|&#30;|&#31;");

    private static class BitelJsonDateFormat extends StdDateFormat {
        private static final TimeZone CURRENT_TIMEZONE = TimeZone.getDefault();

        // the format like 2014-12-01T00:00:00+05:00 is supported preferentially,
        // it's parsed first (other formats are then tried) and everything is serialized into it
        private static final String BITEL_FORMAT = "yyyy-MM-dd'T'HH:mm:ssXXX";

        private BitelJsonDateFormat(TimeZone tz) {
            super(tz, Locale.getDefault(), true);
        }

        @Override
        protected Date _parseDate(String dateStr, ParsePosition pos) throws ParseException {
            Date result = getDateFormat().parse(dateStr, pos);
            if (result == null)
                result = TimeUtils.parse(dateStr, TimeUtils.PATTERN_DDMMYYYY);
            if (result == null)
                result = super._parseDate(dateStr, pos);
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

    private final DBInfo dbInfo;
    private final URL url;
    private final int timeOut;

    private final ObjectMapper jsonMapper = new ObjectMapper();

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

        timeOut = dbInfo.getSetup().getInt("requestTimeOut", 5000);
    }

    public ObjectMapper getObjectMapper() {
        return jsonMapper;
    }

    private class RequestTaskJsonRpc implements Callable<JsonNode> {
        private final RequestJsonRpc request;
        private final UserAccount user;

        public RequestTaskJsonRpc(RequestJsonRpc request, UserAccount user) {
            this.request = request;
            this.user = user;
        }

        @Override
        public JsonNode call() throws IOException, URISyntaxException {
            return postData(request, user);
        }

        private JsonNode postData(RequestJsonRpc request, UserAccount user) throws IOException, URISyntaxException {
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

            URL fullUrl = new URI(url.toString() + "/json/" + request.getUrl()).toURL();

            HttpURLConnection con = (HttpURLConnection) fullUrl.openConnection();
            con.setRequestMethod("POST");
            con.setDoOutput(true);
            con.setDoInput(true);
            con.setReadTimeout(timeOut);

            con.setRequestProperty("Content-type", "application/json; charset=UTF-8");

            String serialized = jsonMapper.writer().writeValueAsString(rootObject);

            if (log.isDebugEnabled()) {
                log.debug(this.hashCode() + " " + fullUrl);
                log.debug(this.hashCode() + " " + (serialized.length() < LOGGING_REQUEST_TRIM_LENGTH ? serialized
                        : serialized.substring(0, LOGGING_REQUEST_TRIM_LENGTH)));
            }

            try (var ps = new PrintStream(con.getOutputStream(), true, REQUEST_ENCODING)) {
                ps.print(serialized);
            }

            if (con.getResponseCode() == HttpURLConnection.HTTP_OK) {
                String response = new String(IOUtils.toByteArray(con.getInputStream()), REQUEST_ENCODING);

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
        public byte[] call() throws IOException {
            return postData(request, userName, userPswd);
        }

        private byte[] postData(Request request, String userName, String userPswd) throws IOException {
            byte[] inBytes = null;

            byte[] userInfo = (userName + ":" + userPswd).getBytes();

            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setDoOutput(true);
            con.setDoInput(true);
            con.setReadTimeout(timeOut);

            con.setRequestProperty("Content-type", "application/x-www-form-urlencoded");
            con.setRequestProperty("Authorization", "Basic " + Base64.getEncoder().encodeToString(userInfo));

            String markerParam = dbInfo.getSetup().get("markerRequestParam");
            if (Utils.notBlankString(markerParam)) {
                String param = StringUtils.substringBefore(markerParam, ":");
                String value = Utils.maskEmpty(StringUtils.substringAfter(markerParam, ":"), "BGERP");

                request.setAttribute(param, value);
            }

            // the HTTP session in the billing is destroyed right after creation, in the future it won't be created at all
            request.setAttribute("authToSession", 0);

            PrintStream ps = new PrintStream(con.getOutputStream(), true);
            // parameters set on the request
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

            if (con.getResponseCode() == HttpURLConnection.HTTP_OK) {
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

        // workaround: protection against malformed characters coming in, so the parser simply doesn't crash (a null character can arrive in odd situations, HD#5692)
        str = CHARACTER_ENTITY_INVALID_REGEXP.matcher(str).replaceAll("?");

        return XMLUtils.parseDocument(new InputSource(new StringReader(str.toString())));
    }

    /**
     * Sends a request to the billing, throws an exception on error, and also throws an exception when the timeout is reached
     * @param request the request
     * @param user the user
     * @return the response document
     */
    public Document postData(Request request, User user) {
        try {
            Document doc = getDocument(new String(postDataInternal(request, user), RESPONSE_ENCODING));

            checkDocumentStatus(doc, user);

            return doc;
        } catch (Exception e) {
            throw new BGException(e);
        }
    }

    /**
     * Sends a request to the web service in JSON-RPC format. Details on working with the format are in the {@link RequestJsonRpc} documentation
     *
     * @param request the request
     * @param user the user
     * @return the {@code data} element from the response
     */
    public JsonNode postData(RequestJsonRpc request, User user) {
        try {
            JsonNode rootNode = postDataInternal(request, user);

            checkDocumentStatus(rootNode, user);

            return rootNode.path("data");
        } catch (BGException e) {
            throw e;
        } catch (Exception e) {
            throw new BGException(e);
        }
    }

    /**
     * Sends a request to the web service in JSON-RPC format. Details on working with the format are in the {@link RequestJsonRpc} documentation
     *
     * @param request the request
     * @param user the user
     * @return the {@code return} element from the response
     */
    public JsonNode postDataReturn(RequestJsonRpc request, User user) {
        return postData(request, user).path("return");
    }

    /**
     * Sends a request and returns the result as a byte array
     * @param request the request
     * @param user the user
     * @return the response bytes
     */
    public byte[] postDataGetBytes(Request request, User user) {
        try {
            return postDataInternal(request, user);
        } catch (Exception e) {
            throw new BGException(e);
        }
    }

    /**
     * Sends a request and returns the result as a string, decoded with {@link #RESPONSE_ENCODING}
     * @param request the request
     * @param user the user
     * @return the response string
     */
    public String postDataGetString(Request request, User user) {
        try {
            return new String(postDataGetBytes(request, user), RESPONSE_ENCODING);
        } catch (UnsupportedEncodingException e) {
            throw new BGException(e);
        }
    }

    /**
     * Uploads a file to the billing server
     * @param handler a string like kernel/0/method, module/id/method, plugin.id/method
     * @param bgServerFile the file metadata
     * @param inputStream the file content
     * @throws IOException
     * @throws URISyntaxException
     */
    public int uploadFile(String handler, BGServerFile bgServerFile, InputStream inputStream, User user) throws IOException, URISyntaxException {
        UserAccount userAccount = UserAccount.getUserAccount(dbInfo.getId(), user);

        String userAndPswd = userAccount.getLogin() + ":" + userAccount.getPassword();
        final HttpURLConnection con = (HttpURLConnection) (new URI(url.toString() + "/upload").toURL()).openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/octet-stream");
        con.setRequestProperty("Authorization", "Basic " + Base64.getEncoder().encodeToString(userAndPswd.getBytes(StandardCharsets.UTF_8)));
        // con.setRequestProperty( "bgbilling-client-version", BGClientInit.getClientVersion() );
        con.setRequestProperty( "bgbilling-handler", handler );
        String json = BaseAction.MAPPER.writeValueAsString(bgServerFile);
        // base64 because headers only accept ascii, and here Russian letters can easily appear
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

    public void initSession(User user) {
        if (dbInfo.getPluginSet() == null) {
            // since PluginDAO will call this same method, set pluginSet
            // right away so it's not null, otherwise infinite recursion
            Set<String> pluginSet = new HashSet<>();
            dbInfo.setPluginSet(pluginSet);

            pluginSet.addAll(new PluginDAO(user, dbInfo).getInstalledPlugins());
        }

        if (dbInfo.getGuiConfigValues() == null) {
            // set right away to avoid an infinite loop
            Preferences prefs = new Preferences();
            dbInfo.setGuiConfigValues(prefs);

            Request request = new Request();
            request.setModule("admin");
            request.setAction("MenuAndToolBar");
            Document doc = postData(request, user);

            for (Element param : XMLUtils.selectElements(doc, "/data/params/param")) {
                prefs.put(param.getAttribute("key"), param.getAttribute("value"));
            }

            String version = doc.getDocumentElement().getAttribute("serverversion");
            if (Utils.notBlankString(version) && Utils.isBlankString(dbInfo.getVersion()) ) {
                for (String supported : DBInfoManager.SUPPORTED_VERSIONS) {
                    if (version.startsWith(supported)) {
                        log.info("Using version: {}", version);
                        dbInfo.setVersion(version);
                        break;
                    }
                }

                if (Utils.isBlankString(dbInfo.getVersion()))
                    throw new BGException("Can't define BGBilling server version");
            }
        }
    }

    private byte[] postDataInternal(Request request, User user) throws IOException {
        UserAccount userAccount = UserAccount.getUserAccount(dbInfo.getId(), user);
        try {
            return new RequestTask(request, userAccount.getLogin(), userAccount.getPassword()).call();
        } catch (SocketTimeoutException e) {
            throw new BGException("Время ожидания ответа от биллинга истекло! ({} мс).", timeOut);
        }
    }

    private JsonNode postDataInternal(RequestJsonRpc request, User user) throws IOException, URISyntaxException {
        try {
            return new RequestTaskJsonRpc(request, UserAccount.getUserAccount(dbInfo.getId(), user)).call();
        } catch (SocketTimeoutException e) {
            throw new BGException("Время ожидания ответа от биллинга истекло! ({} мс).", timeOut);
        }
    }

    private String encode(String inValue) {
        String outValue = "";
        try {
            outValue = URLEncoder.encode(inValue, REQUEST_ENCODING);
        } catch (UnsupportedEncodingException e) {
        }
        return outValue;
    }

    private void checkDocumentStatus(Document doc, User user) throws BGMessageException {
        String status = XMLUtils.selectText(doc, "/data/@status");
        if (!"ok".equals(status)) {
            throw new BGException("На запрос пользователя {} биллинг {} вернул ошибку {}", user.getLogin(), dbInfo.getId(),
                    XMLUtils.selectText(doc, "/data/text()"));
        }
    }

    private void checkDocumentStatus(JsonNode rootNode, User user) throws BGMessageException {
        String status = rootNode.path("status").textValue();
        if (!"ok".equals(status)) {
            String exceptionType = rootNode.path("exception").textValue();
            if (exceptionType != null && exceptionType.equals("ru.bitel.bgbilling.common.BGMessageException")) {
                throw new BGException("На запрос пользователя {} биллинг {} вернул ошибку {}", user.getLogin(), dbInfo.getId(),
                        rootNode.path("message").textValue());
            } else {
                throw new BGException(rootNode.path("message").textValue());
            }
        }
    }

    @Override
    public String toString() {
        return "url = " + url;
    }
}
