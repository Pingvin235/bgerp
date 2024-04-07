package ru.bgcrm.plugin.bgbilling.event.listener;

import org.apache.commons.lang3.StringUtils;
import org.bgerp.app.event.iface.EventListener;
import org.bgerp.app.exception.BGException;
import org.bgerp.app.exception.BGMessageException;
import org.bgerp.app.exception.BGMessageExceptionTransparent;

import ru.bgcrm.event.EventProcessor;
import ru.bgcrm.event.link.LinkAddingEvent;
import ru.bgcrm.event.link.LinksToRemovingEvent;
import ru.bgcrm.model.CommonObjectLink;
import ru.bgcrm.model.customer.Customer;
import ru.bgcrm.plugin.bgbilling.DBInfo;
import ru.bgcrm.plugin.bgbilling.DBInfoManager;
import ru.bgcrm.plugin.bgbilling.proto.dao.ContractParamDAO;
import ru.bgcrm.util.sql.ConnectionSet;

/**
 * Слушатель события перед изменениями привязок.
 * Проверяет возможность и производит привязку/отвязку договора биллинга от контрагента.
 */
public class LinkChangingListener {
    public LinkChangingListener() {
        EventProcessor.subscribe(new EventListener<LinkAddingEvent>() {
            @Override
            public void notify(LinkAddingEvent e, ConnectionSet connectionSet) throws BGMessageException {
                customerChanging(e, e.getLink().getObjectId());
            }
        }, LinkAddingEvent.class);

        EventProcessor.subscribe(new EventListener<LinksToRemovingEvent>() {
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

        String billingId = StringUtils.substringAfter(link.getLinkObjectType(), ":");
        DBInfo dbInfo = DBInfoManager.getInstance().getDbInfoMap().get(billingId);

        if (dbInfo == null) {
            throw new BGMessageExceptionTransparent("Не найден биллинг с идентификатором: " + billingId);
        }

        int customerIdParam = dbInfo.getCustomerIdParam();
        if (customerIdParam <= 0) {
            throw new BGMessageExceptionTransparent("Не определён параметр 'customerIdParam' для сервера.");
        }

        int contractId = link.getLinkObjectId();
        try {
            ContractParamDAO contractParamDAO = new ContractParamDAO(event.getUser(), dbInfo);

            // пустая строка а не 0, т.к. по нулю будет импортировать его сразу же пытаться.
            contractParamDAO.updateTextParameter(contractId, customerIdParam, customerId > 0 ? String.valueOf(customerId) : "");
        } catch (Exception e) {
            // при отвязке не существующего в билллинге договора будет возникать исключение на updateTextParameter, это не должно мешать отвязать договор от контрагента
            if (customerId > 0)
                throw new BGException("Ошибка привязки договора к контрагенту: " + e.getMessage(), e);
        }
    }
}