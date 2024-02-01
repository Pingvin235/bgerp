package org.bgerp.model.base.tree.iface;

import java.util.List;

import org.bgerp.model.base.iface.IdTitle;

public interface TreeItem<T, C> extends IdTitle<T> {
    public T getParentId();
    public List<C> getChildren();
}
