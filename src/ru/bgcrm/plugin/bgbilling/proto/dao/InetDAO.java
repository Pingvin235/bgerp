package ru.bgcrm.plugin.bgbilling.proto.dao;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.JsonNode;

import ru.bgcrm.model.BGException;
import ru.bgcrm.model.Page;
import ru.bgcrm.model.SearchResult;
import ru.bgcrm.model.user.User;
import ru.bgcrm.plugin.bgbilling.DBInfo;
import ru.bgcrm.plugin.bgbilling.RequestJsonRpc;
import ru.bgcrm.plugin.bgbilling.proto.model.inet.InetDevice;
import ru.bgcrm.plugin.bgbilling.proto.model.inet.InetDeviceManagerMethod;
import ru.bgcrm.plugin.bgbilling.proto.model.inet.InetDeviceManagerMethod.DeviceManagerMethodType;
import ru.bgcrm.plugin.bgbilling.proto.model.inet.InetService;
import ru.bgcrm.plugin.bgbilling.proto.model.inet.InetServiceOption;
import ru.bgcrm.plugin.bgbilling.proto.model.inet.InetServiceType;
import ru.bgcrm.plugin.bgbilling.proto.model.inet.InetSessionLog;

public class InetDAO extends BillingModuleDAO {
	private static final String INET_MODULE_ID = "ru.bitel.bgbilling.modules.inet.api";

	public InetDAO(User user, DBInfo dbInfo, int moduleId) throws BGException {
		super(user, dbInfo, moduleId);
	}

	public InetDAO(User user, String billingId, int moduleId) throws BGException {
		super(user, billingId, moduleId);
	}

	/**
	 * Возвращает перечень сервисов в виде плоского списка.
	 * Сборка в дерево, если необходимо, осуществляется на основании кодов сервисов-предков.
	 * 
	 * @param contractId
	 * @return
	 * @throws BGException
	 */
	public List<InetService> getServiceList(int contractId) throws BGException {
		List<InetService> result = new ArrayList<InetService>();

		RequestJsonRpc req = new RequestJsonRpc(INET_MODULE_ID, moduleId, "InetServService", "inetServTree");
		req.setParamContractId(contractId);

		JsonNode jsonNode = transferData.postDataReturn(req, user);
		loadChildren(jsonNode, result);

		return result;
	}

	private void loadChildren(JsonNode node, List<InetService> list) {
		for (JsonNode childNode : node.path("children")) {
			list.add(jsonMapper.convertValue(childNode, InetService.class));
			loadChildren(childNode, list);
		}
	}

	public InetService getService(int id) throws BGException {
		RequestJsonRpc req = new RequestJsonRpc(INET_MODULE_ID, moduleId, "InetServService", "inetServGet");
		req.setParam("inetServId", id);

		JsonNode response = transferData.postDataReturn(req, user);
		return jsonMapper.convertValue(response, InetService.class);
	}

	public void updateService(InetService inetServ, List<InetServiceOption> optionList, boolean generateLogin,
			boolean generatePassword, long saWaitTimeout) throws BGException {
	    if (optionList == null)
	        optionList = Collections.emptyList();
	    
	    RequestJsonRpc req = new RequestJsonRpc(INET_MODULE_ID, moduleId, "InetServService", "inetServUpdate");
		req.setParam("inetServ", inetServ);
		req.setParam("optionList", optionList);
		req.setParam("generateLogin", generateLogin);
		req.setParam("generatePassword", generatePassword);
		req.setParam("saWaitTimeout", saWaitTimeout);

		transferData.postData(req, user);
	}

	public void deleteService(int id) throws BGException {
		RequestJsonRpc req = new RequestJsonRpc(INET_MODULE_ID, moduleId, "InetServService", "inetServDelete");
		req.setParam("id", id);

		transferData.postData(req, user);
	}
	
	public void updateServiceState(int serviceId, int state) throws BGException {
		RequestJsonRpc req = new RequestJsonRpc(INET_MODULE_ID, moduleId, "InetServService", "inetServStateModify");
		req.setParam("id", "inetServId");
		req.setParam("deviceState", state);
		req.setParam("accessCode", -2);

		transferData.postData(req, user);
	}

	public List<InetServiceType> getServiceTypeList() throws BGException {
		RequestJsonRpc req = new RequestJsonRpc(INET_MODULE_ID, moduleId, "InetServService", "inetServTypeList");

		return readJsonValue(transferData.postDataReturn(req, user).traverse(),
				jsonTypeFactory.constructCollectionType(List.class, InetServiceType.class));
	}
	
