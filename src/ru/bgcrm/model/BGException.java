package ru.bgcrm.model;

import org.bgerp.util.Log;

/**
 * Unexpected situation, shown to user and written in log.
 *
 * @author Shamil Vakhitov
 */
public class BGException extends RuntimeException {
    public BGException() {}

    /**
     * Constructor with pattern and replacements for {@link Log#format(String, Object...)}.
     * @param pattern message pattern.
     * @param params parameters for replacements in {@code pattern}.
     */
    public BGException(String pattern, Object... args) {
        super(Log.format(pattern, args));
    }

    /**
     * Constructor with root cause to take message from.
     * @param cause root cause.
     */
    public BGException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructor with message and root cause.
     * @param message plain text message.
     * @param cause root cause.
     */
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
