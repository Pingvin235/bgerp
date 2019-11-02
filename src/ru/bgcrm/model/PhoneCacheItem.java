package ru.bgcrm.model;

import java.util.Date;

import org.w3c.dom.Element;

public class PhoneCacheItem {
    private String key;
    private Date lastTimeAccess;
    private Element item;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Date getLastTimeAccess() {
        return lastTimeAccess;
    }

    public void setLastTimeAccess(Date lastTimeAccess) {
        this.lastTimeAccess = lastTimeAccess;
    }

    public Element getItem() {
        return item;
    }

    public void setItem(Element item) {
        this.item = item;
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append("key = ");
        buf.append(key);
        buf.append("; item = ");
        buf.append(item);
        return buf.toString();
    }
}
