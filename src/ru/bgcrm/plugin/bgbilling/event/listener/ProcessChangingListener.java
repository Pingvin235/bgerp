package ru.bgcrm.plugin.bgbilling.event.listener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.bgerp.app.event.EventProcessor;
import org.bgerp.app.event.iface.EventListener;
import org.bgerp.app.exception.BGMessageException;
import org.bgerp.app.exception.BGMessageExceptionTransparent;
import org.bgerp.cache.ProcessTypeCache;
import org.w3c.dom.Document;

import ru.bgcrm.dao.process.ProcessLinkDAO;
import ru.bgcrm.event.process.ProcessChangingEvent;
import ru.bgcrm.model.CommonObjectLink;
import ru.bgcrm.model.process.ProcessType;
import ru.bgcrm.plugin.bgbilling.Request;
import ru.bgcrm.plugin.bgbilling.dao.BillingDAO;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.XMLUtils;
import ru.bgcrm.util.sql.ConnectionSet;

/**
 * Слушатель изменений процессов.
 */
@Deprecated
public class ProcessChangingListener {
    public ProcessChangingListener() {
        EventProcessor.subscribe(new EventListener<ProcessChangingEvent>() {
            @Override
            public void notify(ProcessChangingEvent e, ConnectionSet connectionSet) throws BGMessageException {
                processChanging(e, connectionSet);
            }
        }, ProcessChangingEvent.class);
    }

    private void processChanging(ProcessChangingEvent e, ConnectionSet conSet) throws BGMessageException {
        if (!e.isStatus()) {
            return;
        }

        ProcessType type = ProcessTypeCache.getProcessType(e.getProcess().getTypeId());

        String paramKey = "bgbilling:processToStatus." + e.getForm().getParamInt("statusId", -1) + ".needLinkedProblemsStatus";

        String needStatus = null;
        if (Utils.isBlankString(needStatus = type.getProperties().getConfigMap().get(paramKey))) {
            return;
        }

        ProcessLinkDAO linkDao = new ProcessLinkDAO(conSet.getConnection());
        List<CommonObjectLink> linkList = linkDao.getObjectLinksWithType(e.getProcess().getId(), "bgbilling-problem%");

        if (linkList.size() == 0) {
            return;
        }

        Map<String, Integer> billingStatusMap = new HashMap<String, Integer>();
        for (String token : needStatus.split(";")) {
            String[] pair = token.split(":");
            if (pair.length != 2) {
                continue;
            }
            billingStatusMap.put(pair[0], Utils.parseInt(pair[1]));
        }

        for (CommonObjectLink link : linkList) {
            String billingId = StringUtils.substringAfter(link.getLinkObjectType(), ":");

            // в данном биллинге нет требования по статусу проблемы
            Integer needStatusCode = billingStatusMap.get(billingId);
            if (needStatusCode == null) {
                continue;
            }

            Request req = new Request();
            req.setModule("ru.bitel.bgbilling.plugins.crm");
            req.setAction("RegisterProblemTable");
            req.setAttribute("id", link.getLinkObjectId());

            Document doc = new BillingDAO(e.getUser(), billingId).doRequest(req);
            int statusCode = Utils.parseInt(XMLUtils.selectText(doc, "/data/table/data/row/@status_code"));

            if (statusCode != needStatusCode) {
                throw new BGMessageExceptionTransparent("Смена статуса невозможна. К процессу привязаны проблемы биллинга в препятствующих смене статусах.");
            }
        }
    }
}