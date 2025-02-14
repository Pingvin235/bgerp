package ru.bgcrm.plugin.bgbilling.proto.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.stream.Collectors;

import org.bgerp.app.exception.BGException;
import org.bgerp.app.exception.BGMessageException;
import org.bgerp.app.exception.BGMessageExceptionWithoutL10n;
import org.bgerp.cache.ParameterCache;
import org.bgerp.dao.param.ParamValueDAO;
import org.bgerp.model.base.IdTitle;
import org.bgerp.model.base.tree.IdStringTitleTreeItem;
import org.bgerp.model.param.Parameter;
import org.bgerp.util.xml.XMLUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.fasterxml.jackson.databind.JsonNode;

import ru.bgcrm.model.Pair;
import ru.bgcrm.model.ParamList;
import ru.bgcrm.model.param.ParameterAddressValue;
import ru.bgcrm.model.param.ParameterEmailValue;
import ru.bgcrm.model.param.ParameterPhoneValue;
import ru.bgcrm.model.param.ParameterPhoneValueItem;
import ru.bgcrm.model.user.User;
import ru.bgcrm.plugin.bgbilling.DBInfo;
import ru.bgcrm.plugin.bgbilling.Request;
import ru.bgcrm.plugin.bgbilling.RequestJsonRpc;
import ru.bgcrm.plugin.bgbilling.dao.BillingDAO;
import ru.bgcrm.plugin.bgbilling.proto.model.ContractParameter;
import ru.bgcrm.plugin.bgbilling.proto.model.ParamAddressValue;
import ru.bgcrm.plugin.bgbilling.proto.model.ParamEmailValue;
import ru.bgcrm.plugin.bgbilling.proto.model.ParameterType;
import ru.bgcrm.plugin.bgbilling.proto.model.entity.EntityAttrAddress;
import ru.bgcrm.plugin.bgbilling.proto.model.entity.EntityAttrDate;
import ru.bgcrm.plugin.bgbilling.proto.model.entity.EntityAttrEmail;
import ru.bgcrm.plugin.bgbilling.proto.model.entity.EntityAttrList;
import ru.bgcrm.plugin.bgbilling.proto.model.entity.EntityAttrPhone;
import ru.bgcrm.plugin.bgbilling.proto.model.entity.EntityAttrText;
import ru.bgcrm.util.AddressUtils;
import ru.bgcrm.util.TimeUtils;
import ru.bgcrm.util.Utils;

public class ContractParamDAO extends BillingDAO {
    private static final String KERNEL_CONTRACT_API = ContractDAO.KERNEL_CONTRACT_API;
    private static final String CONTRACT_MODULE_ID = "contract";

    public ContractParamDAO(User user, String billingId) {
        super(user, billingId);
    }

    public ContractParamDAO(User user, DBInfo dbInfo) {
        super(user, dbInfo);
    }

    public static ParameterAddressValue toCrmObject(ParamAddressValue item, Connection con) throws SQLException {
        ParameterAddressValue crmItem = new ParameterAddressValue();

        if (item != null) {
            crmItem.setComment(item.getComment());
            crmItem.setFlat(item.getFlat());
            crmItem.setFloor(Utils.parseInt(item.getFloor()));
            crmItem.setHouseId(item.getHouseId());
            crmItem.setPod(Utils.parseInt(item.getPod()));
            crmItem.setRoom(item.getRoom());
            crmItem.setValue(AddressUtils.buildAddressValue(crmItem, con));
        }

        return crmItem;
    }

    /**
     * Вызывает {@link #getParameterListWithDir(int, boolean, boolean)} с false, false.
     * @param contractId
     * @return
     */
    public List<ContractParameter> getParameterList(int contractId) {
        return getParameterListWithDir(contractId, false, false).getSecond();
    }

