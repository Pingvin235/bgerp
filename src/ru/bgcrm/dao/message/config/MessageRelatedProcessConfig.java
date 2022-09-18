package ru.bgcrm.dao.message.config;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.bgerp.util.Log;

import ru.bgcrm.cache.ParameterCache;
import ru.bgcrm.model.param.Parameter;
import ru.bgcrm.util.Config;
import ru.bgcrm.util.ParameterMap;

public class MessageRelatedProcessConfig extends Config {
    private static final Log log = Log.getLog();

    private final SortedMap<Integer, Type> types;

    protected MessageRelatedProcessConfig(ParameterMap config) throws InitStopException {
        super(null);
        types = parseTypes(config);
        initWhen(!types.isEmpty());
    }

    private SortedMap<Integer, Type> parseTypes(ParameterMap config) throws InitStopException {
        var result = new TreeMap<Integer, Type>();

        for (var me : config.subIndexed("message.related.process.").entrySet()) {
            var params = me.getValue();

            var type = Type.of(params.get("type"));
            type.color = params.get("color");

            if (type == Type.FOUND_LINK_CUSTOMER_ADDRESS_CITY) {
                final String keyName = "foundLinkCustomerAddressCityProcessParamId";

                type.foundCustomerAddressCityParamCityId = params.getInt(keyName);
                var param = ParameterCache.getParameter(type.foundCustomerAddressCityParamCityId);
                if (param == null || !param.getType().equals(Parameter.TYPE_LIST)) {
                    log.error("Configuration parameter '{}'='{}' is missing or not 'list'", keyName, type.foundCustomerAddressCityParamCityId);
                    throw new InitStopException();
                }
            }

            result.put(me.getKey(), type);
        }

        return Collections.unmodifiableSortedMap(result);
    }

    /**
     * @return search types map with keys equal config IDs.
     */
    public SortedMap<Integer, Type> getTypes() {
        return types;
    }

    /**
     * Possible search types of related processes.
     */
    public static enum Type {
        MESSAGE_FROM("messageFrom"),
        FOUND_LINK("foundLink"),
        FOUND_LINK_CUSTOMER_ADDRESS_CITY("foundLinkCustomerAddressCity");

        private static final Set<Type> VALUES = EnumSet.allOf(Type.class);

        static Type of(String typeStr) {
            for (Type type : VALUES)
                if (typeStr.equals(type.configValue))
                    return type;

            throw new IllegalArgumentException("Not found search type for string: " + typeStr);
        }

        private final String configValue;

        /** Optional color. */
        private String color;
        /** For {@link #FOUND_LINK_CUSTOMER_ADDRESS_CITY} process parameter with type 'list', storing process cities IDs. */
        private int foundCustomerAddressCityParamCityId;

        private Type(String configValue) {
            this.configValue = configValue;
        }

        public String getColor() {
            return color;
        }

        public int getFoundCustomerAddressCityParamCityId() {
            return foundCustomerAddressCityParamCityId;
        }
    }
}
