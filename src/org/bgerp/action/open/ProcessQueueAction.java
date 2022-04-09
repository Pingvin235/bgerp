package org.bgerp.action.open;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.bgerp.model.Pageable;

import ru.bgcrm.cache.ProcessQueueCache;
import ru.bgcrm.dao.process.ProcessDAO;
import ru.bgcrm.model.BGException;
import ru.bgcrm.model.BGSecurityException;
import ru.bgcrm.model.Page;
import ru.bgcrm.model.process.Queue;
import ru.bgcrm.servlet.ActionServlet.Action;
import ru.bgcrm.struts.action.BaseAction;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.ParameterMap;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.sql.ConnectionSet;

@Action(path = "/open/process/queue")
public class ProcessQueueAction extends BaseAction {
    private static final String PATH_JSP =  PATH_JSP_OPEN + "/process/queue";

    /**
     * Configuration for open process queues.
     */
    public static class Config extends ru.bgcrm.util.Config {
        private final Map<String, Queue> openQueues;

        protected Config(ParameterMap setup) throws Exception {
            super(setup);
            this.openQueues = loadOpenQueues();
        }

        private Map<String, Queue> loadOpenQueues() throws Exception {
            Map<String, Queue> result = new HashMap<>();

            for (Queue queue : ProcessQueueCache.getQueueList()) {
                var openUrl = queue.getOpenUrl();
                if (Utils.notBlankString(openUrl))
                    result.put(openUrl, queue);
            }

            return Collections.unmodifiableMap(result);
        }

        public boolean isOpen(int queueId) {
            return openQueues.values().stream()
                .filter(q -> q.getId() == queueId)
                .findFirst().isPresent();
        }

        public Integer getOpenQueueId(String openUrl) {
            var queue = openQueues.get(openUrl);
            return queue == null ? null : queue.getId();
        }
    }

    public ActionForward show(ActionMapping mapping, DynActionForm form, ConnectionSet conSet) throws Exception {
        int queueId = form.getId();

        var config = setup.getConfig(Config.class);
        if (config == null || !config.isOpen(queueId))
            throw new BGSecurityException("Not opened queue was directly requested", form);

        Queue queue = ProcessQueueCache.getQueue(queueId);
        if (queue == null)
            throw new BGException("Queue not found: " + queueId);

        Pageable<Object[]> searchResult = new Pageable<Object[]>(form);
        searchResult.getPage().setPageIndex(Page.PAGE_INDEX_NO_PAGING);

        ProcessDAO processDAO = new ProcessDAO(conSet.getSlaveConnection());
        processDAO.searchProcess(searchResult, null, queue, form);

        form.setRequestAttribute("columnList", queue.getMediaColumnList(Queue.MEDIA_HTML_OPEN));
        queue.processDataForMedia(form, Queue.MEDIA_HTML_OPEN, searchResult.getList());
        form.setRequestAttribute("queue", queue);

        return html(conSet, null, PATH_JSP + "/show.jsp");
    }
}