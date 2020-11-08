package ru.bgcrm.model;

/**
 * Localized message, not written in log.
 */
public class BGMessageException extends BGException {
    private final Object[] args;

    public BGMessageException(String message, Object... args) {
        super(String.format(message, args));
        this.args = args;
    }

    public Object[] getArgs() {
        return args;
    }
}
