package ru.bgcrm.model;

import ru.bgerp.l10n.Localizer;

/**
 * Localized message, shown to end user and not written in log.
 *
 * @author Shamil Vakhitov
 */
public class BGMessageException extends BGException {
    /** Internal localizer, has priority. */
    private final Localizer lInternal;
    /** Arguments for localized message. */
    private final Object[] args;

    /**
     * Constructor without internal localized.
     * @param message message pattern.
     * @param args message arguments.
     */
    public BGMessageException(String message, Object... args) {
        this(null, message, args);
    }

    /**
     * Constructor with internal localizer.
     * @param lInternal internal localizer.
     * @param message message pattern.
     * @param args message arguments.
     */
    public BGMessageException(Localizer lInternal, String message, Object... args) {
        super(message);
        this.lInternal = lInternal;
        this.args = args;
    }

    /**
     * Provides localized message used internal localized {@link #lInternal}, case if exists, or {@code lExternal}.
     * @param lExternal external localizer.
     * @return
     */
    public String getMessage(Localizer lExternal) {
        if (this.lInternal != null)
            lExternal = this.lInternal;
        return lExternal.l(getMessage(), args);
    }
}
