package ru.bgcrm.util;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.util.Iterator;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.log4j.Logger;
import org.apache.xpath.XPathAPI;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.EntityReference;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class XMLUtils {
    private static final Logger logger = Logger.getLogger(XMLUtils.class);

    /***
     * Вытаскивает содержимое XML-элемента в виде строки вместе со всеми дочерними тегами
     * @param node
     * @return
     */
    public static String getElementText(Node node) {
        StringBuffer reply = new StringBuffer();

        NodeList children = node.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);

            if ((child instanceof CharacterData && !(child instanceof Comment)) || child instanceof EntityReference) {
                reply.append(child.getNodeValue());
            } else if (child.getNodeType() == Node.ELEMENT_NODE) {
                reply.append("<").append(child.getNodeName());

                NamedNodeMap attrs = child.getAttributes();
                if (attrs.getLength() > 0) {
                    reply.append(" ");
                }
                for (int j = 0; j < attrs.getLength(); j++) {
                    Node attr = attrs.item(j);
                    reply.append(attr.getNodeName()).append("=\"");
                    if (j == attrs.getLength() - 1)
                        reply.append(attr.getNodeValue()).append("\"");
                    else
                        reply.append(attr.getNodeValue()).append("\" ");
                }

                reply.append(">");
                reply.append(getElementText(child));
                reply.append("</").append(child.getNodeName()).append(">");
            }
        }

        return reply.toString();
    }

    /**
     * Создаёт и возвращает новый объект XML документ.
     * @return
     */
    public static final Document newDocument() {
        try {
            DocumentBuilderFactory dFactory = DocumentBuilderFactory.newInstance();
            dFactory.setNamespaceAware(true);
            DocumentBuilder docBuilder = dFactory.newDocumentBuilder();
            return docBuilder.newDocument();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        return null;
    }

    // модификация документа

    /**
     * Создаёт объект-узел с заданным именем в родительском узле. 
     * @param parent родительский узел.
     * @param name имя нового узла.
     * @return
     */
    public static final Element newElement(Element parent, String name) {
        Element result = parent.getOwnerDocument().createElement(name);
        parent.appendChild(result);
        return result;
    }

    /**
     * Создать элемент на родительском документе. Ибо для Element версия ни
     * при каких условиях не работает (OwnerDocument всегда null), не помогает
     * и всякие getDocumentElement.
     * @param parent
     * @param name
     * @return
     */
    public static final Element newElement(Document parent, String name) {
        Element result = parent.createElement(name);
        parent.appendChild(result);
        return result;
    }

    /**
     * Создание текстового узла - потомка. То есть "устанавливаем текст внутрь
     * указанного нода". Если нод - /data, то будет &lt;data&gt;текст&lt;/data&gt;
     * @param node узел
     * @param text текст
     */
    public static void createTextNode(Node node, String text) {
        if (node != null && text != null) {
            node.appendChild(node.getOwnerDocument().createTextNode(text));
        }
    }

    /**
     * Установка значения атрибута, только если оно не равно null.
     * @param element элемент
     * @param name имя аттрибута
     * @param value значение аттрибута
     */
    public static void setAttribute(org.w3c.dom.Element element, String name, String value) {
        if (value != null)
            element.setAttribute(name, value);
    }

    // парсинг документов

    public static Document parseDocument(InputStream stream) {
        return parseDocument(stream, true);
    }

    public static Document parseDocument(InputStream stream, boolean showError) {
        return parseDocument(new InputSource(stream), showError);
    }

    public static Document parseDocument(InputSource source) {
        return parseDocument(source, true);
    }

    public static Document parseDocument(InputSource source, boolean showError) {
        Document result = null;

        if (source != null) {
            try {
                DocumentBuilderFactory dFactory = DocumentBuilderFactory.newInstance();
                dFactory.setNamespaceAware(true);
                dFactory.setValidating(false);
                DocumentBuilder docBuilder = dFactory.newDocumentBuilder();

                result = docBuilder.parse(source);
            } catch (Exception e) {
                if (showError) {
                    logger.error(e.getMessage(), e);
                }
            }
        }

        return result;
    }

    public static void parseDocument(InputSource source, org.xml.sax.ContentHandler handler) {
        try {
            javax.xml.parsers.SAXParserFactory saxParserFactory = javax.xml.parsers.SAXParserFactory.newInstance();
            javax.xml.parsers.SAXParser saxParser = saxParserFactory.newSAXParser();
            org.xml.sax.XMLReader parser = saxParser.getXMLReader();
            parser.setContentHandler(handler);
            parser.parse(source);
        } catch (Exception ex) {
        }
    }

    // методы выборки узлов	

    /**
     * Ищет элемент в документе по имени. Если не находит - возвращает созданный.
     * @param doc
     * @param elementName
     * @return
     */
    public static Element getElement(Document doc, String elementName) {
        return (Element) getNode(doc, elementName);
    }

    /**
     * Берёт Node по имени тега из Document. В случае отсутствия - создаёт новый Node и возвращает его.
     * @param doc документ
     * @param nodeName имя узла
     * @return узел
     * @see findElement 
     */
    public static Node getNode(Document doc, String nodeName) {
        Node node = null;
        if (doc == null)
            return node;
        NodeList list = doc.getElementsByTagName(nodeName);
        if (list != null && list.getLength() > 0)
            node = list.item(0);
        else {
            node = doc.createElement(nodeName);
            doc.getDocumentElement().appendChild(node);
        }
        return node;
    }

    /**
     * Возвращает элемент по XPath expression.
     * @param node
     * @param expression XPath expression
     * @return элемент, если найден - иначе null
     * @see #selectNode( Node, String )
     */
    public static Element selectElement(Node node, String expression) {
        return (Element) selectNode(node, expression);
    }

    /**
     * Возвращает Node по XPath expression.
     * 
     * Пример: /data/table - выбрать элемент table лежащий в корне.
     * Пример: //table - выбрать элемент table где попало
     * 
     * @param node
     * @param expression XPath expression
     * @return Node, если найден - иначе null
     */
    public static Node selectNode(Node node, String expression) {
        try {
            return XPathAPI.selectSingleNode(node, expression);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return null;
        }
    }

    /**
     * Возвращает NodeList по XPath expression.
     * 
     * @param node
     * @param expression XPath expression
     * @return NodeList, если найден - иначе null
     */
    public static NodeList selectNodeList(Node node, String expression) {
        try {
            return XPathAPI.selectNodeList(node, expression);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return null;
        }
    }

    public static Iterable<Element> selectElements(Node node, String expression) {
        try {
            // Возвращает NodeList по XPath expression.
            final NodeList nodeList = XPathAPI.selectNodeList(node, expression);
            return elements(nodeList);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return null;
        }
    }

    /**
     * Делает Iterable от Element из NodeList, для удобного обхода.
     * @param nodeList исходный NodeList
     * @return Iterable&lt;Element&gt;.
     */
    public static Iterable<Element> elements(final NodeList nodeList) {
        return new Iterable<Element>() {
            @Override
            public Iterator<Element> iterator() {
                return new NodeListElementIterator(nodeList);
            }
        };
    }

    /**
     * Выборка строкового значения по xpath. Дефолт валуе - null.
     * @see #selectText( Node, String, String )
     */
    public static String selectText(Node node, String expression) {
        return selectText(node, expression, null);
    }

    /**
     * Выборка строкового значения по xpath. То есть практически тоже самое, что
     * selectNode, только от него возвращается node value, либо null, если
     * что-то не найдено или какая-то ошибка (нуть не должен возвращать текст итд)
     * Может возвращать и значения текстовых нод и значения атрибутов, универсальная.<br/>
     * <br/>
     * <b>В целом, не предназначен для выборок вроде selectText( node, "@selected", null),
     * потому что в том что getAttribute возвращает не null,
     * а пустую строку нет ничего страшного.</b> Тем более есть метод, который и
     * null возвращает и работает быстрее.<br/>
     * <br/>
     * Примеры запросов:<br>
     * 1) запрос текстового значения атрибута<pre>
     * &lt;data&gt;&lt;payment cardnumber="111"&gt;...
     * /data/payment/@cardnumber
     * </pre>
     * 2) запрос текстового значения ноды<pre>
     * ...&lt;operation&gt;&lt;pursesrc&gt;текст&lt;/pursesrc&gt;...
     * /operation/pursesrc/text()
     * </pre>
     * 3) не от корня, а относительно - начиная не со сшеша<br>
     * 4) искать где попало - два слеша<br>
     * 5) и т.д., ну вы поняли
     * @param node
     *            ноде корневой
     * @param expression
     *            xpath
     * @param defaultValue значение по умолчанию
     * @return строка
     * @see #selectNode
     */
    public static String selectText(Node node, String expression, String defaultValue) {
        try {
            Node result = XPathAPI.selectSingleNode(node, expression);
            return result != null ? result.getNodeValue() : defaultValue;
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            return defaultValue;
        }
    }

    // методы сериализации

    /**
     * Сериализует в выходной поток с указанием кодировки.
     * @param node
     * @param os
     * @param encoding
     */
    public static void serialize(Node node, OutputStream os, String encoding) {
        serialize(node, new StreamResult(os), encoding);
    }

    /**
     * Сериалует в Writer. Обычно используется для отладки, так как правильнее использовать сериализацию в OutputStream.
     * @param node
     * @param writer
     */
    public static void serialize(final Node node, final Writer writer) {
        serialize(node, new StreamResult(writer), null);
    }

    /**
     * Сериализует в Writer. Обычно используется для отладки, так как правильнее использовать сериализацию в OutputStream.
     * @param node
     * @param writer
     */
    public static void serialize(final Node node, final Writer writer, String encoding) {
        serialize(node, new StreamResult(writer), encoding);
    }

    /**
     * Сериализует в Result, отступ - false.
     * @param node
     * @param result
     * @param encoding
     */
    public static void serialize(Node node, Result result, String encoding) {
        serialize(node, result, encoding, false);
    }

    public static void serialize(Node node, Result result, String encoding, boolean indent) {
        try {
            TransformerFactory factory = TransformerFactory.newInstance();
            Transformer transformer = factory.newTransformer();
            if (indent) {
                transformer.setOutputProperty(javax.xml.transform.OutputKeys.INDENT, "yes");
                transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            }
            if (encoding != null) {
                transformer.setOutputProperty(javax.xml.transform.OutputKeys.ENCODING, encoding);
            }

            transformer.transform(new DOMSource(node), result);
        } catch (FactoryConfigurationError e) {
            logger.error("Could not locate a factory class", e);
        } catch (TransformerConfigurationException e) {
            logger.error("TransformerConfigurationException", e);
        } catch (TransformerException e) {
            logger.error("TransformerException", e);
        }
    }

    /**
     * Подготавливает элемент к XML сериализации, заменяет запрещённые символы на \\u{code}.
     * Используется в местах, где возможно появление недопустимых XML символов.
     * @param el исходный элемент в теле, названии, дочерних элементах и атрибутах возможны запрещённые символы. 
     */
    public static void prepareElementToSerialize(Node el) {
        StringBuilder buf = new StringBuilder(100);
        int size = 0;

        NamedNodeMap map = el.getAttributes();
        if (map != null) {
            size = map.getLength();
            for (int i = 0; i < size; i++) {
                Node node = map.item(i);
                String nodeValue = node.getNodeValue();

                node.setNodeValue(prepareString(buf, nodeValue));
            }
        }

        if (el.getNodeValue() != null) {
            el.setNodeValue(prepareString(buf, el.getNodeValue()));
        }

        NodeList childs = el.getChildNodes();
        size = childs.getLength();
        for (int i = 0; i < size; i++) {
            Node child = childs.item(i);
            prepareElementToSerialize(child);
        }
    }

    /**
     * Подготавливает строки к XML сериализации, заменяет запрещённые символы на \\u{code}.
     * Используется в местах, где возможно появление недопустимых XML символов.
     * @param buf вспомогательный буфер, в который складывается результат, чтобы не выделять каждый раз заново.
     * @param nodeValue исходная строка, где возможны запрещённые символы.
     * @return
     */
    public static String prepareString(StringBuilder buf, String nodeValue) {
        char ch;
        buf.setLength(0);

        for (int j = 0; j < nodeValue.length(); j++) {
            ch = nodeValue.charAt(j);
            int ich = ch;

            //То что считается правильным xml-символом по стандарту 
            if (ich == 0x9 || ich == 0xA || ich == 0xD || (ich >= 0x20 && ich <= 0xD7FF) || (ich >= 0xE000 && ich <= 0xFFFD)
                    || (ich >= 0x10000 && ich <= 0x10FFFF)) {
                buf.append(ch);
            } else {
                buf.append("\\u");
                buf.append(ich);
            }
        }

        return buf.toString();
    }

    // трансформация

    /**
     * Трансформация xml+xstl&rarr;выход
     * @param xml исходный документ xml в виде Source.
     * @param xslt исходный документ xslt в виде Source.
     * @param res результат (например, готовый FO-документ) в виде Result.
     * @param enc кодировка, может быть null, тогда получается из шаблона.
     * @throws TransformerException ошибка трансформации.
     */
    public static void transform(Source xml, Source xslt, Result res, String enc) throws TransformerException {
        TransformerFactory factory = TransformerFactory.newInstance();
        Transformer transformer = factory.newTransformer(xslt);

        transformer.setOutputProperty(javax.xml.transform.OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

        // кодировка может быть указана в самом шаблоне
        if (enc != null) {
            transformer.setOutputProperty("encoding", enc);
        }

        transformer.transform(xml, res);
    }
}
