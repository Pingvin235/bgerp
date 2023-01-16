package org.bgerp.model.base;

import java.util.ArrayList;
import java.util.List;

import ru.bgcrm.util.Utils;

public class IdTitleTree {
    private String id;
    private List<Integer> ids;
    private String title;
    private String parentId;
    private List<IdTitleTree> children;

    public IdTitleTree() {
        setId("");
        parentId = "";
        children = new ArrayList<IdTitleTree>();
    }

    public IdTitleTree(String id, String title, String parentId) {
        setId(id);
        this.setTitle(title);
        this.parentId = parentId;
        children = new ArrayList<>();
    }

    public List<IdTitleTree> getChildren() {
        return children;
    }

    public void setChildren(List<IdTitleTree> children) {
        this.children = children;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public IdTitleTree getChild(String id) {
        return getChild(this, id);
    }

    public void addChild(IdTitleTree child) {
        children.add(child);
    }

    private IdTitleTree getChild(IdTitleTree root, String id) {
        for (IdTitleTree child : root.getChildren()) {
            if (id.equals(child.getId())) {
                return child;
            }
            IdTitleTree ch = getChild(child, id);
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
