package org.bgerp.model.param;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.bgerp.app.cfg.ConfigMap;
import org.bgerp.app.cfg.Preferences;
import org.bgerp.cache.ParameterCache;
import org.bgerp.model.base.IdStringTitle;
import org.bgerp.model.base.IdTitle;
import org.bgerp.model.base.IdTitleComment;
import org.bgerp.util.Dynamic;
import org.bgerp.util.Log;

import com.fasterxml.jackson.annotation.JsonIgnore;

import ru.bgcrm.model.param.ParameterEmailValue;
import ru.bgcrm.util.TimeUtils;
import ru.bgcrm.util.Utils;

public class Parameter extends IdTitleComment {
    private static final Log log = Log.getLog();

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
    public static final String TYPE_TREECOUNT = "treecount";

    /** Sorted list of parameter types. */
    @Dynamic
    public static final List<IdStringTitle> TYPES = EnumSet.allOf(Type.class).stream()
        .map(type -> type.name).sorted()
        .map(name -> new IdStringTitle(name, name))
        .collect(Collectors.toList());

    private static Map<String, Type> NAME_MAP = EnumSet.allOf(Type.class).stream()
            .collect(Collectors.toMap(type -> type.name, type -> type));

    /** Parameter types enum. */
    public static enum Type {
        ADDRESS(TYPE_ADDRESS), BLOB(TYPE_BLOB), DATE(TYPE_DATE), DATETIME(TYPE_DATETIME), EMAIL(TYPE_EMAIL),
        FILE(TYPE_FILE), LIST(TYPE_LIST), LISTCOUNT(TYPE_LISTCOUNT), MONEY(TYPE_MONEY), TEXT(TYPE_TEXT),
        PHONE(TYPE_PHONE), TREE(TYPE_TREE), TREECOUNT(TYPE_TREECOUNT);

        public static Type of(String name) {
            return NAME_MAP.get(name);
        }

        /**
         * Unified representation 'email' parameter values as a string.
         * The logic is duplicated in {@link ru.bgcrm.dao.ParamValueSelect#paramSelectQuery(String, String, StringBuilder, StringBuilder, boolean) for process queues.
         * @param values the parameter values
         * @return
         */
        public static String emailToString(Collection<ParameterEmailValue> values) {
            return Utils.toString(values, "", ", ");
        }

         /**
         * Unified representation 'list' parameter values as a string.
         * The logic is duplicated in {@link ru.bgcrm.dao.ParamValueSelect#paramSelectQuery(String, String, StringBuilder, StringBuilder, boolean) for process queues.
         * @param paramId the parameter ID
         * @param values the parameter values with comments
         * @return
         */
        public static String listToString(int paramId, Map<Integer, String> values) {
            var result = new StringBuilder(100);

            for (IdTitle value : ParameterCache.getListParamValues(paramId)) {
                var comment = values.get(value.getId());
                if (comment != null)
                    Utils.addCommaSeparated(result, comment.isEmpty() ? value.getTitle() : value.getTitle() + " [" + comment + "]");
            }

            return result.toString();
        }

        /**
         * Unified representation 'listcount' parameter values as a string.
         * The logic is duplicated in {@link ru.bgcrm.dao.ParamValueSelect#paramSelectQuery(String, String, StringBuilder, StringBuilder, boolean) for process queues.
         * @param paramId the parameter ID
         * @param values the parameter values
         * @return
         */
        public static String listCountToString(int paramId, Map<Integer, BigDecimal> values) {
            var result = new StringBuilder(100);

            for (IdTitle value : ParameterCache.getListParamValues(paramId)) {
                var count = values.get(value.getId());
                if (count != null)
                    Utils.addCommaSeparated(result, value.getTitle() + ": " + count);
            }

            return result.toString();
        }

        /**
         * Unified representation 'tree' parameter values as a string.
         * The logic is duplicated in {@link ru.bgcrm.dao.ParamValueSelect#paramSelectQuery(String, String, StringBuilder, StringBuilder, boolean) for process queues.
         * @param paramId the parameter ID
         * @param values the parameter values
         * @return
         */
        public static String treeToString(int paramId, Set<String> values) {
            var result = new StringBuilder(100);

            Map<String, String> valuesMap = ParameterCache.getTreeParamValues(paramId);
            for (var me : valuesMap.entrySet()) {
                if (values.contains(me.getKey()))
                    Utils.addCommaSeparated(result, me.getValue());
            }

            return result.toString();
        }

        /**
         * Unified representation 'treecount' parameter values as a string.
         * The logic is duplicated in {@link ru.bgcrm.dao.ParamValueSelect#paramSelectQuery(String, String, StringBuilder, StringBuilder, boolean) for process queues.
         * @param paramId the parameter ID
         * @param values the parameter values
         * @return
         */
        public static String treeCountToString(int paramId, Map<String, BigDecimal> values) {
            var result = new StringBuilder(100);

            Map<String, String> valuesMap = ParameterCache.getTreeParamValues(paramId);
            for (var me : valuesMap.entrySet()) {
                BigDecimal count = values.get(me.getKey());
                if (count == null)
                    continue;
                Utils.addCommaSeparated(result, me.getValue() + ": " + count);
            }

            return result.toString();
        }

        private final String name;

        private Type(String name) {
            this.name = name;
        }
    }

    private String type;
    private String objectType;
    private String config = "";
    private ConfigMap configMap;
    private String valuesConfig;
    private int order;

    public Parameter withTitle(String value) {
        setTitle(value);
        return this;
    }

    public Parameter withComment(String value) {
        setComment(value);
        return this;
    }

    public String getType() {
        return type;
    }

    public Type getTypeType() {
        return Type.of(type);
    }

    public void setType(String type) {
        this.type = type;
    }

    public Parameter withType(String value) {
        type = value;
        return this;
    }

    public String getObjectType() {
        return objectType;
    }

    public void setObjectType(String value) {
        this.objectType = value;
    }

    public Parameter withObjectType(String value) {
        objectType = value;
        return this;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public Parameter withOrder(int value) {
        order = value;
        return this;
    }

    public String getConfig() {
        return config;
    }

    public ConfigMap getConfigMap() {
        return configMap;
    }

    public void setConfig(String config) {
        this.config = config;
        configMap = new Preferences(config);
    }

    public Parameter withConfig(String value) {
        setConfig(value);
        return this;
    }

    public void setValuesConfig(String config) {
        this.valuesConfig = config;
    }

    public String getValuesConfig() {
        return valuesConfig;
    }

    public Parameter withValuesConfig(String value) {
        valuesConfig = value;
        return this;
    }

    public List<IdTitle> getListParamValues() {
        return ParameterCache.getListParamValues(this);
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

    public boolean isReadonly() {
        return configMap.getBoolean("readonly", false);
    }

    /**
     * @return ''
     */
    public String getShowAsLink() {
        return configMap.getSok("show.as.link", "showAsLink");
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[ id = ");
        builder.append(id);
        builder.append("; object = ");
        builder.append(objectType);
        builder.append("; type = ");
        builder.append(type);
        builder.append("; title = ");
        builder.append(title);
        builder.append("; config = ");
        builder.append(config == null ? "null" : config.split("\n").length + " lines");
        builder.append(" ]");
        return builder.toString();
    }

    @Deprecated
    @JsonIgnore
    public String getObject() {
        log.warndMethod("getObject", "getObjectType");
        return objectType;
    }

    @Deprecated
    public void setObject(String object) {
        log.warndMethod("setObject", "setObjectType");
        this.objectType = object;
    }
}