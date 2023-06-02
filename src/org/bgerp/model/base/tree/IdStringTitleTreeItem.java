package org.bgerp.model.base.tree;

import java.util.ArrayList;
import java.util.List;

import ru.bgcrm.util.Utils;

public class IdStringTitleTreeItem {
    private String id;
    private List<Integer> ids;
    private String title;
    private String parentId;
    private List<IdStringTitleTreeItem> children;

    public IdStringTitleTreeItem() {
        setId("");
        parentId = "";
        children = new ArrayList<IdStringTitleTreeItem>();
    }

    public IdStringTitleTreeItem(String id, String title, String parentId) {
        setId(id);
        this.setTitle(title);
        this.parentId = parentId;
        children = new ArrayList<>();
    }

    public List<IdStringTitleTreeItem> getChildren() {
        return children;
    }

    public void setChildren(List<IdStringTitleTreeItem> children) {
        this.children = children;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public IdStringTitleTreeItem getChild(String id) {
        return getChild(this, id);
    }

    public void addChild(IdStringTitleTreeItem child) {
        children.add(child);
    }

    private IdStringTitleTreeItem getChild(IdStringTitleTreeItem root, String id) {
        for (IdStringTitleTreeItem child : root.getChildren()) {
            if (id.equals(child.getId())) {
                return child;
            }
            IdStringTitleTreeItem ch = getChild(child, id);
            if (ch != null) {
                return ch;
            }
        }
        return null;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;

        ids = new ArrayList<>();
        for (String idPart : id.split("\\.")) {
            ids.add(Utils.parseInt(idPart));
        }
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<Integer> getIds() {
        return ids;
    }
}
