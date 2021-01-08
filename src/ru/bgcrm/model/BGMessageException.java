package ru.bgcrm.model;

/**
 * Localized message, shown to end user and not written in log.
 * @author Shamil Vakhitov
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
