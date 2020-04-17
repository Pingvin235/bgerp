package ru.bgcrm.model.param;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ParameterPhoneValue {
    private List<ParameterPhoneValueItem> itemList;

    public ParameterPhoneValue() {
        itemList = new ArrayList<>();
    }

    public ParameterPhoneValue(List<ParameterPhoneValueItem> phoneValues) {
        itemList = new ArrayList<>(phoneValues);
    }

    /**
     * Возвращает форматированную строку с телефонными номерами.
     * @return
     */
    public String getValue() {
        return ParameterPhoneValueItem.getPhones(itemList);
    }

    /**
     * Возвращает список номеров в параметре.
     * @return
     */
    public List<ParameterPhoneValueItem> getItemList() {
        return itemList;
    }

    /**
     * Устанавливает список номеров в параметр.
     * @param itemList
     */
    public void setItemList(List<ParameterPhoneValueItem> itemList) {
        this.itemList = itemList;
    }

    /**
     * Добавляет список номеров в параметр с защитой от повторных. 
     * @param itemList
     */
    public void addItems(List<ParameterPhoneValueItem> itemList) {
        if (this.itemList == null) {
            this.itemList = new ArrayList<ParameterPhoneValueItem>();
        }

        for (ParameterPhoneValueItem item : itemList) {
            // защита от повторов
            if (this.itemList.contains(item)) {
                continue;
            }
            this.itemList.addAll(itemList);
        }
    }

    /**
     * Добавляет номер в параметр.
     * @param item
     */
    public void addItem(ParameterPhoneValueItem item) {
        addItems(Collections.singletonList(item));
    }

    /**
     * Добавляет все номера в параметр.
     * @param parameterPhoneValue
     */
    public void add(ParameterPhoneValue parameterPhoneValue) {
        if (parameterPhoneValue != null) {
            addItems(parameterPhoneValue.getItemList());
        }
    }

    @Override
    public String toString() {
        return getValue();
    }

}
