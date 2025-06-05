package ru.bgcrm.plugin.bgbilling.proto.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.bgerp.model.base.IdTitle;
import org.bgerp.model.base.tree.IdTitleTreeItem;
import org.bgerp.util.xml.XMLUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.fasterxml.jackson.databind.JsonNode;

import ru.bgcrm.model.user.User;
import ru.bgcrm.plugin.bgbilling.DBInfo;
import ru.bgcrm.plugin.bgbilling.Request;
import ru.bgcrm.plugin.bgbilling.RequestJsonRpc;
import ru.bgcrm.plugin.bgbilling.dao.BillingDAO;
import ru.bgcrm.plugin.bgbilling.proto.model.UserInfo;
import ru.bgcrm.plugin.bgbilling.proto.model.status.ContractStatus;
import ru.bgcrm.util.Utils;

public class DirectoryDAO extends BillingDAO {
    private static final List<IdTitle> FIXED_OLD_STATUS_LIST = new ArrayList<>();
    static {
        FIXED_OLD_STATUS_LIST.add(new IdTitle(ContractStatus.ACTIVE, "активен"));
        FIXED_OLD_STATUS_LIST.add(new IdTitle(ContractStatus.SUSPENDED, "приостановлен"));
        FIXED_OLD_STATUS_LIST.add(new IdTitle(ContractStatus.CLOSED, "закрыт"));
        FIXED_OLD_STATUS_LIST.add(new IdTitle(ContractStatus.DISABLED, "заблокирован"));
    }

    public DirectoryDAO(User user, String billingId) {
        super(user, billingId);
    }

    public DirectoryDAO(User user, DBInfo dbInfo) {
        super(user, dbInfo);
    }

    public List<IdTitle> getContractStatusList(boolean onlyManual) {
        List<IdTitle> result = null;

        if (dbInfo.versionCompare("5.2") < 0) {
            result = FIXED_OLD_STATUS_LIST;
        }
        else if (dbInfo.versionCompare("8.0") > 0) {
            RequestJsonRpc req = new RequestJsonRpc(ContractDAO.KERNEL_CONTRACT_API, "ContractStatusService", "getStatusList");
            req.setParam("onlyManual", onlyManual);
            result = readJsonValue(transferData.postDataReturn(req, user).traverse(),
                    jsonTypeFactory.constructCollectionType(List.class, IdTitle.class));
        }
        else  {
            RequestJsonRpc req = new RequestJsonRpc("ru.bitel.bgbilling.kernel.contract.status", "ContractStatusMonitorService", "getStatusList");
            req.setParam("onlyManual", onlyManual);
            result = readJsonValue(transferData.postDataReturn(req, user).traverse(),
                    jsonTypeFactory.constructCollectionType(List.class, IdTitle.class));
        }
        return result;
    }

    public List<IdTitle> scriptTypeList() {
        Request request = new Request();
        request.setModule("admin");
        request.setAction("ListDirectory");
        request.setAttribute("mode", "15");

        Document document = transferData.postData(request, user);

        Element dataElement = document.getDocumentElement();
        NodeList nodeList = dataElement.getElementsByTagName("item");

        List<IdTitle> scriptTypeList = new ArrayList<>();
        for (int index = 0; index < nodeList.getLength(); index++) {
            Element rowElement = (Element) nodeList.item(index);
            IdTitle type = new IdTitle();
            type.setId(Utils.parseInt(rowElement.getAttribute("id")));
            type.setTitle(rowElement.getAttribute("title"));

            scriptTypeList.add(type);
        }

        return scriptTypeList;
    }

    public IdTitleTreeItem getContractPaymentTypes(Set<Integer> allowedTypeIds) {
        IdTitleTreeItem contractPaymentTypes = new IdTitleTreeItem();
        contractPaymentTypes.setTitle("Все типы");

        if (dbInfo.versionCompare("6.1") >= 0) {
            RequestJsonRpc req = new RequestJsonRpc("ru.bitel.bgbilling.kernel.contract.balance", "PaymentService",
                    "paymentTypeTree");
            req.setParam("editable", 0);
            req.setParam("showEmptyRoot", false);

            JsonNode result = transferData.postDataReturn(req, user);
            // {"status":"ok","message":"","data":{"return":{"id":0,"title":"Типы","type":1,"editable":true,"parentId":null,"children":[{"id":33,"title":"OptimaPlus","type":0,"editable":true,"parentId":0,"children":null,...
            contractPaymentTypes = jsonMapper.convertValue(result, IdTitleTreeItem.class);
        } else {
            Request request = new Request();
            request.setModule("contract");
            request.setAction("ContractPayment");
            request.setAttribute("id", "new");

            Document document = transferData.postData(request, user);

            Element dataElement = document.getDocumentElement();
            NodeList nodeList = dataElement.getElementsByTagName("item");

            IdTitleTreeItem typeClass = new IdTitleTreeItem();

            for (int index = 0; index < nodeList.getLength(); index++) {
                Element itemElement = (Element) nodeList.item(index);

                int typeId = Utils.parseInt(itemElement.getAttribute("id"));
                if (allowedTypeIds.isEmpty() || allowedTypeIds.contains(typeId)) {

                    if ("1".equals(itemElement.getAttribute("type"))) {
                        if (Utils.notBlankString(typeClass.getTitle()) || !typeClass.getChildren().isEmpty()) {
                            contractPaymentTypes.addChild(typeClass);
                        }
                        typeClass = new IdTitleTreeItem();
                        typeClass.setId(typeId);
                        typeClass.setTitle(itemElement.getAttribute("title"));
                    } else {
                        IdTitleTreeItem type = new IdTitleTreeItem();
                        type.setId(typeId);
                        type.setTitle(itemElement.getAttribute("title"));

                        typeClass.addChild(type);
                    }
                }
            }
            if (Utils.notBlankString(typeClass.getTitle()) || !typeClass.getChildren().isEmpty()) {
                contractPaymentTypes.addChild(typeClass);
            }
        }

        return contractPaymentTypes;
    }

