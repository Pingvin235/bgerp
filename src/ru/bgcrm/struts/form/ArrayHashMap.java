package ru.bgcrm.struts.form;

import java.util.HashMap;
import java.util.Map;

import org.bgerp.util.Log;

/**
 * Map for storing HTTP request parameters,
 * for a string key may be presented multiple values.
 * In the fact values are stored as String[],
 * the second Object generic is used only to return String in the overwritten {@link #get(Object)} method.
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
        throw new UnsupportedOperationException("Use put(String, String) or putArray(String, String[]) methods");
    }

    @Override
    public void putAll(Map<? extends String, ?> arg0) {
        throw new UnsupportedOperationException("Use putArrays(Map<String, String[]>) method");
    }

    /**
     * Returns a single string value for a key
     * @param key the key
     * @return the value or {@code null}
     */
    @Override
    public Object get(Object key) {
        return get((String) key);
    }

    /**
     * Returns a single value for a key
     * @param key the key
     * @return the value or {@code null}
     */
    public String get(String key) {
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

    /**
     * Stores a single parameter value for a key
     * @param key the key
     * @param value the value
     */
    void put(String key, String value) {
        super.put(key, new String[] { value });
    }

    /**
     * Returns a raw string array value for a key
     * @param key the key
     * @return the values array or {@code null}
     */
    public String[] getArray(String key) {
        return (String[]) super.get(key);
    }

    /**
     * Stores a raw string array value for a key
     * @param name the key
     * @param value the value
     */
    public void putArray(String name, String[] value) {
        super.put(name, value);
    }

    /**
     * Stores multiple values for multiple keys
     * @param values the key-value map
     */
    public void putArrays(Map<String, String[]> values) {
        super.putAll(values);
    }
}
