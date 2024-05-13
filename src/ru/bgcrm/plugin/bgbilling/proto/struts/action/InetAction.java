package ru.bgcrm.plugin.bgbilling.proto.struts.action;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.apache.struts.action.ActionForward;
import org.bgerp.model.Pageable;

import ru.bgcrm.plugin.bgbilling.Plugin;
import ru.bgcrm.plugin.bgbilling.proto.dao.ContractObjectDAO;
import ru.bgcrm.plugin.bgbilling.proto.dao.InetDAO;
import ru.bgcrm.plugin.bgbilling.proto.dao.InventoryDAO;
import ru.bgcrm.plugin.bgbilling.proto.dao.ResourceDAO;
import ru.bgcrm.plugin.bgbilling.proto.model.inet.InetDevice;
import ru.bgcrm.plugin.bgbilling.proto.model.inet.InetDeviceInterface;
import ru.bgcrm.plugin.bgbilling.proto.model.inet.InetService;
import ru.bgcrm.plugin.bgbilling.proto.model.inet.InetServiceType;
import ru.bgcrm.plugin.bgbilling.proto.model.inet.InetSessionLog;
import ru.bgcrm.plugin.bgbilling.struts.action.BaseAction;
import ru.bgcrm.servlet.ActionServlet.Action;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.inet.IpNet;
import ru.bgcrm.util.sql.ConnectionSet;

@Action(path = "/user/plugin/bgbilling/proto/inet")
public class InetAction extends BaseAction {
    private static final String PATH_JSP = Plugin.PATH_JSP_USER + "/inet";

    public ActionForward serviceTree(DynActionForm form, ConnectionSet conSet) {
        InetDAO inetDao = InetDAO.getInstance(form.getUser(), form.getParam("billingId"), form.getParamInt("moduleId"));

        form.setResponseData("list", inetDao.getServiceList(form.getParamInt("contractId")));

        return html(conSet, form, PATH_JSP + "/service_tree.jsp");
    }

    public ActionForward serviceGet(DynActionForm form, ConnectionSet conSet) {
        String billingId = form.getParam("billingId");
        int moduleId = form.getParamInt("moduleId");
        int contractId = form.getParamInt("contractId");
        InetDAO inetDao = InetDAO.getInstance(form.getUser(), billingId, moduleId);

        InventoryDAO inventoryDAO = new InventoryDAO(form.getUser(), billingId, moduleId);
        form.setResponseData("typeList", inetDao.getServiceTypeList());

        ContractObjectDAO contractObjectDAO = new ContractObjectDAO(form.getUser(), billingId);
        form.setResponseData("objectList", contractObjectDAO.getContractObjects(contractId));

        if (form.getId() > 0) {
            // использован тот же метод, что и в клиентском приложении, getService не
            // возвращает многие поля сервисов типа deviceTitle
            /*
             * InetService service = inetDao.getServiceList(form.getParamInt("contractId"))
             * .stream().filter(s -> s.getId() == form.getId()) .findFirst().orElse(null);
             */
            InetService service = inetDao.getService(form.getId());
            InetDevice device = inetDao.getDevice(service.getDeviceId());
            if (device != null) {
                service.setDeviceTitle(device.getTitle() + " (" + device.getId() + ')');
                // интерфейсы
                service.setInterfaceTitle(inventoryDAO.devicePort(device.getInvDeviceId(), service.getIfaceId())
                        .map(InetDeviceInterface::getTitle)
                        .orElse(""));
            }

            form.setResponseData("service", service);
        }

        return html(conSet, form, PATH_JSP + "/service_editor.jsp");
    }

    public ActionForward serviceUpdate(DynActionForm form, ConnectionSet conSet) {
        InetDAO inetDao = InetDAO.getInstance(form.getUser(), form.getParam("billingId"), form.getParamInt("moduleId"));

        InetService service = form.getId() > 0 ? inetDao.getService(form.getId()) : new InetService();

        final int typeId = form.getParamInt("typeId");
        service.setTypeId(typeId);
        InetServiceType inetServiceType = inetDao.getServiceTypeList().stream()
                .filter(t -> t.getId() == typeId)
                .findFirst().get();

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

        setAddressToService(service,
                inetServiceType,
                form.getParam("addrFrom"),
                form.getParam("addrTo"),
                form.getParam("mask"));

        service.setContractObjectId(form.getParamInt("contractObjectId"));
        service.setComment(form.getParam("comment"));

        inetDao.updateService(service, Collections.emptyList(), form.getParamBoolean("generateLogin", false),
                form.getParamBoolean("generatePassword", false), 0L);

        return json(conSet, form);
    }

    private void setAddressToService(InetService current, InetServiceType inetServiceType, String addressFrom, String addressTo, String mask) {
        current.setAddrFrom(null);
        current.setAddrTo(null);
        if (Utils.notBlankString(addressFrom)) {
            try {
                current.setAddrFrom(InetAddress.getByName(addressFrom).getAddress());
            } catch (Exception ex) {}
        }

        if (inetServiceType.getAddressType() == InetServiceType.ADDRESS_TYPE_NET) {
            if (current.getAddrFrom() != null && Utils.notBlankString(mask)) {
                try {
                    IpNet net = new IpNet(current.getAddrFrom(), Utils.parseInt(mask));
                    current.setAddrTo(net.getMaxIp());
                } catch (Exception ex) {}
            }
        } else {
            if (current.getAddrFrom() != null && Utils.notBlankString(addressTo)) {
                try {
                    current.setAddrTo(InetAddress.getByName(addressTo).getAddress());
                } catch (Exception ex) {}
            }
        }
    }

