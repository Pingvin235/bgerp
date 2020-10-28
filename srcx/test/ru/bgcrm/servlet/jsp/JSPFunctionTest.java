package ru.bgcrm.servlet.jsp;

import org.junit.Assert;
import org.junit.Test;

public class JSPFunctionTest {
    @Test
    public void testHttpLinksToHtml() {
        var value = "This is sample with link https://google.com";
        value = JSPFunction.httpLinksToHtml(value);
        Assert.assertEquals("This is sample with link <a target=\"_blank\" href=\"https://google.com\">https://google.com</a>", value);

        // second time existing link shouldn't be converted
        value = JSPFunction.httpLinksToHtml(value);
        Assert.assertEquals("This is sample with link <a target=\"_blank\" href=\"https://google.com\">https://google.com</a>", value);

        value = JSPFunction.httpLinksToHtml(value + " Yet another links https://google.com <a target=\"_blank\" href=\"http://google.com\">http://google.com</a>");
        Assert.assertEquals("This is sample with link <a target=\"_blank\" href=\"https://google.com\">https://google.com</a> Yet another links <a target=\"_blank\" href=\"https://google.com\">https://google.com</a> <a target=\"_blank\" href=\"http://google.com\">http://google.com</a>", value);
    }
}
