package ru.bgcrm.plugin.bgbilling.proto.dao;

import org.bgerp.model.user.iface.UserAccount;

import ru.bgcrm.plugin.bgbilling.DBInfo;
import ru.bgcrm.plugin.bgbilling.dao.BillingDAO;

public class BillingModuleDAO extends BillingDAO {
    protected int moduleId;

    protected int getModuleId() {
        return moduleId;
    }

    protected void setModuleId(int moduleId) {
        this.moduleId = moduleId;
    }

    public BillingModuleDAO(UserAccount user, DBInfo dbInfo, int moduleId) {
        super(user, dbInfo);
        setModuleId(moduleId);
    }

    public BillingModuleDAO(UserAccount user, String billingId, int moduleId) {
        super(user, billingId);
        setModuleId(moduleId);
    }
}
