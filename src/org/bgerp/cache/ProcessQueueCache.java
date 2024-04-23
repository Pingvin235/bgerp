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
    private static Log log = Log.getLog();

    private static CacheHolder<ProcessQueueCache> holder = new CacheHolder<>(new ProcessQueueCache());

    public static Queue getQueue(int id, User user) {
        Queue result = holder.getInstance().queueMap.get(id);
        // фильтр по разрешённым очередям процессов
        if (result != null && user != null && !user.getQueueIds().contains(result.getId())) {
            result = null;
        }
        return result;
    }

    public static Queue getQueue(int id) {
        return holder.getInstance().queueMap.get(id);
    }

    public static Map<Integer, Queue> getQueueMap() {
        return holder.getInstance().queueMap;
    }

    public static List<Queue> getQueueList() {
        return holder.getInstance().queueList;
    }

    public static List<Queue> getUserQueueList(User user) {
        List<Queue> result = new ArrayList<>();

        for (Queue queue : holder.getInstance().queueList) {
            if (user.getQueueIds().contains(queue.getId())) {
                result.add(queue);
            }
        }

        return result;
    }

    public static void flush(Connection con) {
        holder.flush(con);
    }

    // конец статической части

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
                // выбор явно указанных в конфигурации очереди типов процессов
                queue.setProcessTypeIds(queueDAO.getQueueProcessTypeIds(queue.getId()));

                log.debug("Queue {} selected process types: {}", queue.getId(), queue.getProcessTypeIds());

                // выбор дочерних типов привязанных процессов
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
