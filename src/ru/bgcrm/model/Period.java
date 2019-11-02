package ru.bgcrm.model;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class Period {
    private Date dateFrom;
    private Date dateTo;

    public Period() {
    }

    public Period(Date dateFrom, Date dateTo) {
        this.dateFrom = dateFrom;
        this.dateTo = dateTo;
    }

    public Date getDateFrom() {
        return dateFrom;
    }

    public Calendar getCalendarFrom() {
        Calendar calendar = null;
        if (dateFrom != null) {
            calendar = new GregorianCalendar();
            calendar.setTime(dateFrom);
        }
        return calendar;
    }

    public void setDateFrom(Date dateFrom) {
        this.dateFrom = dateFrom;
    }

    public Date getDateTo() {
        return dateTo;
    }

    public Calendar getCalendarTo() {
        Calendar calendar = null;
        if (dateTo != null) {
            calendar = new GregorianCalendar();
            calendar.setTime(dateTo);
        }
        return calendar;
    }

    public Date getDateToNext() {
        Date date = null;
        if (dateTo != null) {
            Calendar calendar = new GregorianCalendar();
            calendar.setTime(dateTo);
            calendar.clear(Calendar.MILLISECOND);
            calendar.clear(Calendar.SECOND);
            calendar.clear(Calendar.MINUTE);
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.add(Calendar.DAY_OF_MONTH, 1);
            date = calendar.getTime();
        }
        return date;
    }

    public void setDateTo(Date dateTo) {
        this.dateTo = dateTo;
    }
}
