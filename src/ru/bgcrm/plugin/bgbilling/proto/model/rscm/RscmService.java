package ru.bgcrm.plugin.bgbilling.proto.model.rscm;

import java.util.Date;

import org.bgerp.model.base.Id;

public class RscmService extends Id {
    private int contractId;
    private int serviceId;
    private String serviceTitle;
    private int objectId;
    private String objectTitle;
    private Date date;
    private int amount;
    private int amountUp;
    private int amountDown;
    private String unit;
    private String comment;

    public int getContractId() {
        return contractId;
    }

    public void setContractId(int contractId) {
        this.contractId = contractId;
    }

    public int getServiceId() {
        return serviceId;
    }

    public void setServiceId(int value) {
        this.serviceId = value;
    }

    public String getServiceTitle() {
        return serviceTitle;
    }

    public void setServiceTitle(String serviceTitle) {
        this.serviceTitle = serviceTitle;
    }

    public int getObjectId() {
        return objectId;
    }

    public void setObjectId(int objectId) {
        this.objectId = objectId;
    }

    public String getObjectTitle() {
        return objectTitle;
    }

    public void setObjectTitle(String objectTitle) {
        this.objectTitle = objectTitle;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public int getAmount() {
        return amount > 0 ? amount : amountUp;
    }

    public void setAmount(int value) {
        this.amount = value;
        setAmountUp(value);
    }

    public int getAmountUp() {
        return amountUp;
    }

    public void setAmountUp(int value) {
        this.amountUp = value;
    }

    public int getAmountDown() {
        return amountDown;
    }

    public void setAmountDown(int value) {
        this.amountDown = value;
    }

    public String getAmountStr() {
        return String.valueOf(amount > 0 ? amount : amountUp);
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
