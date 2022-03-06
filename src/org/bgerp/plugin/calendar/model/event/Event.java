package org.bgerp.plugin.calendar.model.event;

import java.time.LocalDate;

/**
 * Calendar day event.
 *
 * @author Shamil Vakhitov
 */
public class Event {
    public static final int STATUS_PLANNED = 0;
    public static final int STATUS_CONFIRMED = 1;
    public static final int STATUS_REJECTED = 2;

    //private int calendarId;
    /** Time type ID. */
    private int typeId;
    private int createUserId;
    private int status;

    /** The day. */
    private LocalDate date;
    /** Minute of the day from, including. */
    private long minuteFrom;
    /** Minute of the day to, not including. */
    private long minuteTo;

    /* public int getCalendarId() {
        return calendarId;
    }

    public Event withCalendarId(int calendarId) {
        this.calendarId = calendarId;
        return this;
    } */

    public int getTypeId() {
        return typeId;
    }

    public Event withTypeId(int typeId) {
        this.typeId = typeId;
        return this;
    }

    public LocalDate getDate() {
        return date;
    }

    public Event withDate(LocalDate value) {
        this.date = value;
        return this;
    }

    public long getMinuteFrom() {
        return minuteFrom;
    }

    public Event withMinuteFrom(long value) {
        this.minuteFrom = value;
        return this;
    }

    public long getMinuteTo() {
        return minuteTo;
    }

    public Event withMinuteTo(long value) {
        this.minuteTo = value;
        return this;
    }

    /**
     * Duration between event's begin and end in minutes.
     * @return
     */
    public long getDuration() {
        return minuteTo - minuteFrom;
    }
}
