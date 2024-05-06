package org.bgerp.app.cfg;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
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

import org.bgerp.app.exception.BGMessageException;
import org.bgerp.util.Log;

import ru.bgcrm.dao.ConfigDAO;
import ru.bgcrm.model.Config;
import ru.bgcrm.util.Utils;

/**
 * {@link ConcurrentHashMap} based implementation of {@link ConfigMap}.
 *
 * @author Shamil Vakhitov
 */
public class Preferences extends ConfigMap {
    private static final Log log = Log.getLog();

    private static final String INSTRUCTION_DELIM = ":";
    private static final String INSTRUCTION_INC = "inc";

    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\{@([\\w\\.:]+)\\}");

    protected final ConcurrentHashMap<String, String> data = new ConcurrentHashMap<>();

    public Preferences() {
        super();
    }

    public Preferences(String data) {
        super();
        try {
            loadData(data, this.data, null, false);
        } catch (Exception e) {
            log.error(e);
        }
    }

    Preferences(String data, Iterable<String> includes, boolean validate) throws BGMessageException {
        super();
        loadData(data, this.data, includes, validate);
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
        clearConfigs();
        return data.put(key, value);
    }

    @Override
    public void putAll(Map<? extends String, ? extends String> m) {
        data.putAll(m);
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
        clearConfigs();
    }

    /**
     * Loads configuration file to map.
     * @param bundleName file name without '.properties' extension.
     * @param data target map.
     * @param validate check used in values variables.
     */
    protected void loadBundle(String bundleName, Map<String, String> data, boolean validate) {
        File file = new File(bundleName.replace('.', '/') + ".properties");
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            MultilineContext context = new MultilineContext();

            String line = null;
            while ((line = reader.readLine()) != null)
                loadDataEntry(context, data, line.trim(), validate);
        } catch (Exception e) {
            log.error(e);
        }
    }

    private void loadData(String conf, Map<String, String> data, Iterable<String> includes, boolean validate) throws BGMessageException {
        final String delim = "\r\n";

        MultilineContext context = new MultilineContext();

        StringTokenizer st = new StringTokenizer(Utils.maskNull(conf), delim);
        while (st.hasMoreTokens())
            loadDataEntry(context, data, st.nextToken().trim(), validate);

        if (includes != null)
            for (String include : includes)
                loadData(include, data, null, validate);
    }

    /**
     * Loads a single key value pair.
     * @param context context for handling multiline values.
     * @param data target map.
     * @param line key-value line.
     * @param validate check variables in values.
     * @throws BGMessageException
     */
    private void loadDataEntry(MultilineContext context, Map<String, String> data, String line, boolean validate) throws BGMessageException {
        // remove terminating non-printable chars
        line = line.replaceAll("\\p{C}+$", "");

        if (line.startsWith("#"))
            return;

        line = insertVariablesValues(line, data, validate);

        // end of multiline
        if (line.equals(context.endOfLine)) {
            data.put(context.key, context.multiline.toString());
            context.endOfLine = null;
            context.multiline.setLength(0);
            return;
        }
        // concat multiline
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

                // begin of multiline
                if (value.startsWith(MultilineContext.MULTILINE_PREFIX)) {
                    context.key = key;
                    context.endOfLine = value.substring(MultilineContext.MULTILINE_PREFIX_LENGTH);
                    return;
                }

                data.put(key, value);
            }
        }
    }

    private String insertVariablesValues(String line, Map<String, String> data, boolean validate) throws BGMessageException {
        StringBuffer result = null;

        int pointer = 0;

        Matcher m = VARIABLE_PATTERN.matcher(line);
        while (m.find()) {
            if (result == null) {
                result = new StringBuffer(line.length() + 16);
            }

            result.append(line.substring(pointer, m.start()));

            String variable = m.group(1);

            // instruction like @inc:
            String[] tokens = variable.split(INSTRUCTION_DELIM);
            if (tokens.length == 2) {
                if (INSTRUCTION_INC.equals(tokens[0])) {
                    String val = data.get(tokens[1]);
                    if (val != null)
                        data.put(tokens[1], String.valueOf(Utils.parseInt(val) + 1));
                    // create counter if doesn't exist
                    else
                        data.put(tokens[1], "1");
                    variable = tokens[1];
                } else if (validate)
                    throw new BGMessageException("Unknown operation: " + tokens[0]);
            }

            String value = data.get(variable);

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
        Map<String, String> result = new HashMap<>();

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
     * Inserts includes placed with <pre>include.ID=1</pre> expressions.
     * @param configDao DAO for getting includes.
     * @param config key-value lines of main configuration.
     * @param validate check existence of includes configurations, variables.
     * @throws BGMessageException
     * @throws SQLException
     */
    public static ConfigMap processIncludes(ConfigDAO configDao, String config, boolean validate) throws BGMessageException, SQLException {
        Iterable<String> includes = Config.getIncludes(configDao, new Preferences(config), validate);
        return new Preferences(config, includes, validate);
    }

    /**
     * Use {@link #subIndexed(String)}
     * @return
     */
    @Deprecated
    public List<Map<String, String>> parseObjects(String prefix) {
        Map<String, Map<String, String>> tmpMap = new HashMap<>();
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
                data = new HashMap<>();
                data.put("id", id);
                tmpMap.put(id, data);
            }

            data.put(key, value.getValue());
        }

        List<Map<String, String>> res = new ArrayList<>();
        res.addAll(tmpMap.values());

        Collections.sort(res, new Comparator<>() {
            public int compare(Map<String, String> o1, Map<String, String> o2) {
                Integer id1 = Utils.parseInt(o1.get("id"));
                Integer id2 = Utils.parseInt(o2.get("id"));

                return id1 - id2;
            }
        });

        return res;
    }
}
