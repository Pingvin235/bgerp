package org.bgerp.dao.message.call;

import org.bgerp.model.msg.Message;

public class CallRegistration {
    final int userId;
    final String number;

    private volatile Message messageForOpen;
    private volatile OutCall outCall;

    public CallRegistration(int userId, String number) {
        this.userId = userId;
        this.number = number;
    }

    public int getUserId() {
        return userId;
    }

    public String getNumber() {
        return number;
    }

    public Message getMessageForOpen() {
        return messageForOpen;
    }

    public void setMessageForOpen(Message value) {
        this.messageForOpen = value;
    }

    public void outCall(String number, int processId) {
        outCall = new OutCall(number, processId);
    }

    public OutCall getOutCall() {
        return outCall;
    }

    public void setOutCall(OutCall outCall) {
        this.outCall = outCall;
    }

    @Override
    public String toString() {
        return "CallRegistration [userId=" + userId + "]";
    }
}