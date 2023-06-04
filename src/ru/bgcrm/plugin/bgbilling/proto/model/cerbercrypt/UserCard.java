package ru.bgcrm.plugin.bgbilling.proto.model.cerbercrypt;

import java.util.Date;

import org.bgerp.model.base.Id;

import ru.bgcrm.model.PeriodSet;

public class UserCard extends Id implements PeriodSet {
    private int baseCardId;
    private int contractId;
    private long number;
    private Date dateFrom;
    private Date dateTo;
    private String comment;
    private Date subscrDate;
    private boolean needSync;
    private String baseCardTitle;

    public int getBaseCardId() {
        return baseCardId;
    }

    public void setBaseCardId(int baseCardId) {
        this.baseCardId = baseCardId;
    }

    public int getContractId() {
        return contractId;
    }

    public void setContractId(int contractId) {
        this.contractId = contractId;
    }

    public long getNumber() {
        return number;
    }

    public void setNumber(long number) {
        this.number = number;
    }

    public Date getDateFrom() {
        return dateFrom;
    }

    @Override
    public void setDateFrom(Date dateFrom) {
        this.dateFrom = dateFrom;
    }

    public Date getDateTo() {
        return dateTo;
    }

    @Deprecated
    public void setDate1(Date dateFrom) {
        this.dateFrom = dateFrom;
    }

    @Override
    public void setDateTo(Date dateTo) {
        this.dateTo = dateTo;
    }

    @Deprecated
    public void setDate2(Date dateTo) {
        this.dateTo = dateTo;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Date getSubscrDate() {
        return subscrDate;
    }

    public void setSubscrDate(Date subscribeDate) {
        this.subscrDate = subscribeDate;
    }

    public boolean isNeedSync() {
        return needSync;
    }

    public void setNeedSync(boolean needSync) {
        this.needSync = needSync;
    }

    public String getBaseCardTitle() {
        return baseCardTitle;
    }

    public void setBaseCardTitle(String baseCardTitle) {
        this.baseCardTitle = baseCardTitle;
    }
}
