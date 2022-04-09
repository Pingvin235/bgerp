package ru.bgcrm.plugin.bgbilling.proto.dao;

import com.fasterxml.jackson.databind.JsonNode;

import org.bgerp.model.Pageable;

import ru.bgcrm.model.BGException;
import ru.bgcrm.model.Page;
import ru.bgcrm.model.user.User;
import ru.bgcrm.plugin.bgbilling.DBInfo;
import ru.bgcrm.plugin.bgbilling.RequestJsonRpc;
import ru.bgcrm.plugin.bgbilling.dao.BillingDAO;
import ru.bgcrm.plugin.bgbilling.proto.dao.version.v8x.InetDAO8x;
import ru.bgcrm.plugin.bgbilling.proto.model.inet.*;
import ru.bgcrm.plugin.bgbilling.proto.model.inet.InetDeviceManagerMethod.DeviceManagerMethodType;

import java.util.*;
import java.util.stream.Collectors;

public class InetDAO extends BillingModuleDAO {
	private static final String INET_MODULE_ID_IMPL = "ru.bitel.bgbilling.modules.inet.api";
	private static final String INET_SERV_SERVICE = "InetServService";
	private static final String INET_DEVICE_SERVICE = "InetDeviceService";
	private static final String INET_SESSION_SERVICE = "InetSessionService";
	protected String INET_MODULE_ID ;


	protected InetDAO(User user, DBInfo dbInfo, int moduleId) throws BGException {
		super(user, dbInfo, moduleId);
		INET_MODULE_ID = INET_MODULE_ID_IMPL;
	}

	protected InetDAO(User user, String billingId, int moduleId) throws BGException {
		super(user, billingId, moduleId);
		INET_MODULE_ID = INET_MODULE_ID_IMPL;

	}

	public static InetDAO getInstance(User user, DBInfo dbInfo, int moduleId) throws BGException {
		if (dbInfo.getVersion().compareTo("8.0") > 0) {
			return new InetDAO8x(user, dbInfo, moduleId);
		} else {
			return new InetDAO(user, dbInfo, moduleId);
		}
	}

	public static InetDAO getInstance(User user, String billingId, int moduleId) throws BGException {
		if (BillingDAO.getVersion(user, billingId).compareTo("8.0") > 0) {
			return new InetDAO8x(user, billingId, moduleId);
		} else {
			return new InetDAO(user, billingId, moduleId);
		}
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

		RequestJsonRpc req = new RequestJsonRpc(INET_MODULE_ID, moduleId, INET_SERV_SERVICE, "inetServTree");
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
		RequestJsonRpc req = new RequestJsonRpc(INET_MODULE_ID, moduleId, INET_SERV_SERVICE, "inetServGet");
		req.setParam("inetServId", id);

		JsonNode response = transferData.postDataReturn(req, user);
		return jsonMapper.convertValue(response, InetService.class);
	}

	public void updateService(InetService inetServ, List<InetServiceOption> optionList, boolean generateLogin,
			boolean generatePassword, long saWaitTimeout) throws BGException {
	    if (optionList == null)
	        optionList = Collections.emptyList();

	    RequestJsonRpc req = new RequestJsonRpc(INET_MODULE_ID, moduleId, INET_SERV_SERVICE, "inetServUpdate");
		req.setParam("inetServ", inetServ);
		req.setParam("optionList", optionList);
		req.setParam("generateLogin", generateLogin);
		req.setParam("generatePassword", generatePassword);
		req.setParam("saWaitTimeout", saWaitTimeout);

		transferData.postData(req, user);
	}

	public void deleteService(int id) throws BGException {
		RequestJsonRpc req = new RequestJsonRpc(INET_MODULE_ID, moduleId, INET_SERV_SERVICE, "inetServDelete");
		req.setParam("id", id);
		req.setParam("force", false);
		transferData.postData(req, user);
	}

	public void updateServiceState(int serviceId, int state) throws BGException {
		RequestJsonRpc req = new RequestJsonRpc(INET_MODULE_ID, moduleId, INET_SERV_SERVICE, "inetServStateModify");
		req.setParam("inetServId", serviceId);
		req.setParam("deviceState", state);
		req.setParam("accessCode", -2);

		transferData.postData(req, user);
	}

