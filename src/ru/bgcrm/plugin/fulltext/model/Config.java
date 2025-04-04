package ru.bgcrm.plugin.fulltext.model;


import static org.bgerp.model.param.Parameter.TYPE_ADDRESS;
import static org.bgerp.model.param.Parameter.TYPE_BLOB;
import static org.bgerp.model.param.Parameter.TYPE_LIST;
import static org.bgerp.model.param.Parameter.TYPE_LISTCOUNT;
import static org.bgerp.model.param.Parameter.TYPE_PHONE;
import static org.bgerp.model.param.Parameter.TYPE_TEXT;
import static org.bgerp.model.param.Parameter.TYPE_TREE;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bgerp.app.cfg.ConfigMap;
import org.bgerp.model.base.IdStringTitle;
import org.bgerp.model.msg.Message;
import org.bgerp.model.param.Parameter;

import com.google.common.collect.Sets;

import ru.bgcrm.model.customer.Customer;
import ru.bgcrm.model.process.Process;
import ru.bgcrm.plugin.fulltext.Plugin;
import ru.bgcrm.util.Utils;

public class Config extends org.bgerp.app.cfg.Config {

    private static Set<String> PARAM_TYPES = Collections.unmodifiableSet(Sets.newHashSet(
        TYPE_TEXT, TYPE_BLOB, TYPE_LIST, TYPE_LISTCOUNT, TYPE_TREE, TYPE_ADDRESS, TYPE_PHONE));

    private final int indexDelay;
    private final Map<String, ObjectType> objectTypeMap = new HashMap<>();
    private final List<IdStringTitle> objectTypeList = new ArrayList<>();
    private final Set<Integer> paramIds = new HashSet<>();

    public Config(ConfigMap config) {
        super(null);
        this.indexDelay = config.getInt(Plugin.ID + ":index.delay", 60);
        for (Map.Entry<String, ConfigMap> me : config.subKeyed(Plugin.ID + ":entry.").entrySet()) {
            ObjectType type = new ObjectType(me.getKey(), me.getValue());
            objectTypeMap.put(type.getObjectType(), type);
            paramIds.addAll(type.paramIds);
        }

        // TODO: Завести где-то в системе справочник сущностей с наименованиями?
        if (objectTypeMap.containsKey(Customer.OBJECT_TYPE))
            objectTypeList.add(new IdStringTitle(Customer.OBJECT_TYPE, "Контрагент"));
        if (objectTypeMap.containsKey(Process.OBJECT_TYPE))
            objectTypeList.add(new IdStringTitle(Process.OBJECT_TYPE, "Процесс"));
        if (objectTypeMap.containsKey(Message.OBJECT_TYPE))
            objectTypeList.add(new IdStringTitle(Message.OBJECT_TYPE, "Сообщения"));
    }

    public int getIndexDelay() {
        return indexDelay;
    }

    public Map<String, ObjectType> getObjectTypeMap() {
        return objectTypeMap;
    }

    public List<IdStringTitle> getObjectTypeList() {
        return objectTypeList;
    }

    public boolean isParamConfigured(Parameter p) {
        return
            PARAM_TYPES.contains(p.getType()) &&
            (paramIds.isEmpty() || paramIds.contains(p.getId()));
    }

    public static final class ObjectType {

        private final String objectType;
        private final Set<Integer> paramIds;

        private ObjectType(String objectType, ConfigMap params) {
            this.objectType = objectType;
            this.paramIds = Utils.toIntegerSet(params.get("paramIds"));
        }

        public String getObjectType() {
            return objectType;
        }

        public Set<Integer> getParamIds() {
            return paramIds;
        }

    }

}
