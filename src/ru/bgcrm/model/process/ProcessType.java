package ru.bgcrm.model.process;

import ru.bgcrm.cache.ProcessTypeCache;
import ru.bgcrm.model.IdTitle;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class ProcessType extends IdTitle {
    private boolean archive = false;

    private int parentId;
    private boolean useParentProperties;
    private TypeProperties properties;
    private List<ProcessType> childs = new ArrayList<ProcessType>();

    public ProcessType() {
        super();
    }

    public ProcessType(int id, String title) {
        super(id, title);
    }

    public boolean isArchive() {
        return archive;
    }

    public void setArchive(boolean archive) {
        this.archive = archive;
    }

    public TypeProperties getProperties() {
        return properties;
    }

    public void setProperties(TypeProperties properies) {
        this.properties = properies;
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
        return childs.size();
    }

    public void addChild(ProcessType processType) {
        childs.add(processType);
    }

    public List<ProcessType> getChilds() {
        return childs;
    }

    public List<Integer> getChildIds() {
        List<Integer> ids = new ArrayList<Integer>();
        for (ProcessType type : childs) {
            ids.add(type.getId());
        }
        return ids;
    }

    public List<Integer> getAllChildIds() {
        List<Integer> ids = new ArrayList<Integer>();
        for (ProcessType type : childs) {
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
