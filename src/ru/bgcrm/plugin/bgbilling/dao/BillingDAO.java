package ru.bgcrm.plugin.bgbilling.dao;

import java.net.URL;
import java.util.Map;
import java.util.StringTokenizer;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;
import javax.xml.ws.WebServiceClient;

import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import ru.bgcrm.model.BGException;
import ru.bgcrm.model.Page;
import ru.bgcrm.model.user.User;
import ru.bgcrm.model.user.UserAccount;
import ru.bgcrm.plugin.bgbilling.DBInfo;
import ru.bgcrm.plugin.bgbilling.DBInfoManager;
import ru.bgcrm.plugin.bgbilling.Request;
import ru.bgcrm.plugin.bgbilling.TransferData;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.XMLUtils;
import ru.bgcrm.util.soap.HeaderHandlerResolver;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;

public class BillingDAO {
    protected static final String TITLE = "title";
    protected static final String VALUE = "value";
    protected static final String DATE_1 = "date1";
    protected static final String DATE_2 = "date2";
    protected static final String PARAMETERS = "parameters";

    // page
    private static final String RECORD_COUNT = "recordCount";
    private static final String PAGE_INDEX = "pageIndex";
    private static final String PAGE_SIZE = "pageSize";
    public static final String COMMENT = "comment";

    protected User user;
    protected DBInfo dbInfo;

    protected TransferData transferData;

    protected ObjectMapper jsonMapper;
    protected TypeFactory jsonTypeFactory;

    public static String getVersion(User user, String billingId) {
        DBInfo dbInfo = DBInfoManager.getInstance().getDbInfoMap().get(billingId);
        return dbInfo.getVersion();
    }

    public BillingDAO(User user, String billingId) throws BGException {
        this.user = user;
        this.dbInfo = DBInfoManager.getInstance().getDbInfoMap().get(billingId);
        if (dbInfo == null) {
            throw new BGException("Не найден биллинг: " + billingId);
        }
        init();
    }

    public BillingDAO(User user, DBInfo dbInfo) throws BGException {
        this.user = user;
        this.dbInfo = dbInfo;

        init();
    }

    private void init() throws BGException {
        try {
            this.transferData = dbInfo.getTransferData();
            this.jsonMapper = transferData.getObjectMapper();
            this.jsonTypeFactory = jsonMapper.getTypeFactory();
        } catch (Exception e) {
            throw new BGException(e);
        }
    }

    public Document doRequest(Request req) throws BGException {
        return transferData.postData(req, user);
    }

    protected void setPage(Request req, Page page) {
        req.setAttribute(PAGE_INDEX, page.getPageIndex());
        req.setAttribute(PAGE_SIZE, page.getPageSize());
    }

    protected void getPage(Page page, Element data) {
        if (data == null)
            return;
        page.setPageSize(Utils.parseInt(data.getAttribute(PAGE_SIZE)));
        page.setPageIndex(Utils.parseInt(data.getAttribute(PAGE_INDEX)));
        page.setRecordCount(Utils.parseInt(data.getAttribute(RECORD_COUNT)));
    }

    /**
     * Возвращает из сроки вида billingId1:paramId1;billingId2:paramId2 код
     * параметра для текущего биллинга.
     *
     * @param values
     * @return
     * @throws BGException
     */
    public Integer getParameterId(String values) throws BGException {
        StringTokenizer st = new StringTokenizer(values, ",;");
        while (st.hasMoreTokens()) {
            String token = st.nextToken();

            String[] pair = token.split(":");
            if (pair.length != 2) {
                throw new BGException("Incorrect token: " + token);
            }

            if (this.dbInfo.getId().equals(pair[0])) {
                return Utils.parseInt(pair[1]);
            }
        }

        throw new BGException("Not found id for billingId: " + this.dbInfo.getId());
    }

    public DBInfo getDbInfo() {
        return dbInfo;
    }

    protected String linesToString(Element node) {
        StringBuilder result = new StringBuilder();
        if (node != null) {
            NodeList nl = node.getElementsByTagName("row");
            final int size = nl.getLength();
            Element row = null;
            for (int i = 0; i < size; i++) {
                row = (Element) nl.item(i);
                String text = XMLUtils.selectText(row, "text()");
                if (Utils.notBlankString(text)) {
                    result.append(text);
                }
                result.append(row.getAttribute("text"));
                result.append("\n");
            }
        }
        return result.toString();
    }

    protected <K extends Service, S> S getWebService(Class<K> clazz, Class<S> serviceClass) throws BGException {
        return getWebService(clazz, serviceClass, 0);
    }

    protected <K extends Service, S> S getWebService(Class<K> clazz, Class<S> serviceClass, int moduleId)
            throws BGException {
        S port = null;
        try {
            UserAccount account = TransferData.getUserAccount(dbInfo.getId(), user);

            String login = account.getLogin();
            String pswd = account.getPassword();

            // замена хоста и порта, с которых генерился сервис на нужные
            WebServiceClient annotation = clazz.getAnnotation(WebServiceClient.class);
            String wsdlLocation = annotation.wsdlLocation();

            final String delim = "/executer";

            if (moduleId > 0) {
                wsdlLocation = wsdlLocation.replaceAll("/\\d*/", "/" + String.valueOf(moduleId) + "/");
            }

            wsdlLocation = dbInfo.getUrl() + StringUtils.substringAfter(wsdlLocation, delim);

            Service webService = Service.create(new URL(wsdlLocation),
                    new QName(annotation.targetNamespace(), annotation.name()));

            webService.setHandlerResolver(new HeaderHandlerResolver(login, pswd));

            port = webService.getPort(serviceClass);

            final Map<String, Object> requestContext = ((BindingProvider) port).getRequestContext();
            requestContext.put(BindingProvider.SESSION_MAINTAIN_PROPERTY, true);
            requestContext.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, wsdlLocation);
        } catch (Exception e) {
            throw new BGException(e);
        }
        return port;
    }

    protected void processWebServiceException(Exception e) throws BGException {
        // TODO: Сделать выделение MessageException
        throw new BGException(e);
    }

    protected <T> T readJsonValue(JsonParser p, JavaType valueType) throws BGException {
        try {
            return jsonMapper.readValue(p, valueType);
        } catch (Exception e) {
            throw new BGException(e.getMessage(), e);
        }
    }
}
