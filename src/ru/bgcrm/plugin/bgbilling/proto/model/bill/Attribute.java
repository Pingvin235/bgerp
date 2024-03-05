package ru.bgcrm.plugin.bgbilling.proto.model.bill;

import java.util.Date;

import org.bgerp.model.base.IdTitle;

import ru.bgcrm.model.Period;
import ru.bgcrm.model.PeriodSet;

public class Attribute extends IdTitle implements PeriodSet {
    private Date dateFrom;
    private Date dateTo;
    private String value;

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

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public void setPeriod(Period period) {
        if (period != null) {
            setDateFrom(period.getDateFrom());
            setDateTo(period.getDateTo());

        }
    }

    public void setName(String name) {
        setTitle(name);
    }
}