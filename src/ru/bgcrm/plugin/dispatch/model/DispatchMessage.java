package ru.bgcrm.plugin.dispatch.model;

import java.util.Collections;
import java.util.Date;
import java.util.Set;

import ru.bgcrm.model.IdTitle;

public class DispatchMessage extends IdTitle {
    private Set<Integer> dispatchIds = Collections.emptySet();
    private boolean ready;
    private Date createTime;
    private Date sentTime;
    private String text;

    public Set<Integer> getDispatchIds() {
        return dispatchIds;
    }

    public void setDispatchIds(Set<Integer> dispatchIds) {
        this.dispatchIds = dispatchIds;
    }

    public boolean isReady() {
        return ready;
    }

    public void setReady(boolean readyToSend) {
        this.ready = readyToSend;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getSentTime() {
        return sentTime;
    }

    public void setSentTime(Date sentTime) {
        this.sentTime = sentTime;
    }

    public String getText() {
        return text;
    }

    public void setText(String message) {
        this.text = message;
    }
}
