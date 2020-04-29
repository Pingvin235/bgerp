package ru.bgcrm.util;

import ru.bgcrm.model.BGException;
import ru.bgcrm.model.BGMessageException;
import ru.bgerp.util.Log;

/**
 * Configuration, created on demand in {@link ParameterMap#getConfig(Class)} and cached before it has changed.
 */
public abstract class Config {
    protected static final Log log = Log.getLog();

    public static final Config EMPTY = new Config (null) {};

    public static class InitStopException extends BGException {}

    protected final ParameterMap setup;
    
    /**
     * Validation mode {@link BGMessageException}.
     */
    protected final boolean validate;

    protected Config(ParameterMap setup) {
        this.setup = setup;
        this.validate = false;
    }
    
    /**
     * The constructor has to be overwritten and implement parameters parsing.
     * @param setup configuration.
     * @param validate validation on parse.
     */
    protected Config(ParameterMap setup, boolean validate) {
        this.setup = setup;
        this.validate = validate;
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
