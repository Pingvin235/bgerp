package org.bgerp.plugin.clb.calendar.model;

public class TimeAccount {
    public static final int TYPE_IN = -1;
    public static final int TYPE_OUT = -2;

    private int userId;
    private int year;
    /** One of {@link #TYPE_IN}, {@link #TYPE_OUT} or ID of {@link TimeType}. */
    private int typeId = TYPE_IN;
    /** Amount in minutes. */
    private int amount;

    public TimeAccount() {}

    public TimeAccount(int userId, int year) {
        this.userId = userId;
        this.year = year;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int value) {
        this.userId = value;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int value) {
        this.year = value;
    }

    public int getTypeId() {
        return typeId;
    }

    public void setTypeId(int value) {
        this.typeId = value;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int value) {
        this.amount = value;
    }
}
