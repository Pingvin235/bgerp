package org.bgerp.plugin.telegram;

import org.junit.Assert;
import org.junit.Test;

public class BotTest {
    @Test
    public void testEscapeSpecialCharacters() {
        Bot bot = new Bot();
        Assert.assertEquals("test", bot.escapeSpecialCharacters("test"));
        Assert.assertEquals("Тест\\.", bot.escapeSpecialCharacters("Тест."));
        Assert.assertEquals("Принято изменение 14893(https://team\\.bgerp\\.org/open/process/14893) от Andrey Zuzenkov: Update Java dependencies",
               bot.escapeSpecialCharacters("Принято изменение 14893(https://team.bgerp.org/open/process/14893) от Andrey Zuzenkov: Update Java dependencies"));
        // TODO: Add more tests here.
    }
}
