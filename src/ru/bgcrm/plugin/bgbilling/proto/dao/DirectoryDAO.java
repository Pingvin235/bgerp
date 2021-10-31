package ru.bgcrm.plugin.bgbilling.proto.dao;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.fasterxml.jackson.databind.JsonNode;

import ru.bgcrm.model.BGException;
import ru.bgcrm.model.IdTitle;
import ru.bgcrm.model.process.TypeTreeItem;
import ru.bgcrm.model.user.User;
import ru.bgcrm.plugin.bgbilling.DBInfo;
import ru.bgcrm.plugin.bgbilling.Request;
import ru.bgcrm.plugin.bgbilling.RequestJsonRpc;
import ru.bgcrm.plugin.bgbilling.dao.BillingDAO;
import ru.bgcrm.plugin.bgbilling.proto.model.ContractObjectModuleInfo;
import ru.bgcrm.plugin.bgbilling.proto.model.ContractObjectModuleInfo.ContractObjectModule;
import ru.bgcrm.plugin.bgbilling.proto.model.ContractObjectModuleInfo.ContractObjectModuleData;
import ru.bgcrm.plugin.bgbilling.proto.model.UserInfo;
import ru.bgcrm.plugin.bgbilling.proto.model.status.ContractStatus;
import ru.bgcrm.plugin.bgbilling.proto.model.tariff.ContractTariff;
import ru.bgcrm.plugin.bgbilling.proto.model.tariff.TariffGroup;
import ru.bgcrm.plugin.bgbilling.ws.contract.balance.paymcharge.AbstractPaymentTypes;
import ru.bgcrm.plugin.bgbilling.ws.contract.balance.paymcharge.PaymentAndChargeService;
import ru.bgcrm.plugin.bgbilling.ws.contract.balance.paymcharge.PaymentAndChargeService_Service;
import ru.bgcrm.plugin.bgbilling.ws.contract.balance.paymcharge.PaymentTypeItem;
import ru.bgcrm.plugin.bgbilling.ws.contract.status.ContractStatusMonitorService;
import ru.bgcrm.plugin.bgbilling.ws.contract.status.ContractStatusMonitorService_Service;
import ru.bgcrm.plugin.bgbilling.ws.contract.status.Status;
import ru.bgcrm.util.TimeUtils;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.XMLUtils;

public class DirectoryDAO extends BillingDAO {
    private static final String CONTRACT_STATUS_MODULE_ID = "ru.bitel.bgbilling.kernel.contract.status";
    
    private static final String TARIFF_MODULE_ID = "tariff";
    private static final String ADMIN_MODULE_ID = "admin";
    private static final String SERVICE_MODULE_ID = "service";
    private static final String CONTRACT_MODULE_ID = "contract";
    private static final String CONTRACT_OBJECT_MODULE_ID = "contract.object";

    private static final List<IdTitle> FIXED_OLD_STATUS_LIST = new ArrayList<IdTitle>();

    static {
        FIXED_OLD_STATUS_LIST.add(new IdTitle(ContractStatus.ACTIVE, "активен"));
        FIXED_OLD_STATUS_LIST.add(new IdTitle(ContractStatus.SUSPENDED, "приостановлен"));
        FIXED_OLD_STATUS_LIST.add(new IdTitle(ContractStatus.CLOSED, "закрыт"));
        FIXED_OLD_STATUS_LIST.add(new IdTitle(ContractStatus.DISABLED, "заблокирован"));
    }

    public DirectoryDAO(User user, String billingId) throws BGException {
        super(user, billingId);
    }

    public DirectoryDAO(User user, DBInfo dbInfo) throws BGException {
        super(user, dbInfo);
    }

