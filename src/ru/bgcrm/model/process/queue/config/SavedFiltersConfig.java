package ru.bgcrm.model.process.queue.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bgerp.app.cfg.Config;
import org.bgerp.app.cfg.ConfigMap;
import org.bgerp.app.cfg.Preferences;
import org.bgerp.model.base.IdTitle;

import ru.bgcrm.util.Utils;

/**
 * Сохранённые фильтры пользователя.
 */
public class SavedFiltersConfig extends Config {
    // ключ - ID очереди
    private Map<Integer, List<SavedFilterSet>> queueSavedFilterSetsMap = new HashMap<>();
    // ключ - ID сохранённого набора
    private Map<Integer, SavedFilterSet> savedFilterSetMap = new HashMap<>();
    // максимальный ID
    private int maxId;

    private static final String QUEUE_SAVED_FILTER_SET_PREFIX = "queueSavedFilterSet.";
    public static final String QUEUE_CURRENT_SAVED_FILTER_SET_PREFIX = "queueCurrentSavedFilterSet.";
    private static final String QUEUE_SAVED_FILTER_SET_ORDER = "queueSavedFilterSetOrder.";

    public SavedFiltersConfig(ConfigMap config) {
        super(null);

        for (Map.Entry<Integer, ConfigMap> me : config.subIndexed(QUEUE_SAVED_FILTER_SET_PREFIX).entrySet()) {
            int id = me.getKey();

            SavedFilterSet set = new SavedFilterSet(id, me.getValue());
            if (id <= 0 || set.getQueueId() <= 0 || Utils.isBlankString(set.getUrl()) || Utils.isBlankString(set.getTitle())) {
                continue;
            }

            getQueueSetList(set.getQueueId()).add(set);

            savedFilterSetMap.put(id, set);

            maxId = id;
        }

        // сортировка
        for (Map.Entry<Integer, List<SavedFilterSet>> queueSets : queueSavedFilterSetsMap.entrySet()) {
            int queueId = queueSets.getKey();
            List<SavedFilterSet> sets = queueSets.getValue();

            List<Integer> order = Utils.toIntegerList(config.get(QUEUE_SAVED_FILTER_SET_ORDER + queueId));
            sets.sort((s1, s2) -> order.indexOf(s1.getId()) - order.indexOf(s2.getId()));
        }
    }

    public Map<Integer, SavedFilterSet> getTopFilters() {
        Map<Integer, SavedFilterSet> result = new HashMap<>();
        savedFilterSetMap.values().stream().filter(SavedFilterSet::isStatusCounterOnPanel).forEach(f -> result.put(f.getId(), f));
        return result;
    }

    public Map<Integer, List<SavedFilterSet>> getQueueSavedFilterSetsMap() {
        return queueSavedFilterSetsMap;
    }

    public Map<Integer, SavedFilterSet> getSavedFilterSetMap() {
        return savedFilterSetMap;
    }

    private List<SavedFilterSet> getQueueSetList(int queueId) {
        List<SavedFilterSet> setList = queueSavedFilterSetsMap.get(queueId);
        if (setList == null) {
            queueSavedFilterSetsMap.put(queueId, setList = new ArrayList<>());
        }
        return setList;
    }

    public void reorderSavedFilterSets(int queueId, List<Integer> order) {
        List<SavedFilterSet> sets = queueSavedFilterSetsMap.get(queueId);
        if (sets != null) {
            sets.sort((s1, s2) -> order.indexOf(s1.getId()) - order.indexOf(s2.getId()));
        }
    }

    public void removeSavedFilterSet(int queueId, int id) {
        List<SavedFilterSet> setList = queueSavedFilterSetsMap.get(queueId);
        if (setList == null) {
            return;
        }

        SavedFilterSet savedSet = null;
        for (SavedFilterSet set : setList) {
            if (set.getId() == id) {
                savedSet = set;
                break;
            }
        }

        if (savedSet != null) {
            setList.remove(savedSet);
        }
    }

