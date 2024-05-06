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
import org.bgerp.app.exception.BGMessageException;
import org.bgerp.util.Dynamic;
import org.bgerp.util.Log;

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

    public abstract Set<Map.Entry<String, String>> entrySet();

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
                var message = Log.format("Used deprecated config key '{}', the actual one is '{}'", keys[i], keys[0]);
                if (validate)
                    throw new BGMessageException(message);
                log.warnd(message);
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

    /**
     * Creates a sub map by a key prefix
     * <pre>
     * prefixkey1=value1
     * prefixkey2=value2
     * ->
     * map
     * key1=value1
     * key2=value2</pre>
     * @param prefix the key prefix for extraction
     * @return not {@code null} config map
     */
    public ConfigMap sub(String prefix) {
        return subSok(prefix);
    }

    /**
     * Creates a sub map by key prefixes
     * <pre>
     * prefixkey1=value1
     * prefixkey2=value2
     * ->
     * config map
     * key1=value1
     * key2=value2</pre>
     * @param prefixes key prefixes for extraction, the first one is the actual, the rest are deprecated prefixes
     * @return not {@code null} config map
     */
    public ConfigMap subSok(String... prefixes) {
        Map<String, String> result = new HashMap<>();

        String deprecatedPrefix = null;

        for (Entry<String, String> e : entrySet()) {
            String key = e.getKey();
            for (int i = 0; i < prefixes.length; i++) {
                String prefix = prefixes[i];
                if (key.startsWith(prefix)) {
                    result.put(key.substring(prefix.length(), key.length()), e.getValue());

                    if (i > 0)
                        deprecatedPrefix = prefix;

                    break;
                }
            }
        }

        if (deprecatedPrefix != null)
            log.warnd("Used deprecated config prefix key '{}', the actual one is '{}'", deprecatedPrefix, prefixes[0]);

        return new SimpleConfigMap(result);
    }

    /**
     * Creates a sorted map of maps by a key prefix
     * <pre>
     * prefix1.12=2
     * prefix1.34=4
     * prefix2.56=2
     * prefix2.78=4
     * ->
     * sorted {@link Map}
     * 1={12=2,34=4}
     * 2={56=2,78=4}</pre>
     * @param prefix the key prefix for extraction
     * @return not {@code null} map
     */
    public SortedMap<Integer, ConfigMap> subIndexed(String prefix) {
        return subSokIndexed(prefix);
    }

    /**
     * Creates a sorted map of maps by key prefixes
     * <pre>
     * prefix1.12=2
     * prefix1.34=4
     * prefix2.56=2
     * prefix2.78=4
     * ->
     * sorted {@link Map}
     * 1={12=2,34=4}
     * 2={56=2,78=4}</pre>
     * @param prefixes key prefixes for extraction, the first one is the actual, the rest are deprecated prefixes
     * @return not {@code null} map
     */
    public SortedMap<Integer, ConfigMap> subSokIndexed(String... prefixes) {
        SortedMap<Integer, ConfigMap> result = new TreeMap<>();

        String deprecatedPrefix = null;

        for (Entry<String, String> e : entrySet()) {
            String key = e.getKey();
            for (int i = 0; i < prefixes.length; i++) {
                String prefix = prefixes[i];
                if (key.startsWith(prefix)) {
                    String suffix = key.substring(prefix.length(), key.length());

                    String[] pref = PATTERN_DOT.split(suffix, 2);

                    int id = 0;
                    try {
                        id = Integer.parseInt(pref[0]);
                    } catch (NumberFormatException ex) {
                        continue;
                    }

                    ConfigMap map = result.computeIfAbsent(id, unused -> new SimpleConfigMap(new HashMap<>()));

                    if (pref.length == 2)
                        map.put(pref[1], e.getValue());
                    else
                        map.put("", e.getValue());

                    if (i > 0)
                        deprecatedPrefix = prefix;

                    break;
                }
            }
        }

        if (deprecatedPrefix != null)
            log.warnd("Used deprecated config prefix key '{}', the actual one is '{}'", deprecatedPrefix, prefixes[0]);

        return result;
    }

    /**
     * Creates a map of maps by a key prefix
     * <pre>
     * prefixa.12=2
     * prefixa.34=4
     * prefixb.56=u
     * prefixb.kk=4
     * ->
     * unsorted map
     * a={12=2,34=4}
     * b={56=u,kk=4}</pre>
     * @param prefix the key prefix for extraction
     * @return not {@code null} map
     * @see #subIndexed(String)
     */
    public Map<String, ConfigMap> subKeyed(String prefix) {
        Map<String, ConfigMap> result = new HashMap<>();
        Map<String, Map<String, String>> resultMap = new HashMap<>();

        for (Entry<String, String> e : entrySet()) {
            String key = e.getKey();
            if (key.startsWith(prefix)) {
                String suffix = key.substring(prefix.length(), key.length());

                String[] pref = PATTERN_DOT.split(suffix, 2);

                Map<String, String> map = resultMap.get(pref[0]);
                if (map == null) {
                    resultMap.put(pref[0], map = new HashMap<>());
                    result.put(pref[0], new SimpleConfigMap(map));
                }

                if (pref.length == 2)
                    map.put(pref[1], e.getValue());
                else
                    // strange logic, seems not to be used anywhere
                    map.put("", e.getValue());
            }
        }

        return result;
    }

    /**
     * Creates if needed and gets pre parsed and cached configuration.
     * Cache key - the class object of the configuration.
     * @param clazz the configuration class.
     * @return
     */
    @SuppressWarnings("unchecked")
    public final <K extends Config> K getConfig(final Class<K> clazz) {
        synchronized (this) {
            if (configMap == null) {
                configMap = new ConcurrentHashMap<>();
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
     * Same with {@link #getConfig(Class)}, but with string parameter for calling from scripts.
     * The method is less recommended as {@link #getConfig(Class)}, which is checked by compiler.
     * @param className the full class name.
     * @return
     */
    @SuppressWarnings("unchecked")
    @Dynamic
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

    /**
     * Serializes the data to {@code key=value} string lines
     * @return the string with pairs on lines
     */
    public String getDataString() {
        return getDataString("");
    }

    private String getDataString(String prefix) {
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
}
