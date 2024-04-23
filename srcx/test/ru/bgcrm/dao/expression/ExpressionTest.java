package ru.bgcrm.dao.expression;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.jexl3.JexlException;
import org.junit.Test;

public class ExpressionTest {
    public static class LibraryMain {
        public void test1(String param1) {
            System.out.println("test1 is called");
        }

        public void test1(String param1, String param2) {
            System.out.println("test1 ext is called");
        }
    }

    @Test
    public void testSeveralFunctions() {
        Map<String, Object> ctx = new HashMap<>();
        ctx.put(null, new LibraryMain());

        Expression exp = new Expression(ctx);
        exp.executeScript("test1('ddd'); test1('ddd', 'mmm');");
    }

    @Test
    public void testMissingMethod() {
        Map<String, Object> ctx = new HashMap<>();
        ctx.put(null, new LibraryMain());

        boolean thrown = false;

        Expression exp = new Expression(ctx);
        try {
            exp.executeScript("test4('ddd');");
        } catch (JexlException.Method e) {
            thrown = true;
        }

        assertTrue(thrown);
    }

    @Test
    public void testWrongSignature() {
        Map<String, Object> ctx = new HashMap<>();
        ctx.put(null, new LibraryMain());

        boolean thrown = false;

        Expression exp = new Expression(ctx);
        try {
            exp.executeScript("test2(11111, \"TEST\");");
        } catch (JexlException.Method e) {
            thrown = true;
        }

        assertTrue(thrown);
    }

    public static class TestExpression {
        private String value;

        public void setValue(String value) {
            this.value = value;
        }

        public List<Integer> getIds() {
            return Arrays.asList(new Integer[] { 1, 3, 4 });
        }
    }

    @Test
    public void testBooleanExpression() {
        Map<String, Object> ctx = new HashMap<>();
        ctx.put("t", new TestExpression());

        Expression exp = new Expression(ctx);
        assertTrue(exp.check("1 =~ t.getIds()"));
        assertFalse(exp.check("7 =~ t.getIds()"));
        assertEquals("testValue",
                exp.executeScript("b = '0'; a = t.getIds(); if (1 =~ a){b = 'testValue'}; return b;"));
    }

    @Test
    public void testReturningValues() {
        Map<String, Object> ctx = new HashMap<>();
        final TestExpression testExpr = new TestExpression();
        ctx.put("t", testExpr);

        Expression exp = new Expression(ctx);
        exp.executeScript("if (!cu.isEmpty(t.getIds())){t.setValue('testValue')};");

        assertEquals("testValue", testExpr.value);
    }

    @Test
    public void testExpressionV3() {
        Map<String, Object> ctx = new HashMap<>();
        final TestExpression testExpr = new TestExpression();
        ctx.put("t", testExpr);

        Expression exp = new Expression(ctx);
        assertEquals(true, exp.executeScript("return !empty(cu.intersection({1,2}, t.getIds()))"));
    }

    @Test
    public void testExpressionAsScript() {
        Map<String, Object> ctx = new HashMap<>();
        final TestExpression testExpr = new TestExpression();
        ctx.put("t", testExpr);

        Expression exp = new Expression(ctx);
        assertEquals("ab", exp.getString("'a'.concat('b')"));
    }

    @Test
    public void testNewCall() {
        Expression exp = new Expression(new HashMap<>());
        assertEquals("value44", exp.getString("new('ru.bgcrm.dao.expression.ExpressionTestClass', 'value44').getValue()"));
    }

    @Test
    public void testIfExpr() {
        Map<String, Object> map = new HashMap<>(1);
        map.put("numberFrom", "13333333333");

        String expr = "if (numberFrom.length() == 11) { numberFrom = numberFrom.substring(1) }; return numberFrom;";
        String processed = new Expression(map).getString(expr);
        assertEquals("3333333333", processed);
    }

    @Test
    public void testStaticMethodCall() {
        String expr =
                "u = ru.bgcrm.util.Utils;"
                + "return u.parseInt('3') + 't';";
        String value = new Expression(new HashMap<>()).getString(expr);
        assertEquals("3t", value);
    }

    @Test
    public void testConcatenationNull() {
        Map<String, Object> map = new HashMap<>(1);
        map.put("a", "t");
        String expr = "b = null;" +
                "return a + b;";
        String value = new Expression(map).getString(expr);
        assertEquals("t", value);
    }
}