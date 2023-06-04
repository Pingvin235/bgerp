package ru.bgcrm.plugin.task.model;

import java.util.Date;

import org.bgerp.model.base.Id;

import ru.bgcrm.util.Preferences;

/**
 * Process assigned task to be executed right now or in future.
 *
 * @author Shamil Vakhitov
 */
public class Task extends Id {

    private String typeId;
    private int processId;
    /** Запланированная дата выполнения. */
    private Date scheduledTime;
    /** Конфигурация задачи. */
    private Preferences config = new Preferences();
    /** Фактическая дата выполнения. */
    private Date executedTime;
    private String log;

    public Task() {}

    public Task(String typeId, int processId, Date scheduledTime) {
        this.typeId = typeId;
        this.processId = processId;
        this.scheduledTime = scheduledTime;
    }

    public String getTypeId() {
        return typeId;
    }

    public void setTypeId(String typeId) {
        this.typeId = typeId;
    }

    public int getProcessId() {
        return processId;
    }

    public void setProcessId(int processId) {
        this.processId = processId;
    }

    public Date getScheduledTime() {
        return scheduledTime;
    }

    public void setScheduledTime(Date planDt) {
        this.scheduledTime = planDt;
    }

    public Date getExecutedTime() {
        return executedTime;
    }

    public void setExecutedTime(Date executeDt) {
        this.executedTime = executeDt;
    }

    public Preferences getConfig() {
        return config;
    }

    public void setConfig(Preferences config) {
        this.config = config;
    }

    public String getLog() {
        return log;
    }

    public void setLog(String log) {
        this.log = log;
    }

}