    /**
     * Возвращает параметры договора со справочниками.
     * @param contractId код договора.
     * @param loadGroups загружать группы.
      * @param onlyGroup учитывать группу параметров договора.
     * @return
     */
    public Pair<ParamList, List<ContractParameter>> getParameterListWithDir(int contractId, boolean loadGroups,
            boolean onlyGroup) {
        Pair<ParamList, List<ContractParameter>> result = new Pair<>();

        ParamList groupDir = new ParamList();
        List<ContractParameter> valueList = new ArrayList<>();

        result.setFirst(groupDir);
        result.setSecond(valueList);

        Request req = new Request();
        req.setModule(CONTRACT_MODULE_ID);
        req.setAction("ContractParameters");
        req.setContractId(contractId);
        if (!onlyGroup) {
            req.setAttribute("all", 1);
        }
        if (loadGroups) {
            req.setAttribute("list", 1);
        }

        Document doc = transferData.postData(req, user);

        for (Element e : XMLUtils.selectElements(doc, "/data/parameters/parameter")) {
            valueList.add(new ContractParameter(Utils.parseInt(e.getAttribute("pid")),
                    Utils.parseInt(e.getAttribute("pt")), e.getAttribute("title"), e.getAttribute("value")));
        }

        groupDir.setId(Utils.parseInt(XMLUtils.selectText(doc, "/data/condel/@pgid")));
        for (Element e : XMLUtils.selectElements(doc, "/data/groups/item")) {
            groupDir.addValue(new IdTitle(Utils.parseInt(e.getAttribute("id")), e.getAttribute("title")));
        }

        return result;
    }

    /** Использовать только для генерации документов!
     * Кэшированние результата убрано, т.к. потенциально может съесть много памяти.
     * @param contractId
     * @return
     */
    public Document getContractParams(int contractId) {
        Request request = new Request();
        request.setModule(CONTRACT_MODULE_ID);
        request.setAction("ContractParameters");
        request.setContractId(contractId);

        return transferData.postData(request, user);
    }

    public String getTextParam(int contractId, int paramId) {
        ContractParameter contractParam = getParameter(contractId, paramId);
        return contractParam != null ? contractParam.getValue() : "";
    }

    public Date getDateParam(int contractId, int paramId) {
        ContractParameter contractParam = getParameter(contractId, paramId);
        return contractParam != null ? TimeUtils.parse(contractParam.getValue(), TimeUtils.PATTERN_DDMMYYYY) : null;
    }

    private ContractParameter getParameter(int contractId, int paramId) {
        for (ContractParameter param : getParameterListWithDir(contractId, false, false).getSecond()) {
            if (param.getParamId() == paramId) {
                return param;
            }
        }
        return null;
    }

    public List<IdTitle> getParamListValues(int paramId) {

        if (dbInfo.versionCompare("9.2") >= 0) {

            RequestJsonRpc req = new RequestJsonRpc(KERNEL_CONTRACT_API,
                    "ContractParameterService",
                    "contractParameterListItemList");

            req.setParam("parameterTypeId", paramId);
            JsonNode ret = transferData.postDataReturn(req, user);
            return readJsonValue(ret.traverse(), jsonTypeFactory.constructCollectionType(List.class, IdTitle.class));

        } else {

            List<IdTitle> valueList = new ArrayList<>();

            Request req = new Request();
            req.setModule("admin");
            req.setAction("ListValues");
            req.setAttribute("pid", paramId);

            Document doc = transferData.postData(req, user);

            for (Element e : XMLUtils.selectElements(doc, "/data/values/item")) {
                valueList.add(new IdTitle(Utils.parseInt(e.getAttribute("id")), e.getAttribute("title")));
            }

            return valueList;
        }
    }

    public ParameterPhoneValue getPhoneParam(int contractId, int paramId) {
        ParameterPhoneValue result = new ParameterPhoneValue();

        Request billingRequest = new Request();
        billingRequest.setModule(CONTRACT_MODULE_ID);
        billingRequest.setAction("PhoneInfo");
        if (dbInfo.versionCompare("5.2") >= 0) {
            billingRequest.setAction("GetPhoneInfo");
        }

        billingRequest.setContractId(contractId);
        billingRequest.setAttribute("pid", paramId);

        Document doc = transferData.postData(billingRequest, user);

        Element phone = XMLUtils.selectElement(doc, "/data/phone");
        if (phone != null) {
            int itemCount = Utils.parseInt(phone.getAttribute("count"));

            // до 5.1 было просто зашито 5 телефонов
            if (dbInfo.versionCompare("5.1") <= 0) {
                itemCount = 5;
            }

            for (int i = 1; i <= itemCount; i++) {
                String number = phone.getAttribute("phone" + i);
                String comment = phone.getAttribute("comment" + i);

                if (Utils.isBlankString(number))
                    continue;

                // удаление форматирование из параметра
                if (dbInfo.versionCompare("9.2") >= 0)
                    number = number.replaceAll("[^\\d.]", "");

                ParameterPhoneValueItem item = new ParameterPhoneValueItem();
                item.setPhone(number);
                item.setComment(comment);

                result.addItem(item);
            }
        }

        return result;
    }

