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
}
