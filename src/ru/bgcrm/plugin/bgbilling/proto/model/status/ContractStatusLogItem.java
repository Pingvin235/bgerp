package ru.bgcrm.plugin.bgbilling.proto.model.status;

import java.util.Date;

public class ContractStatusLogItem extends ContractStatus {
    private String user;
    private int userId;
    private Date date;

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date time) {
        this.date = time;
    }

    public Date getDate1() {
        return super.getDateFrom();
    }

    public void setDate1(Date date1) {
        super.setDateFrom(date1);
    }

    public Date getDate2() {
        return super.getDateTo();
    }

    public void setDate2(Date date2) {
        super.setDateTo(date2);
    }
}
