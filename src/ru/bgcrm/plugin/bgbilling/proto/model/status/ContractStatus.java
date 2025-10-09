package ru.bgcrm.plugin.bgbilling.proto.model.status;

import java.util.Date;

import org.bgerp.model.base.Id;

import ru.bgcrm.model.PeriodSet;

public class ContractStatus extends Id implements PeriodSet {
    private String comment;
    private int statusId;
    private String status;
    private Date dateFrom;
    private Date dateTo;

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public int getStatusId() {
        return statusId;
    }

    public void setStatusId(int statusId) {
        this.statusId = statusId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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

    @Override
    public void setDateTo(Date dateTo) {
        this.dateTo = dateTo;
    }
}
