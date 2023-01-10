package ru.bgcrm.plugin.bgbilling.proto.model.inet;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ru.bgcrm.plugin.bgbilling.Utils;
import ru.bgcrm.util.TimeUtils;

public class InetSessionLog extends InetSession {
    /**
     * Сессия, которая была до разделения
     */
    private Date connectionStart;

    /**
     * Родительская сессия для этой сервисной сессии.
     */
    private long parentConnectionId;

    private String realm;

    private int contractId;
    private int servId;

    private int deviceId;
    private int devicePort;
    private String deviceTitle;

    private int agentDeviceId;

    private String acctSessId;

    private String username;

    private String cdsId;
    private String cnsId;

    private String circuitId;

    private int ipResourceId;
    private String ip;

    private int type;
    private int accessCode;

    private Set<Integer> deviceOptions;

    //нельзя тут ставить this.getClass() , не серилизуется потом это
    private Map<Integer, Long> trafficMap = new HashMap<Integer, Long>();
    private Map<Integer, InetSessionLogAccount> accountMap = new HashMap<Integer, InetSessionLogAccount>();

    private List<InetSessionLog> children;

    private String contractComment = "";
    private String contractTitle = "";
    private String serviceTitle = "";

    public InetSessionLog() {
    }

    public int getContractId() {
        return contractId;
    }

    public void setContractId(int contractId) {
        this.contractId = contractId;
    }

    public int getServId() {
        return servId;
    }