    private List<IdStringTitleTreeItem> getEmailSubscrTree(Element treeElm) {
        // Рекурсивный сбор элементов дерева
        List<IdStringTitleTreeItem> treeValues = new ArrayList<>();

        for (Element e : XMLUtils.selectElements(treeElm, "item")) {
            IdStringTitleTreeItem cur = new IdStringTitleTreeItem();
            cur.setId(e.getAttribute("id"));
            cur.setTitle(e.getAttribute("title"));
            if (e.getAttribute("type").equals("1")) // Если есть child`ы
            {
                cur.setChildren(getEmailSubscrTree(e));
            }
            treeValues.add(cur);
        }

        return treeValues;
    }

    public ParamEmailValue getEmailParam(int contractId, int paramId) {
        if (dbInfo.versionCompare("9.2") >= 0) {

            RequestJsonRpc req = new RequestJsonRpc(KERNEL_CONTRACT_API, "ContractService", "contractParameterGet");

            req.setParam("contractId", contractId);
            req.setParam("parameterId", paramId);
            JsonNode ret = transferData.postDataReturn(req, user);
            EntityAttrEmail attrEmail = jsonMapper.convertValue(ret, ru.bgcrm.plugin.bgbilling.proto.model.entity.EntityAttrEmail.class);
            ParamEmailValue result = new ParamEmailValue();
            List<String> emails = attrEmail.getContactList().stream()
                    .map(i -> i.getAddress()).collect(Collectors.toList());
            result.setEmails(emails);
            result.setEntityAttrEmail(attrEmail);
            return result;

        } else {
            ParamEmailValue result = new ParamEmailValue();

            Request billingRequest = new Request();
            billingRequest.setModule(CONTRACT_MODULE_ID);
            billingRequest.setAction("EmailInfo");
            billingRequest.setContractId(contractId);
            billingRequest.setAttribute("pid", paramId);

            Document doc = transferData.postData(billingRequest, user);

            // Список емейлов
            List<String> emails = new ArrayList<>();
            result.setEmails(emails);

            if (dbInfo.versionCompare("5.2") < 0) {
                Element data = XMLUtils.selectElement(doc, "/data");
                for (Element e : XMLUtils.selectElements(data, "email_list/row")) {
                    emails.add(e.getAttribute("text"));
                }

                // Список активированных рассылок
                result.setSubscrs(Utils.toList(data.getAttribute("buf")));

                // eid
                result.setEid(Utils.parseInt(data.getAttribute("id")));

                // Список существующих рассылок id: title
                billingRequest = new Request();
                billingRequest.setModule(CONTRACT_MODULE_ID);
                billingRequest.setAction("GetEmailTree");
                doc = transferData.postData(billingRequest, user);

                Element treeElm = XMLUtils.selectElement(doc, "/data/tree");
                List<IdStringTitleTreeItem> treeValues = null;
                if (treeElm != null) {
                    treeValues = getEmailSubscrTree(treeElm);
                }
                result.setSubscrsTree(treeValues);
            } else {
                /*c 5.2 xml = <?xml version="1.0" encoding="windows-1251"?><data buf="" error="false" id="1850" status="ok"><table><data><row email="shamil@bitel.ru" name=""/></data></table></data>*/
                for (Element e : XMLUtils.selectElements(doc, "/data/table/data/row")) {
                    emails.add(e.getAttribute("name") + " <" + e.getAttribute("email") + ">");
                }
            }

            return result;
        }
    }