    public List<IdTitle> getContractStatusList() throws BGException {
        List<IdTitle> result = null;

        if (dbInfo.getVersion().compareTo("5.2") < 0) {
            result = FIXED_OLD_STATUS_LIST;
        }
        else if (dbInfo.getVersion().compareTo("8.0") > 0) {
            RequestJsonRpc req = new RequestJsonRpc("ru.bitel.bgbilling.kernel.contract.api", "ContractStatusService", "getStatusList");
            req.setParam("onlyManual", true);
            result = readJsonValue(transferData.postDataReturn(req, user).traverse(),
                    jsonTypeFactory.constructCollectionType(List.class, IdTitle.class));
        }
        else if (dbInfo.getVersion().compareTo("6.2") >= 0) {
            RequestJsonRpc req = new RequestJsonRpc(CONTRACT_STATUS_MODULE_ID, "ContractStatusMonitorService", "getStatusList");
            req.setParam("onlyManual", true);

            result = readJsonValue(transferData.postDataReturn(req, user).traverse(),
                    jsonTypeFactory.constructCollectionType(List.class, IdTitle.class));
        }
        // TODO: Убрать поддержку вместе с сервисом.    
        else {
            result = new ArrayList<IdTitle>();
            try {
                ContractStatusMonitorService service = getWebService(ContractStatusMonitorService_Service.class,
                        ContractStatusMonitorService.class);
                for (Status status : service.getStatusList(true)) {
                    result.add(new IdTitle(status.getId(), status.getTitle()));
                }
            } catch (Exception e) {
                processWebServiceException(e);
            }
        }

        return result;
    }

    public List<IdTitle> scriptTypeList() throws BGException {
        Request request = new Request();
        request.setModule(ADMIN_MODULE_ID);
        request.setAction("ListDirectory");
        request.setAttribute("mode", "15");

        Document document = transferData.postData(request, user);

        Element dataElement = document.getDocumentElement();
        NodeList nodeList = dataElement.getElementsByTagName("item");

        List<IdTitle> scriptTypeList = new ArrayList<IdTitle>();
        for (int index = 0; index < nodeList.getLength(); index++) {
            Element rowElement = (Element) nodeList.item(index);
            IdTitle type = new IdTitle();
            type.setId(Utils.parseInt(rowElement.getAttribute("id")));
            type.setTitle(rowElement.getAttribute("title"));

            scriptTypeList.add(type);
        }

        return scriptTypeList;
    }

    public TypeTreeItem getContractPaymentTypes(Set<Integer> allowedTypeIds) throws BGException {
        TypeTreeItem contractPaymentTypes = new TypeTreeItem();
        contractPaymentTypes.setTitle("Все типы");

        if (dbInfo.getVersion().compareTo("6.1") >= 0) {
            RequestJsonRpc req = new RequestJsonRpc("ru.bitel.bgbilling.kernel.contract.balance", "PaymentService",
                    "paymentTypeTree");
            req.setParam("editable", 0);
            req.setParam("showEmptyRoot", false);

            JsonNode result = transferData.postDataReturn(req, user);

            try {
                // {"status":"ok","message":"","data":{"return":{"id":0,"title":"Типы","type":1,"editable":true,"parentId":null,"children":[{"id":33,"title":"OptimaPlus","type":0,"editable":true,"parentId":0,"children":null,...
                contractPaymentTypes = jsonMapper.convertValue(result, TypeTreeItem.class);
            } catch (Exception e) {
                throw new BGException(e);
            }
        } else if (dbInfo.getVersion().compareTo("5.2") >= 0) {
            try {
                PaymentAndChargeService service = getWebService(PaymentAndChargeService_Service.class,
                        PaymentAndChargeService.class);
                PaymentTypeItem rootItem = service.getPaymentTree(-1);

                loadTypes(contractPaymentTypes, rootItem);
            } catch (Exception e) {
                processWebServiceException(e);
            }
        } else {
            Request request = new Request();
            request.setModule(CONTRACT_MODULE_ID);
            request.setAction("ContractPayment");
            request.setAttribute("id", "new");

            Document document = transferData.postData(request, user);

            Element dataElement = document.getDocumentElement();
            NodeList nodeList = dataElement.getElementsByTagName("item");

            TypeTreeItem typeClass = new TypeTreeItem();

            for (int index = 0; index < nodeList.getLength(); index++) {
                Element itemElement = (Element) nodeList.item(index);

                int typeId = Utils.parseInt(itemElement.getAttribute("id"));
                if (allowedTypeIds.isEmpty() || allowedTypeIds.contains(typeId)) {

                    if ("1".equals(itemElement.getAttribute("type"))) {
                        if (Utils.notBlankString(typeClass.getTitle()) || typeClass.getChildren().size() > 0) {
                            contractPaymentTypes.addChild(typeClass);
                        }
                        typeClass = new TypeTreeItem();
                        typeClass.setId(typeId);
                        typeClass.setTitle(itemElement.getAttribute("title"));
                    } else {
                        TypeTreeItem type = new TypeTreeItem();
                        type.setId(typeId);
                        type.setTitle(itemElement.getAttribute("title"));

                        typeClass.addChild(type);
                    }
                }
            }
            if (Utils.notBlankString(typeClass.getTitle()) || typeClass.getChildren().size() > 0) {
                contractPaymentTypes.addChild(typeClass);
            }
        }

        return contractPaymentTypes;
    }

