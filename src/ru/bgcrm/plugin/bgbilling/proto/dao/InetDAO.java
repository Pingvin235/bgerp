package ru.bgcrm.plugin.bgbilling.proto.dao;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.bgerp.app.exception.BGException;
import org.bgerp.model.Pageable;
import org.bgerp.util.Log;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import ru.bgcrm.model.Page;
import ru.bgcrm.model.user.User;
import ru.bgcrm.plugin.bgbilling.RequestJsonRpc;
import ru.bgcrm.plugin.bgbilling.proto.model.inet.InetDevice;
import ru.bgcrm.plugin.bgbilling.proto.model.inet.InetDeviceManagerMethod;
import ru.bgcrm.plugin.bgbilling.proto.model.inet.InetDeviceManagerMethod.DeviceManagerMethodType;
import ru.bgcrm.plugin.bgbilling.proto.model.inet.InetService;
import ru.bgcrm.plugin.bgbilling.proto.model.inet.InetServiceOption;
import ru.bgcrm.plugin.bgbilling.proto.model.inet.InetServiceType;
import ru.bgcrm.plugin.bgbilling.proto.model.inet.InetSessionLog;
import ru.bgcrm.plugin.bgbilling.proto.model.inet.version.v8x.InetServiceType8x;

public class InetDAO extends BillingModuleDAO {
    private static final Log log = Log.getLog();

    private final String inetModule;

    public InetDAO(User user, String billingId, int moduleId) {
        super(user, billingId, moduleId);
        inetModule = inetModule();
    }

    @Deprecated
    public static InetDAO getInstance(User user, String billingId, int moduleId) {
        log.warnd(Log.MSG_DEPRECATED_METHOD_WAS_CALLED + Log.MSG_WS_CREATE_NEW_INSTANCE_INSTEAD, "getInstance");
        return new InetDAO(user, billingId, moduleId);
    }

    private String inetModule() {
        return dbInfo.versionCompare("8.0") > 0 ? "ru.bitel.bgbilling.modules.inet" : "ru.bitel.bgbilling.modules.inet.api";
    }

    /**
     * Возвращает перечень сервисов в виде плоского списка.
     * Сборка в дерево, если необходимо, осуществляется на основании кодов сервисов-предков.
     *
     * @param contractId
     * @return
     */
    public List<InetService> getServiceList(int contractId) {
        List<InetService> result = new ArrayList<>();

        RequestJsonRpc req = new RequestJsonRpc(inetModule, moduleId, "InetServService", "inetServTree");
        req.setParamContractId(contractId);

        JsonNode jsonNode = transferData.postDataReturn(req, user);
        loadChildren(jsonNode, result);

        return result;
    }

    private void loadChildren(JsonNode node, List<InetService> list) {
        for (JsonNode childNode : node.path("children")) {
            correctStatus(childNode);;
            list.add(jsonMapper.convertValue(childNode, InetService.class));
            loadChildren(childNode, list);
        }
    }

    public InetService getService(int id) {
        RequestJsonRpc req = new RequestJsonRpc(inetModule, moduleId, "InetServService", "inetServGet");
        req.setParam("inetServId", id);

        JsonNode response = transferData.postDataReturn(req, user);
        correctStatus(response);
        return jsonMapper.convertValue(response, InetService.class);
    }

    private void correctStatus(JsonNode childNode) {
        switch (childNode.get("status").asText()) {
            case "STATUS_ON" -> ((ObjectNode) childNode).put("status", InetService.STATUS_ACTIVE);
            case "STATUS_OFF" -> ((ObjectNode) childNode).put("status", InetService.STATUS_CLOSED);
        }
    }

    public void updateService(InetService inetServ, List<InetServiceOption> optionList, boolean generateLogin,
            boolean generatePassword, long saWaitTimeout) {
        if (optionList == null)
            optionList = Collections.emptyList();

        RequestJsonRpc req = new RequestJsonRpc(inetModule, moduleId, "InetServService", "inetServUpdate");
        req.setParam("inetServ", inetServ);
        req.setParam("optionList", optionList);
        req.setParam("generateLogin", generateLogin);
        req.setParam("generatePassword", generatePassword);
        req.setParam("saWaitTimeout", saWaitTimeout);

        transferData.postData(req, user);
    }

    public void deleteService(int id) {
        RequestJsonRpc req = new RequestJsonRpc(inetModule, moduleId, "InetServService", "inetServDelete");
        req.setParam("id", id);
        req.setParam("force", false);
        transferData.postData(req, user);
    }

    public void updateServiceState(int serviceId, int state) {
        RequestJsonRpc req = new RequestJsonRpc(inetModule, moduleId, "InetServService", "inetServStateModify");
        req.setParam("inetServId", serviceId);
        req.setParam("deviceState", state);
        req.setParam("accessCode", -2);

        transferData.postData(req, user);
    }

    public List<InetServiceType> getServiceTypeList() {
        RequestJsonRpc req = new RequestJsonRpc(inetModule, moduleId, "InetServService", "inetServTypeList");

        return readJsonValue(transferData.postDataReturn(req, user).traverse(), jsonTypeFactory.constructCollectionType(List.class,
                dbInfo.versionCompare("8.0") > 0 ? InetServiceType8x.class : InetServiceType.class));
    }

    public InetDevice getDevice(int deviceId) {
        RequestJsonRpc req = new RequestJsonRpc(inetModule, moduleId, "InetDeviceService", "inetDeviceGet");
        req.setParam("id", deviceId);

        JsonNode fromValue = transferData.postDataReturn(req, user);
        return jsonMapper.convertValue(fromValue, InetDevice.class);
    }

