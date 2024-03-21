package org.bgerp.action.open;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.bgerp.action.BaseAction;
import org.bgerp.app.cfg.ConfigMap;
import org.bgerp.app.exception.BGException;
import org.bgerp.app.exception.BGSecurityException;
import org.bgerp.cache.ProcessQueueCache;
import org.bgerp.dao.process.ProcessQueueDAO;
import org.bgerp.model.Pageable;

import ru.bgcrm.model.Page;
import ru.bgcrm.model.process.queue.Queue;
import ru.bgcrm.servlet.ActionServlet.Action;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.sql.ConnectionSet;

@Action(path = "/open/process/queue")
public class ProcessQueueAction extends BaseAction {
    private static final String PATH_JSP =  PATH_JSP_OPEN + "/process/queue";

    /**
     * Configuration for open process queues.
     */
    public static class Config extends org.bgerp.app.cfg.Config {
        private final Map<String, Queue> openQueues;

        protected Config(ConfigMap setup) throws Exception {
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

        new ProcessQueueDAO(conSet.getSlaveConnection()).searchProcess(searchResult, null, queue, form);

        form.setRequestAttribute("columnList", queue.getMediaColumnList(Queue.MEDIA_HTML_OPEN));
        queue.replaceRowsForMedia(form, Queue.MEDIA_HTML_OPEN, searchResult.getList());
        form.setRequestAttribute("queue", queue);

        return html(conSet, null, PATH_JSP + "/show.jsp");
    }
}