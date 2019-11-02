
package ru.bgcrm.plugin.bgbilling.ws.cerbercrypt.usercard;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for dataMap complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="dataMap">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="data" type="{http://common.cerbercrypt.modules.bgbilling.bitel.ru/}list" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "dataMap", namespace = "http://common.bitel.ru", propOrder = {
    "data"
})
public class DataMap {

    protected List data;

    /**
     * Gets the value of the data property.
     * 
     * @return
     *     possible object is
     *     {@link List }
     *     
     */
    public List getData() {
        return data;
    }

    /**
     * Sets the value of the data property.
     * 
     * @param value
     *     allowed object is
     *     {@link List }
     *     
     */
    public void setData(List value) {
        this.data = value;
    }

}
