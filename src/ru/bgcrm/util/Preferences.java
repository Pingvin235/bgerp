package ru.bgcrm.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ru.bgcrm.dao.ConfigDAO;
import ru.bgcrm.model.BGException;
import ru.bgcrm.model.BGMessageException;
import ru.bgcrm.model.Config;
import ru.bgerp.util.Log;

/**
 * {@link ConcurrentHashMap} based implementation of {@link ParameterMap}.
 *
 * @author Shamil Vakhitov
 */
public class Preferences extends ParameterMap {
    private static final Log log = Log.getLog();

    private static final String INC = "inc";
    private static final String INSTRUCTION_DELIM = ":";

    protected final ConcurrentHashMap<String, String> data = new ConcurrentHashMap<>();

    public Preferences() {
        super();
    }

    public Preferences(Preferences preferences) {
        this.data.putAll(preferences.data);
        this.configMap.putAll(preferences.configMap);
    }

    public Preferences(String data) {
        super();
        try {
            loadData(data, "\r\n", this.data, null, false);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    private Preferences(String data, Iterable<ParameterMap> includes, boolean validate) throws BGException {
        super();
        loadData(data, "\r\n", this.data, includes, validate);
    }

    @Override
    public String get(String key, String def) {
        final String result = data.get(key);
        return result != null ? result : def;
    }

    @Override
    public Set<Entry<String, String>> entrySet() {
        return data.entrySet();
    }

    @Override
    public String put(String key, String value) {
        configMap = null;
        return data.put(key, value);
    }

    @Override
    public void putAll(Map<? extends String, ? extends String> m) {
        data.putAll(m);
    }

    /**
     * Use {@link Preferences#put(String, String)}
     * @param key
     * @param value
     */
    @Deprecated
    public void set(String key, String value) {
        put(key, value);
    }

    public Map<String, String> getDataMap() {
        return data;
    }

    public void removeSub(String prefix) {
        for (String key : data.keySet()) {
            if (key.startsWith(prefix)) {
                data.remove(key);
            }
        }
        configMap = null;
    }

    private static final class MultilineContext {
        public String key;
        public StringBuilder multiline = new StringBuilder(10);
        public String endOfLine;
    }

    /**
     * Загрузка файла конфигурации в Map, имя файла определено в поле {@link bundleName}.
     */
    protected void loadBundle(String bundleName, Map<String, String> data, boolean validate) {
        File file = new File(bundleName.replace('.', '/') + ".properties");
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            MultilineContext context = new MultilineContext();

            String line = null;
            while ((line = reader.readLine()) != null)
                loadDataEntry(context, data, line.trim(), null, validate);
        } catch (Exception e) {
            log.error(e);
        }
    }

    protected void loadData(String conf, String delim, Map<String, String> data, Iterable<ParameterMap> includes, boolean validate) throws BGException {
        MultilineContext context = new MultilineContext();
        StringTokenizer st = new StringTokenizer(Utils.maskNull(conf), delim);
        while (st.hasMoreTokens())
            loadDataEntry(context, data, st.nextToken().trim(), includes, validate);

        if (includes != null)
            for (ParameterMap include : includes)
                data.putAll(include);
    }

    private static final String MULTILINE_PREFIX = "<<";
    private static final int MULTILINE_PREFIX_LENGTH = MULTILINE_PREFIX.length();

    private void loadDataEntry(MultilineContext context, Map<String, String> data, String line, Iterable<ParameterMap> includes, boolean validate) throws BGException {
        if (line.startsWith("#")) {
            return;
        }

        line = insertVariablesValues(line, data, includes, validate);

        // конец мультилинии
        if (line.equals(context.endOfLine)) {
            data.put(context.key, context.multiline.toString());
            context.endOfLine = null;
            context.multiline.setLength(0);
            return;
        }
        // склейка мультилинии
        if (context.endOfLine != null) {
            context.multiline.append(line + "\n");
            return;
        }

        int pos = line.indexOf("=");
        if (pos > 0) {
            if (line.charAt(pos - 1) == '+') {
                pos--;
                String key = line.substring(0, pos);
                String value = line.substring(pos + 2);

                if (data.containsKey(key))
                    value = data.get(key) + value;

                data.put(key, value);
            } else {
                String key = line.substring(0, pos);
                String value = line.substring(pos + 1);

                // начало мультилинии
                if (value.startsWith(MULTILINE_PREFIX)) {
                    context.key = key;
                    context.endOfLine = value.substring(MULTILINE_PREFIX_LENGTH);
                    return;
                }

                data.put(key, value);
            }
        }
    }

    private static final Pattern variablePattern = Pattern.compile("\\{@([\\w\\.:]+)\\}");

    // TODO: Во многих местах функция используется очень запутанно.
    public static String insertVariablesValues(String line, Map<String, String> data, Iterable<ParameterMap> includes, boolean validate) throws BGException {
        StringBuffer result = null;

        int pointer = 0;

        Matcher m = variablePattern.matcher(line);
        while (m.find()) {
            if (result == null) {
                result = new StringBuffer(line.length() + 16);
            }

            result.append(line.substring(pointer, m.start()));

            String variable = m.group(1);

            // если строка является инструкцией
            String[] tokens = variable.split(INSTRUCTION_DELIM);
            if (tokens.length == 2) {
                if (INC.equals(tokens[0])) {
                    String val = data.get(tokens[1]);
                    if (val != null)
                        data.put(tokens[1], String.valueOf(Utils.parseInt(val) + 1));
                    // создаём счётчик, если нет
                    else
                        data.put(tokens[1], "1");
                    variable = tokens[1];
                } else if (validate)
                    throw new BGMessageException("Unknown operation: " + tokens[0]);
            }

            // считывание параметра с именем переменной
            String value = data.get(variable);
            if (value == null && includes != null)
                for  (ParameterMap include : includes) {
                    value = include.get(variable);
                    if (value == null)
                        break;
                }

            if (value != null)
                result.append(value);
            else {
                if (validate) throw new BGMessageException("Variable is not found: " + variable);
                // не найденные переменный оставляются "как есть", это необходимо для последующей их замены
                result.append(m.group(0));
            }

            pointer = m.end();
        }

        if (result != null) {
            result.append(line.substring(pointer));
            return result.toString();
        }

        return line;
    }

    public Map<String, String> getHashValuesWithPrefix(String prefix) {
        Map<String, String> result = new HashMap<String, String>();

        for (Entry<String, String> e : entrySet()) {
            String param_name = e.getKey();
            if (param_name.startsWith(prefix)) {
                String param_value = e.getValue();
                String suffix = param_name.substring(prefix.length(), param_name.length());
                result.put(suffix, param_value);
            }
        }
        return result;
    }

    /**
     * Проверяет конфигурацию, включая инклуды и переменные.
     * @param configDao
     * @param config
     * @param validate
     * @throws Exception
     */
    public static ParameterMap processIncludes(ConfigDAO configDao, String config, boolean validate) throws Exception {
        Iterable<ParameterMap> includes = Config.getIncludes(configDao, new Preferences(config), validate);
        return new Preferences(config, includes, validate);
    }

    /**
     * Функция для разбора конфигураций вида:
     *
     * filetype.1.name=...
     * filetype.1.value=...
     * filetype.2.name=...
     * filetype.2.value..
     *
     * разбирает в список Map с ключами name, value, код передается под ключем id.
     *
     * @param prefix
     * @param setup
     * @return
     */
    public List<Map<String, String>> parseObjects(String prefix) {
        Map<String, Map<String, String>> tmpMap = new HashMap<String, Map<String, String>>();
        Map<String, String> values = getHashValuesWithPrefix(prefix);
        for (Map.Entry<String, String> value : values.entrySet()) {
            String id = null;
            String key = null;

            int pos = value.getKey().indexOf('.');
            if (pos <= 0) {
                continue;
            }

            id = value.getKey().substring(0, pos);
            key = value.getKey().substring(pos + 1);

            Map<String, String> data = tmpMap.get(id);
            if (data == null) {
                data = new HashMap<String, String>();
                data.put("id", id);
                tmpMap.put(id, data);
            }

            data.put(key, value.getValue());
        }

        List<Map<String, String>> res = new ArrayList<Map<String, String>>();
        res.addAll(tmpMap.values());

        Collections.sort(res, new Comparator<Map<String, String>>() {
            public int compare(Map<String, String> o1, Map<String, String> o2) {
                Integer id1 = Utils.parseInt(o1.get("id"));
                Integer id2 = Utils.parseInt(o2.get("id"));

                return id1 - id2;
            }

        });

        return res;
    }

    public Map<String, Map<String, String>> parseObjectsNoOrder(String prefix) {
        Map<String, Map<String, String>> result = new HashMap<String, Map<String, String>>();
        Map<String, String> values = getHashValuesWithPrefix(prefix);
        for (Map.Entry<String, String> value : values.entrySet()) {
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
}
