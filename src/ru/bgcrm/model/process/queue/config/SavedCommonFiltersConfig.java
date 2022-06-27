package ru.bgcrm.model.process.queue.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bgerp.util.Log;

import ru.bgcrm.util.Utils;

public class SavedCommonFiltersConfig {
    private Map<Integer, List<SavedFilter>> queueSavedCommonFilterSetsMap = new HashMap<Integer, List<SavedFilter>>();
    private Map<Integer, SavedFilter> savedCommonFilterSetMap = new HashMap<Integer, SavedFilter>();
    public static final String QUEUE_SAVED_COMMON_FILTER_SET_PREFIX = "queueSavedCommonFilterSet.";
    private int lastId;
    private Log log = Log.getLog();

    public SavedCommonFiltersConfig(ArrayList<SavedFilter> commonFilters) {

        for (SavedFilter commonFilter : commonFilters) {
            int id = commonFilter.getId();
            SavedFilter filter = commonFilter;
            if (id <= 0 || filter.getQueueId() <= 0 || Utils.isBlankString(filter.getUrl()) || Utils.isBlankString(filter.getTitle())) {
                continue;
            }

            getSetList(filter.getQueueId()).add(filter);

            savedCommonFilterSetMap.put(id, filter);
            lastId = id;
        }
    }

    private List<SavedFilter> getSetList(int queueId) {
        List<SavedFilter> setList = queueSavedCommonFilterSetsMap.get(queueId);
        if (setList == null) {
            queueSavedCommonFilterSetsMap.put(queueId, setList = new ArrayList<SavedFilter>());
        }
        return setList;
    }

    public int addSavedCommonFilter(int queueId, String title, String url) {
        getSetList(queueId).add(new SavedFilter(queueId, -1, title, url));
        return lastId;
    }

    public void deleteSavedCommonFilter(int queueId, String title, String url) {
        SavedFilter filter = null;
        for (SavedFilter filterSet : getSetList(queueId)) {
            log.debug(filterSet);
            if (filterSet.getTitle().equals(title) && filterSet.getUrl().equals(url)) {
                filter = filterSet;
                break;
            }
        }
        if (filter != null) {
            getSetList(queueId).remove(filter);
        }
    }

    public List<SavedFilter> getQueueSavedCommonFilterSetsMap(Integer queueId) {
        return queueSavedCommonFilterSetsMap.get(queueId);
    }

    public Map<Integer, List<SavedFilter>> getQueueSavedCommonFilterSetsMap() {
        return queueSavedCommonFilterSetsMap;
    }
}