    public ActionForward serviceDelete(DynActionForm form, ConnectionSet conSet) {
        InetDAO inetDao = InetDAO.getInstance(form.getUser(), form.getParam("billingId"), form.getParamInt("moduleId"));

        inetDao.deleteService(form.getId());

        return json(conSet, form);
    }

    public ActionForward sessionAliveContractList(DynActionForm form, ConnectionSet conSet) {
        InetDAO inetDao = InetDAO.getInstance(form.getUser(), form.getParam("billingId"), form.getParamInt("moduleId"));

        inetDao.getSessionAliveContractList(new Pageable<InetSessionLog>(form), form.getParamInt("contractId"));

        return html(conSet, form, PATH_JSP + "/contract_report.jsp");
    }

    public ActionForward sessionLogContractList(DynActionForm form, ConnectionSet conSet) {
        InetDAO inetDao = InetDAO.getInstance(form.getUser(), form.getParam("billingId"), form.getParamInt("moduleId"));

        inetDao.getSessionLogContractList(new Pageable<InetSessionLog>(form), form.getParamInt("contractId"), form.getParamDate("dateFrom"), form.getParamDate("dateTo"));

        return html(conSet, form, PATH_JSP + "/contract_report.jsp");
    }

    public ActionForward connectionClose(DynActionForm form, ConnectionSet conSet) throws Exception {
        InetDAO inetDao = InetDAO.getInstance(form.getUser(), form.getParam("billingId"), form.getParamInt("moduleId"));
        inetDao.connectionClose(form.getParamInt("contractId", Utils::isPositive), form.getParamLong("connectionId"));

        return json(conSet, form);
    }

    public ActionForward connectionFinish(DynActionForm form, ConnectionSet conSet) throws Exception {
        InetDAO inetDao = InetDAO.getInstance(form.getUser(), form.getParam("billingId"), form.getParamInt("moduleId"));
        inetDao.connectionFinish(form.getParamInt("contractId", Utils::isPositive), form.getParamLong("connectionId"));

        return json(conSet, form);
    }

    public ActionForward serviceMenu(DynActionForm form, ConnectionSet conSet) {
        InetDAO inetDao = InetDAO.getInstance(form.getUser(), form.getParam("billingId"), form.getParamInt("moduleId"));

        int deviceId = form.getParamInt("deviceId");

        InetDevice device = inetDao.getDevice(deviceId);
        form.setResponseData("deviceMethods", inetDao.getDeviceManagerMethodList(device.getDeviceTypeId()));

        return html(conSet, form, PATH_JSP + "/service_menu.jsp");
    }

    public ActionForward serviceDeviceManage(DynActionForm form, ConnectionSet conSet) {
        InetDAO inetDao = InetDAO.getInstance(form.getUser(), form.getParam("billingId"), form.getParamInt("moduleId"));

        int deviceId = form.getParamInt("deviceId");
        String operation = form.getParam("operation");

        InetDevice device = inetDao.getDevice(deviceId);

        form.setResponseData("response", inetDao.deviceManage(form.getParamInt("contractId"), device.getInvDeviceId(), form.getId(), 0, operation));

        return json(conSet, form);
    }

    public ActionForward serviceStateModify(DynActionForm form, ConnectionSet conSet) {
        InetDAO inetDao = InetDAO.getInstance(form.getUser(), form.getParam("billingId"), form.getParamInt("moduleId"));

        int state = form.getParamInt("state");
        inetDao.updateServiceState(form.getId(), state);

        return json(conSet, form);
    }

    public ActionForward interfaceListGet(DynActionForm form, ConnectionSet conSet) {
        InetDAO inetDao = InetDAO.getInstance(form.getUser(), form.getParam("billingId"), form.getParamInt("moduleId"));
        InventoryDAO inventoryDAO = new InventoryDAO(form.getUser(), form.getParam("billingId"), form.getParamInt("moduleId"));
        int deviceId = form.getParamInt("deviceId");
        InetDevice device = inetDao.getDevice(deviceId);
        List<InetDeviceInterface> inetDeviceInterfaces;
        if (device != null) {
            inetDeviceInterfaces = inventoryDAO.devicePortList(device.getInvDeviceId(), false);
        } else {
            inetDeviceInterfaces = new ArrayList<>();
        }
        form.setResponseData("interfaces", inetDeviceInterfaces);
        return html(conSet, form, PATH_JSP + "/service/interface_editor.jsp");
    }

    public ActionForward getFreeVlan(DynActionForm form, ConnectionSet conSet) {
        InetDAO inetDao = InetDAO.getInstance(form.getUser(), form.getParam("billingId"), form.getParamInt("moduleId"));
        ResourceDAO resourceDAO = new ResourceDAO(form.getUser(), form.getParam("billingId"), form.getParamInt("moduleId"));

        int deviceId = form.getParamInt("deviceId");
        InetDevice device = inetDao.getDevice(deviceId);
        Integer result = -1;
        if (device != null) {
            Set<Integer> vlanResourceCategoryIds = inetDao.vlanResourceCategoryIds(device.getId());
            result = resourceDAO.getFreeVlan(vlanResourceCategoryIds, form.getParamDate("dateFrom"), form.getParamDate("dateTo"));
        }

        form.setResponseData("vlan", result);

        return json(conSet, form);
    }

    public ActionForward devicesGet(DynActionForm form, ConnectionSet conSet) {
        InetDAO inetDao = InetDAO.getInstance(form.getUser(), form.getParam("billingId"), form.getParamInt("moduleId"));
        form.setResponseData("rootDevice", inetDao.getRootDevice(Utils.toIntegerSet(form.getParam("deviceTypeIds")), Utils.toIntegerSet(form.getParam("deviceGroupIds"))));
        return html(conSet, form, PATH_JSP + "/service/device_tree.jsp");
    }
}
