package ru.bgcrm.util;

import ru.bgcrm.model.BGMessageException;
import ru.bgerp.util.Log;

/**
 * Конфигурация. Создается один раз и хранится в {@link ParameterMap} до его изменения.
 * В конструкторе реализуется парсинг. 
 * Получение объекта через {@link ParameterMap#getConfig(Class)}.
 */
public abstract class Config {
    protected static final Log log = Log.getLog();
    
    /**
     * Конфигурация создана в режиме валидации, можно выбрасывать {@link BGMessageException}.
     */
    protected final boolean validate;

    protected Config(ParameterMap setup) {
        this.validate = false;
    }
    
    protected Config(ParameterMap setup, boolean validate) {
        this.validate = validate;
    }
}
