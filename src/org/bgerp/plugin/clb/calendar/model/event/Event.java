package org.bgerp.plugin.clb.calendar.model.event;

import java.time.LocalDateTime;

/**
 * Calendar day event.
 *
 * @author Shamil Vakhitov
 */
public class Event {
    private int calendarId;
    private int typeId;
    /** Owner. */
    private int userId;

    /** Event from date and minutes, included.*/
    private LocalDateTime from;
    /** Event to date and minutes, excluded. */
    private LocalDateTime to;

    // status - proposed, confirmed

    public int getCalendarId() {
        return calendarId;
    }

    public Event withCalendarId(int calendarId) {
        this.calendarId = calendarId;
        return this;
    }

    public int getTypeId() {
        return typeId;
    }

    public Event withTypeId(int typeId) {
        this.typeId = typeId;
        return this;
    }

    public int getUserId() {
        return userId;
    }

    public Event withUserId(int userId) {
        this.userId = userId;
        return this;
    }

    public LocalDateTime getFrom() {
        return from;
    }

    public Event withFrom(LocalDateTime from) {
        this.from = from;
        return this;
    }

    public LocalDateTime getTo() {
        return to;
    }

    public Event withTo(LocalDateTime to) {
        this.to = to;
        return this;
    }
}
