package ru.bgcrm.plugin.bgbilling.proto.model.card;

public class CardActivationData {
    private String number;
    private String status;
    private String activationDate;
    private String summa;

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getSumma() {
        return summa;
    }

    public void setSumma(String summa) {
        this.summa = summa;
    }

    public String getId() {
        return number;
    }

    public void setId(String id) {
        this.number = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getActivationDate() {
        return activationDate;
    }

    public void setActivationDate(String activationDate) {
        this.activationDate = activationDate;
    }
}
