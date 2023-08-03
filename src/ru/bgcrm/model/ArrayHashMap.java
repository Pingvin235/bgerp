package ru.bgcrm.model;

import java.util.HashMap;
import java.util.Map;

import org.bgerp.util.Log;

/**
 * Map for storing HTTP request parameters,
 * for a string key may be presented multiple values.
 *
 * @author Shamil Vakhitov
 */
public class ArrayHashMap extends HashMap<String, Object> {
    private static final Log log = Log.getLog();

    /* Log tracking ID identifier. */
    private String logTrackingId;

    /**
     * Sets log tracking ID identifier.
     * @param value
     */
    public void setLogTrackingId(String value) {
        this.logTrackingId = value;
    }

    @Override
    public Object put(String arg0, Object arg1) {
        return super.put(arg0, arg1);
    }

    @Override
    public void putAll(Map<? extends String, ? extends Object> arg0) {
        super.putAll(arg0);
    }

    @Override
    public Object get(Object key) {
        return getVal(key);
    }

    private Object getVal(Object key) {
        String[] array = (String[]) super.get(key);
        if (array != null) {
            if (array.length == 1) {
                return array[0];
            }

            log.warn("Error taking as single param: {}, {}", key, logTrackingId);

            return null;
        }
        return null;
    }

    // для получения одиночных строковых параметров
    public String get(String key) {
        return (String) getVal(key);
    }

    // установка одиночных строковых параметров
    // TODO: Возможно, стоит сделать, чтобы одиночные строки так и хранились, а не массивы из одного элемента.
    // Вроде не получается так, т.к. всякие контролы типа checkbox не принимают несколько значений с одним именем если не массив.
    public String put(String key, String value) {
        String oldValue = null;
        String[] oldValueArray = (String[]) super.put(key, new String[] { value });
        if (oldValueArray != null && oldValueArray.length > 0) {
            oldValue = oldValueArray[0];
        }
        return oldValue;
    }

    public String[] getArray(String key) {
        return (String[]) super.get(key);
    }

    public String[] putArray(String key, String[] value) {
        return (String[]) super.put(key, value);
    }

    public String remove(String key) {
        String value = null;
        String[] valueArray = (String[]) super.remove(key);
        if (valueArray != null && valueArray.length > 0) {
            value = valueArray[0];
        }
        return value;
    }
}
