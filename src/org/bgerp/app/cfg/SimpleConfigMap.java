package org.bgerp.app.cfg;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Simple implementation of ConfigMap.
 *
 * @author Shamil Vakhitov
 */
public class SimpleConfigMap extends ConfigMap {
    private Map<String, String> data;

    SimpleConfigMap(Map<String, String> data) {
        this.data = data;
    }

    /**
     * Creates ParameterMap object from key values pairs.
     * @param keyValues key1, value1,... String.valueOf() is applied to each argument.
     * @return
     */
    public static SimpleConfigMap of(Object... keyValues) {
        Map<String, String> map = new HashMap<>(keyValues.length / 2);
        for (int i = 0; i < keyValues.length - 1; i += 2)
            map.put(String.valueOf(keyValues[i]), String.valueOf(keyValues[i + 1]));
        return new SimpleConfigMap(map);
    }

    @Override
    public String get(String key, String def) {
        final String result = data.get(key);
        return result != null ? result : def;
    }

    @Override
    public Set<Map.Entry<String, String>> entrySet() {
        return data.entrySet();
    }
}