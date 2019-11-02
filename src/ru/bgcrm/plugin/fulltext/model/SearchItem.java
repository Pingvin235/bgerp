package ru.bgcrm.plugin.fulltext.model;

import java.util.Date;

public class SearchItem {
    
    private String objectType;
    private int objectId;
    private Date scheduledTime;
    private String text;
    
    public String getObjectType() {
        return objectType;
    }

    public void setObjectType(String objectType) {
        this.objectType = objectType;
    }

    public int getObjectId() {
        return objectId;
    }

    public void setObjectId(int objectId) {
        this.objectId = objectId;
    }

    public Date getScheduledTime() {
        return scheduledTime;
    }

    public void setScheduledTime(Date lm) {
        this.scheduledTime = lm;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
