package org.bgerp.plugin.bgb.getolt.model;

/**
 * Result of OLT port refresh request.
 */
public class RefreshResult {
    private final boolean success;
    private final String errorMessage;
    private final String operationId;
    private final String status;
    private final String message;

    private RefreshResult(boolean success, String errorMessage, String operationId, String status, String message) {
        this.success = success;
        this.errorMessage = errorMessage;
        this.operationId = operationId;
        this.status = status;
        this.message = message;
    }

    public static RefreshResult success(String operationId, String status, String message) {
        return new RefreshResult(true, null, operationId, status, message);
    }

    public static RefreshResult error(String errorMessage) {
        return new RefreshResult(false, errorMessage, null, null, null);
    }

    public boolean isSuccess() {
        return success;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public String getOperationId() {
        return operationId;
    }

    public String getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }
}
