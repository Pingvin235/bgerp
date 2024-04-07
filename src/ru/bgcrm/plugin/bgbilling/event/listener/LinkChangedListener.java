package ru.bgcrm.plugin.bgbilling.event.listener;

import java.sql.Connection;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.bgerp.app.cfg.ConfigMap;
import org.bgerp.app.event.EventProcessor;
import org.bgerp.app.event.iface.EventListener;
import org.bgerp.cache.ProcessTypeCache;
import org.bgerp.util.Log;

import ru.bgcrm.dao.process.ProcessDAO;
import ru.bgcrm.dao.process.ProcessLinkDAO;
import ru.bgcrm.event.client.ProcessChangedEvent;
import ru.bgcrm.event.link.LinkAddedEvent;
import ru.bgcrm.event.link.LinkAddingEvent;
import ru.bgcrm.model.CommonObjectLink;
import ru.bgcrm.model.customer.Customer;
import ru.bgcrm.model.process.Process;
import ru.bgcrm.model.process.ProcessType;
import ru.bgcrm.plugin.bgbilling.dao.ContractCustomerDAO;
import ru.bgcrm.plugin.bgbilling.proto.model.Contract;
import ru.bgcrm.struts.action.LinkAction;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.sql.ConnectionSet;

public class LinkChangedListener {
    private static final Log log = Log.getLog();

    public LinkChangedListener() {
        EventProcessor.subscribe(new EventListener<LinkAddedEvent>() {
            @Override
            public void notify(LinkAddedEvent e, ConnectionSet connectionSet) throws Exception {
                linkAdded(e, connectionSet);
            }
        }, LinkAddedEvent.class);
    }

    private void linkAdded(LinkAddingEvent event, ConnectionSet connectionSet) throws Exception {
        CommonObjectLink link = event.getLink();

        // обработка только привязки договора к процессу
        if (!Process.OBJECT_TYPE.equals(link.getObjectType()) || !link.getLinkObjectType().startsWith("contract:")) {
            return;
        }

        ProcessDAO processDao = new ProcessDAO(connectionSet.getConnection());

        int processId = event.getLink().getObjectId();

        Process process = processDao.getProcess(processId);
        if (process == null) {
            log.warn("Process with id: " + processId + " not found.");
            return;
        }

        ProcessType type = ProcessTypeCache.getProcessType(process.getTypeId());
        if (type == null) {
            log.warn("Process type with id: " + process.getTypeId() + " not found.");
            return;
        }

        String contractTitle = link.getLinkObjectTitle();
        String billingId = StringUtils.substringAfter(link.getLinkObjectType(), ":");

        boolean groupsChanged = false;

        // переход до первого подходящего правила и установка групп
        for (ConfigMap pm : type.getProperties().getConfigMap().subIndexed("bgbilling:processLinkedContract.").values()) {
            String titleRegexp = pm.get("titleRegexp");
            Set<String> billingIds = Utils.toSet(pm.get("billingIds"));

            if ((Utils.notBlankString(titleRegexp) && contractTitle.matches(titleRegexp))
                    || (billingIds.size() > 0 && billingIds.contains(billingId))) {
                Set<Integer> groupIds = Utils.toIntegerSet(pm.get("groupIds"));
                if (groupIds.size() == 0) {
                    continue;
                }

                log.debug("Adding groups: {} for linked contract title: {}", groupIds, contractTitle);

                groupsChanged = process.getGroupIds().addAll(groupIds);

                break;
            }
        }

        if (groupsChanged) {
            processDao.updateProcessGroups(process.getGroups(), processId);
            event.getForm().getResponse().addEvent(new ProcessChangedEvent(processId));
        }

        if (type.getProperties().getConfigMap().getBoolean("bgbilling:linkCustomerOnContractLink", true)) {
            Connection con = connectionSet.getConnection();

            Customer customer = new ContractCustomerDAO(con).getContractCustomer(new Contract(billingId, link.getLinkObjectId()));
            if (customer != null) {
                CommonObjectLink customerLink = new CommonObjectLink();
                customerLink.setObjectId(link.getObjectId());
                customerLink.setObjectType(link.getObjectType());
                customerLink.setLinkObjectId(customer.getId());
                customerLink.setLinkObjectTitle(customer.getTitle());
                customerLink.setLinkObjectType(Customer.OBJECT_TYPE);

                if (!new ProcessLinkDAO(con).isLinkExists(customerLink)) {
                    LinkAction.addLink(event.getForm(), connectionSet.getConnection(), customerLink);
                }
            }
        }
    }
}