    public ParamAddressValue getAddressParam(int contractId, int paramId) {
        ParamAddressValue result = null;

        Request req = new Request();

        req.setModule(CONTRACT_MODULE_ID);
        req.setAction("AddressInfo");
        req.setContractId(contractId);
        req.setAttribute("pid", paramId);

        Document doc = transferData.postData(req, user);

        Element address = XMLUtils.selectElement(doc, "/data/address");
        if (address != null && Utils.notBlankString(XMLUtils.selectText(address, "@hid"))) {
            result = new ParamAddressValue();

            result.setCityId(Utils.parseInt(address.getAttribute("cityid")));
            result.setCityTitle(address.getAttribute("city"));
            result.setAreaTitle(address.getAttribute("areaValue"));
            result.setQuarterTitle(address.getAttribute("quarterValue"));
            result.setStreetId(Utils.parseInt(address.getAttribute("streetid")));
            result.setStreetTitle(address.getAttribute("street"));
            result.setHouseId(Utils.parseInt(address.getAttribute("hid")));
            result.setHouse(address.getAttribute("house"));
            result.setFlat(address.getAttribute("flat"));
            result.setRoom(address.getAttribute("room"));
            result.setComment(address.getAttribute("comment"));
            result.setPod(address.getAttribute("pod"));
            result.setFloor(address.getAttribute("floor"));
        }

        return result;
    }

    public ParamList getListParamValue(int contractId, int paramId) {
        if (dbInfo.versionCompare("9.2") >= 0) {
            RequestJsonRpc req = new RequestJsonRpc(KERNEL_CONTRACT_API, "ContractService", "contractParameterGet");
            req.setParam("contractId", contractId);
            req.setParam("parameterId", paramId);

            JsonNode ret = transferData.postDataReturn(req, user);

            EntityAttrList attrList = jsonMapper.convertValue(ret, EntityAttrList.class);
            ParamList result = new ParamList();
            result.setId(attrList.getValue());
            result.setTitle(attrList.getTitle());
            getParamListValues(paramId).forEach(result::addValue);

            return result;
        } else {
            ParamList paramList = new ParamList();
            Request req = new Request();

            req.setModule(CONTRACT_MODULE_ID);
            req.setAction("GetListParam");
            req.setContractId(contractId);
            req.setAttribute("pid", paramId);

            Document doc = transferData.postData(req, user);

            paramList.setId(Utils.parseInt(XMLUtils.selectText(doc, "/data/values/@value"), -1));

            Element dataElement = doc.getDocumentElement();
            NodeList nodeList = dataElement.getElementsByTagName("item");
            for (int index = 0; index < nodeList.getLength(); index++) {
                IdTitle value = new IdTitle();
                Element element = (Element) nodeList.item(index);

                value.setId(Utils.parseInt(element.getAttribute("id")));
                value.setTitle(element.getAttribute("title"));

                paramList.addValue(value);
            }

            return paramList;
        }
    }

    public void updateFlagParameter(int contractId, int paramId, boolean value) {
        Request req = new Request();

        req.setModule(CONTRACT_MODULE_ID);
        req.setAction("UpdateParameterType5");
        req.setContractId(contractId);
        req.setAttribute("pid", paramId);
        req.setAttribute("value", value);

        transferData.postData(req, user);
    }

    public void updateTextParameter(int contractId, int paramId, String value) {
        if (dbInfo.versionCompare("9.2") >= 0) {
            RequestJsonRpc req = new RequestJsonRpc(KERNEL_CONTRACT_API, "ContractService", "contractParameterTextUpdate");
            req.setParam("contractId", contractId);
            req.setParam("parameter", new EntityAttrText(contractId, paramId, value));
            transferData.postData(req, user);
        } else {
            Request req = new Request();

            req.setModule(CONTRACT_MODULE_ID);
            req.setAction("UpdateParameterType1");
            req.setContractId(contractId);
            req.setAttribute("pid", paramId);
            req.setAttribute("value", value);

            transferData.postData(req, user);
        }
    }

    public void updateListParameter(int contractId, int paramId, int value) {
        if (dbInfo.versionCompare("9.2") >= 0) {
            EntityAttrList attrEmail = new EntityAttrList( contractId, paramId,value,null );

            RequestJsonRpc req = new RequestJsonRpc(KERNEL_CONTRACT_API, "ContractService", "contractParameterListUpdate");
            req.setParam("contractId", contractId);
            req.setParam("parameter", attrEmail);

            transferData.postDataReturn(req, user);
        } else {
            Request req = new Request();

            req.setModule(CONTRACT_MODULE_ID);
            req.setAction("UpdateListParam");
            req.setContractId(contractId);
            req.setAttribute("pid", paramId);
            req.setAttribute("value", value);

            transferData.postData(req, user);
        }
    }

