package ru.bgcrm.plugin.bgbilling.proto.dao;

import java.util.List;
import java.util.Set;

import ru.bgcrm.model.BGException;
import ru.bgcrm.model.user.User;
import ru.bgcrm.plugin.bgbilling.DBInfo;
import ru.bgcrm.plugin.bgbilling.RequestJsonRpc;
import ru.bgcrm.plugin.bgbilling.dao.BillingDAO;
import ru.bgcrm.plugin.bgbilling.proto.model.dispatch.Contact;
import ru.bgcrm.util.Utils;

public class DispatchDAO extends BillingDAO {
    
    private static final String DISPATCH_MODULE_ID = "ru.bitel.bgbilling.plugins.dispatch";

    public DispatchDAO(User user, DBInfo dbInfo) throws BGException {
        super(user, dbInfo);
    }

    public DispatchDAO(User user, String billingId) throws BGException {
        super(user, billingId);
    }
    
    /**
     * Обновляет либо добавляет контакт договора.
     * @param contact
     */
    public void updateContact(Contact contact) throws BGException {
        RequestJsonRpc req = new RequestJsonRpc(DISPATCH_MODULE_ID, "DispatchService", "updateContact");
        req.setParam("current", contact);
        transferData.postData(req, user);
    }
    
    /**
     * Возвращает список контактов договора.
     * @param contractId
     * @return
     * @throws BGException
     */
    public List<Contact> getContactList(int contractId) throws BGException {
        RequestJsonRpc req = new RequestJsonRpc(DISPATCH_MODULE_ID, "DispatchService", "getContacts");
        req.setParamContractId(contractId);
        return readJsonValue(transferData.postDataReturn(req, user).traverse(),
                jsonTypeFactory.constructCollectionType(List.class, Contact.class));
    }
    
    /**
     * Добавляет рассылки на договоры в случае нахождения в них подходящих контактов.
     * @param contractIds коды договоров
     * @param dispatchIds коды рассылок
     */
    public void addSubscriptions(Set<Integer> contractIds, Set<Integer> dispatchIds) throws BGException {
        RequestJsonRpc req = new RequestJsonRpc(DISPATCH_MODULE_ID, "DispatchService", "addSubscriptions");
        req.setParam("contractIds", Utils.toString(contractIds));
        req.setParam("dispatchIds", Utils.toString(dispatchIds));
        transferData.postData(req, user);
    }

}
