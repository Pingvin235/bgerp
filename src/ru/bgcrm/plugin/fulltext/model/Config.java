package ru.bgcrm.plugin.fulltext.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ru.bgcrm.model.Customer;
import ru.bgcrm.model.IdStringTitle;
import ru.bgcrm.plugin.fulltext.Plugin;
import ru.bgcrm.util.ParameterMap;
import ru.bgcrm.util.Utils;

public class Config extends ru.bgcrm.util.Config {
    
    private final int indexDelay;
    private final Map<String, ObjectType> objectTypeMap = new HashMap<>();
    private final List<IdStringTitle> objectTypeList = new ArrayList<>(); 
    private final Set<Integer> paramIds = new HashSet<>();
    
    public Config(ParameterMap setup) {
        super(setup);
        this.indexDelay = setup.getInt(Plugin.ID + ":index.delay", 60);
        for (Map.Entry<String, ParameterMap> me : setup.subKeyed(Plugin.ID + ":entry.").entrySet()) {
            ObjectType type = new ObjectType(me.getKey(), me.getValue());
            objectTypeMap.put(type.getObjectType(), type);
            paramIds.addAll(type.paramIds);
        }
        
        // TODO: Завести где-то в системе справочник сущностей с наименованиями?
        if (objectTypeMap.containsKey(Customer.OBJECT_TYPE))
            objectTypeList.add(new IdStringTitle(Customer.OBJECT_TYPE, "Контрагент"));
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
    
    public boolean isParamConfigured(int paramId) {
        return paramIds.contains(paramId);
    }

    public static final class ObjectType {
        
        private final String objectType;
        private final Set<Integer> paramIds;
                
        private ObjectType(String objectType, ParameterMap params) {
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
