package ru.bgcrm.util.io;

import java.nio.charset.StandardCharsets;

import org.junit.Test;
import org.junit.Assert;

public class IOUtilsTest {
    @Test
    public void testRead() throws Exception {
        byte[] data = IOUtils.read("/ru/bgcrm/util/io/IOUtilsTest.data");
        Assert.assertEquals("12345", new String(data, StandardCharsets.UTF_8));
    }
}
