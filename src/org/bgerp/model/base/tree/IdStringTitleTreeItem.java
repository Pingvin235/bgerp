package org.bgerp.model.base.tree;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import ru.bgcrm.util.Utils;

public class IdStringTitleTreeItem extends TreeItem<String, IdStringTitleTreeItem> {
    /**
     * Comparator items by keys.
     */
    public static Comparator<IdStringTitleTreeItem> COMPARATOR = (a, b) -> {
        Iterator<Integer> idsA = a.getIds().iterator();
        Iterator<Integer> idsB = b.getIds().iterator();

        if (idsA.hasNext() || idsB.hasNext()) {
            if (!idsA.hasNext())
                return -1;
            if (!idsB.hasNext())
                return 1;

            Integer nextA = idsA.next(), nextB = idsB.next();
            if (nextA != nextB)
                return nextA - nextB;
        }
        return 0;
    };

    private List<Integer> ids;

    public IdStringTitleTreeItem() {
        setId("");
        parentId = "";
        children = new ArrayList<>();
    }

    public IdStringTitleTreeItem(String id, String title, String parentId) {
        setId(id);
        this.setTitle(title);
        this.parentId = parentId;
        children = new ArrayList<>();
    }

    @Override
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

    // TODO: The method is used for keys ordering, but only on a single place.
    public List<Integer> getIds() {
        return ids;
    }

    @Override
    protected boolean isRootNode() {
        return Utils.isBlankString(id) && Utils.isBlankString(parentId);
    }
}
