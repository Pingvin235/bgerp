package ru.bgcrm.servlet.jsp;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

public class JSPFunctionTest {

    @Test
    public void testHttpLinksToHtmlSimple() {
        var value = "This is sample with link https://google.com";
        value = JSPFunction.httpLinksToHtml(value);
        Assert.assertEquals("This is sample with link <a target=\"_blank\" href=\"https://google.com\">https://google.com</a>", value);

        // second time existing link shouldn't be converted
        value = JSPFunction.httpLinksToHtml(value);
        Assert.assertEquals("This is sample with link <a target=\"_blank\" href=\"https://google.com\">https://google.com</a>", value);

        value = JSPFunction.httpLinksToHtml(
                value + " Yet another links https://google.com <a target=\"_blank\" href=\"http://google.com\">http://google.com</a>");
        Assert.assertEquals(
                "This is sample with link <a target=\"_blank\" href=\"https://google.com\">https://google.com</a> Yet another links <a target=\"_blank\" href=\"https://google.com\">https://google.com</a> <a target=\"_blank\" href=\"http://google.com\">http://google.com</a>",
                value);
    }

    @Test
    public void testHttpLinksToHtmlWithBreak() {

        //different symbol between links
        var value = "This is sample with link <br/>https://google.com";
        value = JSPFunction.httpLinksToHtml(value);
        Assert.assertEquals("This is sample with link <br/><a target=\"_blank\" href=\"https://google.com\">https://google.com</a>", value);

        value = "This is sample with link\nhttps://google.com";
        value = JSPFunction.httpLinksToHtml(value);
        Assert.assertEquals("This is sample with link\n<a target=\"_blank\" href=\"https://google.com\">https://google.com</a>", value);

        //different symbol after links
        value = "This is sample with link https://google.com\n";
        value = JSPFunction.httpLinksToHtml(value);
        Assert.assertEquals("This is sample with link <a target=\"_blank\" href=\"https://google.com\">https://google.com</a>\n", value);

        value = "This is sample with link https://google.com<br/>";
        value = JSPFunction.httpLinksToHtml(value);
        Assert.assertEquals("This is sample with link <a target=\"_blank\" href=\"https://google.com\">https://google.com</a><br/>", value);

        //different symbol both position
        value = "This is sample with link\nhttps://google.com\n";
        value = JSPFunction.httpLinksToHtml(value);
        Assert.assertEquals("This is sample with link\n<a target=\"_blank\" href=\"https://google.com\">https://google.com</a>\n", value);

        value = "This is sample with link <br/>https://google.com<br/>";
        value = JSPFunction.httpLinksToHtml(value);
        Assert.assertEquals("This is sample with link <br/><a target=\"_blank\" href=\"https://google.com\">https://google.com</a><br/>", value);
    }

    @Ignore
    @Test(timeout = 250)
    public void timeTest() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 250; i++) {
            sb.append(
                    "https://google.com at javax.faces.component.UIComponentBase.encodeChildren<br/>(UIComponentBase.java:890)<a target=\"_blank\" href=\"https://google.com\">https://google.com</a>\n");
        }
        var value = sb.toString();
        value = JSPFunction.httpLinksToHtml(value);
    }

}
