package ru.bgcrm.model.param;

import org.bgerp.cache.ParameterCache;
import org.bgerp.model.param.Parameter;

/**
 * Объект, найденный по значению параметра.
 * @param <T>
 */
public class ParameterSearchedObject<T> {
    private final T object;
    protected final Parameter param;
    protected final Object value;

    public ParameterSearchedObject(T object, int paramId, Object value) {
        this.object = object;
        this.param = ParameterCache.getParameter(paramId);
        this.value = value;
    }

    public T getObject() {
        return object;
    }

    public Parameter getParam() {
        return param;
    }

    public Object getValue() {
        return value;
    }
}
