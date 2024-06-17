package ru.bgcrm.util;

import java.time.LocalDate;

import org.bgerp.util.TimeConvert;
import org.junit.Assert;
import org.junit.Test;

public class TimeUtilsTest {
    @Test
    public void testGetTypeFormat() {
        Assert.assertEquals(TimeUtils.PATTERN_DDMMYYYY, TimeUtils.getTypeFormat(TimeUtils.FORMAT_TYPE_YMD));
        Assert.assertEquals(TimeUtils.PATTERN_DDMMYYYYHHMMSS, TimeUtils.getTypeFormat(TimeUtils.FORMAT_TYPE_YMDHMS));
        Assert.assertEquals("bla-bla", TimeUtils.getTypeFormat("bla-bla"));
    }

    @Test
    public void testGetEndMonth() {
        Assert.assertEquals(TimeUtils.parse("31.01.2023", TimeUtils.PATTERN_DDMMYYYY),
                TimeUtils.getEndMonth(TimeConvert.toDate(LocalDate.of(2023, 1, 14))));
        Assert.assertEquals(null, TimeUtils.getEndMonth(null));
    }
}
