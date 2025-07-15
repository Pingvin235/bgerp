package ru.bgcrm.plugin.bgbilling.proto.model.helpdesk;

import java.math.BigDecimal;
import java.util.Date;

import org.bgerp.model.base.IdTitle;

public class HdTopic extends IdTitle {
    private int contractId;
    private String contractTitle;
    private boolean closed;
    private int userId;
    private int messageCount;
    private Date lastMessageTime;
    private String userTitle;
    private BigDecimal cost;
    private int statusId;
    private String contact;
    private boolean autoClose;

    public int getContractId() {
        return contractId;
    }

    public void setContractId(int contractId) {
        this.contractId = contractId;
    }

    public String getContractTitle() {
        return contractTitle;
    }

    public void setContractTitle(String contractTitle) {
        this.contractTitle = contractTitle;
    }

    public boolean isClosed() {
        return closed;
    }

    public void setClosed(boolean state) {
        this.closed = state;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUserTitle() {
        return userTitle;
    }

    public void setUserTitle(String userTitle) {
        this.userTitle = userTitle;
    }

    public int getMessageCount() {
        return messageCount;
    }

    public void setMessageCount(int messageCount) {
        this.messageCount = messageCount;
    }

    public Date getLastMessageTime() {
        return lastMessageTime;
    }

    public void setLastMessageTime(Date lastMessageTime) {
        this.lastMessageTime = lastMessageTime;
    }

    public BigDecimal getCost() {
        return cost;
    }

    public void setCost(BigDecimal cost) {
        this.cost = cost;
    }

    public int getStatusId() {
        return statusId;
    }

    public void setStatusId(int statusId) {
        this.statusId = statusId;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public boolean isAutoClose() {
        return autoClose;
    }

    public void setAutoClose(boolean autoClose) {
        this.autoClose = autoClose;
    }
}