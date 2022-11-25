package ru.bgcrm.util;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

public class XMLUtilsTest {

    @Test
    public void testSerialize() throws Exception {
        Document doc = XMLUtils.newDocument();
        Element el = XMLUtils.newElement(doc, "test");
        el.setAttribute("a", "1");
        XMLUtils.newElement(el, "child");

        ByteArrayOutputStream bos = new ByteArrayOutputStream(100);
        XMLUtils.serialize(doc, bos, StandardCharsets.UTF_8.name(), true);
        String result = new String(bos.toByteArray(), StandardCharsets.UTF_8.name());
        Assert.assertTrue(result.contains("<test a=\"1\">\n"));
    }

    @Test
    public void testSelectNodeList() {
        var doc = XMLUtils.newDocument();
        var el = XMLUtils.newElement(doc, "test");
        var child = XMLUtils.newElement(el, "child");
        child.setAttribute("a", "1");

        var nodes = XMLUtils.selectNodeList(doc.getDocumentElement(), "/test/child");
        Assert.assertNotNull(nodes);
        Assert.assertEquals(1, nodes.getLength());
    }

    @Test
    public void testGetElementText() throws Exception {
        var doc = XMLUtils.parseDocument(new InputSource(getClass().getResourceAsStream(getClass().getSimpleName() + ".testGetElementText")));
        Assert.assertEquals("Simple text.", XMLUtils.getElementText(XMLUtils.getNode(doc, "item1")));
        Assert.assertEquals("Line\nbreak.", XMLUtils.getElementText(XMLUtils.getNode(doc, "item2")));
        Assert.assertEquals("Formatted HTML <b>text</b><br/>\nwith linebreaks and <i>so on</i>.", XMLUtils.getElementText(XMLUtils.getNode(doc, "item3")));
        Assert.assertEquals("Текст на Великом и Могучем", XMLUtils.getElementText(XMLUtils.getNode(doc, "item4")));
    }
}
