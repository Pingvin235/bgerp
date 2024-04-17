package org.bgerp.model.base.tree;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bgerp.util.Dynamic;
import org.bgerp.util.Log;

import com.fasterxml.jackson.annotation.JsonIgnore;

public abstract class TreeItem<T, C extends TreeItem<T, C>> implements org.bgerp.model.base.tree.iface.TreeItem<T, C> {
    private static final Log log = Log.getLog();

    private static final String ICON_TAG_HOME = iconTag("ti-home");
    private static final String ICON_TAG_FOLDER = iconTag("ti-folder");
    private static final String ICON_TAG_FILE = iconTag("ti-file");

    private static String iconTag(String icon) {
        return " <i class='" + icon + "'></i> ";
    }

    protected T id;
    protected T parentId;
    protected String title;
    protected List<C> children = new ArrayList<C>();

    public T getId() {
        return id;
    }

    public void setId(T id) {
        this.id = id;
    }

    public T getParentId() {
        return parentId;
    }

    public void setParentId(T parentId) {
        this.parentId = parentId;
    }

    @Override
    public String getTitle() {
        return title;
    }

    public void setTitle(String value) {
        this.title = value;
    }

    @JsonIgnore
    public List<C> getChildren() {
        return children;
    }

    @JsonIgnore
    @Deprecated
    public List<C> getChilds() {
        log.warnd("Called deprecated method 'getChilds', use 'getChildren' instead.");
        return children;
    }

    public void setChildren(List<C> children) {
        this.children = children;
        if (children != null)
            for (C child : children)
                child.setParentId(this.id);
    }

    public C addChild(C child) {
        this.children.add(child);
        child.setParentId(this.id);
        return child;
    }

    public C getChild(T id) {
        return getChild(this, id);
    }

    private C getChild(TreeItem<T, C> root, T id) {
        for (C child : root.getChildren()) {
            if (id.equals(child.getId())) {
                return child;
            }
            C ch = getChild(child, id);
            if (ch != null) {
                return ch;
            }
        }
        return null;
    }

    /**
     * Finds node by ID over the node itself and all the children recursively.
     * @param id the ID.
     * @return found node or {@code null}.
     */
    public TreeItem<T, C> getById(T id) {
        if (this.id.equals(id))
            return this;

        if (children != null)
            return children.stream()
                .filter(item -> item.getId().equals(id))
                .findFirst()
                .orElse(null);

        return null;
    }

    @Dynamic
    public int getChildCount() {
        return children.size();
    }

    public Set<T> getChildIds() {
        Set<T> ids = new HashSet<>(100);
        for (C child : children) {
            ids.add(child.getId());
        }
        return ids;
    }

    protected boolean isInSet(Set<T> ids) {
        if (ids.contains(this.getId()))
            return true;
        for (var child : children)
            if (child.isInSet(ids))
                return true;
        return false;
    }

    /**
     * @return set with the node ID and all child IDs from all levels.
     */
    public Set<T> getAllChildIds() {
        Set<T> result = new HashSet<>(100);

        result.add(id);
        for (C childItem : children) {
            result.addAll(childItem.getAllChildIds());
        }

        return result;
    }

    /**
     * @return icon HTML.
     */
    @Dynamic
    public String getIcon() {
        if (children != null && !children.isEmpty()) {
            if (isRootNode())
                return ICON_TAG_HOME;
            return ICON_TAG_FOLDER;
        }
        return ICON_TAG_FILE;
    }

    protected abstract boolean isRootNode();

    protected boolean isRootNodeWithIntegerId(Integer id, Integer parentId) {
        return (id != null && id <= 0) && (parentId == null || parentId <= 0);
    }

    /**
     * @return style attribute for text span.
     */
    @Dynamic
    public String getTextStyle() {
        return null;
    }
}
