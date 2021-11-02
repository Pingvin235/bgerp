package ru.bgerp.util;

import org.apache.log4j.Logger;
import org.apache.logging.log4j.util.StackLocatorUtil;

/**
 * Use {@link org.bgerp.util.Log}.
 */
@Deprecated
public class Log extends org.bgerp.util.Log {
    protected Log(Logger logger) {
        super(logger);
    }

    @Deprecated
    public static Log getLog() {
        return getLog(StackLocatorUtil.getCallerClass(2));
    }

    @Deprecated
    public static Log getLog(Class<?> clazz) {
        return new Log(Logger.getLogger(clazz));
    }
}
