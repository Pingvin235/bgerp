package ru.bgcrm.model.user;

/**
 * Exception must be thrown from action method existing only in {@code action.xml} file.
 *
 * @author Shamil Vakhitov
 */
public class PermissionActionMethodException extends UnsupportedOperationException {
    public PermissionActionMethodException() {
        super("The method exists only for permission checking.");
    }
}
