package org.bgerp.app.cfg;

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

import org.bgerp.app.bean.Bean;
import org.bgerp.app.cfg.Config.InitStopException;
import org.bgerp.util.Log;

import ru.bgcrm.model.BGMessageException;
import ru.bgcrm.util.Utils;

/**
 * Key - value strings map.
 *
 * @author Shamil Vakhitov
 */
public abstract class ConfigMap extends AbstractMap<String, String> {
    private static final Log log = Log.getLog();

    public static ConfigMap EMPTY = new SimpleConfigMap(Collections.emptyMap());

    private static final Pattern PATTERN_DOT = Pattern.compile("\\.");

    private static final Class<?>[] CONFIG_CONSTR_ARGS_WITH_VALIDATION = new Class[] { ConfigMap.class, boolean.class };
    private static final Class<?>[] CONFIG_CONSTR_ARGS = new Class[] { ConfigMap.class };

    /**
     * Parsed configurations.
     */
    private volatile ConcurrentHashMap<Class<?>, Config> configMap;

    public abstract String get(String key, String def);

    @Override
    public String get(Object key) {
        return get((String) key, null);
    }

    /**
     * Retrieves by key value with support of old keys.
     * @param def default value.
     * @param validate throw an exception on using old keys.
     * @param keys first key is the actual one, after - olds.
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
                var message = Log.format("Using deprecated config key '{}', new one is: '{}'", keys[i], keys[0]);
                if (validate)
                    throw new BGMessageException(message);
                log.warn(message);
                return value;
            }
        }

        return def;
    }

    /**
     * Retrieves by key value with support of old keys.
     * @param keys first key is the actual one, after - olds.
     * @return
     */
    public String getSok(String... keys) {
        try {
            return getSok(null, false, keys);
        } catch (BGMessageException e) {
            throw new IllegalStateException("BGMessageException must not appear here");
        }
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
     * Retrieves by key value with support of old keys.
     * @param def default value.
     * @param validate throw an exception on using old keys.
     * @param keys first key is the actual one, after - olds.
     * @return
     * @throws BGMessageException
     */
    public long getSokLong(long def, boolean validate, String... keys) throws BGMessageException {
        return Utils.parseLong(getSok(String.valueOf(def), validate, keys), def);
    }

   /**
     * Retrieves by key value with support of old keys.
     * @param def default value.
     * @param keys first key is the actual one, after - olds.
     * @return
     * @throws BGMessageException
     */
    public long getSokLong(long def, String... keys) {
        try {
            return getSokLong(def, false, keys);
        } catch (BGMessageException e) {
            throw new IllegalStateException("BGMessageException must not appear here");
        }
    }

    /**
     * Retrieves by key a boolean value with default {@code false}.
     * @param key the key.
     * @return
     * @see #getBoolean(String, boolean)
     */
    public final boolean getBoolean(String key) {
        return getBoolean(key, false);
    }

    /**
     * Retrieves by key a boolean value.
     * @param key the key.
     * @param defaultValue default value.
     * @return
     * @see Utils#parseBoolean(String, Boolean)
     */
    public final boolean getBoolean(String key, boolean defaultValue) {
        return Utils.parseBoolean(get(key, "").trim(), defaultValue);
    }

    /**
     * Retrieves by key value with support of old keys.
     * @param def default value.
     * @param validate throw an exception on using old keys.
     * @param keys first key is the actual one, after - olds.
     * @return
     * @throws BGMessageException
     */
    public boolean getSokBoolean(boolean def, boolean validate, String... keys) throws BGMessageException {
        return Utils.parseBoolean(getSok(String.valueOf(def), validate, keys), def);
    }

    /**
     * Retrieves by key value with support of old keys.
     * @param def default value.
     * @param keys first key is the actual one, after - olds.
     * @return
     * @throws BGMessageException
     */
    public boolean getSokBoolean(boolean def, String... keys) {
        try {
            return getSokBoolean(def, false, keys);
        } catch (BGMessageException e) {
            throw new IllegalStateException("BGMessageException must not appear here");
        }
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

    public abstract Set<Map.Entry<String, String>> entrySet();

    @Deprecated
    public String fingerprint() {
        throw new UnsupportedOperationException();
    }

    /**
     * Selects subset of parameters by key prefixes.
     * @param prefixes key prefixes.
     * @return
     */
    public ConfigMap sub(String... prefixes) {
        Map<String, String> result = new HashMap<String, String>();

        for (Entry<String, String> e : entrySet()) {
            String paramKey = e.getKey();
            for (String prefix : prefixes) {
                if (paramKey.startsWith(prefix)) {
                    String suffix = paramKey.substring(prefix.length(), paramKey.length());
                    result.put(suffix, e.getValue());
                }
            }
        }

        return new SimpleConfigMap(result);
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
     * @Use {@link ConfigMap#getDataString()}.
     */
    @Deprecated
    public static final String getDataString(ConfigMap config) {
        if (config != null) {
            return config.getDataString();
        }
        return "";
    }

    /**
     * Creates a new sorted sub-map with integer keys.
     * <pre>
     * prefix.1.12=2
     * prefix.1.34=4
     * prefix.2.56=2
     * prefix.2.78=4
     * ->
     * sorted {@link Map}
     * 1={12=2,34=4}
     * 2={56=2,78=4}</pre>
     * @param prefixes prefixes for extraction.
     * @return never {@code null}.
     * @see #subKeyed(String)
     */
    public SortedMap<Integer, ConfigMap> subIndexed(final String... prefixes) {
        SortedMap<Integer, ConfigMap> result = new TreeMap<>();
        Map<Integer, Map<String, String>> resultMap = new HashMap<>();

        for (Entry<String, String> e : entrySet()) {
            String paramKey = e.getKey();
            for (String prefix : prefixes) {
                if (paramKey.startsWith(prefix)) {
                    String suffix = paramKey.substring(prefix.length(), paramKey.length());

                    String[] pref = PATTERN_DOT.split(suffix, 2);
                    int id = 0;
                    try {
                        id = Integer.parseInt(pref[0]);
                    } catch (NumberFormatException ex) {
                        continue;
                    }

                    Map<String, String> map = resultMap.get(id);
                    if (map == null) {
                        resultMap.put(id, map = new HashMap<String, String>());
                        result.put(id, new SimpleConfigMap(map));
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
     * Creates a new unsorted sub-map with integer keys.
     * <pre>
     * prefix.a.12=2
     * prefix.a.34=4
     * prefix.b.56=2
     * prefix.b.78=4
     * ->
     * unsorted map
     * a={12=2,34=4}
     * b={56=2,78=4}</pre>
     * @param prefixes prefixes for extraction.
     * @return never {@code null}.
     * @see #subIndexed(String)
     */
    public Map<String, ConfigMap> subKeyed(final String... prefixes) {
        Map<String, ConfigMap> result = new HashMap<>();
        Map<String, Map<String, String>> resultMap = new HashMap<>();

        for (Entry<String, String> e : entrySet()) {
            String paramKey = e.getKey();
            for (String prefix : prefixes) {
                if (paramKey.startsWith(prefix)) {
                    String suffix = paramKey.substring(prefix.length(), paramKey.length());

                    String[] pref = PATTERN_DOT.split(suffix, 2);

                    Map<String, String> map = resultMap.get(pref[0]);
                    if (map == null) {
                        resultMap.put(pref[0], map = new HashMap<String, String>());
                        result.put(pref[0], new SimpleConfigMap(map));
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
        Map<String, Map<String, String>> result = new HashMap<>();
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

        log.trace("getConfig {} => {}", clazz, result);

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
            return getConfig((Class<Config>) Bean.getClass(className));
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
     * Clears all the parsed configurations from {@link #configMap}.
     */
    protected void clearConfigs() {
        configMap = null;
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
                Constructor<K> constr = clazz.getDeclaredConstructor(CONFIG_CONSTR_ARGS_WITH_VALIDATION);
                if (constr != null) {
                    constr.setAccessible(true);
                    return constr.newInstance(new Object[] { this, validate });
                }
            } catch (NoSuchMethodException e) {}

            Constructor<K> constr = clazz.getDeclaredConstructor(CONFIG_CONSTR_ARGS);
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
