package ru.bgcrm.event.listener;

import ru.bgcrm.dao.process.ProcessLinkDAO;
import ru.bgcrm.event.EventProcessor;
import ru.bgcrm.event.process.ProcessChangedEvent;
import ru.bgcrm.model.BGException;
import ru.bgcrm.model.BGMessageException;
import ru.bgcrm.model.Pair;
import ru.bgcrm.model.SearchResult;
import ru.bgcrm.model.process.Process;
import ru.bgcrm.util.sql.ConnectionSet;

public class ProcessClosingListener {
    public ProcessClosingListener() {
        EventProcessor.subscribe(new EventListener<ProcessChangedEvent>() {
            @Override
            public void notify(ProcessChangedEvent e, ConnectionSet connectionSet) throws BGException {
                closingListener(e, connectionSet);
            }
        }, ProcessChangedEvent.class);
    }

    private void closingListener(ProcessChangedEvent e, ConnectionSet connectionSet) throws BGException {
        if (!e.isStatus()) {
            return;
        }

        Process process = e.getProcess();

        if (process.getCloseTime() != null) {
            SearchResult<Pair<String, Process>> result = new SearchResult<Pair<String, Process>>();
            new ProcessLinkDAO(connectionSet.getConnection(), e.getUser()).searchLinkProcessList(result, process.getId());

            for (Pair<String, Process> linkProcess : result.getList()) {
                if (Process.LINK_TYPE_DEPEND.equals(linkProcess.getFirst()) && linkProcess.getSecond().getCloseTime() == null) {
                    throw new BGMessageException("Есть незакрытые зависящие процессы");
                }
            }
        }
    }
}
