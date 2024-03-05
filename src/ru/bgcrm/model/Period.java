package ru.bgcrm.model;

import java.util.Date;

import ru.bgcrm.util.TimeUtils;

public class Period {
    private Date dateFrom;
    private Date dateTo;

    public Period() {
    }

    public Period(Date dateFrom, Date dateTo) {
        this.dateFrom = dateFrom;
        this.dateTo = dateTo;
    }

    public Period(String period) {
        int pos = period.indexOf('-');
        if (pos < 0)
            throw new IllegalArgumentException("Period string doesn't contain '-'");
        dateFrom = TimeUtils.parse(period.substring(0, pos), TimeUtils.FORMAT_TYPE_YMD);
        dateTo = TimeUtils.parse(period.substring(pos + 1), TimeUtils.FORMAT_TYPE_YMD);
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

    @Override
    public String toString() {
        return TimeUtils.formatPeriod(dateFrom, dateTo);
    }
}
