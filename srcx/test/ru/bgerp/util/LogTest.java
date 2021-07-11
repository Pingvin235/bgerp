package ru.bgerp.util;

import org.junit.Assert;
import org.junit.Test;

public class LogTest {
    @Test
    public void testFormat() {
        Assert.assertEquals("Test with 1 and 2", Log.format("Test with {} and {}", "1", 2));
        Assert.assertEquals("Test with 1 and {}", Log.format("Test with {} and {}", "1"));
        Assert.assertEquals("Use String.format 3 and 'bb'", Log.format("Use String.format %s and '%s'", 3, "bb"));
    }
}
