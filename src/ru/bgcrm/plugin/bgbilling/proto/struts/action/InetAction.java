package ru.bgcrm.plugin.bgbilling.proto.struts.action;

import org.apache.struts.action.ActionForward;
import org.bgerp.model.Pageable;
import ru.bgcrm.model.BGException;
import ru.bgcrm.plugin.bgbilling.Plugin;
import ru.bgcrm.plugin.bgbilling.proto.dao.ContractObjectDAO;
import ru.bgcrm.plugin.bgbilling.proto.dao.InetDAO;
import ru.bgcrm.plugin.bgbilling.proto.dao.InventoryDAO;
import ru.bgcrm.plugin.bgbilling.proto.dao.ResourceDAO;
import ru.bgcrm.plugin.bgbilling.proto.model.inet.*;
import ru.bgcrm.plugin.bgbilling.struts.action.BaseAction;
import ru.bgcrm.servlet.ActionServlet.Action;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.TimeUtils;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.inet.IpNet;
import ru.bgcrm.util.sql.ConnectionSet;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

@Action(path = "/user/plugin/bgbilling/proto/inet")
public class InetAction extends BaseAction {
    private static final String PATH_JSP = Plugin.PATH_JSP_USER + "/inet";
    public static final String BILLING_ID = "billingId";
    public static final String MODULE_ID = "moduleId";
    public static final String CONTRACT_ID = "contractId";
    public static final String DATE_FROM = "dateFrom";
    public static final String DATE_TO = "dateTo";
    public static final String DEVICE_ID = "deviceId";

    public ActionForward serviceTree(DynActionForm form, ConnectionSet conSet) throws BGException {
        InetDAO inetDao = InetDAO.getInstance(form.getUser(), form.getParam(BILLING_ID), form.getParamInt(MODULE_ID));

        form.getResponse().setData("list", inetDao.getServiceList(form.getParamInt(CONTRACT_ID)));

        return html(conSet, form, PATH_JSP + "/service_tree.jsp");
    }

    public ActionForward serviceGet(DynActionForm form, ConnectionSet conSet) throws BGException {
        String billingId = form.getParam(BILLING_ID);
        int moduleId = form.getParamInt(MODULE_ID);
        int contractId = form.getParamInt(CONTRACT_ID);
        InetDAO inetDao = InetDAO.getInstance(form.getUser(), billingId, moduleId);

        InventoryDAO inventoryDAO = new InventoryDAO(form.getUser(), billingId, moduleId);
        form.getResponse().setData("typeList", inetDao.getServiceTypeList());

        ContractObjectDAO contractObjectDAO = new ContractObjectDAO(form.getUser(), billingId);
        form.getResponse().setData("objectList", contractObjectDAO.getContractObjects(contractId));

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

            form.getResponse().setData("service", service);
        }