    public InetDevice getRootDevice(Set<Integer> deviceTypeIds, Set<Integer> deviceGroupIds) {
        RequestJsonRpc req = new RequestJsonRpc(inetModule, moduleId, "InetDeviceService", "inetDeviceRoot");

        if (dbInfo.versionCompare("9.2") >= 0) {
            req.setParam("deviceTreeFilter", Map.of("host", "", "identifier", "", "loadDeviceGroupLink", "false"));
        } else {
            req.setParam("identifier", null);
            req.setParam("host", null);
            req.setParam("deviceTypeIds", deviceTypeIds);
            req.setParam("deviceGroupIds", deviceGroupIds);
            req.setParam("dateFrom", null);
            req.setParam("dateTo", null);
            req.setParam("intersectDateFrom", null);
            req.setParam("intersectDateTo", null);
            req.setParam("entityFilter", null);
            req.setParam("loadDeviceGroupIds", false);
        }
        req.setParam("loadAncestors", true);

        JsonNode fromValue = transferData.postDataReturn(req, user);
        return jsonMapper.convertValue(fromValue, InetDevice.class);
    }

    public List<InetDeviceManagerMethod> getDeviceManagerMethodList(int deviceTypeId) {
        RequestJsonRpc req = new RequestJsonRpc(inetModule, moduleId, "InetDeviceService", "deviceManagerMethodList");
        req.setParam("deviceTypeId", deviceTypeId);

        List<InetDeviceManagerMethod> methodList = readJsonValue(transferData.postDataReturn(req, user).traverse(),
                jsonTypeFactory.constructCollectionType(List.class, InetDeviceManagerMethod.class));
        methodList = methodList.stream().filter(m -> m.getTypes().contains(DeviceManagerMethodType.ACCOUNT)).collect(Collectors.toList());

        return methodList;
    }

    public String deviceManage(int contractId, int deviceId, int serviceId, int connectionId, String operation) {
        RequestJsonRpc req = new RequestJsonRpc(inetModule, moduleId, "InetDeviceService", "deviceManage");
        req.setParamContractId(contractId);
        req.setParam("id", deviceId);
        req.setParam("inetDeviceId", deviceId);
        req.setParam("servId", serviceId);
        req.setParam("operation", operation);
        req.setParam("connectionId", connectionId);
        req.setParam("timeout", 180000);

        return jsonMapper.convertValue(transferData.postDataReturn(req, user), String.class);
    }

    public void getSessionAliveContractList(Pageable<InetSessionLog> result, int contractId) {
        RequestJsonRpc req = new RequestJsonRpc(inetModule, moduleId, "InetSessionService", "inetSessionAliveContractList");
        req.setParamContractId(contractId);
        req.setParam("trafficTypeIds", new int[] { 0 });
        req.setParam("serviceIds", Collections.emptyList());
        req.setParam("page", result.getPage());
        req.setParam("servIds", Collections.emptyList());

        extractSessions(result, req);
        setDeviceTitles(result);
    }

    public void getSessionLogContractList(Pageable<InetSessionLog> result, int contractId, Date dateFrom, Date dateTo) {
        RequestJsonRpc req = new RequestJsonRpc(inetModule, moduleId, "InetSessionService", "inetSessionLogContractList");
        req.setParamContractId(contractId);
        req.setParam("dateFrom", dateFrom);
        req.setParam("dateTo", dateTo);
        req.setParam("trafficTypeIds", new int[] { 0 });
        req.setParam("servIds", Collections.emptyList());
        req.setParam("page", result.getPage());

        extractSessions(result, req);
        setDeviceTitles(result);
    }

    private void extractSessions(Pageable<InetSessionLog> result, RequestJsonRpc req) {
        JsonNode ret = transferData.postDataReturn(req, user);
        List<InetSessionLog> sessionList = readJsonValue(ret.findValue("list").traverse(),
                jsonTypeFactory.constructCollectionType(List.class, InetSessionLog.class));

        result.getList().addAll(sessionList);
        result.getPage().setData(jsonMapper.convertValue(ret.findValue("page"), Page.class));
    }


    private void setDeviceTitles(Pageable<InetSessionLog> pageable) {
        Map<Integer, InetDevice> deviceMap = new TreeMap<>();
        for (var item : pageable.getList()) {
            var device = deviceMap.computeIfAbsent(item.getDeviceId(), id -> {
                try {
                    return getDevice(id);
                } catch (BGException e) {
                    log.error(e);
                    return null;
                }
            });
            item.setDeviceTitle(device.getTitle());
        }
    }

    public Set<Integer> vlanResourceCategoryIds(int deviceId) {
        RequestJsonRpc req = new RequestJsonRpc(inetModule, moduleId, "InetServService", "vlanResourceCategoryIds");
        req.setParam("deviceId", deviceId);

        Set<Integer> methodList = readJsonValue(transferData.postDataReturn(req, user).traverse(),
                jsonTypeFactory.constructCollectionType(Set.class, Integer.class));
        return methodList;
    }

    public void connectionClose(int contractId, long connectionId) throws Exception {
        RequestJsonRpc req = new RequestJsonRpc(inetModule, moduleId, "InetSessionService", "connectionClose");
        req.setParam("contractId", contractId);
        req.setParam("connectionId", connectionId);
        transferData.postData(req, user);
    }

    public void connectionFinish(int contractId, long connectionId) throws Exception {
        RequestJsonRpc req = new RequestJsonRpc(inetModule, moduleId, "InetSessionService", "connectionFinish");
        req.setParam("contractId", contractId);
        req.setParam("connectionId", connectionId);
        transferData.postData(req, user);
    }
}