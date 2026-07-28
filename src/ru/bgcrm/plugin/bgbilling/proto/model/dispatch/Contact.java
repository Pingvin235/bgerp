package ru.bgcrm.plugin.bgbilling.proto.model.dispatch;

import ru.bgcrm.util.Utils;

/**
 * Contact for sending messages
 */
public class Contact extends org.bgerp.model.base.Id {

    private String value;
    private int contactTypeId;
    private int contractId;
    private String defaultValue;

    /**
     * @param value the contact value
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * @return the contact value
     */
    public String getValue() {
        return value;
    }

    /**
     * @return the contact value to send
     */
    public String getValueForSend() {
        String result = value;
        if (Utils.isEmptyString(value) && Utils.notEmptyString(defaultValue)) {
            result = defaultValue;
        }
        return result;
    }

    /**
     * @return the contact value to display
     */
    public String getValueForView() {
        return toString();
    }

    /**
     * @param contactTypeId the contact type ID
     */
    public void setContactTypeId(int contactTypeId) {
        this.contactTypeId = contactTypeId;
    }

    /**
     * @return the contact type ID
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
     * @return the default contact value
     */
    public String getDefaultValue() {
        return defaultValue;
    }

    /**
     * @param defaultValue the default contact value
     */
    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

}
