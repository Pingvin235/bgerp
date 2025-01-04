package org.bgerp.util.xml;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class NodeListElementIterator implements java.util.Iterator<Element> {
    protected final NodeList list;
    protected final int length;
    protected Element next;
    protected int index;

    public NodeListElementIterator(NodeList list) {
        super();

        this.list = list;
        this.length = list != null ? list.getLength() : 0;
        this.index = 0;
        this.next = null;
    }

    public boolean hasNext() {
        Node next = null;
        while ((index < length || (next = null) != null) && (next = list.item(index++)) != null && next.getNodeType() != Node.ELEMENT_NODE) {
        }

        if (next != null) {
            this.next = (Element) next;
            return true;
        } else {
            this.next = null;
            return false;
        }
    }

    public org.w3c.dom.Element next() {
        return next;
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }
}