    public TypeTreeItem getContractChargeTypes(Set<Integer> allowedTypeIds) throws BGException {
        TypeTreeItem contractChargeTypes = new TypeTreeItem();
        contractChargeTypes.setTitle("Все типы");

        if (dbInfo.getVersion().compareTo("6.1") >= 0) {
            RequestJsonRpc req = new RequestJsonRpc("ru.bitel.bgbilling.kernel.contract.balance", "ChargeService",
                    "chargeTypeTree");
            req.setParam("editable", 0);
            req.setParam("isPayback", false);
            req.setParam("showEmptyRoot", false);

            JsonNode result = transferData.postDataReturn(req, user);

            try {
                // {"status":"ok","message":"","data":{"return":{"id":0,"title":"Типы","type":1,"editable":true,"parentId":null,"children":[{"id":33,"title":"OptimaPlus","type":0,"editable":true,"parentId":0,"children":null,...
                contractChargeTypes = jsonMapper.convertValue(result, TypeTreeItem.class);
            } catch (Exception e) {
                throw new BGException(e);
            }

        } else if (dbInfo.getVersion().compareTo("5.2") >= 0) {
            try {
                PaymentAndChargeService service = getWebService(PaymentAndChargeService_Service.class,
                        PaymentAndChargeService.class);
                PaymentTypeItem rootItem = service.getChargeTree(-1);

                loadTypes(contractChargeTypes, rootItem);
            } catch (Exception e) {
                processWebServiceException(e);
            }
        } else {
            Request request = new Request();
            request.setModule(CONTRACT_MODULE_ID);
            request.setAction("ContractCharge");
            request.setAttribute("id", "new");

            Document document = transferData.postData(request, user);

            Element dataElement = document.getDocumentElement();
            NodeList nodeList = dataElement.getElementsByTagName("item");

            /*TypeTreeItem contractChargeTypes = new TypeTreeItem();
            contractChargeTypes.setTitle( "Все типы" );*/

            TypeTreeItem typeClass = new TypeTreeItem();

            for (int index = 0; index < nodeList.getLength(); index++) {
                Element itemElement = (Element) nodeList.item(index);

                int typeId = Utils.parseInt(itemElement.getAttribute("id"));
                if (allowedTypeIds.isEmpty() || allowedTypeIds.contains(typeId)) {
                    if ("1".equals(itemElement.getAttribute("type"))) {
                        if (Utils.notBlankString(typeClass.getTitle()) || typeClass.getChildren().size() > 0) {
                            contractChargeTypes.addChild(typeClass);
                        }
                        typeClass = new TypeTreeItem();
                        typeClass.setId(typeId);
                        typeClass.setTitle(itemElement.getAttribute("title"));
                    } else {
                        TypeTreeItem type = new TypeTreeItem();
                        type.setId(typeId);
                        type.setTitle(itemElement.getAttribute("title"));

                        typeClass.addChild(type);
                    }
                }
            }
            if (Utils.notBlankString(typeClass.getTitle()) || typeClass.getChildren().size() > 0) {
                contractChargeTypes.addChild(typeClass);
            }
        }

        return contractChargeTypes;
    }

    public void loadTypes(TypeTreeItem contractChargeTypes, AbstractPaymentTypes rootItem) {
        for (AbstractPaymentTypes type : rootItem.getChildren()) {
            TypeTreeItem item = new TypeTreeItem();
            item.setId(type.getId());
            item.setTitle(type.getTitle());

            contractChargeTypes.addChild(item);

            loadTypes(item, type);
        }
    }

    public List<IdTitle> getServiceTypeList(int moduleId) throws BGException {
        List<IdTitle> list = new ArrayList<IdTitle>();

        Request req = new Request();
        req.setModule("service");
        req.setAction("GetServiceList");
        req.setModuleID(moduleId);

        Document doc = transferData.postData(req, user);
        for (Element e : XMLUtils.selectElements(doc, "/data/services/service")) {
            list.add(new IdTitle(Utils.parseInt(e.getAttribute("id")), e.getAttribute("title")));
        }

        return list;
    }