    public List<IdTitle> paymentTypeList() {
        var req = new RequestJsonRpc(BalanceDAO.CONTRACT_BALANCE_MODULE, "PaymentService", "paymentTypeList");
        return readJsonValue(transferData.postDataReturn(req, user).traverse(), jsonTypeFactory.constructCollectionType(List.class, IdTitle.class));
    }

    public IdTitleTreeItem getContractChargeTypes(Set<Integer> allowedTypeIds) {
        IdTitleTreeItem contractChargeTypes = new IdTitleTreeItem();
        contractChargeTypes.setTitle("Все типы");

        if (dbInfo.versionCompare("6.1") >= 0) {
            RequestJsonRpc req = new RequestJsonRpc("ru.bitel.bgbilling.kernel.contract.balance", "ChargeService",
                    "chargeTypeTree");
            req.setParam("editable", 0);
            req.setParam("isPayback", false);
            req.setParam("showEmptyRoot", false);

            JsonNode result = transferData.postDataReturn(req, user);

            // {"status":"ok","message":"","data":{"return":{"id":0,"title":"Типы","type":1,"editable":true,"parentId":null,"children":[{"id":33,"title":"OptimaPlus","type":0,"editable":true,"parentId":0,"children":null,...
            contractChargeTypes = jsonMapper.convertValue(result, IdTitleTreeItem.class);
        } else {
            Request request = new Request();
            request.setModule("contract");
            request.setAction("ContractCharge");
            request.setAttribute("id", "new");

            Document document = transferData.postData(request, user);

            Element dataElement = document.getDocumentElement();
            NodeList nodeList = dataElement.getElementsByTagName("item");

            IdTitleTreeItem typeClass = new IdTitleTreeItem();

            for (int index = 0; index < nodeList.getLength(); index++) {
                Element itemElement = (Element) nodeList.item(index);

                int typeId = Utils.parseInt(itemElement.getAttribute("id"));
                if (allowedTypeIds.isEmpty() || allowedTypeIds.contains(typeId)) {
                    if ("1".equals(itemElement.getAttribute("type"))) {
                        if (Utils.notBlankString(typeClass.getTitle()) || !typeClass.getChildren().isEmpty()) {
                            contractChargeTypes.addChild(typeClass);
                        }
                        typeClass = new IdTitleTreeItem();
                        typeClass.setId(typeId);
                        typeClass.setTitle(itemElement.getAttribute("title"));
                    } else {
                        IdTitleTreeItem type = new IdTitleTreeItem();
                        type.setId(typeId);
                        type.setTitle(itemElement.getAttribute("title"));

                        typeClass.addChild(type);
                    }
                }
            }
            if (Utils.notBlankString(typeClass.getTitle()) || !typeClass.getChildren().isEmpty()) {
                contractChargeTypes.addChild(typeClass);
            }
        }

        return contractChargeTypes;
    }

    public List<IdTitle> getServiceTypeList(int moduleId) {
        if (dbInfo.versionCompare("8.0") > 0) {
            RequestJsonRpc req = new RequestJsonRpc("ru.bitel.bgbilling.kernel.module", moduleId, "ModuleService", "moduleServiceList");
            req.setParam("moduleId", moduleId);
            return readJsonValue(transferData.postDataReturn(req, user).traverse(), jsonTypeFactory.constructCollectionType(List.class, IdTitle.class));
        } else {
            List<IdTitle> list = new ArrayList<>();

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
    }

    public List<IdTitle> getRegistredTariffGroupList(int selectedTariffGroupId) {
        Request request = new Request();
        request.setModule("tariff");
        request.setAction("ListTariffGroups");

        Document document = transferData.postData(request, user);

        Element dataElement = document.getDocumentElement();
        NodeList nodeList = dataElement.getElementsByTagName("item");

        List<IdTitle> registerGroupTariffList = new ArrayList<>();
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

    public List<IdTitle> getBillingModuleList() {
        Request request = new Request();
        request.setModule("service");
        request.setAction("Modules");

        Document document = transferData.postData(request, user);

        Element dataElement = document.getDocumentElement();
        NodeList nodeList = dataElement.getElementsByTagName("module");

        List<IdTitle> moduleList = new ArrayList<>();
        for (int index = 0; index < nodeList.getLength(); index++) {
            Element rowElement = (Element) nodeList.item(index);
            IdTitle module = new IdTitle();
            module.setId(Utils.parseInt(rowElement.getAttribute("id")));
            module.setTitle(rowElement.getAttribute("title"));

            moduleList.add(module);
        }

        return moduleList;
    }

    public List<UserInfo> getUserInfoList() {
        RequestJsonRpc req = new RequestJsonRpc("ru.bitel.bgbilling.kernel.bgsecure", "UserService", "userInfoList");
        JsonNode res = transferData.postDataReturn(req, user);
        List<UserInfo> result = readJsonValue(res.traverse(), jsonTypeFactory.constructCollectionType(List.class, UserInfo.class));
        // пользователь "customer" имеет конфликтующий ID=-1 с пользователем "Пользователь"
        return result.stream().filter(user -> !"customer".equals(user.getName())).toList();
    }

    public long getDirectoryVersion(String directoryItemClass, int moduleId) {
        var req = new RequestJsonRpc("ru.bitel.bgbilling.kernel.directory.api", "DirectoryService", "getVersion");
        req.setParam("directoryItemClass", directoryItemClass);
        req.setParam("moduleId", moduleId);
        return transferData.postDataReturn(req, user).longValue();
    }
}
