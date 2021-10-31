package ru.bgcrm.plugin.bgbilling.proto.struts.action;

import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import ru.bgcrm.model.BGException;
import ru.bgcrm.model.SearchResult;
import ru.bgcrm.plugin.bgbilling.proto.dao.InetDAO;
import ru.bgcrm.plugin.bgbilling.proto.model.inet.InetDevice;
import ru.bgcrm.plugin.bgbilling.proto.model.inet.InetService;
import ru.bgcrm.plugin.bgbilling.proto.model.inet.InetServiceOption;
import ru.bgcrm.plugin.bgbilling.proto.model.inet.InetSessionLog;
import ru.bgcrm.plugin.bgbilling.struts.action.BaseAction;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.TimeUtils;
import ru.bgcrm.util.sql.ConnectionSet;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

public class InetAction extends BaseAction {
	public ActionForward serviceTree(ActionMapping mapping, DynActionForm form, ConnectionSet conSet)
			throws BGException {
		InetDAO inetDao = InetDAO.getInstance(form.getUser(), form.getParam("billingId"), form.getParamInt("moduleId"));

		form.getResponse().setData("list", inetDao.getServiceList(form.getParamInt("contractId")));

		return html(conSet, mapping, form, "serviceTree");
	}

	public ActionForward serviceGet(ActionMapping mapping, DynActionForm form, ConnectionSet conSet)
			throws BGException {
		InetDAO inetDao = InetDAO.getInstance(form.getUser(), form.getParam("billingId"), form.getParamInt("moduleId"));

		form.getResponse().setData("typeList", inetDao.getServiceTypeList());
		if (form.getId() > 0) {
		    // использован тот же метод, что и в клиентском приложении, getService не возвращает многие поля сервисов типа deviceTitle
		    /*InetService service = inetDao.getServiceList(form.getParamInt("contractId"))
		            .stream().filter(s -> s.getId() == form.getId())
		            .findFirst().orElse(null);*/
			InetService service = inetDao.getService(form.getId());
			InetDevice device = inetDao.getDevice(service.getDeviceId());
			if (device != null) {
				service.setDeviceTitle(device.getTitle());
			}
			form.getResponse().setData("service", service);
		}

		return html(conSet, mapping, form, "serviceEditor");
	}

	public ActionForward serviceUpdate(ActionMapping mapping, DynActionForm form, ConnectionSet conSet)
			throws BGException {
		InetDAO inetDao = InetDAO.getInstance(form.getUser(), form.getParam("billingId"), form.getParamInt("moduleId"));

		List<InetServiceOption> optionList = new ArrayList<InetServiceOption>();

		InetService service = new InetService();
		// FIXME: Убрать потом, когда все поля будут заполнены корректно.
		if (form.getId() > 0)
		    service = inetDao.getService(form.getId());
		
		service.setContractId(form.getParamInt("contractId"));
		service.setId(form.getId());
		service.setDateFrom(form.getParamDate("dateFrom"));
		service.setDateTo(form.getParamDate("dateTo"));
		service.setStatus(form.getParamInt("status"));
		service.setSessionCountLimit(form.getParamInt("sessions"));
		service.setLogin(form.getParam("login"));
		service.setDeviceId(form.getParamInt("deviceId"));
		service.setIfaceId(form.getParamInt("ifaceId"));
		service.setVlan(form.getParamInt("vlan", -1));
		service.setMacAddressStr(form.getParam("macAddress"));
		service.setComment(form.getParam("comment"));

		inetDao.updateService(service, optionList, form.getParamBoolean("generateLogin", false),
				form.getParamBoolean("generatePassword", false), 0L);

		return json(conSet, form);
	}

	public ActionForward serviceDelete(ActionMapping mapping, DynActionForm form, ConnectionSet conSet) throws BGException {
		InetDAO inetDao = InetDAO.getInstance(form.getUser(), form.getParam("billingId"), form.getParamInt("moduleId"));

		inetDao.deleteService(form.getId());

		return json(conSet, form);
	}

	public ActionForward sessionAliveContractList(ActionMapping mapping, DynActionForm form, ConnectionSet conSet)
			throws BGException {
		InetDAO inetDao = InetDAO.getInstance(form.getUser(), form.getParam("billingId"), form.getParamInt("moduleId"));

		inetDao.getContractSessionAlive(new SearchResult<InetSessionLog>(form), form.getParamInt("contractId"));

		return html(conSet, mapping, form, "contractReport");
	}

	public ActionForward sessionLogContractList(ActionMapping mapping, DynActionForm form, ConnectionSet conSet)
			throws BGException {
		InetDAO inetDao = InetDAO.getInstance(form.getUser(), form.getParam("billingId"), form.getParamInt("moduleId"));

		inetDao.getContractSessionLog(new SearchResult<InetSessionLog>(form), form.getParamInt("contractId"),
				TimeUtils.clear_HOUR_MIN_MIL_SEC(
						form.getParamDate("dateFrom", TimeUtils.getStartMonth(new GregorianCalendar()).getTime())),
				TimeUtils.clear_HOUR_MIN_MIL_SEC(
						form.getParamDate("dateTo", TimeUtils.getEndMonth(new GregorianCalendar()).getTime())));

		return html(conSet, mapping, form, "contractReport");
	}
	
	public ActionForward serviceMenu(ActionMapping mapping, DynActionForm form, ConnectionSet conSet)
			throws BGException {
		InetDAO inetDao = InetDAO.getInstance(form.getUser(), form.getParam("billingId"), form.getParamInt("moduleId"));
		
		int deviceId = form.getParamInt("deviceId");
		
		InetDevice device = inetDao.getDevice(deviceId);
		form.setResponseData("deviceMethods", inetDao.getDeviceManagerMethodList(device.getDeviceTypeId()));
		
		return html(conSet, mapping, form, "serviceMenu");
	}
	
	public ActionForward serviceDeviceManage(ActionMapping mapping, DynActionForm form, ConnectionSet conSet)
			throws BGException {
		InetDAO inetDao = InetDAO.getInstance(form.getUser(), form.getParam("billingId"), form.getParamInt("moduleId"));
		
		int deviceId = form.getParamInt("deviceId");
		String operation = form.getParam("operation");

		InetDevice device = inetDao.getDevice(deviceId);
		
		form.setResponseData("response", inetDao.deviceManage(device.getInvDeviceId(), form.getId(), 0, operation));
		
		return json(conSet, form);
	}
	
	public ActionForward serviceStateModify(ActionMapping mapping, DynActionForm form, ConnectionSet conSet)
			throws BGException {
		InetDAO inetDao = InetDAO.getInstance(form.getUser(), form.getParam("billingId"), form.getParamInt("moduleId"));
		
		int state = form.getParamInt("state");
		inetDao.updateServiceState(form.getId(), state);
		
		return json(conSet, form);
	}
}
