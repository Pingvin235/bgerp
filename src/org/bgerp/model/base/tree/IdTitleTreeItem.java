package org.bgerp.model.base.tree;

/**
 * Tree node base class.
 *
 * @author Shamil Vakhitov
 */
public class IdTitleTreeItem extends TreeItem<Integer, IdTitleTreeItem> {
    public IdTitleTreeItem() {
        super();
    }

    public IdTitleTreeItem(Integer id, String title) {
        this.id = id;
        this.title = title;
    }

    @Override
    protected boolean isRootNode() {
        return isRootNodeWithIntegerId(id, parentId);
    }
}