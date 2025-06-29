package ru.bgcrm.plugin.bgbilling.dao;

import java.util.StringTokenizer;

import org.bgerp.app.exception.BGException;
import org.bgerp.util.xml.XMLUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;

import ru.bgcrm.model.Page;
import ru.bgcrm.model.user.User;
import ru.bgcrm.plugin.bgbilling.DBInfo;
import ru.bgcrm.plugin.bgbilling.DBInfoManager;
import ru.bgcrm.plugin.bgbilling.Request;
import ru.bgcrm.plugin.bgbilling.TransferData;
import ru.bgcrm.util.Utils;

public class BillingDAO {
    // page
    private static final String RECORD_COUNT = "recordCount";
    private static final String PAGE_INDEX = "pageIndex";
    private static final String PAGE_SIZE = "pageSize";

    protected User user;
    protected DBInfo dbInfo;

    protected TransferData transferData;

    protected ObjectMapper jsonMapper;
    protected TypeFactory jsonTypeFactory;

    public static String getVersion(User user, String billingId) {
        DBInfo dbInfo = DBInfoManager.getInstance().getDbInfoMap().get(billingId);
        return dbInfo.getVersion();
    }

    public BillingDAO(User user, String billingId) {
        this.user = user;
        this.dbInfo = DBInfoManager.getInstance().getDbInfoMap().get(billingId);
        if (dbInfo == null) {
            throw new BGException("Не найден биллинг: " + billingId);
        }
        init();
    }

    public BillingDAO(User user, DBInfo dbInfo) {
        this.user = user;
        this.dbInfo = dbInfo;

        init();
    }

    private void init() {
        transferData = dbInfo.getTransferData();
        jsonMapper = transferData.getObjectMapper();
        jsonTypeFactory = jsonMapper.getTypeFactory();
        transferData.initSession(user);
    }

    public Document doRequest(Request req) {
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
     */
    public Integer getParameterId(String values) {
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

    protected <T> T readJsonValue(JsonParser p, JavaType valueType) {
        try {
            return jsonMapper.readValue(p, valueType);
        } catch (Exception e) {
            throw new BGException(e);
        }
    }
}