	public List<InetServiceType> getServiceTypeList() throws BGException {
		RequestJsonRpc req = new RequestJsonRpc(INET_MODULE_ID, moduleId, INET_SERV_SERVICE, "inetServTypeList");

		return readJsonValue(transferData.postDataReturn(req, user).traverse(),
				jsonTypeFactory.constructCollectionType(List.class, InetServiceType.class));
	}

	public InetDevice getDevice(int deviceId) throws BGException {
		RequestJsonRpc req = new RequestJsonRpc(INET_MODULE_ID, moduleId, INET_DEVICE_SERVICE, "inetDeviceGet");
		req.setParam("id", deviceId);

		return jsonMapper.convertValue(transferData.postDataReturn(req, user), InetDevice.class);
	}

	public List<InetDeviceManagerMethod> getDeviceManagerMethodList(int deviceTypeId) throws BGException {
		RequestJsonRpc req = new RequestJsonRpc(INET_MODULE_ID, moduleId, INET_DEVICE_SERVICE, "deviceManagerMethodList");
		req.setParam("deviceTypeId", deviceTypeId);

		List<InetDeviceManagerMethod> methodList = readJsonValue(transferData.postDataReturn(req, user).traverse(),
				jsonTypeFactory.constructCollectionType(List.class, InetDeviceManagerMethod.class));
		methodList = methodList.stream().filter(m -> m.getTypes().contains(DeviceManagerMethodType.ACCOUNT)).collect(Collectors.toList());

		return methodList;
	}

	public String deviceManage(int deviceId, int serviceId, int connectionId, String operation) throws BGException {
		RequestJsonRpc req = new RequestJsonRpc(INET_MODULE_ID, moduleId, INET_DEVICE_SERVICE, "deviceManage");
		req.setParam("id", deviceId);
		req.setParam("servId", serviceId);
		req.setParam("operation", operation);
		req.setParam("connectionId", connectionId);
		req.setParam("timeout", 180000);

		return jsonMapper.convertValue(transferData.postDataReturn(req, user), String.class);
	}

	@Deprecated
	public void getContractSessionAlive(Pageable<InetSessionLog> result, int contractId) throws BGException {
		getSessionAliveContractList(result, contractId);
	}

	public void getSessionAliveContractList(Pageable<InetSessionLog> result, int contractId) throws BGException {
		RequestJsonRpc req = new RequestJsonRpc(INET_MODULE_ID, moduleId, INET_SESSION_SERVICE,
				"inetSessionAliveContractList");
		req.setParamContractId(contractId);
		req.setParam("trafficTypeIds", new int[] { 0 });
		req.setParam("serviceIds", Collections.emptyList());
		req.setParam("page", result.getPage());
		req.setParam("servIds", Collections.emptyList());

		extractSessions(result, req);
	}

	@Deprecated
	public void getContractSessionLog(Pageable<InetSessionLog> result, int contractId, Date dateFrom, Date dateTo) throws BGException {
		getSessionLogContractList(result, contractId, dateFrom, dateTo);
	}

	public void getSessionLogContractList(Pageable<InetSessionLog> result, int contractId, Date dateFrom, Date dateTo)
			throws BGException {
		RequestJsonRpc req = new RequestJsonRpc(INET_MODULE_ID, moduleId, INET_SESSION_SERVICE,
				"inetSessionLogContractList");
		req.setParamContractId(contractId);
		req.setParam("dateFrom", dateFrom);
		req.setParam("dateTo", dateTo);
		req.setParam("trafficTypeIds", new int[] { 0 });
		req.setParam("servIds", Collections.emptyList());
		req.setParam("page", result.getPage());

		extractSessions(result, req);
	}

	public void getSessionAliveList(Pageable<InetSessionLog> result,
			Set<Integer> deviceIds, Set<Integer> contractIds,
			String contract, String login, String ip, String callingStation,
			Date timeFrom, Date timeTo) throws BGException {
		RequestJsonRpc req = new RequestJsonRpc(INET_MODULE_ID, moduleId, INET_SESSION_SERVICE,
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

	private void extractSessions(Pageable<InetSessionLog> result, RequestJsonRpc req) throws BGException {
		JsonNode ret = transferData.postDataReturn(req, user);
		List<InetSessionLog> sessionList = readJsonValue(ret.findValue("list").traverse(),
				jsonTypeFactory.constructCollectionType(List.class, InetSessionLog.class));

		result.getList().addAll(sessionList);
		result.getPage().setData(jsonMapper.convertValue(ret.findValue("page"), Page.class));
	}
}