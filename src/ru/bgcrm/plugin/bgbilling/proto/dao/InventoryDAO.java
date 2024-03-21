package ru.bgcrm.plugin.bgbilling.proto.dao;

import ru.bgcrm.model.user.User;
import ru.bgcrm.plugin.bgbilling.DBInfo;
import ru.bgcrm.plugin.bgbilling.RequestJsonRpc;
import ru.bgcrm.plugin.bgbilling.proto.model.inet.InetDeviceInterface;

import java.util.List;
import java.util.Optional;

import org.bgerp.app.exception.BGException;

public class InventoryDAO extends BillingModuleDAO {
    private static final String DEVICE_INTERFACE_SERVICE = "DeviceInterfaceService";
    private static final String INVENTORY_MODULE_ID = "ru.bitel.oss.systems.inventory.resource";

    public InventoryDAO(User user, DBInfo dbInfo, int moduleId) throws BGException {
        super(user, dbInfo, moduleId);
    }

    public InventoryDAO(User user, String billingId, int moduleId) throws BGException {
        super(user, billingId, moduleId);
    }


    public List<InetDeviceInterface> devicePortList(int invDeviceId, boolean subscriber) throws BGException {
        RequestJsonRpc req = new RequestJsonRpc(INVENTORY_MODULE_ID,moduleId, DEVICE_INTERFACE_SERVICE, "devicePortList");
        req.setParam("deviceId", invDeviceId);
        req.setParam("subscriber", subscriber);
        return readJsonValue(transferData.postDataReturn(req, user).traverse(),
                jsonTypeFactory.constructCollectionType(List.class, InetDeviceInterface.class));
    }

    public Optional<InetDeviceInterface> devicePort(int invDeviceId, int  port) throws BGException {
        RequestJsonRpc req = new RequestJsonRpc(INVENTORY_MODULE_ID,moduleId, DEVICE_INTERFACE_SERVICE, "devicePort");
        req.setParam("deviceId", invDeviceId);
        req.setParam("port", port);
        return Optional.ofNullable(jsonMapper.convertValue(transferData.postDataReturn(req, user), InetDeviceInterface.class));
    }
}
