package ru.bgerp.l10n;

import org.junit.Assert;
import org.junit.Test;

public class LocalizationTest {
    @Test
    public void testMultilineMessage() {
        var pl = new org.bgerp.plugin.msg.email.Plugin();
        var l = new Localizer(Localization.LANG_RU, Localization.getLocalization(pl));
        var localizedText = l.l("email.sign.standard");
        Assert.assertTrue(localizedText.contains("Сообщение подготовлено системой"));
        Assert.assertTrue(localizedText.contains("\nНе изменяйте,"));
    }

    @Test
    public void testMessagePattern() {
        var pl = new org.bgerp.plugin.msg.email.Plugin();
        var l = new Localizer(Localization.LANG_EN, Localization.getLocalization(pl));
        var localizedText = l.l("История сообщений по процессу #{}", 123);
        Assert.assertEquals("Messages history for process #123", localizedText);
    }
}
