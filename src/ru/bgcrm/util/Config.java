package ru.bgcrm.util;

import ru.bgcrm.model.BGMessageException;
import ru.bgerp.util.Log;

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

    protected final ParameterMap setup;

    /**
     * The constructor has to be overwritten and implement parameters parsing.
     * Old-style constructor, without deprecated keys validation support.
     * @param setup configuration.
     */
    protected Config(ParameterMap setup) {
        this(setup, false);
    }

    /**
     * The constructor has to be overwritten and implement parameters parsing.
     * @param setup configuration.
     * @param validate validation old configuration keys.
     */
    protected Config(ParameterMap setup, boolean validate) {
        this.setup = setup;
    }

    /**
     * Check, is the configuration inited.
     * @param criteria
     * @throws InitStopException
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
