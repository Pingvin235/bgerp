package org.bgerp.plugin.bgb.getolt.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Internet service from billing system (InetMac API).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class InetService {
    @JsonProperty("serviceId")
    @JsonAlias("id")
    private int serviceId;

    @JsonProperty("login")
    private String login;

    @JsonProperty("mac")
    private String mac;

    @JsonProperty("typeTitle")
    private String typeTitle;

    @JsonProperty("status")
    private int status;

    @JsonProperty("statusTitle")
    private String statusTitle;

    @JsonProperty("connectionType")
    private String connectionType;

    @JsonProperty("comment")
    private String comment;

    public InetService() {
    }

    public InetService(int serviceId, String login, String mac) {
        this.serviceId = serviceId;
        this.login = login;
        this.mac = mac;
    }

    public int getServiceId() {
        return serviceId;
    }

    public void setServiceId(int serviceId) {
        this.serviceId = serviceId;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public String getTypeTitle() {
        return typeTitle;
    }

    public void setTypeTitle(String typeTitle) {
        this.typeTitle = typeTitle;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getStatusTitle() {
        return statusTitle;
    }

    public void setStatusTitle(String statusTitle) {
        this.statusTitle = statusTitle;
    }

    public String getConnectionType() {
        return connectionType;
    }

    public void setConnectionType(String connectionType) {
        this.connectionType = connectionType;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    /**
     * Get display title for service selection UI.
     */
    public String getDisplayTitle() {
        StringBuilder sb = new StringBuilder();
        if (login != null && !login.isEmpty()) {
            sb.append(login);
        }
        if (typeTitle != null && !typeTitle.isEmpty()) {
            if (sb.length() > 0) sb.append(" - ");
            sb.append(typeTitle);
        }
        if (mac != null && !mac.isEmpty()) {
            if (sb.length() > 0) sb.append(" (MAC: ");
            else sb.append("MAC: ");
            sb.append(mac);
            if (sb.toString().contains("(")) sb.append(")");
        }
        return sb.length() > 0 ? sb.toString() : "Услуга #" + serviceId;
    }

    @Override
    public String toString() {
        return "InetService{serviceId=" + serviceId + ", login='" + login + "', mac='" + mac + "'}";
    }
}
