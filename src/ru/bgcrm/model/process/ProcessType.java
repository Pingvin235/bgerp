package ru.bgcrm.model.process;

import java.util.ArrayList;
import java.util.List;

import org.bgerp.util.Dynamic;
import org.bgerp.util.Log;

import com.fasterxml.jackson.annotation.JsonIgnore;

import ru.bgcrm.cache.ProcessTypeCache;
import ru.bgcrm.model.IdTitleTreeItem;

public class ProcessType extends IdTitleTreeItem<ProcessType> implements Comparable<ProcessType> {
    private static final Log log = Log.getLog();

    private boolean useParentProperties;
    private TypeProperties properties;

    public ProcessType() {
        super();
    }

    public ProcessType(int id, String title) {
        super(id, title);
    }

    public TypeProperties getProperties() {
        return properties;
    }

    public void setProperties(TypeProperties properties) {
        this.properties = properties;
    }

    public boolean isUseParentProperties() {
        return useParentProperties;
    }

    public void setUseParentProperties(boolean useParentProperties) {
        this.useParentProperties = useParentProperties;
    }

    @Dynamic
    public int getChildCount() {
        return children.size();
    }

    @Deprecated
    @JsonIgnore
    public List<ProcessType> getChilds() {
        log.warn("Called deprecated method getChilds()");
        return children;
    }

    public List<Integer> getChildIds() {
        List<Integer> ids = new ArrayList<Integer>();
        for (ProcessType type : children) {
            ids.add(type.getId());
        }
        return ids;
    }

    public List<Integer> getAllChildIds() {
        List<Integer> ids = new ArrayList<Integer>();
        for (ProcessType type : children) {
            ids.add(type.getId());
            ids.addAll(type.getAllChildIds());
        }
        return ids;
    }

    @JsonIgnore
    public List<ProcessType> getPath() {
        return ProcessTypeCache.getTypePath(id);
    }

    @Override
    public int compareTo(ProcessType o) {
        return id - o.getId();
    }
}
