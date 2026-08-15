package org.bgerp.cache;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bgerp.app.cfg.Setup;
import org.bgerp.util.Log;

import ru.bgcrm.dao.process.QueueDAO;
import ru.bgcrm.model.process.queue.Queue;
import ru.bgcrm.model.user.User;

public class ProcessQueueCache extends Cache<ProcessQueueCache> {
    private static final Log log = Log.getLog();

    private static final CacheHolder<ProcessQueueCache> HOLDER = new CacheHolder<>(new ProcessQueueCache());

    public static Queue getQueue(int id, User user) {
        Queue result = HOLDER.getInstance().queueMap.get(id);
        // filter by allowed process queues
        if (result != null && user != null && !user.getQueueIds().contains(result.getId())) {
            result = null;
        }
        return result;
    }

    public static Queue getQueue(int id) {
        return HOLDER.getInstance().queueMap.get(id);
    }

    public static Map<Integer, Queue> getQueueMap() {
        return HOLDER.getInstance().queueMap;
    }

    public static List<Queue> getQueueList() {
        return HOLDER.getInstance().queueList;
    }

    public static List<Queue> getUserQueueList(User user) {
        List<Queue> result = new ArrayList<>();

        for (Queue queue : HOLDER.getInstance().queueList) {
            if (user.getQueueIds().contains(queue.getId())) {
                result.add(queue);
            }
        }

        return result;
    }

    public static void flush(Connection con) {
        HOLDER.flush(con);
    }

    // end of static part

    private Map<Integer, Queue> queueMap;
    private List<Queue> queueList;

    @Override
    protected ProcessQueueCache newInstance() {
        ProcessQueueCache result = new ProcessQueueCache();

        try (var con = Setup.getSetup().getDBConnectionFromPool()) {
            result.queueMap = new HashMap<>();
            result.queueList = new ArrayList<>();

            QueueDAO queueDAO = new QueueDAO(con);
            for (Queue queue : queueDAO.getQueueList()) {
                // select process types explicitly specified in the queue's configuration
                queue.setProcessTypeIds(queueDAO.getQueueProcessTypeIds(queue.getId()));

                log.debug("Queue {} selected process types: {}", queue.getId(), queue.getProcessTypeIds());

                // select child types of the linked process types
                queue.setProcessTypeIds(ProcessTypeCache.getTypeTreeRoot().getSelectedChildIds(queue.getProcessTypeIds()));

                log.debug("Queue {} process types with children: {}", queue.getId(), queue.getProcessTypeIds());

                queue.extractFiltersAndSorts();

                result.queueMap.put(queue.getId(), queue);
                result.queueList.add(queue);
            }
        } catch (Exception e) {
            log.error(e);
        }

        return result;
    }
}
