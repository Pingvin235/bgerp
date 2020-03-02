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
            checkLinkedProcesses(connectionSet, process, Process.LINK_TYPE_DEPEND, DIRECTION_UP);
            checkLinkedProcesses(connectionSet, process, Process.LINK_TYPE_MADE, DIRECTION_DOWN);
        }
    }

    private void checkLinkedProcesses(ConnectionSet connectionSet, Process process, String linkType, String defaultDirection) throws Exception {
        ProcessType type = ProcessTypeCache.getProcessType(process.getTypeId());
        String checkDirection = type.getProperties().getConfigMap().get("process.close.check." + linkType, defaultDirection);

        List<Process> result = null;
        if (DIRECTION_DOWN.equals(checkDirection)) {
            result = new ProcessLinkDAO(connectionSet.getConnection()).getLinkProcessList(process.getId(), linkType, false, null);
            for (Process link : result)
                if (link.getCloseTime() == null)
                    throw new BGMessageException("Есть незакрытые привязанные процессы типа: %s", linkType);
        } else if (DIRECTION_UP.equals(checkDirection)) {
            result = new ProcessLinkDAO(connectionSet.getConnection()).getLinkedProcessList(process.getId(), linkType, false, null);
            for (Process linked : result)
                if (linked.getCloseTime() == null)
                    throw new BGMessageException("Есть незакрытые процессы, к которым привязан данный с типом: %s", linkType);
        }
    }
}
