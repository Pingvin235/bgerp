package ru.bgcrm.plugin.bgbilling.proto.model.voice;

import org.bgerp.model.base.IdTitle;

public class VoiceAccountType extends IdTitle {
    private boolean checkPassword;
    private boolean needPhone;
    private boolean needLogin;
    private boolean needDevice;
    private boolean needPorts;
    private boolean needLines;
    private boolean needObject;

    private boolean sessionCountLimitLock;
    private byte sessionCountLimit;

    private String statusConfig = "{}";

    public boolean isCheckPassword() {
        return checkPassword;
    }

    public void setCheckPassword(boolean checkPassword) {
        this.checkPassword = checkPassword;
    }

    public boolean isNeedPhone() {
        return needPhone;
    }

    public void setNeedPhone(boolean needPhone) {
        this.needPhone = needPhone;
    }

    public boolean isNeedLogin() {
        return needLogin;
    }

    public void setNeedLogin(boolean needLogin) {
        this.needLogin = needLogin;
    }

    public boolean isNeedDevice() {
        return needDevice;
    }

    public void setNeedDevice(boolean needDevice) {
        this.needDevice = needDevice;
    }

    public boolean isNeedPorts() {
        return needPorts;
    }

    public void setNeedPorts(boolean needPorts) {
        this.needPorts = needPorts;
    }

    public boolean isNeedLines() {
        return needLines;
    }

    public void setNeedLines(boolean needLines) {
        this.needLines = needLines;
    }

    public boolean isNeedObject() {
        return needObject;
    }

    public void setNeedObject(boolean needObject) {
        this.needObject = needObject;
    }

    public boolean isSessionCountLimitLock() {
        return sessionCountLimitLock;
    }

    public void setSessionCountLimitLock(boolean sessionCountLimitLock) {
        this.sessionCountLimitLock = sessionCountLimitLock;
    }

    public byte getSessionCountLimit() {
        return sessionCountLimit;
    }

    public void setSessionCountLimit(byte sessionCountLimit) {
        this.sessionCountLimit = sessionCountLimit;
    }

    public String getStatusConfig() {
        return statusConfig;
    }

    public void setStatusConfig(String statusConfig) {
        this.statusConfig = statusConfig;
    }
}