    public void updateListParameter(int contractId, int paramId, String value) {
        updateListParameter(contractId, paramId, Utils.parseInt(value));
    }

    public void updateAddressParameter(int contractId, int paramId, ParamAddressValue address) {
        if (dbInfo.versionCompare("9.2") >= 0) {
            EntityAttrAddress attrAddress = new EntityAttrAddress(0, paramId);
            attrAddress.setHouseId(address.getHouseId());
            attrAddress.setPod( Utils.parseInt(address.getPod()));
            attrAddress.setFloor( Utils.parseInt(address.getFloor()));
            attrAddress.setFlat( Utils.maskNull(address.getFlat()));
            attrAddress.setRoom(Utils.maskNull(address.getRoom()));
            attrAddress.setComment(Utils.maskNull(address.getComment()));

            RequestJsonRpc req = new RequestJsonRpc(KERNEL_CONTRACT_API, "ContractService", "contractParameterAddressUpdate");
            req.setParam("contractId", contractId);
            req.setParam("parameter", attrAddress);

            transferData.postDataReturn(req, user);
        } else {
            Request req = new Request();

            req.setModule(CONTRACT_MODULE_ID);
            req.setAction("UpdateAddressInfo");
            req.setContractId(contractId);
            req.setAttribute("pid", paramId);
            req.setAttribute("index", address.getIndex());
            req.setAttribute("cityStr", address.getCityTitle());
            req.setAttribute("streetStr", address.getStreetTitle());
            req.setAttribute("houseAndFrac", address.getHouse());
            req.setAttribute("hid", address.getHouseId());
            req.setAttribute("pod", address.getPod());
            req.setAttribute("floor", address.getFloor());
            req.setAttribute("flat", address.getFlat());
            req.setAttribute("room", address.getRoom());
            req.setAttribute("comment", address.getComment());

            transferData.postData(req, user);
        }
    }

    public void updateDateParameter(int contractId, int paramId, Date value) {
        Request req = new Request();

        req.setModule(CONTRACT_MODULE_ID);
        req.setAction("UpdateParameterType" + String.valueOf(ParameterType.ContractType.TYPE_DATE));
        req.setContractId(contractId);
        req.setAttribute("pid", paramId);
        req.setAttribute("value", new SimpleDateFormat(TimeUtils.PATTERN_DDMMYYYY).format(value));

        transferData.postData(req, user);
    }

    public void updateDateParameter(int contractId, int paramId, String value) {
        if (dbInfo.versionCompare("9.2") >= 0) {
            RequestJsonRpc req = new RequestJsonRpc(KERNEL_CONTRACT_API, "ContractService", "contractParameterDateUpdate");
            req.setParam("contractId", contractId);
            req.setParam("parameter", new EntityAttrDate(contractId, paramId, TimeUtils.parse(value, TimeUtils.FORMAT_TYPE_YMD)));
            transferData.postData(req, user);
        } else {
            Request req = new Request();

            req.setModule(CONTRACT_MODULE_ID);
            req.setAction("UpdateParameterType6");
            req.setContractId(contractId);
            req.setAttribute("pid", paramId);
            req.setAttribute("value", value);

            transferData.postData(req, user);
        }
    }

