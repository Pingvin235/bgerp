package ru.bgcrm.model.process;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bgerp.cache.ProcessTypeCache;
import org.bgerp.model.base.tree.TreeItem;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class ProcessType extends TreeItem<Integer, ProcessType> implements Comparable<ProcessType> {
    private static final int MIN_TITLE_LENGTH = 15;

    private boolean useParentProperties;
    private TypeProperties properties;

    public ProcessType() {
        super();
    }

    public ProcessType(int id, String title) {
        this.id = id;
        this.title = title;
    }

    /**
     * @return the type's title, prefixed by the parent's one, if shorter than {@link #MIN_TITLE_LENGTH}
     */
    public String getTypeTitle() {
        String result = super.getTitle();

        if (result.length() < MIN_TITLE_LENGTH && parentId != null && parentId > 0)
            result = ProcessTypeCache.getProcessTypeSafe(parentId).getTitle() + " / " + result;

        return result;
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

    @JsonIgnore
    public List<ProcessType> getPath() {
        return ProcessTypeCache.getTypePath(id);
    }

    /**
     * Selects process type IDs with filter by other IDs.
     * @param typeSet the filter set, if a type ID is there, it has been added together with all its child IDs.
     * @return
     */
    public Set<Integer> getSelectedChildIds(Set<Integer> typeSet) {
        Set<Integer> result = new HashSet<>(typeSet.size());

        for (ProcessType childItem : children) {
            // если узел есть в результате - есть там и уже все его потомки
            if (result.contains(childItem.getId())) {
                continue;
            }

            if (typeSet.contains(childItem.getId())) {
                result.addAll(childItem.getAllChildIds());
            } else {
                result.addAll(childItem.getSelectedChildIds(typeSet));
            }
        }

        return result;
    }

    @Override
    public int compareTo(ProcessType o) {
        return id - o.getId();
    }

    @Override
    protected boolean isRootNode() {
        return isRootNodeWithIntegerId(id, parentId);
    }
}
