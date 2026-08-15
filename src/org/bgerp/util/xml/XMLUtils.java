package org.bgerp.util.xml;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.Set;

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
     * Extracts content of a XML element as a string with all sub-tags
     * @param node the element node
     * @return the nodes' content
     */
    public static String getElementText(Node node) {
        return getElementText(node, null);
    }

    /***
     * Extracts content of a XML element as a string with all sub-tags
     * @param node the element node
     * @param skipNodeNames if not {@code null} - node names to be skipped
     * @return the nodes' content
     */
    public static String getElementText(Node node, Set<String> skipNodeNames) {
        StringBuffer reply = new StringBuffer();

        NodeList children = node.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);

            if ((child instanceof CharacterData && !(child instanceof Comment)) || child instanceof EntityReference) {
                reply.append(child.getNodeValue());
            } else if (child.getNodeType() == Node.ELEMENT_NODE && (skipNodeNames == null || !skipNodeNames.contains(child.getNodeName()))) {
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
     * Creates and returns a new XML document object
     * @return the created document
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

    // document modification

    /**
     * Creates a node object with the given name in the parent node
     * @param parent the parent node
     * @param name the new node's name
     * @return the created element
     */
    public static final Element newElement(Element parent, String name) {
        Element result = parent.getOwnerDocument().createElement(name);
        parent.appendChild(result);
        return result;
    }

    /**
     * Creates an element on the parent document. The {@code Element} overload never works
     * (OwnerDocument is always {@code null}), and {@code getDocumentElement} doesn't help either.
     * @param parent
     * @param name
     * @return the created element
     */
    public static final Element newElement(Document parent, String name) {
        Element result = parent.createElement(name);
        parent.appendChild(result);
        return result;
    }

    /**
     * Creates a text child node. In other words, sets text inside the specified node.
     * If the node is /data, the result will be &lt;data&gt;text&lt;/data&gt;
     * @param node the node
     * @param text the text
     */
    public static void createTextNode(Node node, String text) {
        if (node != null && text != null) {
            node.appendChild(node.getOwnerDocument().createTextNode(text));
        }
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

    // node selection methods

    /**
     * Searches for an element in the document by name. If not found - returns a created one.
     * @param doc
     * @param elementName
     * @return the found or created element
     */
    public static Element getElement(Document doc, String elementName) {
        return (Element) getNode(doc, elementName);
    }

    /**
     * Gets a Node by tag name from Document. If absent - creates a new Node and returns it.
     * @param doc the document
     * @param nodeName the node name
     * @return the node
     * @see #getElement(Document, String)
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
     * Returns element by XPath expression
     * @param node
     * @param expression XPath expression
     * @return the element, if found - otherwise {@code null}
     * @see #selectNode( Node, String )
     */
    public static Element selectElement(Node node, String expression) {
        return (Element) selectNode(node, expression);
    }

    /**
     * Returns Node by XPath expression
     *
     * Example: /data/table - select the table element at the root
     * Example: //table - select the table element anywhere
     *
     * @param node
     * @param expression XPath expression
     * @return Node, if found - otherwise {@code null}
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
     * Returns NodeList by XPath expression
     *
     * @param node
     * @param expression XPath expression
     * @return NodeList, if found - otherwise {@code null}
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
            // Returns NodeList by XPath expression
            final NodeList nodeList = selectNodeList(node, expression);
            return elements(nodeList);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return null;
        }
    }

    /**
     * Makes an Iterable of Element from NodeList, for convenient iteration
     * @param nodeList the source NodeList
     * @return Iterable&lt;Element&gt;
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
     * Selection of a string value by xpath. Default value - {@code null}.
     * @see #selectText( Node, String, String )
     */
    public static String selectText(Node node, String expression) {
        return selectText(node, expression, null);
    }

    /**
     * Selection of a string value by xpath. That is, practically the same as
     * selectNode, only it returns the node value instead, or {@code null} if
     * nothing is found or some error occurs (should not return text etc.)
     * Can return both text node values and attribute values, universal.<br/>
     * <br/>
     * <b>In general, not intended for selections like selectText(node, "@selected", null),
     * because it's not a problem that getAttribute returns not {@code null}
     * but an empty string.</b> Moreover, there is a method that both
     * returns {@code null} and works faster.<br/>
     * <br/>
     * Query examples:<br>
     * 1) query for an attribute's text value<pre>
     * &lt;data&gt;&lt;payment cardnumber="111"&gt;...
     * /data/payment/@cardnumber
     * </pre>
     * 2) query for a node's text value<pre>
     * ...&lt;operation&gt;&lt;pursesrc&gt;text&lt;/pursesrc&gt;...
     * /operation/pursesrc/text()
     * </pre>
     * 3) not from the root, but relative - not starting with a slash<br>
     * 4) search anywhere - double slash<br>
     * 5) etc., you get the idea
     * @param node
     *            the root node
     * @param expression
     *            xpath
     * @param defaultValue the default value
     * @return the string
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

    // serialization methods

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
