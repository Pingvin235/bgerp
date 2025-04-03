package org.bgerp.model.base.tree;

import java.util.Set;
import java.util.TreeSet;

import org.junit.Assert;
import org.junit.Test;

public class TreeItemTest {
    @Test
    public void testIsInPathTo() {
       /* 0
        *   1
        *     2
        *        4
        *        5
        *     3
        *   6
        *     7
        *       8
        *         9
        */
        var i0 = createItem(null, 0);
        var i1 = createItem(i0, 1);
        var i2 = createItem(i1, 2);
        var i4 = createItem(i2, 4);
        createItem(i2, 5);
        var i3 = createItem(i1, 3);
        var i6 = createItem(i0, 6);
        var i7 = createItem(i6, 7);
        var i8 = createItem(i7, 8);
        createItem(i8, 9);

        var ids = Set.of(2, 3);
        Assert.assertTrue(i0.isInPathTo(ids));
        Assert.assertTrue(i1.isInPathTo(ids));
        Assert.assertTrue(i3.isInPathTo(ids));
        Assert.assertFalse(i4.isInPathTo(ids));
        Assert.assertFalse(i6.isInPathTo(ids));

        Assert.assertEquals(Set.of(0, 1, 2, 3, 6, 7, 8), getPathIds(new TreeSet<>(), i0, Set.of(2, 3, 8)));
        Assert.assertEquals(Set.of(0, 1, 2, 5, 6, 7, 8, 9), getPathIds(new TreeSet<>(), i0, Set.of(9, 5)));
        Assert.assertEquals(Set.of(0, 1, 2, 5, 3), getPathIds(new TreeSet<>(), i0, Set.of(3, 5)));
        Assert.assertEquals(Set.of(6, 7, 8), getPathIds(new TreeSet<>(), i6, Set.of(7, 8, 0)));
    }

    private Set<Integer> getPathIds(Set<Integer> result, IdTitleTreeItem item, Set<Integer> ids) {
        if (item.isInPathTo(ids))
            result.add(item.getId());

        for (var child : item.getChildren())
            getPathIds(result, child, ids);

        return result;
    }

    private IdTitleTreeItem createItem(IdTitleTreeItem parent, int id) {
        var item = new IdTitleTreeItem(id, String.valueOf(id));
        if (parent != null)
            parent.addChild(item);
        return item;
    }
}
