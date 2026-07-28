package ru.bgcrm.plugin.bgbilling.proto.dao;

import java.util.List;
import java.util.Set;

import ru.bgcrm.model.user.User;
import ru.bgcrm.plugin.bgbilling.DBInfo;
import ru.bgcrm.plugin.bgbilling.RequestJsonRpc;
import ru.bgcrm.plugin.bgbilling.dao.BillingDAO;
import ru.bgcrm.plugin.bgbilling.proto.model.dispatch.Contact;
import ru.bgcrm.util.Utils;

public class DispatchDAO extends BillingDAO {

    private static final String DISPATCH_MODULE_ID = "ru.bitel.bgbilling.plugins.dispatch";

    public DispatchDAO(User user, DBInfo dbInfo) {
        super(user, dbInfo);
    }

    public DispatchDAO(User user, String billingId) {
        super(user, billingId);
    }

    /**
     * Updates or adds the contract's contact
     * @param contact the contact
     */
    public void updateContact(Contact contact) {
        RequestJsonRpc req = new RequestJsonRpc(DISPATCH_MODULE_ID, "DispatchService", "updateContact");
        req.setParam("current", contact);
        transferData.postData(req, user);
    }

    /**
     * Returns the contract's contacts
     * @param contractId the contract ID
     * @return the contact list
     */
    public List<Contact> getContactList(int contractId) {
        RequestJsonRpc req = new RequestJsonRpc(DISPATCH_MODULE_ID, "DispatchService", "getContacts");
        req.setParamContractId(contractId);
        return readJsonValue(transferData.postDataReturn(req, user).traverse(),
                jsonTypeFactory.constructCollectionType(List.class, Contact.class));
    }

    /**
     * Adds subscriptions to contracts if matching contacts are found on them
     * @param contractIds the contract IDs
     * @param dispatchIds the subscription IDs
     */
    public void addSubscriptions(Set<Integer> contractIds, Set<Integer> dispatchIds) {
        RequestJsonRpc req = new RequestJsonRpc(DISPATCH_MODULE_ID, "DispatchService", "addSubscriptions");
        req.setParam("contractIds", Utils.toString(contractIds));
        req.setParam("dispatchIds", Utils.toString(dispatchIds));
        transferData.postData(req, user);
    }

}
