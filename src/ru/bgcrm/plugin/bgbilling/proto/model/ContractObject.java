package ru.bgcrm.plugin.bgbilling.proto.model;

import java.util.Date;

import org.bgerp.model.base.IdTitle;
import ru.bgcrm.model.PeriodSet;
import ru.bgcrm.util.TimeUtils;

public class ContractObject
        extends IdTitle
        implements PeriodSet {


    private int contractId;
    private Date dateFrom;
    private Date dateTo;
    private int typeId;
    private String type;

    public String getPeriod() {
        return TimeUtils.formatPeriod(dateFrom, dateTo);
    }

    public void setPeriod(String period) {
        TimeUtils.parsePeriod(period, this);
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getTypeId() {
        return typeId;
    }

    public void setTypeId(int typeId) {
        this.typeId = typeId;
    }

    public Date getDateFrom() {
        return dateFrom;
    }

    public void setDateFrom(Date dateFrom) {
        this.dateFrom = dateFrom;
    }

    public Date getDateTo() {
        return dateTo;
    }

    public void setDateTo(Date dateTo) {
        this.dateTo = dateTo;
    }

    public int getContractId() {
        return contractId;
    }

    public void setContractId(int contractId) {
        this.contractId = contractId;
    }
}
