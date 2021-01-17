package ru.bgcrm.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.util.AbstractMap;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import ru.bgcrm.dynamic.DynamicClassManager;
import ru.bgcrm.model.BGMessageException;
import ru.bgcrm.util.Config.InitStopException;
import ru.bgerp.util.Log;

/**
 * Исходный map параметров. Его главная функция - #{@link ParameterMap#get(String, String)}.
 * Остальные используют эту функцию для парсинга значений.
 */
public abstract class ParameterMap extends AbstractMap<String, String> {
    private static final Log log = Log.getLog();

    /**
     * Конфигурации, разбираются при первом обращении и кешируются далее.
     */
    protected volatile ConcurrentHashMap<Class<?>, Config> configMap;

    private static class DefaultParameterMap extends ParameterMap {
        protected Map<String, String> data;

        public DefaultParameterMap(Map<String, String> data) {
            this.data = data;
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

    public static ParameterMap EMPTY = new DefaultParameterMap(Collections.emptyMap());

    public static ParameterMap of(Object... keyValues) {
        Map<String, String> map = new HashMap<>(keyValues.length / 2);
        for (int i = 0; i < keyValues.length - 1; i += 2)
            map.put(String.valueOf(keyValues[i]), String.valueOf(keyValues[i + 1]));
        return new DefaultParameterMap(map);
    }

    protected String mapPrint = null;

    public abstract String get(String key, String def);

    @Override
    public String get(Object key) {
        return get((String) key, null);
    }

    /**
     * Retrieves value with Support of Old Keys.
     * @param def default value
     * @param validate true - throw an exception on using old keys
     * @param keys first element is actual ond, after - old values
     * @return
     * @throws BGMessageException
     */
    public String getSok(String def, boolean validate, String... keys) throws BGMessageException {
        String value = get(keys[0]);
        if (!Utils.isEmptyString(value))
            return value;

        for (int i = 1; i < keys.length; i++) {
            value = get(keys[i]);
            if (!Utils.isEmptyString(value)) {
                var message = String.format("Using deprecated config key '%s', new one is: '%s'", keys[i], keys[0]);
                if (validate)
                    throw new BGMessageException(message);
                log.warn(message);
                return value;
            }
        }

        return def;
    }

    /**
     * Calls {@link #getSok(String, boolean, String...)} with def = null and validate = false.
     * @param keys
     * @return
     * @throws BGMessageException
     */
    public String getSok(String... keys) throws BGMessageException {
        return getSok(null, false, keys);
    }

    public int getInt(String key, int def) {
        try {
            final String value = get(key, null);
            if (Utils.isEmptyString(value))
                return def;
            else
                return Integer.parseInt(value.trim());
        } catch (Exception ex) {
            return def;
        }
    }

    public int getInt(String key) {
        return getInt(key, 0);
    }

    public long getLong(String key, long def) {
        try {
            final String value = get(key, null);
            if (Utils.isEmptyString(value))
                return def;
            else
                return Long.parseLong(value.trim());
        } catch (Exception ex) {
            return def;
        }
    }

    public long getLong(String key) {
        return getLong(key, 0L);
    }

    /**
     * Analog of {@link #getSok(String, boolean, String...)} for long values.
     * @throws BGMessageException
     */
    public long getSokLong(long def, boolean validate, String... keys) throws BGMessageException {
        return Utils.parseLong(getSok(String.valueOf(def), validate, keys), def);
    }

    /**
     * Calls {@link #getSokLong(long, boolean, String...)} with validate=false.
     * @throws BGMessageException
     */ 
    public long getSokLong(long def, String... keys) throws BGMessageException {
        return getSokLong(def, false, keys);
    }

    public final boolean getBoolean(String key, boolean defaultValue) {
        return Utils.parseBoolean(get(key, "").trim(), defaultValue);
    }

    public BigDecimal getBigDecimal(final String key, final BigDecimal def) {
        try {
            final String value = get(key, null);
            if (Utils.isEmptyString(value))
                return def;
            else
                return new BigDecimal(value.trim());
        } catch (Exception ex) {
            return def;
        }
    }

    /** The data type is not needed in business app. */
    @Deprecated
    public float getFloat(String key, float def) {
        try {
            final String value = get(key, null);
            if (Utils.isEmptyString(value))
                return def;
            else
                return Float.parseFloat(value.trim());
        } catch (Exception ex) {
            return def;
        }
    }

    /** The data type is not needed in business app. */
    @Deprecated
    public double getDouble(String key, double def) {
        try {
            final String value = get(key, null);
            if (Utils.isEmptyString(value))
                return def;
            else
                return Double.parseDouble(value.trim());
        } catch (Exception ex) {
            return def;
        }
    }

    public abstract Set<Map.Entry<String, String>> entrySet();

    public String fingerprint() {
        throw new UnsupportedOperationException();
    }

    /**
     * Извлечение поднабора параметров по префиксу.
     * @param prefix
     * @return
     */
    public ParameterMap sub(String... prefixies) {
        Map<String, String> result = new HashMap<String, String>();

        for (Entry<String, String> e : entrySet()) {
            String paramKey = e.getKey();
            for (String prefix : prefixies) {
                if (paramKey.startsWith(prefix)) {
                    String suffix = paramKey.substring(prefix.length(), paramKey.length());
                    result.put(suffix, e.getValue());
                }
            }
        }

        return new ParameterMap.DefaultParameterMap(result);
    }

    /**
     * Сериализация набора параметров в строку <prefix><ключ>=<значение> с переносами строк.
     * @return
     */
    public String getDataString() {
        return getDataString("");
    }

    /**
     * Сериализация набора параметров в строку <prefix><ключ>=<значение> с переносами строк.
     * К каждой строке добавляется префикс.
     * @param prefix
     * @return
     */
    public String getDataString(String prefix) {
        StringBuilder result = new StringBuilder();

        for (Entry<String, String> e : entrySet()) {
            result.append(prefix);
            result.append(e.getKey());
            result.append("=");
            result.append(e.getValue());
            result.append("\n");
        }

        return result.toString();
    }

    /**
     * Для JSP получение конфига в текстовом виде.
     * Нельзя использовать .dataString, т.к. ParameterMap будет отдавать как Map.
     * @return
     */
    public static final String getDataString(ParameterMap config) {
        if (config != null) {
            return config.getDataString();
        }
        return "";
    }

    private static final Pattern patternDot = Pattern.compile("\\.");

    /**
     * Возвращает новый мап. Берёт всё под префиксами и иставляет мэп из
     * числовых ид за ними и последующих значений, формируя из них ParameterMap.
     * Аналогична subKeyed, но составляет сортированный мэп с числовыми ключами.
     * <pre>
     * prefix.1.12=2
     * prefix.1.34=4
     * prefix.2.56=2
     * prefix.2.78=4
     * ->
     * сортированный мэп
     * 1={12=2,34=4}
     * 2={56=2,78=4}</pre>
     * @param prefix префикс определяющий мэп
     * @return SortedMap. Никогда не null.
     * @see #subKeyed(String)
     */
    public SortedMap<Integer, ParameterMap> subIndexed(final String... prefixies) {
        SortedMap<Integer, ParameterMap> result = new TreeMap<Integer, ParameterMap>();
        Map<Integer, Map<String, String>> resultMap = new HashMap<Integer, Map<String, String>>();

        for (Entry<String, String> e : entrySet()) {
            String paramKey = e.getKey();
            for (String prefix : prefixies) {
                if (paramKey.startsWith(prefix)) {
                    String suffix = paramKey.substring(prefix.length(), paramKey.length());

                    String[] pref = patternDot.split(suffix, 2);
                    try {
                        Integer.parseInt(pref[0]);
                    } catch (Exception ex) {
                        continue;
                    }

                    Integer id = Utils.parseInt(pref[0]);

                    Map<String, String> map = resultMap.get(id);
                    if (map == null) {
                        resultMap.put(id, map = new HashMap<String, String>());
                        result.put(id, new ParameterMap.DefaultParameterMap(map));
                    }

                    if (pref.length == 2) {
                        map.put(pref[1], e.getValue());
                    } else {
                        map.put("", e.getValue());
                    }
                }
            }
        }

        return result;
    }

    /**
     * Возвращает новый мап. Берёт всё под префиксами и иставляет мэп из
     * строковых ид за ними и последующих значений, формируя из них ParameterMap.
     * Аналогична subIndexed, но составляет несортированный мэп со строковыми ключами.
     * <pre>
     * prefix.a.12=2
     * prefix.a.34=4
     * prefix.b.56=2
     * prefix.b.78=4
     * ->
     * несортированный мэп
     * a={12=2,34=4}
     * b={56=2,78=4}</pre>
     * @param prefix префикс определяющий мэп
     * @return Map. Никогда не null.
     * @see #subIndexed(String)
     */
    public Map<String, ParameterMap> subKeyed(final String... prefixies) {
        Map<String, ParameterMap> result = new HashMap<String, ParameterMap>();
        Map<String, Map<String, String>> resultMap = new HashMap<String, Map<String, String>>();

        for (Entry<String, String> e : entrySet()) {
            String paramKey = e.getKey();
            for (String prefix : prefixies) {
                if (paramKey.startsWith(prefix)) {
                    String suffix = paramKey.substring(prefix.length(), paramKey.length());

                    String[] pref = patternDot.split(suffix, 2);

                    Map<String, String> map = resultMap.get(pref[0]);
                    if (map == null) {
                        resultMap.put(pref[0], map = new HashMap<String, String>());
                        result.put(pref[0], new ParameterMap.DefaultParameterMap(map));
                    }

                    if (pref.length == 2) {
                        map.put(pref[1], e.getValue());
                    } else {
                        map.put("", e.getValue());
                    }
                }
            }
        }

        return result;
    }

    /**
     * Use {@link #subKeyed(String...)}
     * @param prefix
     * @return
     */
    @Deprecated
    public Map<String, Map<String, String>> parseObjectsNoOrder(String prefix) {
        Map<String, Map<String, String>> result = new HashMap<String, Map<String, String>>();
        for (Map.Entry<String, String> value : sub(prefix).entrySet()) {
            String id = null;
            String key = null;

            int pos = value.getKey().indexOf('.');
            if (pos <= 0) {
                continue;
            }

            id = value.getKey().substring(0, pos);
            key = value.getKey().substring(pos + 1);

            Map<String, String> data = result.get(id);
            if (data == null) {
                data = new HashMap<String, String>();
                data.put("id", String.valueOf(id));
                result.put(id, data);
            }

            data.put(key, value.getValue());
        }
        return result;
    }

    /**
     * Creates if needed and gets pre parsed and cached configuration. 
     * Cache key - the class object of the configuration.
     * @param clazz
     * @return
     */
    @SuppressWarnings("unchecked")
    public final <K extends Config> K getConfig(final Class<K> clazz) {
        synchronized (this) {
            if (configMap == null) {
                configMap = new ConcurrentHashMap<Class<?>, Config>();
            }
        }

        K result = (K) configMap.get(clazz);
        if (result == null) {
           try {
                result = createConfig(clazz, false);
                if (result == null)
                    result = (K) Config.EMPTY;
                configMap.put(clazz, result);
            } catch (Exception e) {
                log.error(e);
            }
        }
        return result == Config.EMPTY ? null : result;
    }

    /**
     * Same with {@link #getConfig(Class)}, but with string parameter for calling from JSP and JEXL.
     * @param className
     * @return
     */
    @SuppressWarnings("unchecked")
    public final Object getConfig(String className) {
        try {
            return getConfig((Class<Config>) DynamicClassManager.getClass(className));
        } catch (Exception e) {
            log.error(e);
        }
        return null;
    }

    /**
     * Removes config from cache.
     * @param <K>
     * @param clazz
     */
    public <K extends Config> void removeConfig(Class<K> clazz) {
        configMap.remove(clazz);
    }

    /**
     * Creates a configuration for validation purposes only..
     * @param clazz
     * @return
     */
    public final <K extends Config> void validateConfig(final Class<K> clazz) throws BGMessageException {
        try {
            createConfig(clazz, true);
        } catch (BGMessageException e) {
            throw e;
        } catch (Exception e) {
            log.error(e);
            throw new BGMessageException(e.getMessage());
        }
    }

    private static final Class<?>[] getConfigParamsValidate = new Class[] { ParameterMap.class, boolean.class };
    private static final Class<?>[] getConfigParams = new Class[] { ParameterMap.class };

    /**
     * Creates a configuration.
     * @param <K>
     * @param clazz
     * @param validate run validation.
     * @return
     * @throws Exception
     */
    private <K extends Config> K createConfig(final Class<K> clazz, boolean validate) throws Exception {
        try {
            try {
                Constructor<K> constr = clazz.getDeclaredConstructor(getConfigParamsValidate);
                if (constr != null) {
                    constr.setAccessible(true);
                    return constr.newInstance(new Object[] { this, validate });
                }
            } catch (NoSuchMethodException e) {}

            Constructor<K> constr = clazz.getDeclaredConstructor(getConfigParams);
            if (constr != null) {
                constr.setAccessible(true);
                return constr.newInstance(new Object[] { this });
            }
        } catch (InvocationTargetException e) {
            var target = (Exception) e.getTargetException();
            if (target instanceof InitStopException)
                return null;
            if (!validate && (target instanceof BGMessageException))
                return null;
            throw target;
        }
        return null;
    }

}
