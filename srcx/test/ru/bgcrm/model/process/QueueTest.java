package ru.bgcrm.model.process;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;

import ru.bgcrm.struts.form.DynActionForm;

public class QueueTest {
    @Test
    public void testExtractSort() throws IOException {
        var q = new Queue();
        q.setConfig(IOUtils.toString(getClass().getResourceAsStream("QueueTest.config.sort.txt"), StandardCharsets.UTF_8));
        q.extractFiltersAndSorts();
        var ss = q.getSortSet();
        Assert.assertEquals("2, 3", ss.getOrders(null));

        var form = new DynActionForm("url?sort=a&sort=b");
        Assert.assertEquals("2, 3", ss.getOrders(form));
    }

    @Test
    public void testExtractSortDefault() throws IOException {
        var q = new Queue();
        q.setConfig(IOUtils.toString(getClass().getResourceAsStream("QueueTest.config.sort.default.txt"), StandardCharsets.UTF_8));
        q.extractFiltersAndSorts();
        var ss = q.getSortSet();
        Assert.assertEquals("3, 2", ss.getOrders(null));

        var form = new DynActionForm("url?sort=a&sort=b");
        Assert.assertEquals("a, b", ss.getOrders(form));
    }
}
