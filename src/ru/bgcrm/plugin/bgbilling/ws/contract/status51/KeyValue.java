
package ru.bgcrm.plugin.bgbilling.ws.contract.status51;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for keyValue complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="keyValue">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="key" type="{http://www.w3.org/2001/XMLSchema}anyType" minOccurs="0"/>
 *         &lt;element name="value" type="{http://www.w3.org/2001/XMLSchema}anyType" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "keyValue", namespace = "http://common.bitel.ru", propOrder = {
    "key",
    "value"
})
public class KeyValue {

    protected Object key;
    protected Object value;

    /**
     * Gets the value of the key property.
     * 
     * @return
     *     possible object is
     *     {@link Object }
     *     
     */
    public Object getKey() {
        return key;
    }

    /**
     * Sets the value of the key property.
     * 
     * @param value
     *     allowed object is
     *     {@link Object }
     *     
     */
    public void setKey(Object value) {
        this.key = value;
    }

    /**
     * Gets the value of the value property.
     * 
     * @return
     *     possible object is
     *     {@link Object }
     *     
     */
    public Object getValue() {
        return value;
    }

    /**
     * Sets the value of the value property.
     * 
     * @param value
     *     allowed object is
     *     {@link Object }
     *     
     */
    public void setValue(Object value) {
        this.value = value;
    }

}
