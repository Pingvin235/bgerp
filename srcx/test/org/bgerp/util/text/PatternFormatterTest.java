package org.bgerp.util.text;

import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

public class PatternFormatterTest {
    @Test
    public void testProcessPatternSimple() {
        final String pattern = "(x ${key1}) тест ( ${key2})";

        String value = PatternFormatter.processPattern(pattern, variable -> {
            if ("key1".equals(variable))
                return "value1";
            if ("key2".equals(variable))
                return "";
            return "???";
        });
        Assert.assertEquals("x value1 тест ", value);

        value = PatternFormatter.processPattern(pattern, Map.of("key1", "value1", "key2", "value2"));
        Assert.assertEquals("x value1 тест  value2", value);
    }

    @Test
    public void testProcessPatternSemicolon() {
        final String pattern = "(x ${key1:extra1}) тест ( ${key2:extra2}) end";

        String value = PatternFormatter.processPattern(pattern, variable -> {
            if (variable.startsWith("key1"))
                return "value1";
            return variable;
        });
        Assert.assertEquals("x value1 тест  key2:extra2 end", value);
    }

    @Test
    public void testProcessPatternUnderscore() {
        final String pattern = "(x ${key_1}) bla  ${key_2}";

        String value = PatternFormatter.processPattern(pattern, variable -> {
            if ("key_1".equals(variable))
                return "value1";
            if ("key_2".equals(variable))
                return "value2";
            return "???";
        });
        Assert.assertEquals("x value1 bla  value2", value);
    }

    @Test
    public void testProcessPatternEmptyVars() {
        final String pattern = "(x ${key_1}) bla  ${key_2}";

        String value = PatternFormatter.processPattern(pattern, variable -> {
            if ("key_1".equals(variable))
                return null;
            if ("key_2".equals(variable))
                return "";
            return "???";
        });
        Assert.assertEquals(" bla  ", value);
    }

    @Test
    public void testProcessPatternCommas() {
        final String pattern = "(${key_1})(, ${key_2})(, ${key_3})";

        String value = PatternFormatter.processPattern(pattern, variable -> {
            if ("key_1".equals(variable))
                return null;
            if ("key_2".equals(variable))
                return "value2";
            if ("key_3".equals(variable))
                return "value3";
            return "???";
        });
        Assert.assertEquals(", value2, value3", value);
    }

    @Test
    public void testProcessPatternOrgTitle() {
        final String pattern = "${param_33}( Бла-бла: ${param_45})";

        String value = PatternFormatter.processPattern(pattern, variable -> {
            if ("param_33".equals(variable))
                return "ИП";
            if ("param_45".equals(variable))
                return "Образцов И.И.";
            return "???";
        });
        Assert.assertEquals("ИП Бла-бла: Образцов И.И.", value);
    }

    @Test
    public void testProcessPatternAddress() {
        final String pattern = "(${street})(, ${house})(, ${floor} floor)(, apt. ${flat})( ${room})( ${comment})( ${index})( ${city})( [${comment}])";

        String value = PatternFormatter.processPattern(pattern, variable -> {
            if ("index".equals(variable))
                return "450103";
            if ("city".equals(variable))
                return "Уфа";
            if ("street".equals(variable))
                return "Габдуллы Амантая";
            if ("house".equals(variable))
                return "6";
            if ("flat".equals(variable))
                return "33";
            return "";
        });
        Assert.assertEquals("Габдуллы Амантая, 6, apt. 33 450103 Уфа", value);
    }

    @Test
    public void testProcessPatternPhone() {
        final String pattern = "(${number})( [${comment}])";

        String value = PatternFormatter.processPattern(pattern, variable -> {
            if ("number".equals(variable))
                return "42";
            if ("comment".equals(variable))
                return "Comment";
            return "";
        });
        Assert.assertEquals("42 [Comment]", value);
    }

    @Test
    public void testProcessPatternOrgName() {
        final String pattern = "${param_1} \"${param_2}\"";

        String value = PatternFormatter.processPattern(pattern, variable -> {
            if ("param_1".equals(variable))
                return "ИП";
            if ("param_2".equals(variable))
                return "Образцов Иван";
            return "";
        });
        Assert.assertEquals("ИП \"Образцов Иван\"", value);
    }
}
