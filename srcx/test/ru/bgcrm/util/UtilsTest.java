package ru.bgcrm.util;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.bgerp.model.base.IdTitle;
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

    @Test
    public void testParseBigDecimal() {
        Assert.assertEquals(new BigDecimal("2.33"), Utils.parseBigDecimal("2.33"));
        Assert.assertEquals(BigDecimal.ZERO, Utils.parseBigDecimal("fufu"));
        Assert.assertEquals(new BigDecimal("1.00"), Utils.parseBigDecimal(1).setScale(2));
    }

    @Test
    public void getGetObjectTitles() {
        List<IdTitle> list = List.of(new IdTitle(1, "A"), new IdTitle(2, "B"), new IdTitle(3, "C"));
        Map<Integer, IdTitle> map = list.stream().collect(Collectors.toMap(IdTitle::getId, Function.identity()));
        Assert.assertEquals("A, B, C", Utils.getObjectTitles(list));
        Assert.assertEquals("C, A, B", Utils.getObjectTitles(map, List.of(3, 1, 2)));
        Assert.assertEquals("B, C", Utils.getObjectTitles(list, Set.of(2, 3)));
        Assert.assertEquals("", Utils.getObjectTitles(List.of()));
    }
}
