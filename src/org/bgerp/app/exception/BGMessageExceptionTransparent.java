package org.bgerp.app.exception;

/**
 * Use {@link BGMessageExceptionWithoutL10n}
 */
@Deprecated
public class BGMessageExceptionTransparent extends BGMessageExceptionWithoutL10n {
    public BGMessageExceptionTransparent(String pattern, Object... args) {
        super(pattern, args);
    }
}
