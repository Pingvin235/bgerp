package ru.bgcrm.plugin.bgbilling.proto.dao.version.v8x;

import ru.bgcrm.model.BGException;
import ru.bgcrm.model.user.User;
import ru.bgcrm.plugin.bgbilling.DBInfo;
import ru.bgcrm.plugin.bgbilling.RequestJsonRpc;
import ru.bgcrm.plugin.bgbilling.proto.dao.InetDAO;
import ru.bgcrm.plugin.bgbilling.proto.model.inet.InetServiceType;
import ru.bgcrm.plugin.bgbilling.proto.model.inet.version.v8x.InetServiceType8x;

import java.util.List;

public class InetDAO8x extends InetDAO {
    private static final String INET_MODULE_ID_IMPL = "ru.bitel.bgbilling.modules.inet";

    public InetDAO8x(User user, String billingId, int moduleId) throws BGException {
        super(user, billingId, moduleId);
        INET_MODULE_ID = INET_MODULE_ID_IMPL;
    }

    public InetDAO8x(User user, DBInfo dbInfo, int moduleId) throws BGException {
        super(user, dbInfo, moduleId);
        INET_MODULE_ID = INET_MODULE_ID_IMPL;
    }

    public List<InetServiceType> getServiceTypeList() throws BGException {
        RequestJsonRpc req = new RequestJsonRpc(INET_MODULE_ID, moduleId, "InetServService", "inetServTypeList");

        return readJsonValue(transferData.postDataReturn(req, user).traverse(),
                jsonTypeFactory.constructCollectionType(List.class, InetServiceType8x.class));
    }
}
