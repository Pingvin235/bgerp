package ru.bgcrm.model;

import java.util.Date;

public class EventProcessorLogEntry {
    private String event;
    private String script;
    private Date time;
    private int connectionId;
    private int duration;
    private int id;
    private String instanceHostName;
    private String resultStatus;

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public String getScript() {
        return script;
    }

    public void setScript(String script) {
        this.script = script;
    }

    public Date getTime() {
        return time;
    }

    public int getConnectionId() {
        return connectionId;
    }

    public void setConnectionId(int connectionId) {
        this.connectionId = connectionId;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int getId() {
        return id;
    }

    public String getInstanceHostName() {
        return instanceHostName;
    }

    public void setInstanceHostName(String instanceHostName) {
        this.instanceHostName = instanceHostName;
    }

    public String getResultStatus() {
        return resultStatus;
    }

    public void setResultStatus(String resultStatus) {
        this.resultStatus = resultStatus;
    }
}
