package ru.bgcrm.model.process.queue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import ru.bgcrm.util.Utils;
import ru.bgerp.util.Log;

public class SortSet {
    private static final Log log = Log.getLog();

    private int comboCount;
    private List<SortMode> modeList = new ArrayList<SortMode>();
    private Map<Integer, Integer> defaultSortValues = new HashMap<Integer, Integer>();
    private SortedMap<Integer, Integer> sortValues = new TreeMap<Integer, Integer>();

    public int getComboCount() {
        return comboCount;
    }

    public void setComboCount(int comboCount) {
        this.comboCount = comboCount;
    }

    public List<SortMode> getModeList() {
        return modeList;
    }

    public void addMode(SortMode mode) {
        this.modeList.add(mode);
    }

    public void setDefaultSortValue(int comboNum, int value) {
        defaultSortValues.put(comboNum, value);
    }

    public void setSortValue(int comboNum, int value) {
        sortValues.put(comboNum, value);
    }

    public SortedMap<Integer, Integer> getSortValues() {
        return sortValues;
    }

    public Map<Integer, Integer> getDefaultSortValues() {
        return defaultSortValues;
    }

    /**
     * Returns strictly defined sort orders.
     * @return
     */
    public String getOrders() {
        var result = new StringBuilder("");
        for (Integer value : sortValues.values()) {
            int pos = value - 1;
            if (pos < 0 || pos >= modeList.size()) {
                log.error("Incorrect sort value in queue: " + value);
                continue;
            }
            Utils.addCommaSeparated(result, modeList.get(pos).getOrderExpression());
        }
        return result.toString();
    }

}