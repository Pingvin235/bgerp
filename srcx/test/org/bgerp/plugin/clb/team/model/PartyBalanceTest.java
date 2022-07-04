package org.bgerp.plugin.clb.team.model;

import java.math.BigDecimal;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import ru.bgcrm.model.Pair;

public class PartyBalanceTest {
    @Test
    public void testBalance1() {
        var balance = new PartyBalance(List.of(
            new Pair<>(1, new BigDecimal(100)),
            new Pair<>(2, new BigDecimal(200)),
            new Pair<>(3, new BigDecimal(300))
        ));

        Assert.assertNull(balance.get(1, 2));
        Assert.assertNull(balance.get(2, 1));
        Assert.assertEquals(new BigDecimal(100), balance.get(1, 3));
        Assert.assertNull(balance.get(3, 1));
    }

    @Test
    public void testBalance2() {
        var balance = new PartyBalance(List.of(
            new Pair<>(12, new BigDecimal(1000)),
            new Pair<>(10, new BigDecimal(0)),
            new Pair<>(2, new BigDecimal(300)),
            new Pair<>(17, new BigDecimal(800))
        ));

        Assert.assertNull(balance.get(1, -2));
        Assert.assertNull(balance.get(12, 10));
        Assert.assertEquals(new BigDecimal(475), balance.get(10, 12));
        Assert.assertNull(balance.get(17, 10));
        Assert.assertEquals(new BigDecimal(50), balance.get(10, 17));
        Assert.assertNull(balance.get(17, 2));
        Assert.assertEquals(new BigDecimal(225), balance.get(2, 17));
    }

    @Test
    public void testBalance3() {
        var balance = new PartyBalance(List.of(
            new Pair<>(2, new BigDecimal("10.0")),
            new Pair<>(3, new BigDecimal("25.0")),
            new Pair<>(1, new BigDecimal("3.3"))
        ));

        Assert.assertNull(balance.get(1, 2));
        Assert.assertNull(balance.get(2, 1));
        Assert.assertNull(balance.get(3, 1));
        Assert.assertEquals(new BigDecimal("9.4"), balance.get(1, 3));
        Assert.assertNull(balance.get(3, 2));
        Assert.assertEquals(new BigDecimal("2.8"), balance.get(2, 3));
    }
}
