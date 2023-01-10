package ru.bgcrm.plugin.bgbilling.proto.model.inet;

public class InetDeviceInterface {
    private int deviceId;
    private int port;

    private String title;

    public int getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(int deviceId) {
        this.deviceId = deviceId;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getRemoteDeviceId() {
        return remoteDeviceId;
    }

    public void setRemoteDeviceId(int remoteDeviceId) {
        this.remoteDeviceId = remoteDeviceId;
    }

    public int getRemotePort() {
        return remotePort;
    }

    public void setRemotePort(int remotePort) {
        this.remotePort = remotePort;
    }

    public int getIpCategoryId() {
        return ipCategoryId;
    }

    public void setIpCategoryId(int ipCategoryId) {
        this.ipCategoryId = ipCategoryId;
    }

    public int getSubscriberId() {
        return subscriberId;
    }

    public void setSubscriberId(int subscriberId) {
        this.subscriberId = subscriberId;
    }

    public String getSubscriberTitle() {
        return subscriberTitle;
    }

    public void setSubscriberTitle(String subscriberTitle) {
        this.subscriberTitle = subscriberTitle;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    private int status;

    private int remoteDeviceId;
    private int remotePort;

    //private List<DeviceInterfaceIndex> indexList;

    private int ipCategoryId;

    private int subscriberId;
    private String subscriberTitle;

    private String comment;
}
