package ru.bgcrm.plugin.bgbilling.proto.dao.version.v8x;

import java.util.List;

import ru.bgcrm.model.BGException;
import ru.bgcrm.model.user.User;
import ru.bgcrm.plugin.bgbilling.DBInfo;
import ru.bgcrm.plugin.bgbilling.RequestJsonRpc;
import ru.bgcrm.plugin.bgbilling.proto.dao.InetDAO;
import ru.bgcrm.plugin.bgbilling.proto.model.inet.InetServiceType;
import ru.bgcrm.plugin.bgbilling.proto.model.inet.version.v8x.InetServiceType8x;

public class InetDAO8x extends InetDAO {
    private static final String INET_MODULE = "ru.bitel.bgbilling.modules.inet";

    public InetDAO8x(User user, String billingId, int moduleId) throws BGException {
        super(user, billingId, INET_MODULE, moduleId);
    }

    public InetDAO8x(User user, DBInfo dbInfo, int moduleId) throws BGException {
        super(user, dbInfo, INET_MODULE, moduleId);
    }

    public List<InetServiceType> getServiceTypeList() throws BGException {
        RequestJsonRpc req = new RequestJsonRpc(inetModule, moduleId, "InetServService", "inetServTypeList");

        return readJsonValue(transferData.postDataReturn(req, user).traverse(),
                jsonTypeFactory.constructCollectionType(List.class, InetServiceType8x.class));
    }
}
