package ru.bgcrm.model;

import java.util.Date;

public class LogEntry {
    private int id;
    private int uid;
    private String action;
    private String parameters;
    private Date time;
    private String ipAddress;
    private long duration;

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getParameters() {
        return parameters == null ? "" : parameters;
    }

    public void setParameters(String parameters) {
        this.parameters = parameters;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }
}
