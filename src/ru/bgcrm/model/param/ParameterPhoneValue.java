package ru.bgcrm.model.param;

import java.util.ArrayList;
import java.util.List;

import org.bgerp.util.Log;

public class ParameterPhoneValue {
    private static final Log log = Log.getLog();

    private List<ParameterPhoneValueItem> itemList;

    public ParameterPhoneValue() {
        itemList = new ArrayList<>();
    }

    public ParameterPhoneValue(List<ParameterPhoneValueItem> items) {
        itemList = new ArrayList<>(items);
    }

    /**
     * @return number list.
     */
    public List<ParameterPhoneValueItem> getItemList() {
        return itemList;
    }

    /**
     * Sets number list.
     * @param itemList
     */
    public void setItemList(List<ParameterPhoneValueItem> itemList) {
        this.itemList = itemList;
    }

    /**
     * Adds numbers to the existing list with duplicates protection.
     * @param itemList
     */
    public void addItems(List<ParameterPhoneValueItem> itemList) {
        if (this.itemList == null) {
            this.itemList = new ArrayList<ParameterPhoneValueItem>();
        }

        for (ParameterPhoneValueItem item : itemList) {
            // duplicates protection
            if (this.itemList.contains(item)) {
                continue;
            }
            this.itemList.addAll(itemList);
        }
    }

    /**
     * Adds a single number.
     * @param item
     */
    public void addItem(ParameterPhoneValueItem item) {
        addItems(List.of(item));
    }

    /**
     * Use {@link #addItem(ParameterPhoneValueItem)}.
     */
    @Deprecated
    public void add(ParameterPhoneValue parameterPhoneValue) {
        if (parameterPhoneValue != null) {
            addItems(parameterPhoneValue.getItemList());
        }
    }

    /**
     * Use {@link #toString()}
     */
    @Deprecated
    public String getValue() {
        log.warndMethod("getValue", "toString");
        return toString();
    }

    /**
     * @return formatted comma separated string with phone numbers.
     */
    @Override
    public String toString() {
        StringBuffer result = new StringBuffer();

        for (ParameterPhoneValueItem item : itemList) {
            if (!result.isEmpty())
                result.append(", ");
            result.append(item.toString());
        }

        return result.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((itemList == null) ? 0 : itemList.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ParameterPhoneValue other = (ParameterPhoneValue) obj;
        if (itemList == null) {
            if (other.itemList != null)
                return false;
        } else if (!itemList.equals(other.itemList))
            return false;
        return true;
    }
}
