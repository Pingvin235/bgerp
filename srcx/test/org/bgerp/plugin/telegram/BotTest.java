package org.bgerp.plugin.telegram;

import org.junit.Assert;
import org.junit.Test;

public class BotTest {
    @Test
    public void testEscapeSpecialCharacters() {
        Bot bot = new Bot();
        Assert.assertEquals("test", bot.escapeSpecialCharacters("test"));
        Assert.assertEquals("Тест\\.", bot.escapeSpecialCharacters("Тест."));
        // TODO: Add more tests here.
    }
}
