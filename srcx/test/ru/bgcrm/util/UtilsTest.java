package ru.bgcrm.util;

import org.junit.Assert;
import org.junit.Test;

public class UtilsTest {
    @Test
    public void testIsValidEmail() {
        Assert.assertFalse(Utils.isValidEmail(" "));
        Assert.assertFalse(Utils.isValidEmail(" vv"));
        Assert.assertFalse(Utils.isValidEmail("vv.com"));
        Assert.assertTrue(Utils.isValidEmail("vasya@server.com"));
        Assert.assertTrue(Utils.isValidEmail("vova <vasya@server.com>"));
    }

    @Test
    public void testHasClass() {
        Assert.assertTrue("Should has class", Utils.hasClass("", "a.b.c", "java.lang.String"));
        Assert.assertFalse("Should hasn't class", Utils.hasClass("", "t.b.c"));
    }
}
