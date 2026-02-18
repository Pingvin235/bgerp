package org.bgerp.plugin.bgb.getolt.model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.bgerp.plugin.bgb.getolt.model.json.LocalDateTimeDeserializer;
import org.bgerp.plugin.bgb.getolt.model.json.StringOrListDeserializer;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 * ONU data model representing information from GetOLT API.
 * Uses Jackson annotations for automatic JSON deserialization.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class OnuData {
    private static final DateTimeFormatter DISPLAY_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

    @JsonProperty("mac")
    private String mac;

    @JsonProperty("oltIp")
    @JsonAlias({"olt_ip", "olt"})
    private String oltIp;

    @JsonProperty("oltId")
    @JsonAlias("olt_id")
    private Integer oltId;

    @JsonProperty("portNumber")
    @JsonAlias({"port", "port_number"})
    private int port;

    @JsonProperty("onuId")
    @JsonAlias({"onu_id", "id"})
    private int onuId;

    @JsonProperty("status")
    private String status;

    @JsonIgnore
    private boolean online;

    private Double rxSignal;

    @JsonIgnore
    private String rxQuality;

    private Double txSignal;

    @JsonIgnore
    private String txQuality;

    @JsonProperty("distance")
    private Integer distance;

    @JsonProperty("temperature")
    @JsonAlias("temp")
    private Double temperature;

    @JsonProperty("voltage")
    private Double voltage;

    @JsonProperty("macsBehind")
    @JsonAlias({"macs_behind", "macBehind"})
    @JsonDeserialize(using = StringOrListDeserializer.class)
    private List<String> macsBehind;

    @JsonProperty("neighbors")
    private List<PortNeighbor> neighbors;

    @JsonProperty("contractNumber")
    @JsonAlias({"contract_number", "contract"})
    private String contractNumber;

    @JsonProperty("cid")
    private Integer cid;

    @JsonProperty("operator")
    private String operator;

    @JsonProperty("lastUpdate")
    @JsonAlias("last_update")
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime lastUpdate;

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public String getOltIp() {
        return oltIp;
    }

    public void setOltIp(String oltIp) {
        this.oltIp = oltIp;
    }

    public Integer getOltId() {
        return oltId;
    }

    public void setOltId(Integer oltId) {
        this.oltId = oltId;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getOnuId() {
        return onuId;
    }

    public void setOnuId(int onuId) {
        this.onuId = onuId;
    }

    public String getStatus() {
        return status;
    }

    @JsonSetter("status")
    public void setStatus(String status) {
        this.status = status;
        this.online = "online".equalsIgnoreCase(status) || "up".equalsIgnoreCase(status);
    }

    public boolean isOnline() {
        return online;
    }

    public Double getRxSignal() {
        return rxSignal;
    }

    @JsonSetter("rxOptical")
    @JsonAlias({"rxSignal", "rx_signal", "rx"})
    public void setRxSignal(Double rxSignal) {
        this.rxSignal = rxSignal;
        this.rxQuality = calculateRxQuality(rxSignal);
    }

    public Double getTxSignal() {
        return txSignal;
    }

    @JsonSetter("txOptical")
    @JsonAlias({"txSignal", "tx_signal", "tx"})
    public void setTxSignal(Double txSignal) {
        this.txSignal = txSignal;
        this.txQuality = calculateTxQuality(txSignal);
    }

    public String getRxQuality() {
        return rxQuality;
    }

    public String getTxQuality() {
        return txQuality;
    }

    public Integer getDistance() {
        return distance;
    }

    public void setDistance(Integer distance) {
        this.distance = distance;
    }

    public Double getTemperature() {
        return temperature;
    }

    public void setTemperature(Double temperature) {
        this.temperature = temperature;
    }

    public Double getVoltage() {
        return voltage;
    }

    public void setVoltage(Double voltage) {
        this.voltage = voltage;
    }

    public List<String> getMacsBehind() {
        return macsBehind;
    }

    public void setMacsBehind(List<String> macsBehind) {
        this.macsBehind = macsBehind;
    }

    public List<PortNeighbor> getNeighbors() {
        return neighbors;
    }

    public void setNeighbors(List<PortNeighbor> neighbors) {
        this.neighbors = neighbors;
    }

    public String getContractNumber() {
        return contractNumber;
    }

    public void setContractNumber(String contractNumber) {
        this.contractNumber = contractNumber;
    }

    public Integer getCid() {
        return cid;
    }

    public void setCid(Integer cid) {
        this.cid = cid;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public LocalDateTime getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(LocalDateTime lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public String getLastUpdateFormatted() {
        return formatDateTime(lastUpdate);
    }

    public String getLastUpdateAgo() {
        return timeAgo(lastUpdate);
    }

    /**
     * Calculate RX signal quality based on dBm value.
     * Thresholds based on GetOLT UI:
     * - Overexposed: 0 to -15 dBm
     * - Good: -15 to -25 dBm
     * - Medium: -25 to -27 dBm
     * - Poor: < -27 dBm
     */
    static String calculateRxQuality(Double signal) {
        if (signal == null) return null;
        if (signal > -15) return "overexposed";
        if (signal >= -25) return "good";
        if (signal >= -27) return "medium";
        return "poor";
    }

    /**
     * Calculate TX signal quality.
     * Thresholds: 0.5 to 5 dBm is normal.
     */
    private static String calculateTxQuality(Double signal) {
        if (signal == null) return null;
        if (signal > 5) return "overexposed";
        if (signal >= 0.5) return "good";
        if (signal >= -1) return "medium";
        return "poor";
    }

    /**
     * Get CSS class for signal quality color.
     */
    public static String getQualityClass(String quality) {
        if (quality == null) return "";
        return switch (quality) {
            case "good" -> "getolt-signal-good";
            case "medium" -> "getolt-signal-medium";
            case "poor" -> "getolt-signal-poor";
            case "overexposed" -> "getolt-signal-overexposed";
            default -> "";
        };
    }

    /**
     * Format LocalDateTime to display string.
     */
    static String formatDateTime(LocalDateTime dt) {
        return dt != null ? dt.format(DISPLAY_FORMAT) : null;
    }

    /**
     * Get human-readable "time ago" string.
     */
    static String timeAgo(LocalDateTime dt) {
        if (dt == null) return null;
        long seconds = Duration.between(dt, LocalDateTime.now()).getSeconds();
        if (seconds < 60) return seconds + " сек. назад";
        if (seconds < 3600) return seconds / 60 + " мин. назад";
        if (seconds < 86400) return seconds / 3600 + " ч. назад";
        return seconds / 86400 + " дн. назад";
    }

    /**
     * Port neighbor model
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PortNeighbor {
        @JsonProperty("mac")
        private String mac;

        @JsonProperty("onuId")
        @JsonAlias({"onu_id", "id"})
        private int onuId;

        @JsonProperty("status")
        private String status;

        @JsonProperty("contractNumber")
        @JsonAlias({"contract_number", "contract"})
        private String contractNumber;

        private Double rxOptical;

        @JsonIgnore
        private String signalQuality;

        @JsonProperty("lastUpdate")
        @JsonAlias("last_update")
        @JsonDeserialize(using = LocalDateTimeDeserializer.class)
        private LocalDateTime lastUpdate;

        public String getMac() {
            return mac;
        }

        public void setMac(String mac) {
            this.mac = mac;
        }

        public int getOnuId() {
            return onuId;
        }

        public void setOnuId(int onuId) {
            this.onuId = onuId;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getContractNumber() {
            return contractNumber;
        }

        public void setContractNumber(String contractNumber) {
            this.contractNumber = contractNumber;
        }

        public Double getRxOptical() {
            return rxOptical;
        }

        @JsonSetter("rxOptical")
        @JsonAlias({"rxSignal", "rx"})
        public void setRxOptical(Double rxOptical) {
            this.rxOptical = rxOptical;
            this.signalQuality = calculateRxQuality(rxOptical);
        }

        public String getSignalQuality() {
            return signalQuality;
        }

        /**
         * Get CSS class for row background based on signal quality.
         */
        public String getRowClass() {
            if (signalQuality == null) return "";
            return switch (signalQuality) {
                case "poor" -> "getolt-signal-row-poor";
                case "medium" -> "getolt-signal-row-medium";
                case "overexposed" -> "getolt-signal-row-overexposed";
                default -> "";
            };
        }

        public LocalDateTime getLastUpdate() {
            return lastUpdate;
        }

        public void setLastUpdate(LocalDateTime lastUpdate) {
            this.lastUpdate = lastUpdate;
        }

        public String getLastUpdateFormatted() {
            return formatDateTime(lastUpdate);
        }

        public String getLastUpdateAgo() {
            return timeAgo(lastUpdate);
        }
    }
}