    public TariffGroup getTariffGroup(int groupId) throws BGException {
        Request request = new Request();
        request.setModule(TARIFF_MODULE_ID);
        request.setAction("GetTariffGroup");
        request.setAttribute("id", groupId);

        Document doc = transferData.postData(request, user);

        TariffGroup tariffGroup = new TariffGroup();

        XPathFactory xpathFact = XPathFactory.newInstance();
        XPath xpath = xpathFact.newXPath();

        xpath.setNamespaceContext(new NamespaceContext() {
            @Override
            public String getNamespaceURI(String prefix) {
                if ("common".equals(prefix)) {
                    return "http://common.bitel.ru";
                } else if ("xsi".equals(prefix)) {
                    return "http://www.w3.org/2001/XMLSchema-instanc";
                }
                return "?";
            }

            @Override
            public String getPrefix(String namespaceURI) {
                if ("http://common.bitel.ru".equals(namespaceURI)) {
                    return "common";
                } else if ("http://www.w3.org/2001/XMLSchema-instanc".equals(namespaceURI)) {
                    return "xsi";
                }
                return "?";
            }

            @SuppressWarnings("rawtypes")
            @Override
            public Iterator getPrefixes(String namespaceURI) {
                return null;
            }
        });

        try {
            tariffGroup.setId(groupId);
            tariffGroup.setDaysForward(
                    Utils.parseInt(xpath.evaluate("/data/common:result/attributes/item[@key=tm]/value/text()", doc)));
            tariffGroup.setPosition(
                    Utils.parseInt(xpath.evaluate("/data/common:result/attributes/item[@key=pos]/value/text()", doc)));
            tariffGroup.setTitle(xpath.evaluate("/data/common:result/attributes/item[@key=title]/value/text()", doc));

            NodeList nodeSet = (NodeList) xpath.evaluate("/data/common:result/data/node()", doc,
                    XPathConstants.NODESET);
            List<ContractTariff> contractTariffList = new ArrayList<ContractTariff>();
            for (int i = 0; i < nodeSet.getLength(); i++) {
                Node node = nodeSet.item(i);
                ContractTariff contractTariff = new ContractTariff();
                contractTariff.setId(Utils.parseInt(XMLUtils.selectText(node, "@id")));
                contractTariff.setTitle(XMLUtils.selectText(node, "@title"));
                contractTariff
                        .setDateFrom(TimeUtils.parse(XMLUtils.selectText(node, "@date1"), TimeUtils.FORMAT_TYPE_YMD));
                contractTariff
                        .setDateTo(TimeUtils.parse(XMLUtils.selectText(node, "@date2"), TimeUtils.FORMAT_TYPE_YMD));
                contractTariffList.add(contractTariff);
            }

            tariffGroup.setTariffList(contractTariffList);
        } catch (XPathExpressionException e) {
            return null;
        }

        return tariffGroup;
    }

    public List<IdTitle> getRegistredTariffGroupList(int selectedTariffGroupId) throws BGException {
        Request request = new Request();
        request.setModule(TARIFF_MODULE_ID);
        request.setAction("ListTariffGroups");

        Document document = transferData.postData(request, user);

        Element dataElement = document.getDocumentElement();
        NodeList nodeList = dataElement.getElementsByTagName("item");

        List<IdTitle> registerGroupTariffList = new ArrayList<IdTitle>();
        for (int index = 0; index < nodeList.getLength(); index++) {
            Element rowElement = (Element) nodeList.item(index);
            IdTitle type = new IdTitle();
            type.setId(Utils.parseInt(rowElement.getAttribute("id")));
            type.setTitle(rowElement.getAttribute("title"));

            if (selectedTariffGroupId == type.getId()) {
                registerGroupTariffList.add(0, type);
            } else {
                registerGroupTariffList.add(type);
            }
        }

        return registerGroupTariffList;
    }

