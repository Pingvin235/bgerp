package ru.bgcrm.model;

/**
 * The exception is unchecked and therefore has not to be caught, same as the actually used {@link org.bgerp.app.exception.BGException}.
 * So just remove the class from your catch expressions.
 */
@Deprecated
public class BGException extends RuntimeException {
    public BGException() {}
}
