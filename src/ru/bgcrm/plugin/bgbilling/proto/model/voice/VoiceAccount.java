package ru.bgcrm.plugin.bgbilling.proto.model.voice;

import java.util.Date;

import org.bgerp.model.base.IdTitleComment;

public class VoiceAccount extends IdTitleComment {
    private int contractId;
    private Date dateFrom;
    private Date dateTo;
    private int deviceId;
    private String deviceTitle;
    private int typeId;
    private String typeTitle;
    private String login;
    private String password;
    private Long number;
    private VoiceAccountStatus status = VoiceAccountStatus.STATUS_ON;
    private int lineCount = 1;
    private VoiceAccountState deviceState;
    /**
     * Последний accessCode, с которым меняли deviceStates
     */
    private int accessCode;
    private byte sessionCountLimit;

    public int getContractId() {
        return contractId;
    }

    public void setContractId(int contractId) {
        this.contractId = contractId;
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

    public int getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(int deviceId) {
        this.deviceId = deviceId;
    }

    public String getDeviceTitle() {
        return deviceTitle;
    }

    public void setDeviceTitle(String deviceTitle) {
        this.deviceTitle = deviceTitle;
    }

    public int getTypeId() {
        return typeId;
    }

    public void setTypeId(int typeId) {
        this.typeId = typeId;
    }

    public String getTypeTitle() {
        return typeTitle;
    }

    public void setTypeTitle(String typeTitle) {
        this.typeTitle = typeTitle;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Long getNumber() {
        return number;
    }

    public void setNumber(Long number) {
        this.number = number;
    }

    public VoiceAccountStatus getStatus() {
        return status;
    }

    public void setStatus(VoiceAccountStatus status) {
        this.status = status;
    }

    public int getLineCount() {
        return lineCount;
    }

    public void setLineCount(int lineCount) {
        this.lineCount = lineCount;
    }

    public VoiceAccountState getDeviceState() {
        return deviceState;
    }

    public void setDeviceState(VoiceAccountState deviceState) {
        this.deviceState = deviceState;
    }

    public int getAccessCode() {
        return accessCode;
    }

    public void setAccessCode(int accessCode) {
        this.accessCode = accessCode;
    }

    public byte getSessionCountLimit() {
        return sessionCountLimit;
    }

    public void setSessionCountLimit(byte sessionCountLimit) {
        this.sessionCountLimit = sessionCountLimit;
    }
}
