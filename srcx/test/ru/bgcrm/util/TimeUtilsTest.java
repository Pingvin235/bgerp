package ru.bgcrm.util;

import org.junit.Assert;
import org.junit.Test;

public class TimeUtilsTest {
    @Test
    public void testGetTypeFormat() {
        Assert.assertEquals(TimeUtils.PATTERN_DDMMYYYY, TimeUtils.getTypeFormat(TimeUtils.FORMAT_TYPE_YMD));
        Assert.assertEquals(TimeUtils.PATTERN_DDMMYYYYHHMMSS, TimeUtils.getTypeFormat(TimeUtils.FORMAT_TYPE_YMDHMS));
        Assert.assertEquals("bla-bla", TimeUtils.getTypeFormat("bla-bla"));
    }
}