    public int addSavedFilterSet(int queueId, String title, String url) {
        getQueueSetList(queueId).add(new SavedFilterSet(queueId, ++maxId, title, url));
        return maxId;
    }

    public void setRareStatus(int queueId, int filterId, Boolean value) {
        List<SavedFilterSet> filterList = getQueueSetList(queueId);
        for (SavedFilterSet filter : filterList) {
            if (filter.getId() == filterId) {
                filter.setRare(value);
            }
        }
    }

    public void setStatusCounterOnPanel(int queueId, int filterId, String color, Boolean value, String title, String queueName) {
        List<SavedFilterSet> filterList = getQueueSetList(queueId);
        for (SavedFilterSet filter : filterList) {
            if (filter.getId() == filterId) {
                filter.setStatusCounterOnPanel(value);
                if (value) {
                    filter.setColor(color);
                    filter.setTitle(title);
                    filter.setQueueName(queueName);
                }
            }
        }
    }

    public void updateConfig(Preferences userConfig) {
        userConfig.removeSub(QUEUE_SAVED_FILTER_SET_PREFIX);
        for (Map.Entry<Integer, List<SavedFilterSet>> me : queueSavedFilterSetsMap.entrySet()) {
            int queueId = me.getKey();
            List<SavedFilterSet> setList = me.getValue();

            userConfig.put(QUEUE_SAVED_FILTER_SET_ORDER + queueId, Utils.getObjectIds(setList));
            for (SavedFilterSet set : setList) {
                final String setPrefix = QUEUE_SAVED_FILTER_SET_PREFIX + set.getId();

                userConfig.put(setPrefix + ".queueId", String.valueOf(set.getQueueId()));
                userConfig.put(setPrefix + ".title", set.getTitle());
                userConfig.put(setPrefix + ".queueName", set.getQueueName());
                userConfig.put(setPrefix + ".url", set.getUrl());
                userConfig.put(setPrefix + ".rare", String.valueOf(set.isRare()));
                userConfig.put(setPrefix + ".statusCounterOfPanel", String.valueOf(set.isStatusCounterOnPanel()));
                userConfig.put(setPrefix + ".color", set.getColor());
            }
        }
    }

    /**
     * Сохранённый фильтр в очереди процессов.
     */
    public static class SavedFilterSet extends IdTitle {
        private final int queueId;
        private final String url;
        private boolean rare;
        private boolean statusCounterOnPanel;
        private String color = "";
        private String queueName = "";

        private SavedFilterSet(int id, ConfigMap config) {
            this.id = id;
            this.queueId = config.getInt("queueId", 0);
            this.queueName = config.get("queueName") == null ? "" : config.get("queueName");
            this.title = config.get("title");
            this.url = config.get("url");
            this.rare = config.getBoolean("rare", false);
            this.statusCounterOnPanel = config.getBoolean("statusCounterOfPanel", false);
            this.color = config.get("color") == null ? "" : config.get("color");
        }

        private SavedFilterSet(int queueId, int id, String title, String url) {
            super(id, title);
            this.queueId = queueId;
            this.title = title;
            this.url = url;
        }

        public int getQueueId() {
            return queueId;
        }

        public String getUrl() {
            return url;
        }

        public void setRare(boolean value) {
            rare = value;
        }

        public boolean isRare() {
            return rare;
        }

        public void setStatusCounterOnPanel(boolean value) {
            this.statusCounterOnPanel = value;
        }

        public Boolean isStatusCounterOnPanel() {
            return this.statusCounterOnPanel;
        }

        public void setColor(String color) {
            this.color = color;
        }

        public String getColor() {
            return this.color;
        }

        public void setQueueName(String queueName) {
            this.queueName = queueName;
        }

        public String getQueueName() {
            return this.queueName;
        }
    }
}