    public void updatePhoneParameter(int contractId, int paramId, ParameterPhoneValue phoneValue) {
        if (dbInfo.versionCompare("9.2") >= 0) {
            var value = new EntityAttrPhone(contractId, paramId);
            for (var item : phoneValue.getItemList())
                value.addContact(item.getPhone(), item.getComment());

            RequestJsonRpc req = new RequestJsonRpc(KERNEL_CONTRACT_API, "ContractService", "contractParameterPhoneUpdate");
            req.setParam("contractId", contractId);
            req.setParam("parameter", value);
            transferData.postData(req, user);
        } else {
            Request req = new Request();

            req.setModule(CONTRACT_MODULE_ID);
            req.setAction("UpdatePhoneInfo");
            req.setContractId(contractId);
            req.setAttribute("pid", paramId);

            List<ParameterPhoneValueItem> phones = phoneValue.getItemList();

            int itemCount = phones.size();
            // до 5.1 было просто зашито 5 телефонов
            if (dbInfo.versionCompare("5.1") <= 0) {
                itemCount = 5;
            }

            for (int i = 0; i < itemCount; i++) {
                req.setAttribute("phone" + (i + 1), i < phones.size() ? phones.get(i).getPhone() : "");
                req.setAttribute("comment" + (i + 1), i < phones.size() ? phones.get(i).getComment() : "");
            }

            // с 5.2 требуется count
            req.setAttribute("count", itemCount);

            transferData.postData(req, user);
        }
    }

    public void updateEmailParameter(int contractId, int paramId, Collection<ParameterEmailValue> emailValues)
            {

        if (dbInfo.versionCompare("9.2") >= 0) {

            EntityAttrEmail attrEmail = new EntityAttrEmail( contractId, paramId );
            emailValues.stream().forEach(p->attrEmail.addContact(p.getComment(),p.getValue()));

            RequestJsonRpc req = new RequestJsonRpc(KERNEL_CONTRACT_API, "ContractService", "contractParameterEmailUpdate");
            req.setParam("contractId", contractId);
            req.setParam("parameter", attrEmail);
            transferData.postDataReturn(req, user);

        } else {
            Request req = new Request();

            req.setModule(CONTRACT_MODULE_ID);
            req.setAction("UpdateEmailInfo");
            req.setContractId(contractId);
            req.setAttribute("pid", paramId);

            StringBuilder emails = new StringBuilder();
            for (ParameterEmailValue email : emailValues) {
                if (Utils.isValidEmail(email.getValue())) {
                    Utils.addSeparated(emails, "\n", email.getComment() + " <" + email.getValue().toLowerCase() + ">");
                }
            }
            req.setAttribute("e-mail", emails);

            req.setAttribute("eid", 0);
            req.setAttribute("buf", "");

            transferData.postData(req, user);
        }
    }

    public void updateParameterGroup(int contractId, int groupId) {
        if (dbInfo.versionCompare("9.2") >= 0) {
            RequestJsonRpc req = new RequestJsonRpc(KERNEL_CONTRACT_API, "ContractService", "contractParameterGroupSet");
            req.setParam("contractId", contractId);
            req.setParam("paramGroupId", groupId);

            transferData.postDataReturn(req, user);
        } else {
            Request req = new Request();

            req.setModule(CONTRACT_MODULE_ID);
            req.setAction("SetGrContract");
            req.setContractId(contractId);
            req.setAttribute("pgid", groupId);

            transferData.postData(req, user);
        }
    }

    public void copyObjectParamsToContract(Connection con, int objectId, int contractId) throws SQLException, BGMessageException {
        String copyParamsMapping = dbInfo.getSetup().get("copyParamMapping", "");

        if (Utils.isBlankString(copyParamsMapping)) {
            return;
        }

        String[] params = copyParamsMapping.split(";");

        for (String pair : params) {
            String[] keyValue = pair.split(":");
            int fromParamId = Utils.parseInt(
                    keyValue[0].indexOf('[') == -1 ? keyValue[0] : keyValue[0].substring(0, keyValue[0].indexOf('[')));

            copyObjectParamToContract(objectId, contractId, fromParamId, con);
        }
    }