        return html(conSet, form, PATH_JSP + "/service_editor.jsp");
    }

    public ActionForward serviceUpdate(DynActionForm form, ConnectionSet conSet) throws BGException {
        InetDAO inetDao = InetDAO.getInstance(form.getUser(), form.getParam(BILLING_ID), form.getParamInt(MODULE_ID));

        InetService service = form.getId() > 0 ? inetDao.getService(form.getId()) : new InetService();

        final int typeId = form.getParamInt("typeId");
        service.setTypeId(typeId);
        InetServiceType inetServiceType = inetDao.getServiceTypeList().stream()
                .filter(t -> t.getId() == typeId)
                .findFirst().get();

        service.setContractId(form.getParamInt(CONTRACT_ID));
        service.setId(form.getId());
        service.setDateFrom(form.getParamDate(DATE_FROM));
        service.setDateTo(form.getParamDate(DATE_TO));
        service.setStatus(form.getParamInt("status"));
        service.setSessionCountLimit(form.getParamInt("sessions"));
        service.setLogin(form.getParam("login"));
        service.setDeviceId(form.getParamInt(DEVICE_ID));
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

    public ActionForward serviceDelete(DynActionForm form, ConnectionSet conSet) throws BGException {
        InetDAO inetDao = InetDAO.getInstance(form.getUser(), form.getParam(BILLING_ID), form.getParamInt(MODULE_ID));

        inetDao.deleteService(form.getId());

        return json(conSet, form);
    }

    public ActionForward sessionAliveContractList(DynActionForm form, ConnectionSet conSet) throws BGException {
        InetDAO inetDao = InetDAO.getInstance(form.getUser(), form.getParam(BILLING_ID), form.getParamInt(MODULE_ID));

        Pageable<InetSessionLog> result = new Pageable<>(form);
        inetDao.getSessionAliveContractList(result, form.getParamInt(CONTRACT_ID));
        setDeviceTitles(inetDao, result);

        return html(conSet, form, PATH_JSP + "/contract_report.jsp");
    }

    public ActionForward sessionLogContractList(DynActionForm form, ConnectionSet conSet) throws BGException {
        InetDAO inetDao = InetDAO.getInstance(form.getUser(), form.getParam(BILLING_ID), form.getParamInt(MODULE_ID));

        Pageable<InetSessionLog> result = new Pageable<>(form);
        inetDao.getSessionLogContractList(result, form.getParamInt(CONTRACT_ID),
                TimeUtils.clear_HOUR_MIN_MIL_SEC(
                        form.getParamDate(DATE_FROM, TimeUtils.getStartMonth(new GregorianCalendar()).getTime())),
                TimeUtils.clear_HOUR_MIN_MIL_SEC(
                        form.getParamDate(DATE_TO, TimeUtils.getEndMonth(new GregorianCalendar()).getTime())));
        setDeviceTitles(inetDao, result);

        return html(conSet, form, PATH_JSP + "/contract_report.jsp");
    }

    private void setDeviceTitles(InetDAO inetDao, Pageable<InetSessionLog> pageable) throws BGException {
        Map<Integer, InetDevice> deviceMap = new TreeMap<>();
        for (var item : pageable.getList()) {
            var device = deviceMap.computeIfAbsent(item.getDeviceId(), id -> {
                try {
                    return inetDao.getDevice(id);
                } catch (BGException e) {
                    log.error(e);
                    return null;
                }
            });
            item.setDeviceTitle(device.getTitle());
        }
    }

    public ActionForward serviceMenu(DynActionForm form, ConnectionSet conSet) throws BGException {
        InetDAO inetDao = InetDAO.getInstance(form.getUser(), form.getParam(BILLING_ID), form.getParamInt(MODULE_ID));

        int deviceId = form.getParamInt(DEVICE_ID);

        InetDevice device = inetDao.getDevice(deviceId);
        form.setResponseData("deviceMethods", inetDao.getDeviceManagerMethodList(device.getDeviceTypeId()));

        return html(conSet, form, PATH_JSP + "/service_menu.jsp");
    }

    public ActionForward serviceDeviceManage(DynActionForm form, ConnectionSet conSet) throws BGException {
        InetDAO inetDao = InetDAO.getInstance(form.getUser(), form.getParam(BILLING_ID), form.getParamInt(MODULE_ID));

        int deviceId = form.getParamInt(DEVICE_ID);
        String operation = form.getParam("operation");

        InetDevice device = inetDao.getDevice(deviceId);

        form.setResponseData("response", inetDao.deviceManage(device.getInvDeviceId(), form.getId(), 0, operation));

        return json(conSet, form);
    }

    public ActionForward serviceStateModify(DynActionForm form, ConnectionSet conSet) throws BGException {
        InetDAO inetDao = InetDAO.getInstance(form.getUser(), form.getParam(BILLING_ID), form.getParamInt(MODULE_ID));

        int state = form.getParamInt("state");
        inetDao.updateServiceState(form.getId(), state);

        return json(conSet, form);
    }

    public ActionForward interfaceListGet(DynActionForm form, ConnectionSet conSet) throws BGException {
        InetDAO inetDao = InetDAO.getInstance(form.getUser(), form.getParam(BILLING_ID), form.getParamInt(MODULE_ID));
        InventoryDAO inventoryDAO = new InventoryDAO(form.getUser(), form.getParam(BILLING_ID), form.getParamInt(MODULE_ID));
        int deviceId = form.getParamInt(DEVICE_ID);
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

    public ActionForward getFreeVlan(DynActionForm form, ConnectionSet conSet) throws BGException {
        InetDAO inetDao = InetDAO.getInstance(form.getUser(), form.getParam(BILLING_ID), form.getParamInt(MODULE_ID));
        ResourceDAO resourceDAO = new ResourceDAO(form.getUser(), form.getParam(BILLING_ID), form.getParamInt(MODULE_ID));

        int deviceId = form.getParamInt(DEVICE_ID);
        InetDevice device = inetDao.getDevice(deviceId);
        Integer result = -1;
        if (device != null) {
            Set<Integer> vlanResourceCategoryIds = inetDao.vlanResourceCategoryIds(device.getId());
            result = resourceDAO.getFreeVlan(vlanResourceCategoryIds, form.getParamDate(DATE_FROM), form.getParamDate(DATE_TO));
        }

        form.setResponseData("vlan", result);

        return json(conSet, form);
    }

    public ActionForward devicesGet(DynActionForm form, ConnectionSet conSet) throws BGException {
        InetDAO inetDao = InetDAO.getInstance(form.getUser(), form.getParam(BILLING_ID), form.getParamInt(MODULE_ID));
        form.setResponseData("rootDevice", inetDao.getRootDevice(Utils.toIntegerSet(form.getParam("deviceTypeIds")), Utils.toIntegerSet(form.getParam("deviceGroupIds"))));
        return html(conSet, form, PATH_JSP + "/service/device_tree.jsp");
    }
}
