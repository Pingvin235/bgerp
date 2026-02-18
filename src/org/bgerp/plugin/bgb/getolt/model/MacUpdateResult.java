package org.bgerp.plugin.bgb.getolt.model;

import java.util.Collections;
import java.util.List;

/**
 * Result of MAC address update operation.
 */
public class MacUpdateResult {

    public enum Status {
        /** MAC updated successfully */
        OK,
        /** Multiple services found, user must select one */
        MULTIPLE_SERVICES,
        /** Error occurred */
        ERROR,
        /** No services found for contract */
        NO_SERVICES
    }

    private final Status status;
    private final int serviceId;
    private final String oldMac;
    private final String newMac;
    private final String message;
    private final List<InetService> services;

    private MacUpdateResult(Status status, int serviceId, String oldMac, String newMac,
                           String message, List<InetService> services) {
        this.status = status;
        this.serviceId = serviceId;
        this.oldMac = oldMac;
        this.newMac = newMac;
        this.message = message;
        this.services = services != null ? services : Collections.emptyList();
    }

    /**
     * Create success result.
     */
    public static MacUpdateResult success(int serviceId, String oldMac, String newMac) {
        return new MacUpdateResult(Status.OK, serviceId, oldMac, newMac,
                "MAC-адрес успешно обновлён", null);
    }

    /**
     * Create success result with custom message.
     */
    public static MacUpdateResult success(int serviceId, String oldMac, String newMac, String message) {
        return new MacUpdateResult(Status.OK, serviceId, oldMac, newMac, message, null);
    }

    /**
     * Create multiple services result.
     */
    public static MacUpdateResult multipleServices(List<InetService> services) {
        return new MacUpdateResult(Status.MULTIPLE_SERVICES, 0, null, null,
                "Найдено несколько услуг, выберите нужную", services);
    }

    /**
     * Create no services result.
     */
    public static MacUpdateResult noServices() {
        return new MacUpdateResult(Status.NO_SERVICES, 0, null, null,
                "Услуги не найдены для данного договора", null);
    }

    /**
     * Create error result.
     */
    public static MacUpdateResult error(String message) {
        return new MacUpdateResult(Status.ERROR, 0, null, null, message, null);
    }

    public Status getStatus() {
        return status;
    }

    public boolean isSuccess() {
        return status == Status.OK;
    }

    public boolean isMultipleServices() {
        return status == Status.MULTIPLE_SERVICES;
    }

    public boolean isError() {
        return status == Status.ERROR || status == Status.NO_SERVICES;
    }

    public int getServiceId() {
        return serviceId;
    }

    public String getOldMac() {
        return oldMac;
    }

    public String getNewMac() {
        return newMac;
    }

    public String getMessage() {
        return message;
    }

    public List<InetService> getServices() {
        return services;
    }

    @Override
    public String toString() {
        return "MacUpdateResult{status=" + status + ", serviceId=" + serviceId +
               ", oldMac='" + oldMac + "', newMac='" + newMac + "', message='" + message + "'}";
    }
}
