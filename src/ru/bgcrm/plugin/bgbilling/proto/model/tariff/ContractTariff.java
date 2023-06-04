package ru.bgcrm.plugin.bgbilling.proto.model.tariff;

import java.util.Date;

import org.bgerp.model.base.Id;

import com.fasterxml.jackson.annotation.JsonIgnore;

import ru.bgcrm.model.Period;
import ru.bgcrm.model.PeriodSet;

public class ContractTariff extends Id implements PeriodSet {
    private int contractId;
    private int tariffPlanId;
    private Date dateFrom;
    private Date dateTo;
    private String title;
    private String comment = "";
    private int position;

    public int getContractId() {
        return contractId;
    }

    public void setContractId(int contractId) {
        this.contractId = contractId;
    }

    @JsonIgnore
    public Date getDateFrom() {
        return dateFrom;
    }

    @Override
    public void setDateFrom(Date dateFrom) {
        this.dateFrom = dateFrom;
    }

    @JsonIgnore
    public Date getDateTo() {
        return dateTo;
    }

    @Override
    public void setDateTo(Date dateTo) {
        this.dateTo = dateTo;
    }

    public Period getPeriod() {
        return new Period(dateFrom, dateTo);
    }

    public void setPeriod(Period value) {
        this.dateFrom = value.getDateFrom();
        this.dateTo = value.getDateTo();
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public int getTariffPlanId() {
        return tariffPlanId;
    }

    public void setTariffPlanId(int tpid) {
        this.tariffPlanId = tpid;
    }
}
