package org.bgerp.dao.expression;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.jexl3.JexlException;
import org.junit.Test;
import ru.bgcrm.model.process.Process;

public class ExpressionTest {
    public static class ExpressionObjectTopNamespace {
        public void test1(String param1) {
            System.out.println("test1 is called");
        }

        public void test1(String param1, String param2) {
            System.out.println("test1 ext is called");
        }

        private static Map<String, Object> toContext() {
            var result = new HashMap<String, Object>();
            result.put(null, new ExpressionObjectTopNamespace());
            return result;
        }
    }

    @Test
    public void testSeveralFunctions() {
        Expression exp = new Expression(ExpressionObjectTopNamespace.toContext());
        exp.execute("test1('ddd'); test1('ddd', 'mmm');");
    }

    @Test
    public void testMissingMethod() {
        boolean thrown = false;

        Expression exp = new Expression(ExpressionObjectTopNamespace.toContext());
        try {
            exp.execute("test4('ddd');");
        } catch (JexlException.Method e) {
            thrown = e.getMessage().endsWith("unsolvable function/method 'test4(String)'");
        }

        assertTrue(thrown);
    }

    @Test
    public void testWrongSignature() {
        boolean thrown = false;

        Expression exp = new Expression(ExpressionObjectTopNamespace.toContext());
        try {
            exp.execute("test2(11111, \"TEST\");");
        } catch (JexlException.Method e) {
            thrown = e.getMessage().endsWith("unsolvable function/method 'test2(Short, String)'");
        }

        assertTrue(thrown);
    }

    public static class ExpressionObject {
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
        Expression exp = new Expression(Map.of("t", new ExpressionObject()));
        assertTrue(exp.executeCheck("1 =~ t.getIds()"));
        assertFalse(exp.executeCheck("7 =~ t.getIds()"));
        assertEquals("testValue",
                exp.execute("b = '0'; a = t.getIds(); if (1 =~ a){b = 'testValue'}; return b;"));
    }

    @Test
    public void testChangeValue() {
        final ExpressionObject obj = new ExpressionObject();

        Expression exp = new Expression(Map.of("t", obj));
        exp.execute("if (!cu.isEmpty(t.getIds())){t.setValue('testValue')};");

        assertEquals("testValue", obj.value);
    }

    @Test
    public void testStringUtils() {
        Expression exp = new Expression(Map.of("t", new ExpressionObject()));
        assertEquals(true, exp.execute("return !empty(cu.intersection({1,2}, t.getIds()))"));
    }

    @Test
    public void testGetStringWithoutReturn() {
        Expression exp = new Expression(Map.of("t", new ExpressionObject()));
        assertEquals("ab", exp.executeGetString("'a'.concat('b')"));
    }

    @Test
    public void testNewCall() {
        Expression exp = new Expression(Map.of());
        assertEquals("value44", exp.executeGetString("new('org.bgerp.dao.expression.ExpressionTestClass', 'value44').getValue()"));
    }

    @Test
    public void testIfExpr() {
        String expr = "if (numberFrom.length() == 11) { numberFrom = numberFrom.substring(1) }; return numberFrom;";
        String processed = new Expression(Map.of("numberFrom", "13333333333")).executeGetString(expr);
        assertEquals("3333333333", processed);
    }

    @Test
    public void testStaticMethodCall() {
        String expr =
                "u = ru.bgcrm.util.Utils;"
                + "return u.parseInt('3') + 't';";
        String value = new Expression(Map.of()).executeGetString(expr);
        assertEquals("3t", value);
    }

    @Test
    public void testConcatenationNull() {
        String expr = "b = null;" +
                "return a + b;";
        String value = new Expression(Map.of("a", "t")).executeGetString(expr);
        assertEquals("t", value);
    }

    @Test
    public void testMethodCallOfNullObject() {
        Map<String, Object> context = new HashMap<>();
        context.put("p", null);

        String expr = "p.value()";

        String value = new Expression(context, true).executeGetString(expr);
        assertNull(value);

        boolean thrown = false;
        try {
            new Expression(context).execute(expr);
        } catch (JexlException.Variable e) {
            thrown = e.getMessage().endsWith("variable 'p' is null");
        }
        assertTrue(thrown);
    }

    @Test
    public void testMethodCallOfReturnedNullObject() {
        Map<String, Object> context = new HashMap<>();
        context.put("p", new Process());

        boolean thrown = false;
        try {
            new Expression(context).execute("p.getCloseTime().getTime()");
        } catch (JexlException.Property e) {
            thrown = e.getMessage().endsWith("undefined property '.getCloseTime'");
        }
        assertTrue(thrown);
    }

    @Test
    public void testCallOfMissingMethod() {
        boolean thrown = false;
        try {
            new Expression(Map.of("t", new ExpressionObject())).execute("t.doSomething('arg');");
        } catch (JexlException.Method e) {
            thrown = e.getMessage().endsWith("unsolvable function/method 'doSomething(String)'");
        }
        assertTrue(thrown);

        thrown = false;
        try {
            new Expression(Map.of("t", new ExpressionObject())).execute("t.getSomething();");
        } catch (JexlException.Method e) {
            thrown = e.getMessage().endsWith("unsolvable function/method 'getSomething'");
        }
        assertTrue(thrown);
    }
}