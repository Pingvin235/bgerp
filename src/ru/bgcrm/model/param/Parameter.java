package ru.bgcrm.model.param;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ru.bgcrm.cache.ParameterCache;
import ru.bgcrm.model.IdTitle;
import ru.bgcrm.util.ParameterMap;
import ru.bgcrm.util.Preferences;
import ru.bgcrm.util.TimeUtils;

public class Parameter extends IdTitle {
    public static final String PARAM_MULTIPLE_KEY = "multiple";

    public static final String LIST_PARAM_USE_DIRECTORY_KEY = "directory";
    public static final String LIST_PARAM_AVAILABLE_VALUES_KEY = "availableValues";
    public static final String LIST_PARAM_AVAILABLE_VALUES_INNER_JOIN_FILTER_KEY = "availableValuesInnerJoinFilter";

    public static final String TYPE_TEXT = "text";
    public static final String TYPE_BLOB = "blob";
    public static final String TYPE_DATE = "date";
    public static final String TYPE_DATETIME = "datetime";
    public static final String TYPE_EMAIL = "email";
    public static final String TYPE_LIST = "list";
    public static final String TYPE_LISTCOUNT = "listcount";
    public static final String TYPE_PHONE = "phone";
    //public static final String TYPE_BOOLEAN = "boolean"; // не реализован
    public static final String TYPE_ADDRESS = "address";
    public static final String TYPE_FILE = "file";
    public static final String TYPE_TREE = "tree";

    // не известно, где используется
    public static final Set<String> VALID_TYPES = new HashSet<String>(Arrays
            .asList(new String[] { TYPE_TEXT, TYPE_BLOB, TYPE_DATE, TYPE_DATETIME, TYPE_PHONE, TYPE_ADDRESS, TYPE_FILE, TYPE_TREE, TYPE_LISTCOUNT }));

    public enum Type {
        TEXT(TYPE_TEXT), BLOB(TYPE_BLOB), DATE(TYPE_DATE), DATETIME(TYPE_DATETIME), EMAIL(TYPE_EMAIL), LIST(TYPE_LIST), LISTCOUNT(
                TYPE_LISTCOUNT), PHONE(TYPE_PHONE), ADDRESS(TYPE_ADDRESS), FILE(TYPE_FILE), TREE(TYPE_TREE);

        private String name;
        private static Map<String, Type> NAME_MAP = new HashMap<String, Type>();
        static {
            for (Type type : Type.values()) {
                NAME_MAP.put(type.name, type);
            }
        }

        public static Type fromString(String name) {
            return NAME_MAP.get(name);
        }

        private Type(String name) {
            this.name = name;
        }
    }

    private String type;
    private String object;
    private String script = "";
    private String config;
    private String comment;
    private ParameterMap configMap;
    private String valuesConfig;
    private int order;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getObject() {
        return object;
    }

    public void setObject(String object) {
        this.object = object;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public List<String> getScripts() {
        List<String> scripts = new ArrayList<String>();
        scripts.add(script);
        return scripts;
    }

    public String getScript() {
        return script;
    }

    public void setScript(String script) {
        this.script = script;
    }

    public String getConfig() {
        return config;
    }

    public ParameterMap getConfigMap() {
        return configMap;
    }

    public void setConfig(String config) {
        this.config = config;
        configMap = new Preferences(config);
    }

    public void setValuesConfig(String config) {
        this.valuesConfig = config;
    }

    public String getValuesConfig() {
        return valuesConfig;
    }

    public List<IdTitle> getListParamValues() {
        return ParameterCache.getListParamValues(this);
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getDateParamFormat() {
        String format = "";

        if (Parameter.TYPE_DATETIME.equals(type)) {
            format = TimeUtils.getTypeFormat(getConfigMap().get("type", "ymd"));
        } else {
            format = TimeUtils.getTypeFormat(TimeUtils.FORMAT_TYPE_YMD);
        }

        return format;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[ id = ");
        builder.append(id);
        builder.append("; object = ");
        builder.append(object);
        builder.append("; type = ");
        builder.append(type);
        builder.append("; title = ");
        builder.append(title);
        builder.append("; script = ");
        builder.append(script == null ? "null" : script.split("\n").length + " lines");
        builder.append("; config = ");
        builder.append(config == null ? "null" : config.split("\n").length + " lines");
        builder.append(" ]");
        return builder.toString();
    }
}