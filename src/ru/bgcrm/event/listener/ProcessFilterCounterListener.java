package ru.bgcrm.event.listener;

import java.util.HashMap;
import java.util.Map;

import org.bgerp.app.cfg.Preferences;
import org.bgerp.cache.ProcessQueueCache;
import org.bgerp.dao.process.FilterEntryCounter;
import org.bgerp.util.Log;

import ru.bgcrm.event.EventProcessor;
import ru.bgcrm.event.GetPoolTasksEvent;
import ru.bgcrm.event.client.FilterCounterEvent;
import ru.bgcrm.model.process.queue.Queue;
import ru.bgcrm.model.process.queue.config.SavedFiltersConfig;
import ru.bgcrm.model.process.queue.config.SavedFiltersConfig.SavedFilterSet;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.sql.ConnectionSet;

public class ProcessFilterCounterListener {
    private static final Log log = Log.getLog();

    public ProcessFilterCounterListener() {
        EventProcessor.subscribe((e, conSet) -> processListener(e.getForm(), conSet), GetPoolTasksEvent.class);
    }

    private void processListener(DynActionForm form, ConnectionSet conSet) {
        Preferences personalizationMap = form.getUser().getPersonalizationMap();

        // впоследствии они вернуться в это же место кода в processCounterUrls параметре запроса
        SavedFiltersConfig config = personalizationMap.getConfig(SavedFiltersConfig.class);

        // сохранённые в конфигурации счётчики для отображения справа сверху
        Map<Integer, SavedFilterSet> topFilters = config.getTopFilters();

        // значения счётчиков
        // id очереди, id счётчика (кнопки), значение
        HashMap<Integer, HashMap<Integer, Integer>> valuesToReturn = new HashMap<>();

        String urlsParam = form.getParam("processCounterUrls");
        if (Utils.notBlankString(urlsParam)) {
            String urls[] = urlsParam.split(",");
            for (String url : urls) {
                final String[] tokens = url.split(":");

                Integer buttonId = Integer.valueOf(tokens[0]);
                Integer queueId = Integer.valueOf(tokens[1]);
                url = tokens[2];

                Queue queue = ProcessQueueCache.getQueue(queueId, form.getUser());
                if (queue == null) {
                    log.warn("Not found process queue with ID: {}, stored in counters for user with ID: {}.", queueId, form.getUserId());
                    continue;
                }

                int count = 0;
                try {
                    count = FilterEntryCounter.getInstance().parseUrlAndGetCount(queue, url, form.getUser());
                } catch (Exception e) {
                    log.error(e);
                }

                HashMap<Integer, Integer> btnIdAndEntryCount = valuesToReturn.get(queueId);
                if (btnIdAndEntryCount == null) {
                    valuesToReturn.put(queueId, btnIdAndEntryCount = new HashMap<>());
                }

                btnIdAndEntryCount.put(buttonId, count);
            }
        }

        FilterCounterEvent filterCountEvent = new FilterCounterEvent();
        filterCountEvent.setCount(valuesToReturn);
        filterCountEvent.setFilters(topFilters);
        form.getResponse().addEvent(filterCountEvent);
    }
}