    public void copyObjectParamToContract(int objectId, int contractId, int fromParamId, Connection con)
            throws SQLException, BGMessageException {
        if (objectId == 0 || contractId == 0 || fromParamId == 0) {
            throw new BGMessageException(
                    "Ошибка входных параметров при копировании параметра в биллинг " + dbInfo.getId() + "!");
        }

        ParamValueDAO paramDAO = new ParamValueDAO(con);
        String copyParamsMapping = dbInfo.getSetup().get("copyParamMapping", "");

        if (Utils.isBlankString(copyParamsMapping)) {
            return;
        }

        int toParamId = 0;
        String[] params = copyParamsMapping.split(";");
        String[] keyValue = new String[0];

        for (String pair : params) {
            keyValue = pair.split(":");

            if (fromParamId == Utils.parseInt(keyValue[0].indexOf('[') == -1 ? keyValue[0]
                    : keyValue[0].substring(0, keyValue[0].indexOf('[')))) {
                toParamId = Utils.parseInt(keyValue[1].indexOf('[') == -1 ? keyValue[1]
                        : keyValue[1].substring(0, keyValue[1].indexOf('[')));
                break;
            }
        }

        if (toParamId == 0) {
            return;
        }

        try {
            Request request = new Request();
            request.setModule("contract");
            request.setAttribute("cid", contractId);
            request.setAttribute("pid", toParamId);

            Parameter param = ParameterCache.getParameter(fromParamId);
            if (param == null) {
                throw new BGMessageException(
                        "Ошибка при копировании параметра: параметр с ID=" + fromParamId + " не существует!");
            }
            String type = param.getType();

            if (Parameter.TYPE_ADDRESS.equals(type)) {
                SortedMap<Integer, ParameterAddressValue> values = paramDAO.getParamAddress(objectId, fromParamId);

                if (values.size() > 0) {
                    ParameterAddressValue value = values.get(values.firstKey());

                    request.setAction("UpdateAddressInfo");

                    request.setAttribute("hid", value.getHouseId());
                    request.setAttribute("flat", value.getFlat());
                    request.setAttribute("floor", value.getFloor() == null ? "" : value.getFloor());
                    request.setAttribute("pod", value.getPod());
                    request.setAttribute("room", value.getRoom());
                    request.setAttribute("comment", value.getComment());

                    transferData.postData(request, user);
                }
            } else if (Parameter.TYPE_TEXT.equals(type)) {
                String value = paramDAO.getParamText(objectId, fromParamId);
                if (Utils.notBlankString(value)) {
                    request.setAction("UpdateParameterType1");
                    request.setAttribute("value", value);

                    transferData.postData(request, user);
                }
            } else if (Parameter.TYPE_LIST.equals(type)) {
                Set<Integer> listValue = paramDAO.getParamList(objectId, fromParamId);
                String fromValue;

                if (listValue != null && listValue.size() > 0) {
                    // биллинг не поддерживает множественные значения списков, поэтому берем первый
                    fromValue = listValue.iterator().next().toString();

                    String toValue = null;
                    // преобразование по карте соответствий
                    if (keyValue[0].indexOf('[') > 0) {
                        String[] fromValues = keyValue[0]
                                .substring(keyValue[0].indexOf('[') + 1, keyValue[0].indexOf(']')).split(",");
                        String[] toValues = keyValue[1]
                                .substring(keyValue[1].indexOf('[') + 1, keyValue[1].indexOf(']')).split(",");

                        for (int i = 0; i < fromValues.length; i++) {
                            if (fromValues[i].equals(fromValue)) {
                                toValue = toValues[i];
                                break;
                            }
                        }
                    } else {
                        toValue = fromValue;
                    }

                    if (Utils.notBlankString(toValue)) {
                        new ContractParamDAO(user, dbInfo).updateListParameter(contractId, toParamId, toValue);
                    }
                }
            } else if (Parameter.TYPE_PHONE.equals(type)) {
                ParameterPhoneValue value = paramDAO.getParamPhone(objectId, fromParamId);
                if (value != null) {
                    new ContractParamDAO(user, dbInfo).updatePhoneParameter(contractId, toParamId, value);
                }
            } else if (Parameter.TYPE_DATE.equals(type)) {
                Date value = paramDAO.getParamDate(objectId, fromParamId);
                if (value != null) {
                    new ContractParamDAO(user, dbInfo).updateDateParameter(contractId, toParamId, value);
                }
            } else if (Parameter.TYPE_EMAIL.equals(type)) {
                SortedMap<Integer, ParameterEmailValue> value = paramDAO.getParamEmail(objectId, fromParamId);
                if (value.size() > 0) {
                    new ContractParamDAO(user, dbInfo).updateEmailParameter(contractId, toParamId, value.values());
                }
            }
        } catch (BGException exp) {
            throw new BGMessageExceptionWithoutL10n("Ошибка при копировании параметра в биллинг! [" + fromParamId + " - "
                    + toParamId + "] " + exp.getMessage());
        }
    }
}