    public void setServId(int loginId) {
        this.servId = loginId;
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

    public int getAgentDeviceId() {
        return agentDeviceId;
    }

    public void setAgentDeviceId(int agentDeviceId) {
        this.agentDeviceId = agentDeviceId;
    }

    public Date getConnectionStart() {
        return connectionStart;
    }

    public void setConnectionStart(Date connectionStart) {
        this.connectionStart = connectionStart;
    }

    public long getParentConnectionId() {
        return parentConnectionId;
    }

    public void setParentConnectionId(long parentSessionId) {
        this.parentConnectionId = parentSessionId;
    }

    public String getRealm() {
        return realm;
    }

    public void setRealm(String realm) {
        this.realm = realm;
    }

    public List<InetSessionLog> getChildren() {
        return children;
    }

    public void setChildren(List<InetSessionLog> children) {
        this.children = children;
    }

    public String getAcctSessId() {
        return acctSessId;
    }

    public void setAcctSessId(String acctSessionId) {
        this.acctSessId = acctSessionId;
    }

    @Deprecated
    public String getAcctSessionId() {
        return acctSessId;
    }

    @Deprecated
    public void setAcctSessionId(String acctSessionId) {
        this.acctSessId = acctSessionId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String userName) {
        this.username = userName;
    }

    public String getCdsId() {
        return cdsId;
    }

    public void setCdsId(String fromNumber) {
        this.cdsId = fromNumber;
    }

    @Deprecated
    public String getCalledStationId() {
        return cdsId;
    }

    @Deprecated
    public void setCalledStationId(String fromNumber) {
        this.cdsId = fromNumber;
    }

    public String getCnsId() {
        return cnsId;
    }

    public void setCnsId(String toNumber) {
        this.cnsId = toNumber;
    }

    @Deprecated
    public String getCallingStationId() {
        return cnsId;
    }

    @Deprecated
    public void setCallingStationId(String toNumber) {
        this.cnsId = toNumber;
    }

    public String getCircuitId() {
        return circuitId;
    }

    public void setCircuitId(String circuitId) {
        this.circuitId = circuitId;
    }

    /** Синоним для сервиса {@link #getCircuitId()} */
    public String getCrctId() {
        return circuitId;
    }

    /** Синоним для сервиса {@link #setCircuitId(String)} */
    public void setCrctId(String value) {
        circuitId = value;
    }

    public int getIpResourceId() {
        return ipResourceId;
    }

    public void setIpResourceId(int ipResourceId) {
        this.ipResourceId = ipResourceId;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String inetAddress) {
        this.ip = inetAddress;
    }

    @Deprecated
    public String getInetAddress() {
        return ip;
    }

    @Deprecated
    public void setInetAddress(String inetAddress) {
        this.ip = inetAddress;
    }

    /**
     * Авторизированная ли сессия. Если true - как fake сессия в dialup.
     * @return
     */
    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    /**
     * Для неавторизованной сессии - код ошибки.
     * @return
     */
    public int getAccessCode() {
        return accessCode;
    }

    public void setAccessCode(int accessCode) {
        this.accessCode = accessCode;
    }

    public Set<Integer> getDeviceOptions() {
        return deviceOptions;
    }

    public void setDeviceOptions(Set<Integer> deviceOptions) {
        this.deviceOptions = deviceOptions;
    }

    @Override
    public InetSessionLog clone() throws CloneNotSupportedException {
        return (InetSessionLog) super.clone();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(64);

        sb.append("InetSession [id=").append(connectionId).append('-').append(parentConnectionId).append('-').append(id)
                .append(',');
        sb.append(" iface=").append(deviceId).append(':').append(devicePort).append(',');
        sb.append(" sessId=").append(acctSessId).append(',');
        sb.append(" start=").append(TimeUtils.format(start, "dd.MM.yyyy HH:mm:ss")).append(',');
        sb.append(" uname=").append(username).append(',');
        sb.append(" addr=").append(ip).append(']');

        return sb.toString();
    }

    public Map<Integer, Long> getTrafficMap() {
        return trafficMap;
    }

    public void setTrafficMap(Map<Integer, Long> trafficMap) {
        this.trafficMap = trafficMap;
    }

    public Map<Integer, InetSessionLogAccount> getAccountMap() {
        return accountMap;
    }

    public void setAccountMap(Map<Integer, InetSessionLogAccount> accountMap) {
        this.accountMap = accountMap;
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

    public String getContractComment() {
        return contractComment;
    }

    public void setContractComment(String contractComment) {
        this.contractComment = contractComment;
    }

    public void setContractTitle(String contractTitle) {
        this.contractTitle = contractTitle;
    }

    public String getContractTitle() {
        return contractTitle;
    }

    public String getServiceTitle() {
        return serviceTitle;
    }

    public void setServiceTitle(String serviceTitle) {
        this.serviceTitle = serviceTitle;
    }

    public InetSession toSession() {
        InetSession result = new InetSession();
        result.setConnectionId(this.getConnectionId());
        result.setId(this.getId());
        result.setLastActive(this.getLastActive());
        result.setSessionCost(this.getSessionCost());
        result.setSessionStart(this.getSessionStart());
        result.setSessionStop(this.getSessionStop());
        result.setSessionTime(this.getSessionTime());
        result.setStatus(this.getStatus());

        return result;
    }

    /**
     * Возвращает строку "с номера/на номер" для отображения в таблице
     * @return
     */
    public String getFromNumberToNumberAsString() {
        String result = "";
        boolean flag = false;
        if (getCallingStationId() != null) {
            result = result + getCallingStationId() + "";
            flag = true;
        }

        if (getCalledStationId() != null) {
            if (flag) {
                result = result + "/";
            }

            result = result + getCalledStationId();
        }

        return result;
    }

    /**
     * Возвращает продолжительность сессии в строковом виде.
     * @return
     */
    public String getDurationAsString() {
        String result;

        Long amount = getTrafficMap().get(TrafficType.TIME_ID);
        if (amount != null && amount != 0) {
            result = Utils.formatSessionTime(amount.intValue());
        } else {
            result = "0";
        }

        return result;
    }

    public String getSessionStartAsString() {
        return TimeUtils.format(getSessionStart(), TimeUtils.PATTERN_DDMMYYYYHHMMSS);
    }

    public String getSessionStopAsString() {
        return TimeUtils.format(getSessionStop(), TimeUtils.PATTERN_DDMMYYYYHHMMSS);
    }

    public String getSessionActivityAsString() {
        return TimeUtils.format(getLastActive(), TimeUtils.PATTERN_DDMMYYYYHHMMSS);
    }
}