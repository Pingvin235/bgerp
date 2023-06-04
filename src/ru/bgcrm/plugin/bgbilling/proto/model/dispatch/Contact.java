package ru.bgcrm.plugin.bgbilling.proto.model.dispatch;

import ru.bgcrm.util.Utils;

/**
 * Контакт для отправки сообщений
 */
public class Contact extends org.bgerp.model.base.Id {

    private String value;
    private int contactTypeId;
    private int contractId;
    private String defaultValue;

    /**
     * Устанавливает значение контакта
     * @param value
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * Возвращает значение контакта
     * @return
     */
    public String getValue() {
        return value;
    }

    /**
     * Возвращает значение контакта для отправки
     * @return
     */
    public String getValueForSend() {
        String result = value;
        if (Utils.isEmptyString(value) && Utils.notEmptyString(defaultValue)) {
            result = defaultValue;
        }
        return result;
    }

    /**
     * Возвращает значение контакта для отправки просмотра
     *
     * @return
     */
    public String getValueForView() {
        return toString();
    }

    /**
     * Устанавливает тип для данного контакта
     * @param contactTypeId
     */
    public void setContactTypeId(int contactTypeId) {
        this.contactTypeId = contactTypeId;
    }

    /**
     * Возвращает тип данного контакта
     * @return
     */
    public int getContactTypeId() {
        return contactTypeId;
    }

    public int getContractId() {
        return contractId;
    }

    public void setContractId(int contractId) {
        this.contractId = contractId;
    }

    @Override
    public String toString() {
        String result = value;
        if (ru.bgcrm.util.Utils.isEmptyString(value) && Utils.notEmptyString(defaultValue)) {
            result = "[" + defaultValue + "]";
        }
        return result;
    }

    /**
     * Возвращает значение контакта по умолчанию
     *
     * @return
     */
    public String getDefaultValue() {
        return defaultValue;
    }

    /**
     * Устанавливает значение контакта по умолчанию
     *
     * @param defaultValue
     */
    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

}