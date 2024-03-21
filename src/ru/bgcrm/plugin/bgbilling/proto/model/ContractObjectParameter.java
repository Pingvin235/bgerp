package ru.bgcrm.plugin.bgbilling.proto.model;

public class ContractObjectParameter {
    private int parameterId = -1;
    private int typeId = -1;
    private String title;
    private String value;

    private String history;

    public ContractObjectParameter() {
    }

    public ContractObjectParameter(int parameterId, int typeId, String paramTitle, String paramValue, String history) {
        this.parameterId = parameterId;
        this.typeId = typeId;
        this.title = paramTitle;
        this.value = paramValue;
        this.history = history;
    }

    public int getParameterId() {
        return parameterId;
    }

    public void setParameterId(int parameterId) {
        this.parameterId = parameterId;
    }

    public int getTypeId() {
        return typeId;
    }

    public void setTypeId(int typeId) {
        this.typeId = typeId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getHistory() {
        return history;
    }

    public void setHistory(String history) {
        this.history = history;
    }
}
