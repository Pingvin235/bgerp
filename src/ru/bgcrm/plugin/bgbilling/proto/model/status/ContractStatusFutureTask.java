package ru.bgcrm.plugin.bgbilling.proto.model.status;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ContractStatusFutureTask {
    public static enum Status {
        ALL(0,"все"), ACTIVE(1,"активная"), EXECUTED(2,"выполненая"), CANCEL(3,"отменённая");

        private int id = 1;
        private String title;

        private Status(int code, String title) {
            this.id = code;
            this.title = title;
        }

        public int getId() {
            return id;
        }

        public String getTitle() {
            return title;
        }
    }

    private Status taskStatus = Status.ACTIVE;
    private Date createTime;
    private int createUserId;
    private String user;

    private String data;
    private String status;
    private Date dateFrom;
    private Date dateTo;
    private String comment;

    @JsonProperty("status")
    public Status getTaskStatus() {
        return taskStatus;
    }

    public void setTaskStatus(Status status) {
        this.taskStatus = status;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public int getCreateUserId() {
        return createUserId;
    }

    public void setCreateUserId(int createUserId) {
        this.createUserId = createUserId;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    @JsonIgnore
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getDateFrom() {
        return dateFrom;
    }

    public void setDateFrom(Date dateFrom) {
        this.dateFrom = dateFrom;
    }

    public Date getDateTo() {
        return dateTo;
    }

    public void setDateTo(Date dateTo) {
        this.dateTo = dateTo;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
