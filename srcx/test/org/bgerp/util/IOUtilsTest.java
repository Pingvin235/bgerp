package org.bgerp.util;

import java.nio.charset.StandardCharsets;

import org.junit.Assert;
import org.junit.Test;

public class IOUtilsTest {
    @Test
    public void testRead() throws Exception {
        byte[] data = IOUtils.read("/org/bgerp/util/IOUtilsTest.data");
        Assert.assertEquals("12345", new String(data, StandardCharsets.UTF_8));
    }
}
