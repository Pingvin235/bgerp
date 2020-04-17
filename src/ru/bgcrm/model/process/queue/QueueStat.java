package ru.bgcrm.model.process.queue;

import java.util.List;

public class QueueStat {
    private String queueTitle;
    private List<QueueUserStat> userStat;
    private List<QueueProcessStat> processStat;

    public String getQueueTitle() {
        return queueTitle;
    }

    public void setQueueTitle(String queueTitle) {
        this.queueTitle = queueTitle;
    }

    public List<QueueUserStat> getUserStat() {
        return userStat;
    }

    public void setUserStat(List<QueueUserStat> userStat) {
        this.userStat = userStat;
    }

    public List<QueueProcessStat> getProcessStat() {
        return processStat;
    }

    public void setProcessStat(List<QueueProcessStat> processStat) {
        this.processStat = processStat;
    }
}
