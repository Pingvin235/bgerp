package org.bgerp.plugin.bgb.getolt.model;

/**
 * Result of session drop operation.
 */
public class SessionDropResult {
    private final boolean success;
    private final String message;
    private final int droppedSessions;

    private SessionDropResult(boolean success, String message, int droppedSessions) {
        this.success = success;
        this.message = message;
        this.droppedSessions = droppedSessions;
    }

    /**
     * Create success result.
     */
    public static SessionDropResult success(int droppedSessions) {
        String msg = droppedSessions > 0
            ? "Сброшено сессий: " + droppedSessions
            : "Активных сессий не найдено";
        return new SessionDropResult(true, msg, droppedSessions);
    }

    /**
     * Create success result with custom message.
     */
    public static SessionDropResult success(String message, int droppedSessions) {
        return new SessionDropResult(true, message, droppedSessions);
    }

    /**
     * Create error result.
     */
    public static SessionDropResult error(String message) {
        return new SessionDropResult(false, message, 0);
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public int getDroppedSessions() {
        return droppedSessions;
    }

    @Override
    public String toString() {
        return "SessionDropResult{success=" + success + ", message='" + message +
               "', droppedSessions=" + droppedSessions + "}";
    }
}
