package ru.bgcrm.model.param;

import java.util.Date;

public class ParameterHistory {
    private Object value;
    private Date dateChanged;
    private int userIdChanged = -1;
    private String userNameChanged;

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public Date getDateChanged() {
        return dateChanged;
    }

    public void setDateChanged(Date dateChanged) {
        this.dateChanged = dateChanged;
    }

    public int getUserIdChanged() {
        return userIdChanged;
    }

    public void setUserIdChanged(int userIdChanged) {
        this.userIdChanged = userIdChanged;
    }

    public String getUserNameChanged() {
        return userNameChanged;
    }

    public void setUserNameChanged(String userNameChanged) {
        this.userNameChanged = userNameChanged;
    }
}
