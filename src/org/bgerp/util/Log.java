package org.bgerp.util;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.Priority;
import org.apache.logging.log4j.message.FormattedMessage;
import org.apache.logging.log4j.util.StackLocatorUtil;

/**
 * Logging wrapper around Log4j version 1 with additional functions from version 2.
 *
 * @author Shamil Vakhitov
 */
public class Log {
    /**
     * Class connected logger.
     * @param clazz the class.
     * @return logger configured for {@code clazz}
     */
    public static Log getLog(Class<?> clazz) {
        return new Log(Logger.getLogger(clazz));
    }

    /**
     * Using {@link StackLocatorUtil} gets the caller's object and logger for its class.
     * @return logger configured for the caller object's class.
     */
    public static Log getLog() {
       return getLog(StackLocatorUtil.getCallerClass(2));
    }

    /**
     * Formats message using pattern with substitutions.
     * @param message format using {@link FormattedMessage}, supports both {@code &#123;&#125;} and {@code %s} substitutions.
     * @param args parameters for replacements in {@code message}.
     * @return
     */
    public static String format(String message, Object... args) {
        return new FormattedMessage(message, args).getFormattedMessage();
    }

    /** Wrapped logger. */
    private final Logger logger;

    /** Make private later, temporary protected for backward compatible inherited class. */
    protected Log(Logger logger) {
        this.logger = logger;
    }

    /**
     * Executes {@link #log(Priority, String, Object...)} with {@link Level#TRACE}
     */
    public void trace(String message, Object... params) {
        log(Level.TRACE, message, params);
    }

    /**
     * Executes {@link #log(Priority, String, Object...)} with {@link Level#DEBUG}
     */
    public void debug(String message, Object... args) {
        log(Level.DEBUG, message, args);
    }

    /** @return is {@link Level#DEBUG} enabled. */
    public boolean isDebugEnabled() {
        return logger.isDebugEnabled();
    }

    /**
     * Executes {@link #log(Priority, String, Object...)} with {@link Level#INFO}.
     */
    public void info(String message, Object... args) {
        log(Level.INFO, message, args);
    }

    /**
     * Executes {@link #log(Priority, String, Object...)} with {@link Level#WARN}.
     */
    public void warn(String message, Object... args) {
        log(Level.WARN, message, args);
    }

    /**
     * Special warn call for deprecation messages.
     * @param message the message text pattern.
     * @param args
     */
    public void warnd(String message, Object... args) {
        warn(message, args);
    }

    /**
     * Writes deprecation warning for a called method.
     * @param deprecatedName the deprecated method name.
     * @param actualName the actual method name or {@code null} if no such exists.
     */
    public void warndMethod(String deprecatedName, String actualName) {
        if (actualName == null)
            warn("Deprecated method '{}' was called.", deprecatedName);
        else
            warn("Deprecated method '{}' was called. Use '{}' instead.", deprecatedName, actualName);
    }

    /**
     * Writes deprecation warning for a JSP call.
     * @param deprecatedName the deprecated call string.
     * @param actualName the actual call string.
     */
    public void warndJsp(String deprecatedCall, String actionCall) {
        warn("Deprecated JSP call '{}', use '{}' instead.", deprecatedCall, actionCall);
    }

    /**
     * Executes {@link #log(Priority, String, Object...)} with {@link Level#ERROR}.
     */
    public void error(String message, Object... args) {
        log(Level.ERROR, message, args);
    }

    /**
     * Logs error message from {@code e} and stack trace.
     * @param e the exception.
     */
    public void error(Throwable e) {
        logger.error(e.getMessage(), e);
    }

    /**
     * Logs error message and stack trace.
     * @param message message text.
     * @param e exception.
     */
    public void error(String message, Throwable e) {
        logger.error(message, e);
    }

    /**
     * Wraps function to {@link Logger}.
     * @param level
     * @param message
     */
    public final void log(Priority level, Object message) {
        logger.log(level, message);
    }

    /**
     * Writes log message with possibilities of pattern definitions.
     * @param level log level.
     * @param message format using {@link FormattedMessage}, supports both {@code &#123;&#125;} and {@code %s} substitutions.
     * @param args parameters for replacements in {@code pattern}.
     */
    private final void log(Priority level, String message, Object... args) {
        if (logger.isEnabledFor(level)) {
            logger.log(level, format(message, args));
        }
    }
}
