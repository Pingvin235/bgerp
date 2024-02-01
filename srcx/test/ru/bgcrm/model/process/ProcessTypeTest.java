package ru.bgcrm.model.process;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

public class ProcessTypeTest {

    @Test
    public void testClone() {
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
        var root = createItem(null, 0);
        var i1 = createItem(root, 1);
        var i2 = createItem(i1, 2);
        createItem(i2, 4);
        createItem(i2, 5);
        var i3 = createItem(i1, 3);
        var i6 = createItem(root, 6);
        var i7 = createItem(i6, 7);
        var i8 = createItem(i7, 8);
        createItem(i8, 9);

        var clone = root.sub(getTypeList(2, 3));
        Assert.assertTrue(root != clone);
        Assert.assertEquals(1, clone.getChildren().size());

        clone = root.sub(getTypeList(2, 3, 8));
        Assert.assertEquals(2, clone.getChildren().size());

        clone = root.sub(getTypeList(9, 5));
        Assert.assertEquals(2, clone.getChildren().size());

        clone = root.sub(getTypeList(3, 5));
        Assert.assertEquals(1, clone.getChildren().size());
        i1 = clone.getChildren().get(0);
        Assert.assertEquals((Integer) 1, i1.getId());
        Assert.assertEquals(2, i1.getChildren().size());
        i2 = i1.getChildren().get(0);
        Assert.assertEquals((Integer) 2, i2.getId());
        Assert.assertEquals(1, i2.getChildren().size());
        i3 = i1.getChildren().get(1);
        Assert.assertEquals((Integer) 3, i3.getId());
        Assert.assertEquals(0, i3.getChildren().size());
    }

    private List<ProcessType> getTypeList(int... ids) {
        var result = new ArrayList<ProcessType>();
        for (int id : ids)
            result.add(new ProcessType(id, String.valueOf(id)));
        return result;
    }

    private ProcessType createItem(ProcessType parent, int id) {
        var item = new ProcessType();
        item.setId(id);
        item.setTitle(String.valueOf(id));
        if (parent != null)
            parent.addChild(item);
        return item;
    }

}
