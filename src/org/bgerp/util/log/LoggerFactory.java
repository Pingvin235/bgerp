package org.bgerp.util.log;

/**
 * Factory setting additivity to {@code false} for configured loggers.
 *
 * @author Shamil Vakhitov
 */
public class LoggerFactory implements org.apache.log4j.spi.LoggerFactory {
    @Override
    public Logger makeNewLoggerInstance(String name) {
        var result = new Logger(name);
        result.setAdditivity(false);
        return result;
    }
}
