package ru.bgcrm.plugin.bgbilling.proto.model.inet;

import java.math.BigDecimal;
import java.util.Date;

// TODO: Move all the class' content to InetSessionLog
public class InetSession {
    protected long id;
    protected long connectionId;

    protected Date start;
    protected Date stop;
    protected Date last;

    protected short deviceState;

    protected long time;
    protected BigDecimal cost;

    protected int status;

    public InetSession() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getConId() {
        return connectionId;
    }

    public void setConId(long connectionId) {
        this.connectionId = connectionId;
    }

    public short getDevState() {
        return deviceState;
    }

    public String getDevStateTitle() {
        return deviceState == 1 ? "подключено" : "отключено";
    }

    public void setDevState(short type) {
        this.deviceState = type;
    }

    public Date getStart() {
        return start;
    }

    public void setStart(Date start) {
        this.start = start;
    }

    public Date getStop() {
        return stop;
    }

    public void setStop(Date sessionStop) {
        this.stop = sessionStop;
    }

    public Date getLast() {
        return last;
    }

    public void setLast(Date lastActive) {
        this.last = lastActive;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long sessionTime) {
        this.time = sessionTime;
    }

    public BigDecimal getCost() {
        return cost;
    }

    public void setCost(BigDecimal sessionCost) {
        this.cost = sessionCost;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
