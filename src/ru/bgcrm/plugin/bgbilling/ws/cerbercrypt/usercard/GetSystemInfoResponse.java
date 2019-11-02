
package ru.bgcrm.plugin.bgbilling.ws.cerbercrypt.usercard;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for getSystemInfoResponse complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="getSystemInfoResponse">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="return" type="{http://common.bitel.ru}dataMap" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "getSystemInfoResponse", propOrder = {
    "_return"
})
public class GetSystemInfoResponse {

    @XmlElement(name = "return")
    protected DataMap _return;

    /**
     * Gets the value of the return property.
     * 
     * @return
     *     possible object is
     *     {@link DataMap }
     *     
     */
    public DataMap getReturn() {
        return _return;
    }

    /**
     * Sets the value of the return property.
     * 
     * @param value
     *     allowed object is
     *     {@link DataMap }
     *     
     */
    public void setReturn(DataMap value) {
        this._return = value;
    }

}
