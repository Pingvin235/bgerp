package ru.bgcrm.model.process;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.bgerp.model.base.tree.IdTitleTreeItem;

public class TypeTreeItem extends IdTitleTreeItem<TypeTreeItem> {

    public TypeTreeItem sub(Collection<ProcessType> typeList) {
        var typeSet = typeList.stream().map(ProcessType::getId).collect(Collectors.toSet());
        return sub(typeSet);
    }

    /**
     * Recursive copy of the tree with selected nodes with paths nodes to them.
     *
     * @param typeSet each node is chosen, when presented in the set or any child is there.
     * @return
     */
    private TypeTreeItem sub(Set<Integer> typeSet) {
        if (typeSet == null) return this;

        var result = new TypeTreeItem();
        result.setId(id);
        result.setTitle(title);

        var children = new ArrayList<TypeTreeItem>(this.children.size());
        for (var child : this.children) {
            if (child.isInSet(typeSet))
                children.add(child.sub(typeSet));
        }

        result.setChildren(children);

        return result;
    }

    private boolean isInSet(Set<Integer> ids) {
        if (ids.contains(this.getId()))
            return true;
        for (var child : children)
            if (child.isInSet(ids))
                return true;
        return false;
    }

    /**
     * Возвращает код узла и коды всех узлов-потомков данного узла.
     *
     * @return
     */
    public Set<Integer> getAllChildIds() {
        Set<Integer> result = new HashSet<Integer>();

        result.add(id);
        for (TypeTreeItem childItem : children) {
            result.addAll(childItem.getAllChildIds());
        }

        return result;
    }

    /**
     * Возвращает коды типов процессов с фильтром по выбранным типам.
     * Если узел выбран в наборе - добавляются все его дочерние узлы.
     *
     * @return
     */
    public Set<Integer> getSelectedChildIds(Set<Integer> typeSet) {
        Set<Integer> result = new HashSet<Integer>(typeSet.size());

        for (TypeTreeItem childItem : children) {
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
}