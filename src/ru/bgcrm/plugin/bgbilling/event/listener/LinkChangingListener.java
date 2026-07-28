package ru.bgcrm.plugin.bgbilling.event.listener;

import org.bgerp.app.event.EventProcessor;
import org.bgerp.app.event.iface.EventListener;
import org.bgerp.app.exception.BGException;
import org.bgerp.app.exception.BGMessageException;
import org.bgerp.app.exception.BGMessageExceptionWithoutL10n;

import ru.bgcrm.event.link.LinkAddingEvent;
import ru.bgcrm.event.link.LinksToRemovingEvent;
import ru.bgcrm.model.CommonObjectLink;
import ru.bgcrm.model.customer.Customer;
import ru.bgcrm.plugin.bgbilling.DBInfo;
import ru.bgcrm.plugin.bgbilling.DBInfoManager;
import ru.bgcrm.plugin.bgbilling.proto.dao.ContractParamDAO;
import ru.bgcrm.plugin.bgbilling.proto.model.Contract;
import ru.bgcrm.util.sql.ConnectionSet;

/**
 * Listener for the event before link changes. Checks whether it's possible, and links/unlinks the billing contract from the customer
 */
public class LinkChangingListener {
    public LinkChangingListener() {
        EventProcessor.subscribe(new EventListener<>() {
            @Override
            public void notify(LinkAddingEvent e, ConnectionSet connectionSet) throws BGMessageException {
                customerChanging(e, e.getLink().getObjectId());
            }
        }, LinkAddingEvent.class);

        EventProcessor.subscribe(new EventListener<>() {
            @Override
            public void notify(LinksToRemovingEvent e, ConnectionSet connectionSet) throws BGMessageException {
                customerChanging(e, 0);
            }
        }, LinksToRemovingEvent.class);
    }

    private void customerChanging(LinkAddingEvent event, int customerId) throws BGMessageException {
        CommonObjectLink link = event.getLink();
        if (!Customer.OBJECT_TYPE.equals(link.getObjectType()) || !link.getLinkObjectType().startsWith("contract:")) {
            return;
        }

        String billingId = new Contract(link).getBillingId();
        DBInfo dbInfo = DBInfoManager.getInstance().getDbInfoMap().get(billingId);

        if (dbInfo == null) {
            throw new BGMessageExceptionWithoutL10n("Не найден биллинг с идентификатором: " + billingId);
        }

        int customerIdParam = dbInfo.getCustomerIdParam();
        if (customerIdParam <= 0) {
            throw new BGMessageExceptionWithoutL10n("Не определён параметр 'customerIdParam' для сервера.");
        }

        int contractId = link.getLinkObjectId();
        try {
            ContractParamDAO contractParamDAO = new ContractParamDAO(event.getUser(), dbInfo);

            // an empty string, not 0, since 0 would make it try to import it right away
            contractParamDAO.updateTextParameter(contractId, customerIdParam, customerId > 0 ? String.valueOf(customerId) : "");
        } catch (Exception e) {
            // unlinking a contract that doesn't exist in the billing raises an exception on updateTextParameter, which shouldn't prevent unlinking the contract from the customer
            if (customerId > 0)
                throw new BGException("Ошибка привязки договора к контрагенту: " + e.getMessage(), e);
        }
    }
}