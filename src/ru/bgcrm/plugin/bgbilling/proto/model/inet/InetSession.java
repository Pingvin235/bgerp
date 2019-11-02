package ru.bgcrm.plugin.bgbilling.proto.model.inet;

import java.math.BigDecimal;
import java.util.Date;

public class InetSession {
    public static final short STATUS_ALIVE = InetConnection.STATUS_ALIVE;
    public static final short STATUS_SUSPENDED = InetConnection.STATUS_SUSPENDED;
    public static final short STATUS_CLOSED = InetConnection.STATUS_CLOSED;
    public static final short STATUS_FINISHED = InetConnection.STATUS_FINISHED;

    public static final int TYPE_NORMAL = 0;
    public static final int TYPE_FAKE = 1;

    protected long id;
    protected long connectionId;

    protected long parentId;
    protected long splittedId;

    protected Date start;
    protected Date stop;

    protected Date last;

    /**
     * Состояние на устройстве для сессии (на момент начала сессии).<br/>
     * Для CoA соединений, когда доступ отключается CoA - начинается новая session с deviceState={@link InetUtils#STATE_DISABLE}
     */
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

    @Deprecated
    public long getConnectionId() {
        return connectionId;
    }

    @Deprecated
    public void setConnectionId(long connectionId) {
        this.connectionId = connectionId;
    }

    public long getParentId() {
        return parentId;
    }

    public void setParentId(long parentId) {
        this.parentId = parentId;
    }

    public long getSplittedId() {
        return splittedId;
    }

    public void setSplittedId(long splittedId) {
        this.splittedId = splittedId;
    }

    /**
     * Состояние на устройстве для сессии (на момент начала сессии).<br/>
     * Для CoA соединений, когда доступ отключается CoA - начинается новая session с deviceState={@link InetUtils#STATE_DISABLE}
     * @return
     */
    public short getDevState() {
        return deviceState;
    }

    /**
     * Состояние на устройстве для сессии (на момент начала сессии).<br/>
     * Для CoA соединений, когда доступ отключается CoA - начинается новая session с deviceState={@link InetUtils#STATE_DISABLE}
     * @return
     */
    public void setDevState(short type) {
        this.deviceState = type;
    }
    
    @Deprecated
    public short getDeviceState() {
        return deviceState;
    }

    @Deprecated
    public void setDeviceState(short type) {
        this.deviceState = type;
    }

    public Date getStart() {
        return start;
    }

    public void setStart(Date start) {
        this.start = start;
    }

    @Deprecated
    public Date getSessionStart() {
        return start;
    }

    @Deprecated
    public void setSessionStart(Date sessionStart) {
        this.start = sessionStart;
    }
    
    public Date getStop() {
        return stop;
    }

    public void setStop(Date sessionStop) {
        this.stop = sessionStop;
    }

    @Deprecated
    public Date getSessionStop() {
        return stop;
    }

    @Deprecated
    public void setSessionStop(Date sessionStop) {
        this.stop = sessionStop;
    }
    
    public Date getLast() {
        return last;
    }

    public void setLast(Date lastActive) {
        this.last = lastActive;
    }

    @Deprecated
    public Date getLastActive() {
        return last;
    }

    @Deprecated
    public void setLastActive(Date lastActive) {
        this.last = lastActive;
    }
    
    public long getTime() {
        return time;
    }

    public void setTime(long sessionTime) {
        this.time = sessionTime;
    }

    @Deprecated
    public long getSessionTime() {
        return time;
    }

    @Deprecated
    public void setSessionTime(long sessionTime) {
        this.time = sessionTime;
    }
    
    public BigDecimal getCost() {
        return cost;
    }

    public void setCost(BigDecimal sessionCost) {
        this.cost = sessionCost;
    }

    @Deprecated
    public BigDecimal getSessionCost() {
        return cost;
    }

    @Deprecated
    public void setSessionCost(BigDecimal sessionCost) {
        this.cost = sessionCost;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
