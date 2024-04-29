package ru.bgcrm.plugin.bgbilling.proto.dao;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.JsonNode;

import org.bgerp.app.exception.BGException;
import org.bgerp.model.base.Id;
import org.bgerp.model.base.IdTitle;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import ru.bgcrm.model.user.User;
import ru.bgcrm.plugin.bgbilling.DBInfo;
import ru.bgcrm.plugin.bgbilling.Request;
import ru.bgcrm.plugin.bgbilling.RequestJsonRpc;
import ru.bgcrm.plugin.bgbilling.dao.BillingDAO;
import ru.bgcrm.plugin.bgbilling.proto.model.ContractObject;
import ru.bgcrm.plugin.bgbilling.proto.model.ContractObjectModuleInfo;
import ru.bgcrm.plugin.bgbilling.proto.model.ContractObjectModuleInfo.ContractObjectModule;
import ru.bgcrm.plugin.bgbilling.proto.model.ContractObjectModuleInfo.ContractObjectModuleData;

import ru.bgcrm.util.TimeUtils;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.XMLUtils;

public class ContractObjectDAO extends BillingDAO {
    public ContractObjectDAO(User user, String billingId) {
        super(user, billingId);
    }

    public ContractObjectDAO(User user, DBInfo dbInfo) {
        super(user, dbInfo);
    }

    public List<ContractObject> getContractObjects(int contractId) {
        List<ContractObject> objects = new ArrayList<>();
        if (dbInfo.versionCompare("9.2") >= 0) {
            RequestJsonRpc req = new RequestJsonRpc("ru.bitel.bgbilling.kernel.contract.object", "ContractObjectService", "contractObjectList");
            req.setParam("contractId", contractId);
            JsonNode ret = transferData.postDataReturn(req, user);
            objects = readJsonValue(ret.traverse(), jsonTypeFactory.constructCollectionType(List.class, ContractObject.class));
            Map<Integer, String> types = getContractObjectsTypes();
            objects.forEach(o -> o.setType(types.get(o.getTypeId())));
        } else {
            Request request = new Request();
            request.setModule("contract.object");
            request.setAction("ObjectTable");
            request.setContractId(contractId);

            Document doc = transferData.postData(request, user);

            for (Element e : XMLUtils.selectElements(doc, "/data/table/data/row")) {
                ContractObject object = new ContractObject();
                object.setId(Utils.parseInt(e.getAttribute("id")));
                object.setTitle(Utils.maskNull(e.getAttribute("title")));
                object.setTypeId(Utils.parseInt(e.getAttribute("type_id")));
                object.setType(Utils.maskNull(e.getAttribute("type")));
                object.setPeriod(Utils.maskNull(e.getAttribute("period")));
                objects.add(object);
            }
        }

        return objects;
    }

    public ContractObject getContractObject(int contractId, int objectId) {
        if (dbInfo.versionCompare("9.2") >= 0) {
            RequestJsonRpc req = new RequestJsonRpc("ru.bitel.bgbilling.kernel.contract.object", "ContractObjectService", "contractObjectGetById");
            req.setParam("contractId", contractId);
            req.setParam("objectId", objectId);
            JsonNode ret = transferData.postDataReturn(req, user);
            ContractObject contractObject = jsonMapper.convertValue(ret, ContractObject.class);
            Map<Integer, String> types = getContractObjectsTypes();
            contractObject.setType(types.get(contractObject.getTypeId()));
            return contractObject;
        } else {
            Request request = new Request();
            request.setModule("contract.object");
            request.setAction("ObjectGet");
            request.setAttribute("id", objectId);

            Document document = transferData.postData(request, user);

            Element dataElement = document.getDocumentElement();
            NodeList nodeList = dataElement.getElementsByTagName("object");

            ContractObject object = new ContractObject();
            if (nodeList.getLength() > 0) {
                Element rowElement = (Element) nodeList.item(0);

                object.setId(objectId);

                object.setTitle(rowElement.getAttribute("title"));
                object.setTypeId(Utils.parseInt(rowElement.getAttribute("type_id")));

                try {
                    SimpleDateFormat dateFormatter = new SimpleDateFormat(TimeUtils.PATTERN_DDMMYYYY);

                    String dateFrom = rowElement.getAttribute("date1");
                    if (Utils.notBlankString(dateFrom))
                        object.setDateFrom(dateFormatter.parse(dateFrom));

                    String dateTo = rowElement.getAttribute("date2");
                    if (Utils.notBlankString(dateTo))
                        object.setDateTo(dateFormatter.parse(dateTo));
                } catch (ParseException e) {
                    throw new BGException(e);
                }
            }

            return object;
        }
    }

    public ContractObjectModuleInfo contractObjectModuleList(int objectId) {
        Request request = new Request();
        request.setModule("contract.object");
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

    public void deleteContractObject(int contractId, int objectId) {
        if (dbInfo.versionCompare("9.2") >= 0) {
            RequestJsonRpc req = new RequestJsonRpc("ru.bitel.bgbilling.kernel.contract.object", "ContractObjectService", "contractObjectDelete");
            req.setParam("objectId", objectId);
            transferData.postDataReturn(req, user);
        } else {
            Request request = new Request();
            request.setModule("contract.object");
            request.setAction("ObjectDelete");
            request.setAttribute("id", objectId);
            request.setContractId(contractId);

            transferData.postData(request, user);
        }
    }

    public int updateContractObject(int contractId, int objectId, int typeId, String title, Date dateFrom, Date dateTo) {
        if (dbInfo.versionCompare("9.2") >= 0) {
            ContractObject contractObject = new ContractObject();
            contractObject.setContractId(contractId);
            contractObject.setId(objectId);
            contractObject.setTypeId(typeId);
            contractObject.setTitle(title);
            contractObject.setDateFrom(dateFrom);
            contractObject.setDateTo(dateTo);

            RequestJsonRpc req = new RequestJsonRpc("ru.bitel.bgbilling.kernel.contract.object", "ContractObjectService", "contractObjectUpdate");
            req.setParamContractId(contractId);
            req.setParam("contractObject", contractObject);

            return transferData.postDataReturn(req, user).asInt();
        } else {
            SimpleDateFormat dateFormatter = new SimpleDateFormat(TimeUtils.PATTERN_DDMMYYYY);
            Request request = new Request();
            request.setModule("contract.object");
            request.setAction("ObjectUpdate");
            request.setAttribute("id", objectId);
            request.setAttribute("title", Utils.maskNull(title));

            if (dateFrom != null) {
                request.setAttribute("date1", dateFormatter.format(dateFrom));
            }

            if (dateTo != null) {
                request.setAttribute("date2", dateFormatter.format(dateTo));
            }

            if (contractId > 0) {
                request.setAttribute("cid", contractId);
            }

            return Utils.parseInt(XMLUtils.getElement(transferData.postData(request, user), "data").getAttribute("id"));
        }
    }

    public Map<Integer, String> getContractObjectsTypes() {
        if (dbInfo.versionCompare("8.2") >= 0) {
            RequestJsonRpc req = new RequestJsonRpc("ru.bitel.bgbilling.kernel.contract.object",
                dbInfo.versionCompare("9.2") >= 0 ? "ContractObjectService" : "ObjectTypeService",
                "objectTypeList");
            req.setParam("onlyVisible", false);
            JsonNode ret = transferData.postDataReturn(req, user);
            List<IdTitle> list = readJsonValue(ret.traverse(), jsonTypeFactory.constructCollectionType(List.class, IdTitle.class));
            return list.stream().collect(Collectors.toMap(Id::getId, IdTitle::getTitle));
        }
        return Collections.emptyMap();
    }
}
