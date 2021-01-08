package ru.bgcrm.model;

/**
 * Unexpected situation, not shown to user.
 * @author Shamil Vakhitov
 */
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
     * For calling from scripts.
     * @param message
     * @throws BGException
     */
    public static void throwNew(String message) throws BGException {
        throw new BGException(message);
    }
}
