package org.bgerp.app.exception;

import java.util.Arrays;

import org.bgerp.app.l10n.Localizer;
import org.bgerp.util.Log;

/**
 * Localized message, shown to end user and not written in log.
 *
 * @author Shamil Vakhitov
 */
public class BGMessageException extends Exception {
    private static final Log log = Log.getLog();

    /** Internal localizer, has priority. */
    private final Localizer lInternal;
    /** Message pattern. */
    private final String pattern;
    /** Arguments for localized message. */
    private final Object[] args;

    /**
     * Constructor with default kernel + plugin localizer.
     * @param pattern message pattern.
     * @param args message arguments.
     */
    public BGMessageException(String pattern, Object... args) {
        this(null, pattern, args);
    }

    /**
     * Constructor with internal localizer.
     * @param lInternal internal localizer.
     * @param pattern message pattern.
     * @param args message arguments.
     */
    public BGMessageException(Localizer lInternal, String pattern, Object... args) {
        super();
        this.lInternal = lInternal;
        this.pattern = pattern;
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
        return lExternal.l(pattern, args);
    }

    /**
     * Provides exception's message with substituted pattern, but without localization like {@link #getMessage(Localizer)} does.
     * The method should not be normally called, produces WARN to log output.
     * @return
     */
    @Override
    public String getLocalizedMessage() {
        log.warn("The method 'getLocalizedMessage' should not be normally called.");
        return Log.format(pattern, args);
    }

    /**
     * Provides exception's message with substituted pattern, but without localization like {@link #getMessage(Localizer)} does.
     * The method should not be normally called, produces WARN to log output.
     * @return
     */
    @Override
    public String getMessage() {
        log.warn("The method 'getMessage' should not be normally called.");
        return Log.format(pattern, args);
    }

    @Override
    public String toString() {
        return "BGMessageException [pattern=" + pattern + ", args=" + Arrays.toString(args) + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((pattern == null) ? 0 : pattern.hashCode());
        result = prime * result + Arrays.deepHashCode(args);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        BGMessageException other = (BGMessageException) obj;
        if (pattern == null) {
            if (other.pattern != null)
                return false;
        } else if (!pattern.equals(other.pattern))
            return false;
        if (!Arrays.deepEquals(args, other.args))
            return false;
        return true;
    }
}
