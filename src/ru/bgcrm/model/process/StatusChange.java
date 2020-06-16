package ru.bgcrm.model.process;

import java.util.Date;

import ru.bgcrm.cache.ProcessTypeCache;
import ru.bgcrm.cache.UserCache;

public class StatusChange {
    private int processId;
    private Date date;
    private int userId;
    private int statusId;
    private String comment;

    public int getProcessId() {
        return processId;
    }

    public void setProcessId(int processId) {
        this.processId = processId;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public int getStatusId() {
        return statusId;
    }

    public void setStatusId(int statusId) {
        this.statusId = statusId;
    }

    public String getStatusTitle() {
        Status status = ProcessTypeCache.getStatusMap().get(statusId);
        return status == null ? "??? [" + statusId + "]" : status.getTitle();
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUserTitle() {
        return UserCache.getUser(userId).getTitle();
    }
}