	public InetDevice getDevice(int deviceId) throws BGException {
		RequestJsonRpc req = new RequestJsonRpc(INET_MODULE_ID, moduleId, "InetDeviceService", "inetDeviceGet");
		req.setParam("id", deviceId);
		
		return jsonMapper.convertValue(transferData.postDataReturn(req, user), InetDevice.class);
	}
	
	public List<InetDeviceManagerMethod> getDeviceManagerMethodList(int deviceTypeId) throws BGException {
		RequestJsonRpc req = new RequestJsonRpc(INET_MODULE_ID, moduleId, "InetDeviceService", "deviceManagerMethodList");
		req.setParam("deviceTypeId", deviceTypeId);
		
		List<InetDeviceManagerMethod> methodList = readJsonValue(transferData.postDataReturn(req, user).traverse(),
				jsonTypeFactory.constructCollectionType(List.class, InetDeviceManagerMethod.class));
		methodList = methodList.stream().filter(m -> m.getTypes().contains(DeviceManagerMethodType.ACCOUNT)).collect(Collectors.toList());
				
		return methodList;		
	}
	
	public String deviceManage(int deviceId, int serviceId, int connectionId, String operation) throws BGException {
		RequestJsonRpc req = new RequestJsonRpc(INET_MODULE_ID, moduleId, "InetDeviceService", "deviceManage");
		req.setParam("id", deviceId);
		req.setParam("servId", serviceId);
		req.setParam("operation", operation);
		req.setParam("connectionId", connectionId);
		req.setParam("timeout", 180000);
		
		return jsonMapper.convertValue(transferData.postDataReturn(req, user), String.class);		
	}
	
	@Deprecated
	public void getContractSessionAlive(SearchResult<InetSessionLog> result, int contractId) throws BGException {
		getSessionAliveContractList(result, contractId);
	}

	public void getSessionAliveContractList(SearchResult<InetSessionLog> result, int contractId) throws BGException {
		RequestJsonRpc req = new RequestJsonRpc(INET_MODULE_ID, moduleId, "InetSessionService",
				"inetSessionAliveContractList");
		req.setParamContractId(contractId);
		req.setParam("trafficTypeIds", new int[] { 0 });
		req.setParam("serviceIds", Collections.emptyList());
		req.setParam("page", result.getPage());
		req.setParam("servIds", Collections.emptyList());

		extractSessions(result, req);
	}
	
	@Deprecated
	public void getContractSessionLog(SearchResult<InetSessionLog> result, int contractId, Date dateFrom, Date dateTo) throws BGException {
		getSessionLogContractList(result, contractId, dateFrom, dateTo);
	}
	
	public void getSessionLogContractList(SearchResult<InetSessionLog> result, int contractId, Date dateFrom, Date dateTo)
			throws BGException {
		RequestJsonRpc req = new RequestJsonRpc(INET_MODULE_ID, moduleId, "InetSessionService",
				"inetSessionLogContractList");
		req.setParamContractId(contractId);
		req.setParam("dateFrom", dateFrom);
		req.setParam("dateTo", dateTo);
		req.setParam("trafficTypeIds", new int[] { 0 });
		req.setParam("servIds", Collections.emptyList());
		req.setParam("page", result.getPage());

		extractSessions(result, req);
	}
	
	public void getSessionAliveList(SearchResult<InetSessionLog> result, 
			Set<Integer> deviceIds, Set<Integer> contractIds, 
			String contract, String login, String ip, String callingStation, 
			Date timeFrom, Date timeTo) throws BGException {
		RequestJsonRpc req = new RequestJsonRpc(INET_MODULE_ID, moduleId, "InetSessionService",
				"inetSessionAliveList");
		req.setParam("deviceIds", deviceIds);
		req.setParam("contractIds", contractIds);
		req.setParam("contract", contract);
		req.setParam("login", login);
		req.setParam("ip", ip);
		req.setParam("callingStation", callingStation);
		req.setParam("timeFrom", timeFrom);
		req.setParam("timeTo", timeTo);
		req.setParam("page", result.getPage());
		
		extractSessions(result, req);
	}

	private void extractSessions(SearchResult<InetSessionLog> result, RequestJsonRpc req) throws BGException {
		JsonNode ret = transferData.postDataReturn(req, user);
		List<InetSessionLog> sessionList = readJsonValue(ret.findValue("list").traverse(),
				jsonTypeFactory.constructCollectionType(List.class, InetSessionLog.class));

		result.getList().addAll(sessionList);
		result.getPage().setData(jsonMapper.convertValue(ret.findValue("page"), Page.class));
	}
}