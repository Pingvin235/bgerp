package org.bgerp.model.base.tree;

import java.util.ArrayList;
import java.util.List;

import org.bgerp.model.base.IdTitle;
import org.bgerp.util.Dynamic;
import org.bgerp.util.Log;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Tree node base class.
 *
 * @author Shamil Vakhitov
 */
public class IdTitleTreeItem<T extends IdTitleTreeItem<T>> extends IdTitle {
    private static final Log log = Log.getLog();

    private static final String ICON_TAG_HOME = iconTag("ti-home");
    private static final String ICON_TAG_FOLDER = iconTag("ti-folder");
    private static final String ICON_TAG_FILE = iconTag("ti-file");

    private static String iconTag(String icon) {
        return " <i class='" + icon + "'></i> ";
    }

    /**
     * Default implementation
     */
    public static class Default extends IdTitleTreeItem<Default>{
        public Default(int id, String title) {
            super(id, title);
        }
    }

    protected int parentId;
    protected List<T> children = new ArrayList<T>();

    protected IdTitleTreeItem() {}

    public IdTitleTreeItem(int id, String title) {
        super(id, title);
    }

    public int getParentId() {
        return parentId;
    }

    public void setParentId(int parentId) {
        this.parentId = parentId;
    }

    @Dynamic
    public List<T> getChildren() {
        return children;
    }

    protected void setChildren(List<T> children) {
        this.children = children;
        if (children != null)
            for (T child : children)
                child.parentId = this.id;
    }

    public T addChild(T child) {
        this.children.add(child);
        child.parentId = this.id;
        return child;
    }

    /**
     * @return icon HTML.
     */
    @Dynamic
    public String getIcon() {
        if (children != null && !children.isEmpty()) {
            if (parentId <= 0)
                return ICON_TAG_HOME;
            return ICON_TAG_FOLDER;
        }
        return ICON_TAG_FILE;
    }

    /**
     * @return style attribute for text span.
     */
    @Dynamic
    public String getTextStyle() {
        return null;
    }

    /**
     * Finds node by ID over the node itself and all the children recursively.
     * @param id the ID.
     * @return found node or {@code null}.
     */
    @SuppressWarnings("unchecked")
    public T getById(int id) {
        if (this.id == id)
            return (T)this;

        if (children != null)
            return children.stream()
                .filter(item -> item.getId() == id)
                .findFirst()
                .orElse(null);

        return null;
    }

    @JsonIgnore
    @Deprecated
    public List<T> getChilds() {
        log.warnd("Called deprecated method 'getChilds', use 'getChildren' instead.");
        return children;
    }
}
