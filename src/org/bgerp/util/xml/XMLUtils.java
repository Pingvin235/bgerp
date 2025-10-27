package org.bgerp.util.xml;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.bgerp.util.Log;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.EntityReference;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSSerializer;
import org.xml.sax.InputSource;

public class XMLUtils {
    private static final Log logger = Log.getLog();

    /***
     * Extracts content of a XML element as a string with all sub-tags.
     * @param node the element node.
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
                if (child.getChildNodes().getLength() == 0) {
                    reply.append("<").append(child.getNodeName()).append("/>");
                } else {
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

    public static Document parseDocument(InputStream stream) {
        return parseDocument(new InputSource(stream));
    }

    public static Document parseDocument(InputSource source) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);

            return factory.newDocumentBuilder().parse(source);
        } catch (Exception e) {
            logger.error(e);
            return null;
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
            return (Node) XPathFactory.newInstance().newXPath().evaluate(expression, node, XPathConstants.NODE);
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
            return (NodeList) XPathFactory.newInstance().newXPath().evaluate(expression, node, XPathConstants.NODESET);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return null;
        }
    }

    public static Iterable<Element> selectElements(Node node, String expression) {
        try {
            // Возвращает NodeList по XPath expression.
            final NodeList nodeList = selectNodeList(node, expression);
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
        return new Iterable<>() {
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
            Node result = selectNode(node, expression);
            return result != null ? result.getNodeValue() : defaultValue;
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            return defaultValue;
        }
    }

    // методы сериализации

    public static void serialize(Node xml, OutputStream result, String encoding, boolean pretty) throws Exception {
        // https://github.com/AdoptOpenJDK/openjdk-jdk11/blob/master/test/jaxp/javax/xml/jaxp/unittest/common/prettyprint/PrettyPrintTest.java#L362
        DOMImplementationRegistry registry = DOMImplementationRegistry.newInstance();
        DOMImplementationLS domImplementation = (DOMImplementationLS) registry.getDOMImplementation("LS");
        LSOutput formattedOutput = domImplementation.createLSOutput();
        formattedOutput.setByteStream(result);
        formattedOutput.setEncoding(encoding);
        LSSerializer domSerializer = domImplementation.createLSSerializer();
        domSerializer.getDomConfig().setParameter("format-pretty-print", pretty);
        domSerializer.getDomConfig().setParameter("xml-declaration", false);
        domSerializer.write(xml, formattedOutput);
    }
}
