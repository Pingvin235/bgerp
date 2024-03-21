package ru.bgcrm.model.process;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.bgerp.cache.ProcessTypeCache;
import org.bgerp.model.base.tree.TreeItem;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class ProcessType extends TreeItem<Integer, ProcessType> implements Comparable<ProcessType> {
    private boolean useParentProperties;
    private TypeProperties properties;

    public ProcessType() {
        super();
    }

    public ProcessType(int id, String title) {
        this.id = id;
        this.title = title;
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

    public ProcessType sub(Collection<ProcessType> typeList) {
        var typeSet = typeList.stream().map(ProcessType::getId).collect(Collectors.toSet());
        return sub(typeSet);
    }

    /**
     * Recursive copy of the tree with selected nodes with paths nodes to them.
     *
     * @param typeSet each node is chosen, when presented in the set or any child is there.
     * @return
     */
    public ProcessType sub(Set<Integer> typeSet) {
        if (typeSet == null) return this;

        var result = new ProcessType();
        result.setId(id);
        result.setTitle(title);

        var children = new ArrayList<ProcessType>(this.children.size());
        for (var child : this.children) {
            if (child.isInSet(typeSet))
                children.add(child.sub(typeSet));
        }

        result.setChildren(children);

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
