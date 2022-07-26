package org.bgerp.util.sql;

import org.junit.Assert;
import org.junit.Test;

public class LikePatternTest {
    @Test
    public void testEnd() {
        Assert.assertEquals("%value", LikePattern.END.get("value"));
        Assert.assertEquals("", LikePattern.END.get(""));
        Assert.assertEquals(null, LikePattern.END.get(null));
    }

    @Test
    public void testStart() {
        Assert.assertEquals("value%", LikePattern.START.get("value"));
        Assert.assertEquals("", LikePattern.START.get(""));
        Assert.assertEquals(null, LikePattern.START.get(null));
    }

    @Test
    public void testSub() {
        Assert.assertEquals("%value%", LikePattern.SUB.get("value"));
        Assert.assertEquals("", LikePattern.SUB.get(""));
        Assert.assertEquals(null, LikePattern.SUB.get(null));
    }
}
