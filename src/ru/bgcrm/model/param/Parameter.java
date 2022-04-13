package ru.bgcrm.model.param;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.bgerp.util.Dynamic;

import ru.bgcrm.cache.ParameterCache;
import ru.bgcrm.model.IdStringTitle;
import ru.bgcrm.model.IdTitle;
import ru.bgcrm.util.ParameterMap;
import ru.bgcrm.util.Preferences;
import ru.bgcrm.util.TimeUtils;

public class Parameter extends IdTitle {
    public static final String PARAM_MULTIPLE_KEY = "multiple";

    public static final String LIST_PARAM_USE_DIRECTORY_KEY = "directory";
    public static final String LIST_PARAM_AVAILABLE_VALUES_KEY = "availableValues";
    public static final String LIST_PARAM_AVAILABLE_VALUES_INNER_JOIN_FILTER_KEY = "availableValuesInnerJoinFilter";

    public static final String TYPE_ADDRESS = "address";
    public static final String TYPE_BLOB = "blob";
    public static final String TYPE_DATE = "date";
    public static final String TYPE_DATETIME = "datetime";
    public static final String TYPE_EMAIL = "email";
    public static final String TYPE_FILE = "file";
    public static final String TYPE_LIST = "list";
    public static final String TYPE_LISTCOUNT = "listcount";
    public static final String TYPE_MONEY = "money";
    public static final String TYPE_TEXT = "text";
    public static final String TYPE_PHONE = "phone";
    public static final String TYPE_TREE = "tree";

    /** Sorted list of parameter types. */
    @Dynamic
    public static final List<IdStringTitle> TYPES = EnumSet.allOf(Type.class).stream()
        .map(type -> type.name).sorted()
        .map(name -> new IdStringTitle(name, name))
        .collect(Collectors.toList());

    private static Map<String, Type> NAME_MAP = EnumSet.allOf(Type.class).stream()
            .collect(Collectors.toMap(type -> type.name, type -> type));

    /** Parameter types enum. */
    public enum Type {
        ADDRESS(TYPE_ADDRESS), BLOB(TYPE_BLOB), DATE(TYPE_DATE), DATETIME(TYPE_DATETIME), EMAIL(TYPE_EMAIL),
        FILE(TYPE_FILE), LIST(TYPE_LIST), LISTCOUNT(TYPE_LISTCOUNT), MONEY(TYPE_MONEY), TEXT(TYPE_TEXT),
        PHONE(TYPE_PHONE), TREE(TYPE_TREE);

        public static Type of(String name) {
            return NAME_MAP.get(name);
        }

        private final String name;

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

    @Deprecated
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