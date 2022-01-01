package ru.bgcrm.util;

import org.bgerp.util.Log;

import ru.bgcrm.model.BGMessageException;

/**
 * Configuration, created on demand in {@link ParameterMap#getConfig(Class)} and cached before it has changed.
 * @author Shamil Vakhitov
 */
public abstract class Config {
    protected static final Log log = Log.getLog();

    public static final Config EMPTY = new Config (null) {};
    /**
     * The exception, thrown on empty configuration.
     */
    public static class InitStopException extends BGMessageException {
        public InitStopException() {
            super("Init stop");
        }
    }

    @Deprecated
    protected final ParameterMap setup;

    /**
     * The constructor has to be overwritten and implement parameters parsing.
     * Simple constructor, without deprecated keys validation support.
     * @param config configuration, MUST be {@code null} when calling {@code super}.
     */
    protected Config(ParameterMap config) {
        this(config, false);
    }

    /**
     * The constructor has to be overwritten and implement parameters parsing.
     * @param config configuration, MUST be {@code null} when calling {@code super}.
     * @param validate validation old configuration keys.
     */
    protected Config(ParameterMap config, boolean validate) {
        if (config != null)
            log.warn("Used not null 'config' parameter for constructor of {}", this.getClass());
        this.setup = config;
    }

    /**
     * Check, is the configuration initialized.
     * @param criteria
     * @throws InitStopException if {@param criteria} is false.
     */
    protected void initWhen(boolean criteria) throws InitStopException {
        if (!criteria)
            throw new InitStopException();
    }

    /**
     * Throws validation exception.
     * @param message
     * @param args
     * @return
     * @throws BGMessageException
     */
    protected void throwValidationException(String message, Object... args) throws BGMessageException {
        throw new BGMessageException(message, args);
    }
}
