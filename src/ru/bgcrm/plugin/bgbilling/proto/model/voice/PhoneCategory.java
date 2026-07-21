package ru.bgcrm.plugin.bgbilling.proto.model.voice;

import org.bgerp.model.base.tree.TreeItem;

public class PhoneCategory extends TreeItem<Integer, PhoneCategory> {
    @Override
    protected boolean isRootNode() {
        return isRootNodeWithIntegerId(id, parentId);
    }
}
