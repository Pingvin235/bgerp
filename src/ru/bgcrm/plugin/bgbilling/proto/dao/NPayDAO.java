package ru.bgcrm.plugin.bgbilling.proto.dao;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import ru.bgcrm.model.user.User;
import ru.bgcrm.plugin.bgbilling.DBInfo;
import ru.bgcrm.plugin.bgbilling.Request;
import ru.bgcrm.plugin.bgbilling.RequestJsonRpc;
import ru.bgcrm.plugin.bgbilling.proto.model.npay.NPayService;
import ru.bgcrm.util.TimeUtils;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.XMLUtils;

public class NPayDAO extends BillingModuleDAO {
    private static final String NPAY_MODULE_ID = "npay";

    public NPayDAO(User user, String billingId, int moduleId) {
        super(user, billingId, moduleId);
    }

    public NPayDAO(User user, DBInfo dbInfo, int moduleId) {
        super(user, dbInfo.getId(), moduleId);
    }

    /**
     * Возвращает список абонплат договора.
     * @param contractId
     * @return
     */
    public List<NPayService> getServiceList(int contractId) {
        if (dbInfo.versionCompare("9.2") >= 0) {
            RequestJsonRpc req = new RequestJsonRpc("ru.bitel.bgbilling.modules.npay", moduleId, "NPayService", "serviceObjectList");
            req.setParamContractId(contractId);
            req.setParam("objectId", 0);
            req.setParam("entityModuleId", -1);
            req.setParam("entityId", -1);
            req.setParam("actualItemsOnly", true);
            req.setParam("actualItemsDate", new Date());
            return readJsonValue(transferData.postDataReturn(req, user).traverse(),
                    jsonTypeFactory.constructCollectionType(List.class, NPayService.class));
        } else {
            Request req = new Request();
            req.setModule(NPAY_MODULE_ID);
            req.setAction("ServiceObjectTable");
            req.setModuleID(moduleId);
            req.setContractId(contractId);
            req.setAttribute("object_id", 0);

            Document document = transferData.postData(req, user);

            List<NPayService> serviceList = new ArrayList<>();

            if (document != null) {
                Element dataElement = document.getDocumentElement();
                NodeList nodeList = dataElement.getElementsByTagName("row");

                for (int index = 0; index < nodeList.getLength(); index++) {
                    NPayService service = new NPayService();
                    Element rowElement = (Element) nodeList.item(index);

                    service.setComment(rowElement.getAttribute("comment"));
                    service.setDateFrom(TimeUtils.parse(rowElement.getAttribute("date1"), TimeUtils.PATTERN_DDMMYYYY));
                    service.setDateTo(TimeUtils.parse(rowElement.getAttribute("date2"), TimeUtils.PATTERN_DDMMYYYY));
                    service.setId(Utils.parseInt(rowElement.getAttribute("id")));
                    service.setObjectId(Utils.parseInt(rowElement.getAttribute("objectId")));
                    service.setObjectTitle(rowElement.getAttribute("object"));
                    service.setServiceTitle(rowElement.getAttribute("service"));
                    service.setServiceId(Utils.parseInt(rowElement.getAttribute("sid")));
                    service.setCount(Utils.parseInt(rowElement.getAttribute("col")));
                    service.setContractId(contractId);

                    serviceList.add(service);
                }
            }
            return serviceList;
        }
    }

    /**
     * Возвращает абонплату договора по коду записи.
     * @param id
     * @return
     */
    public NPayService getService(int id) {
        if (dbInfo.versionCompare("8.0") > 0) {
            RequestJsonRpc req = new RequestJsonRpc("ru.bitel.bgbilling.modules.npay", moduleId, "NPayService", "serviceObjectGet");
            req.setParam("id", id);
            return jsonMapper.convertValue(transferData.postDataReturn(req, user), NPayService.class);
        } else {
            NPayService result = null;

            Request req = new Request();
            req.setModule(NPAY_MODULE_ID);
            req.setAction("ServiceObjectGet");
            req.setModuleID(moduleId);
            req.setAttribute("id", id);

            Document doc = transferData.postData(req, user);
            Element serviceEl = XMLUtils.selectElement(doc, "/data/object");
            if (serviceEl != null) {
                result = new NPayService();
                result.setId(id);
                result.setServiceId(Utils.parseInt(serviceEl.getAttribute("sid")));
                result.setObjectId(Utils.parseInt(serviceEl.getAttribute("oid")));
                result.setCount(Utils.parseInt(serviceEl.getAttribute("col")));
                result.setDateFrom(TimeUtils.parse(serviceEl.getAttribute("date1"), TimeUtils.PATTERN_DDMMYYYY));
                result.setDateTo(TimeUtils.parse(serviceEl.getAttribute("date2"), TimeUtils.PATTERN_DDMMYYYY));
                result.setComment(linesToString(XMLUtils.selectElement(doc, "/data/comment/")));
            }

            return result;
        }
    }

    /**
     * Изменяет либо добавляет абонплату договора.
     * @param service
     */
    public void updateService(NPayService service) {
        if (dbInfo.versionCompare("9.2") >= 0) {
            RequestJsonRpc req = new RequestJsonRpc("ru.bitel.bgbilling.modules.npay", moduleId, "NPayService", "serviceObjectUpdate");
            req.setParam("serviceObject", service);
            transferData.postData(req, user);
        } else
            updateService(service.getId(), service.getContractId(), service.getServiceId(), service.getDateFrom(),
                    service.getDateTo(), service.getObjectId(), service.getCount(), service.getComment());
    }

    /**
     * Изменяет либо добавляет абонплату договора.
     * @param id 0 - добавление, иначе - изменение
     * @param contractId
     * @param serviceId
     * @param dateFrom
     * @param dateTo
     * @param objectId
     * @param count
     * @param comment
     */
    public void updateService(int id, int contractId, int serviceId, Date dateFrom, Date dateTo, int objectId, int count, String comment)
            {
        Request req = new Request();
        req.setModule(NPAY_MODULE_ID);
        req.setAction("ServiceObjectUpdate");
        req.setModuleID(String.valueOf(moduleId));
        req.setContractId(contractId);
        req.setAttribute("object_id", 0);
        req.setAttribute("id", id <= 0 ? "new" : id);
        req.setAttribute("sid", serviceId);
        req.setAttribute("oid", objectId);
        req.setAttribute("col", count);
        req.setAttribute("comment", comment);
        if (dateFrom != null) {
            req.setAttribute("date1", TimeUtils.format(dateFrom, TimeUtils.PATTERN_DDMMYYYY));
        }
        if (dateTo != null) {
            req.setAttribute("date2", TimeUtils.format(dateTo, TimeUtils.PATTERN_DDMMYYYY));
        }
        transferData.postData(req, user);
    }

    /**
     * Удаляет абонплату договора.
     * @param contractId
     * @param id
     */
    public void deleteService( int id) {
        if (dbInfo.versionCompare("9.2") >= 0) {
            RequestJsonRpc req = new RequestJsonRpc("ru.bitel.bgbilling.modules.npay", moduleId, "NPayService", "serviceObjectDelete");
            req.setParam("id", id);
            transferData.postData(req, user);
        } else {
            Request req = new Request();
            req.setModule(NPAY_MODULE_ID);
            req.setAction("ServiceObjectDelete");
            req.setModuleID(moduleId);
            req.setAttribute("id", id);

            transferData.postData(req, user);
        }
    }
}