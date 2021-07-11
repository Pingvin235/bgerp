package ru.bgerp.util;

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
    /** Wrapped logger. */
    private final Logger logger;

    private Log(Logger logger)  {
        this.logger = logger;
    }

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
     * Executes {@link #log(Priority, String)} with {@link Level#DEBUG}
     */
    public void debug(Object message) {
        log(Level.DEBUG, message);
    }

    /**
     * Executes {@link #log(Priority, String, Object...)} with {@link Level#DEBUG}
     */
    public void debug(String pattern, Object... params) {
        log(Level.DEBUG, pattern, params);
    }

    /** @return is {@link Level#DEBUG} enabled. */
    public boolean isDebugEnabled() {
        return logger.isDebugEnabled();
    }

    /**
     * Executes {@link #log(Priority, String)} with {@link Level#INFO}.
     */
    public void info(Object message) {
        log(Level.INFO, message);
    }

    /**
     * Executes {@link #log(Priority, String, Object...)} with {@link Level#INFO}.
     */
    public void info(String pattern, Object... params) {
        log(Level.INFO, pattern, params);
    }

    /**
     * Executes {@link #log(Priority, String)} with {@link Level#WARN}.
     */
    public void warn(Object message) {
        log(Level.WARN, message);
    }

    /**
     * Executes {@link #log(Priority, String, Object...)} with {@link Level#WARN}.
     */
    public void warn(String pattern, Object... params) {
        log(Level.WARN, pattern, params);
    }

    /**
     * Executes {@link #log(Priority, String)} with {@link Level#ERROR}.
     */
    public void error(Object message) {
        log(Level.ERROR, message);
    }

    /**
     * Executes {@link #log(Priority, String, Object...)} with {@link Level#ERROR}.
     */
    public void error(String pattern, Object params) {
        log(Level.ERROR, pattern, params);
    }

    /**
     * Logs error message from {@code e} and stack trace.
     * @param e exception.
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
     * Writes log message with possibilities of pattern definitions.
     * @param level log level.
     * @param pattern format using {@link FormattedMessage}, supports both {@code &#123;&#125;} and {@code %s} substitutions.
     * @param params parameters for replacements in {@code pattern}.
     */
    private final void log(Priority level, String pattern, Object... params) {
        if (logger.isEnabledFor(level)) {
            logger.log(level, format(pattern, params));
        }
    }

    /**
     * Universal formatter.
     * @param pattern format using {@link FormattedMessage}, supports both {@code &#123;&#125;} and {@code %s} substitutions.
     * @param params parameters for replacements in {@code pattern}.
     * @return
     */
    public static String format(String pattern, Object... params) {
        return new FormattedMessage(pattern, params).getFormattedMessage();
    }

    /**
     * Wrapper function for {@link Logger}.
     * @param level
     * @param message
     */
    public final void log(Priority level, Object message) {
        logger.log(level, message);
    }
}
