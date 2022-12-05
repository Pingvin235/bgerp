package org.bgerp.plugin.telegram;

import org.junit.Assert;
import org.junit.Test;

public class ExpressionObjectTest {
    @Test
    public void testEscapeMarkdown() {
        Assert.assertEquals("the text \\( with escape \\)", ExpressionObject.escapeMarkdown("the text ( with escape )"));
    }
}
