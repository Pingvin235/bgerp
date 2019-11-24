package ru.bgcrm.event.listener;

import java.util.List;

import ru.bgcrm.cache.ProcessTypeCache;
import ru.bgcrm.dao.process.ProcessLinkDAO;
import ru.bgcrm.event.EventProcessor;
import ru.bgcrm.event.process.ProcessChangedEvent;
import ru.bgcrm.model.BGMessageException;
import ru.bgcrm.model.process.Process;
import ru.bgcrm.model.process.ProcessType;
import ru.bgcrm.util.sql.ConnectionSet;

public class ProcessClosingListener {
    private static final String DIRECTION_DOWN = "down";
    private static final String DIRECTION_UP = "up";

    public ProcessClosingListener() {
        EventProcessor.subscribe((e, conSet) -> closingListener(e, conSet), ProcessChangedEvent.class);
    }

    private void closingListener(ProcessChangedEvent e, ConnectionSet connectionSet) throws Exception {
        if (!e.isStatus()) return;

        Process process = e.getProcess();
        if (process.getCloseTime() != null) {
            ProcessType type = ProcessTypeCache.getProcessType(process.getTypeId());
            String checkDirection = type.getProperties().getConfigMap().get("processDependCloseCheckDirection", DIRECTION_DOWN);

            List<Process> result = null;
            if (DIRECTION_DOWN.equals(checkDirection)) {
                result = new ProcessLinkDAO(connectionSet.getConnection(), e.getUser()).getLinkProcessList(process.getId(), Process.LINK_TYPE_DEPEND, false, null);
                for (Process link : result)
                    if (link.getCloseTime() == null)
                        throw new BGMessageException("Есть незакрытые зависящие процессы");
            } else if (DIRECTION_UP.equals(checkDirection)) {
                result = new ProcessLinkDAO(connectionSet.getConnection(), e.getUser()).getLinkedProcessList(process.getId(), Process.LINK_TYPE_DEPEND, false, null);
                for (Process linked : result)
                    if (linked.getCloseTime() == null)
                        throw new BGMessageException("Есть незакрытые процессы, от которых зависит данный");
            }
        }
    }
}
