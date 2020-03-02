package ru.bgcrm.model;

public class BGException extends Exception {
    public BGException() {}

    public BGException(String message) {
        super(message);
    }

    public BGException(Throwable cause) {
        super(cause);
    }

    public BGException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Для вызова в JEXL скриптах.
     * @param message
     * @throws BGException
     */
    public static void throwNew(String message) throws BGException {
        throw new BGException(message);
    }
}
