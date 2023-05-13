package ru.bgcrm.model;

import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;
import org.bgerp.app.l10n.Localizer;
import org.bgerp.util.Log;

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
        super(pattern);
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
        return lExternal.l(super.getMessage(), args);
    }

    @Override
    public String getLocalizedMessage() {
        return Log.format(super.getLocalizedMessage(), args);
    }

    @Override
    public String getMessage() {
        return Log.format(super.getMessage(), args);
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
        // added manually
        if (StringUtils.compare(getMessage(), other.getMessage()) != 0)
            return false;
        // lInternal is skipped as there is no equals method available
        if (!Arrays.deepEquals(args, other.args))
            return false;
        return true;
    }
}
