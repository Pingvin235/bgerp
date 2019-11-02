package ru.bgcrm.model;

import java.util.ArrayList;
import java.util.List;

public class IdTitleTreeItem<T extends IdTitleTreeItem<T>> extends IdTitle {
    protected List<T> children = new ArrayList<T>();

    public List<T> getChildren() {
        return children;
    }

    protected void setChildren(List<T> childs) {
        this.children = childs;
    }

    public void addChild(T child) {
        this.children.add(child);
    }

    @Deprecated
    public List<T> getChilds() {
        return children;
    }
}
