package ru.bgcrm.plugin.bgbilling.proto.model.inet;

import ru.bgcrm.util.TimeUtils;

public class InetSessionLog extends InetSession {
    private static final short STATUS_ALIVE = 1;
    private static final short STATUS_SUSPENDED = 2;
    private static final short STATUS_CLOSED = 3;
    private static final short STATUS_FINISHED = 4;

    private int servId;
    private String serviceTitle = "";

    private int deviceId;
    private int devicePort;
    private String deviceTitle;

    private String acctSessId;
    private String cdsId;
    private String cnsId;
    private String ip;

    public InetSessionLog() {
    }

    public int getServId() {
        return servId;
    }

    public void setServId(int loginId) {
        this.servId = loginId;
    }

    public String getServiceTitle() {
        return serviceTitle;
    }

    public void setServiceTitle(String serviceTitle) {
        this.serviceTitle = serviceTitle;
    }

    public int getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(int deviceId) {
        this.deviceId = deviceId;
    }

    public int getDevicePort() {
        return devicePort;
    }

    public void setDevicePort(int port) {
        this.devicePort = port;
    }

    public String getDeviceTitle() {
        return deviceTitle;
    }

    public void setDeviceTitle(String deviceTitle) {
        this.deviceTitle = deviceTitle;
    }

    public String getAcctSessId() {
        return acctSessId;
    }

    public void setAcctSessId(String acctSessionId) {
        this.acctSessId = acctSessionId;
    }

    public String getCdsId() {
        return cdsId;
    }

    public void setCdsId(String fromNumber) {
        this.cdsId = fromNumber;
    }

    public String getCnsId() {
        return cnsId;
    }

    public void setCnsId(String toNumber) {
        this.cnsId = toNumber;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String inetAddress) {
        this.ip = inetAddress;
    }

    public String getStatusName() {
        String result = "";
        switch (status) {
            case STATUS_ALIVE:
                result = "активна";
                break;
            case STATUS_SUSPENDED:
                result = "приостановлена";
                break;
            case STATUS_CLOSED:
                result = "закрыта";
                break;
            case STATUS_FINISHED:
                result = "завершена";
                break;
        }
        return result;
    }

    public String getFromNumberToNumberAsString() {
        String result = "";
        boolean flag = false;
        if (cnsId != null) {
            result = result + cnsId + "";
            flag = true;
        }

        if (cdsId != null) {
            if (flag) {
                result = result + "/";
            }

            result = result + cdsId;
        }

        return result;
    }

    public String getSessionStartAsString() {
        return TimeUtils.format(getStart(), TimeUtils.PATTERN_DDMMYYYYHHMMSS);
    }

    public String getSessionStopAsString() {
        return TimeUtils.format(getStop(), TimeUtils.PATTERN_DDMMYYYYHHMMSS);
    }

    public String getSessionActivityAsString() {
        return TimeUtils.format(getLast(), TimeUtils.PATTERN_DDMMYYYYHHMMSS);
    }
}