package org.bgerp.util;

import org.junit.Assert;
import org.junit.Test;

public class PhoneFormatTest {
    @Test
    public void testFormat() {
        var format = new PhoneFormat("+7 (347) XXX-XX-XX,+7 9XX XXX-XX-XX,X XXX XXX-XX-XX");
        Assert.assertEquals("+7 (347) 222-22-22", format.format("73472222222"));
        Assert.assertEquals("+7 917 444-44-44", format.format("79174444444"));

        format = new PhoneFormat("017X XXXXXXX,089 XXXXXXX");
        Assert.assertEquals("0179 2752222", format.format("01792752222"));
        Assert.assertEquals("089 2222222", format.format("0892222222"));
    }
}
