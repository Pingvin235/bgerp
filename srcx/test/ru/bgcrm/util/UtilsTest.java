package ru.bgcrm.util;

import java.util.Collections;
import java.util.List;
import java.util.Set;

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

    @Test
    public void testGetOpenId() {
        Assert.assertEquals(0, Utils.getOpenId(null));
        Assert.assertEquals(0, Utils.getOpenId(""));
        Assert.assertEquals(0, Utils.getOpenId("bla-bla"));
        Assert.assertEquals(5, Utils.getOpenId("/open/process/5"));
        Assert.assertEquals(5, Utils.getOpenId("/open/process/5#43"));
        Assert.assertEquals(5, Utils.getOpenId("/open/process/5?param=value#43"));
    }

    @Test
    public void testGenerateSecret() {
        var secret = Utils.generateSecret();
        Assert.assertEquals(32, secret.length());
        Assert.assertEquals(secret, secret.toUpperCase());
        Assert.assertNotEquals(Utils.generateSecret(), secret);
    }

    @Test
    public void testToSet() {
        Assert.assertEquals(Collections.emptySet(), Utils.toSet("", " "));
        Assert.assertEquals(Collections.emptySet(), Utils.toSet(null, " "));
        Assert.assertEquals(Set.of("a", "b", "c"), Utils.toSet("a b c", " "));
        Assert.assertEquals(Set.of("a", "b", "c"), Utils.toSet("a b  c ", " "));
        Assert.assertEquals(Set.of("a", "b", "c"), Utils.toSet("a,b, c"));
        Assert.assertEquals(Set.of("a", "b", "c"), Utils.toSet("a,b; c"));
    }

    @Test
    public void testToList() {
        Assert.assertEquals(Collections.emptyList(), Utils.toList("", " "));
        Assert.assertEquals(Collections.emptyList(), Utils.toList(null, " "));
        Assert.assertEquals(List.of("a", "b", "c"), Utils.toList("a b c", " "));
        Assert.assertEquals(List.of("a", "b", "c"), Utils.toList("a b  c ", " "));
        Assert.assertEquals(List.of("a", "b", "c"), Utils.toList("a, b,  c"));
        Assert.assertEquals(List.of("a", "b", "c"), Utils.toList("a,b; c"));
    }
}
