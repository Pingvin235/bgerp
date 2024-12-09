package ru.bgcrm.util;

import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

public class PatternFormatterTest {
    private static final String PATTERN = "(x ${key1}) тест ( ${key2})";
    private static final String PATTERN_COLUMN = "(x ${key1:extra1}) тест ( ${key2:extra2})";

    @Test
    public void testProcessPattern() {
        String value = PatternFormatter.processPattern(PATTERN, variable -> {
            if ("key1".equals(variable))
                return "value1";
            if ("key2".equals(variable))
                return "";
            return "???";
        });
        Assert.assertEquals("x value1 тест ", value);

        value = PatternFormatter.processPattern(PATTERN, Map.of("key1", "value1", "key2", "value2"));
        Assert.assertEquals("x value1 тест  value2", value);

        value = PatternFormatter.processPattern(PATTERN_COLUMN, variable -> {
            if (variable.startsWith("key1"))
                return "value1";
            return variable;
        });
        Assert.assertEquals("x value1 тест  key2:extra2", value);
    }
}
