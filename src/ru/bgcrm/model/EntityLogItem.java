package ru.bgcrm.model;

import java.util.Date;

import ru.bgcrm.util.TimeUtils;

public class EntityLogItem {
    private int id;
    private int userId;
    private String text;
    private Date date;

    public EntityLogItem(Date date, int id, int userId, String text) {
        super();
        this.id = id;
        this.userId = userId;
        this.text = text;
        this.date = date;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getDateFormatted() {
        return TimeUtils.format(date, TimeUtils.FORMAT_TYPE_YMDHMS);
    }
}
