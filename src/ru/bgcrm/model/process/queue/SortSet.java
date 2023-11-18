package ru.bgcrm.model.process.queue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.bgerp.util.Log;

import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.Utils;

/**
 * Queue sort options. Sorting is defined in list of comboboxes.
 * @author Shamil Vakhitov
 */
public class SortSet {
    private static final Log log = Log.getLog();

    private int comboCount;
    private final List<SortMode> modeList = new ArrayList<>();
    private final Map<Integer, Integer> defaultSortValues = new HashMap<>();
    private final SortedMap<Integer, Integer> sortValues = new TreeMap<>();

    /**
     * Quantity of sequential comboboxes, available for sorting.
     * @return
     */
    public int getComboCount() {
        return comboCount;
    }

    public void setComboCount(int comboCount) {
        this.comboCount = comboCount;
    }

    /**
     * List of modes, available for choose in each of combos.
     * @return
     */
    public List<SortMode> getModeList() {
        return modeList;
    }

    public void addMode(SortMode mode) {
        this.modeList.add(mode);
    }

    /**
     * Strictly defined sort modes for each of combo.
     * Key - 0 based combo id, value - ID of a sort mode.
     * @return
     */
    public SortedMap<Integer, Integer> getSortValues() {
        return sortValues;
    }

    public void setSortValue(int comboNum, int value) {
        sortValues.put(comboNum, value);
    }

    /**
     * Default sort modes for each of combo, if nope of {@link #getSortValues()} and HTTP request options set.
     * @return
     */
    public Map<Integer, Integer> getDefaultSortValues() {
        return defaultSortValues;
    }

    public void setDefaultSortValue(int comboNum, int value) {
        defaultSortValues.put(comboNum, value);
    }

    /**
     * Comma separated SQL column numbers, using for sorting.
     * @param form request's parameters.
     * @return
     */
    public String getOrders(DynActionForm form) {
        String orders = getOrders(sortValues);
        if (Utils.isBlankString(orders) && form != null )
            orders = Utils.toString(form.getParamValuesListStr("sort", "0"));
        if (Utils.isBlankString(orders))
            orders = getOrders(defaultSortValues);
        return orders;
    }

    private String getOrders(Map<Integer, Integer> sortValues) {
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