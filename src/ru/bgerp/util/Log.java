package ru.bgerp.util;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.Priority;
import org.apache.logging.log4j.util.StackLocatorUtil;

/**
 * Logger wrapper with additional functions.
 * 
 * @author Shamil
 */
public class Log {
    /** Wrapped logger. */
    private final Logger logger;
    
    private Log(Logger logger)  {
        this.logger = logger;
    }
    
    /**
     * Returns class connected logger.
     * @param clazz
     * @return
     */
    public static Log getLog(Class<?> clazz) {
        return new Log(Logger.getLogger(clazz));
    }
    
    /**
     * Returns caller class connected logger.
     * @return
     */
    public static Log getLog() {
       return getLog(StackLocatorUtil.getCallerClass(2));
    }
    
    /**
     * Runs {@link #log(Priority, String)} with {@link Level#DEBUG}
     */
    public void debug(Object message) {
        log(Level.DEBUG, message);
    }
    
    /**
     * Runs {@link #log(Priority, String, Object...)} with {@link Level#DEBUG}
     */
    public void debug(String pattern, Object... params) {
        log(Level.DEBUG, pattern, params);
    }
    
    /** Checks if debug enabled. */
    public boolean isDebugEnabled() {
        return logger.isDebugEnabled();
    }
    
    /**
     * Runs {@link #log(Priority, String)} with {@link Level#INFO}
     */
    public void info(Object message) {
        log(Level.INFO, message);
    }

    /**
     * Runs {@link #log(Priority, String, Object...)} with {@link Level#INFO}
     */
    public void info(String pattern, Object params) {
        log(Level.INFO, pattern, params);
    }
    
    /**
     * Runs {@link #log(Priority, String)} with {@link Level#WARN}
     */
    public void warn(Object message) {
        log(Level.WARN, message);
    }
    
    /**
     * Runs {@link #log(Priority, String, Object...)} with {@link Level#WARN}
     */
    public void warn(String pattern, Object params) {
        log(Level.WARN, pattern, params);
    }
    
    /**
     * Runs {@link #log(Priority, String)} with {@link Level#ERROR}
     */
    public void error(Object message) {
        log(Level.ERROR, message);
    }
    
    /**
     * Runs {@link #log(Priority, String, Object...)} with {@link Level#ERROR}
     */
    public void error(String pattern, Object params) {
        log(Level.ERROR, pattern, params);
    }
    
    public void error(Throwable e) {
        logger.error(e.getMessage(), e);
    }
    
    public void error(String message, Throwable e) {
        logger.error(message, e);
    }
    

    /**
     * Writes log message with possibilities of pattern definitions.
     * Pattern has to be done using {@link String#format(String, Object...)}.
     * @param level
     * @param pattern
     * @param params
     */
    private final void log(Priority level, String pattern, Object... params) {
        if (logger.isEnabledFor(level))
            logger.log(level, String.format(pattern, params));
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
