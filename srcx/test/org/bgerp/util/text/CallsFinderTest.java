package org.bgerp.util.text;

import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

public class CallsFinderTest {
    @Test
    public void testFind() {
        String expression = "pp.getDescription().concat('something')";
        Assert.assertTrue(new CallsFinder(Set.of("p", "pp"), Set.of("getDescription(", "val(4)")).find(expression));
        Assert.assertFalse(new CallsFinder(Set.of("a"), Set.of("getDescription(")).find(expression));

        expression = """
            if (u.isBlank(p.getDescription()) {
                result = pp.val(3).concat(pp.val(12));
            }""";
        Assert.assertTrue(new CallsFinder(Set.of("pp"), Set.of("val(12)", "val(2)")).find(expression));
    }
}
