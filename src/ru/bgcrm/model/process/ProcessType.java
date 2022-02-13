package ru.bgcrm.model.process;

import ru.bgcrm.cache.ProcessTypeCache;
import ru.bgcrm.model.IdTitle;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import org.bgerp.util.Log;

public class ProcessType extends IdTitle {
    private static final Log log = Log.getLog();

    private int parentId;
    private boolean useParentProperties;
    private TypeProperties properties;
    private List<ProcessType> children = new ArrayList<>();

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

    public int getParentId() {
        return parentId;
    }

    public void setParentId(int parentId) {
        this.parentId = parentId;
    }

    public boolean isUseParentProperties() {
        return useParentProperties;
    }

    public void setUseParentProperties(boolean useParentProperties) {
        this.useParentProperties = useParentProperties;
    }

    public int getChildCount() {
        return children.size();
    }

    public void addChild(ProcessType processType) {
        children.add(processType);
    }

    public List<ProcessType> getChildren() {
        return children;
    }

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
}