    public List<IdTitle> getBillingModuleList() throws BGException {
        Request request = new Request();
        request.setModule(SERVICE_MODULE_ID);
        request.setAction("Modules");

        Document document = transferData.postData(request, user);

        Element dataElement = document.getDocumentElement();
        NodeList nodeList = dataElement.getElementsByTagName("module");

        List<IdTitle> moduleList = new ArrayList<IdTitle>();
        for (int index = 0; index < nodeList.getLength(); index++) {
            Element rowElement = (Element) nodeList.item(index);
            IdTitle module = new IdTitle();
            module.setId(Utils.parseInt(rowElement.getAttribute("id")));
            module.setTitle(rowElement.getAttribute("title"));

            moduleList.add(module);
        }

        return moduleList;
    }

    public List<IdTitle> getContractGroups() throws BGException {
        Request request = new Request();
        request.setModule(ADMIN_MODULE_ID);
        request.setAction("GetContractGroupList");

        Document doc = transferData.postData(request, user);

        Element dataElement = doc.getDocumentElement();
        NodeList nodeList = dataElement.getElementsByTagName("row");

        List<IdTitle> groupList = new ArrayList<>();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Element rowElement = (Element) nodeList.item(i);
            IdTitle group = new IdTitle();
            group.setId(Utils.parseInt(rowElement.getAttribute("f0")));
            group.setTitle(rowElement.getAttribute("f2"));

            groupList.add(group);
        }
        return groupList;
    }

    public List<IdTitle> contractObjectTypeList() throws BGException {
        Request request = new Request();
        request.setModule(CONTRACT_OBJECT_MODULE_ID);
        request.setAction("TypeList");
        request.setAttribute("onlyVisible", "1");

        Document document = transferData.postData(request, user);

        Element dataElement = document.getDocumentElement();
        NodeList nodeList = dataElement.getElementsByTagName("item");

        List<IdTitle> objectTypeList = new ArrayList<IdTitle>();
        for (int index = 0; index < nodeList.getLength(); index++) {
            Element rowElement = (Element) nodeList.item(index);
            IdTitle type = new IdTitle();
            type.setId(Utils.parseInt(rowElement.getAttribute("id")));
            type.setTitle(rowElement.getAttribute("title"));

            objectTypeList.add(type);
        }

        return objectTypeList;
    }

    public ContractObjectModuleInfo contractObjectModuleList(int objectId) throws BGException {
        Request request = new Request();
        request.setModule(CONTRACT_OBJECT_MODULE_ID);
        request.setAction("ObjectModuleTable");
        request.setAttribute("object_id", objectId);

        Document document = transferData.postData(request, user);

        ContractObjectModuleInfo moduleInfo = new ContractObjectModuleInfo();

        Element dataElement = document.getDocumentElement();
        NodeList nodeList = dataElement.getElementsByTagName("row");

        for (int index = 0; index < nodeList.getLength(); index++) {
            Element rowElement = (Element) nodeList.item(index);
            ContractObjectModuleData data = moduleInfo.new ContractObjectModuleData();

            data.setComment(rowElement.getAttribute("comment"));
            data.setData(rowElement.getAttribute("data"));
            data.setModule(rowElement.getAttribute("module"));
            data.setPeriod(rowElement.getAttribute("period"));

            moduleInfo.getModuleDataList().add(data);
        }

        nodeList = dataElement.getElementsByTagName("module");

        for (int index = 0; index < nodeList.getLength(); index++) {
            Element rowElement = (Element) nodeList.item(index);
            ContractObjectModule data = moduleInfo.new ContractObjectModule();

            data.setId(objectId);
            data.setName(rowElement.getAttribute("name"));
            data.setPackClient(rowElement.getAttribute("pack_client"));
            data.setTitle(rowElement.getAttribute("title"));

            moduleInfo.getModuleList().add(data);
        }

        return moduleInfo;
    }

    public Map<Integer, UserInfo> getUsersInfo() throws BGException {
        RequestJsonRpc req = new RequestJsonRpc("ru.bitel.bgbilling.kernel.bgsecure", "UserService",
                "userInfoList");
        JsonNode res = transferData.postDataReturn(req, user);
        System.out.println(res);
        List<UserInfo> userList = readJsonValue(res.traverse(), jsonTypeFactory.constructCollectionType(List.class, UserInfo.class));
        Map<Integer, UserInfo> resultMap = userList.stream().collect(Collectors.toMap(i->{
                if(i.getId()==-1&&!i.getName().equals("Пользователь")){
                    return -1*i.getId()*i.getName().codePointAt(0);//надо кудато деть эту гадость
                }; return i.getId();
            }, Function.identity()));
        return resultMap;

    }
}
