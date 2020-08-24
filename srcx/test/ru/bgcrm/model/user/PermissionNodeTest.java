package ru.bgcrm.model.user;

import org.junit.Assert;
import org.junit.Test;

import ru.bgcrm.plugin.task.Plugin;

public class PermissionNodeTest {
    @Test
    public void testBuildTree() {
        var node = new PermissionNode();
        PermissionNode.buildTree(new Plugin().getXml(PermissionNode.FILE_NAME, null).getDocumentElement(), node);
        Assert.assertEquals(1, node.getChildren().size());
        node = node.getChildren().get(0);
        Assert.assertEquals(3, node.getChildren().size());
    }
}