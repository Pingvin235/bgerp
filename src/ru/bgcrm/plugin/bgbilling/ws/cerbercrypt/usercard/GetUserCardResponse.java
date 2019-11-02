
package ru.bgcrm.plugin.bgbilling.ws.cerbercrypt.usercard;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for getUserCardResponse complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="getUserCardResponse">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="return" type="{http://common.cerbercrypt.modules.bgbilling.bitel.ru/}userCard" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "getUserCardResponse", propOrder = {
    "_return"
})
public class GetUserCardResponse {

    @XmlElement(name = "return")
    protected UserCard _return;

    /**
     * Gets the value of the return property.
     * 
     * @return
     *     possible object is
     *     {@link UserCard }
     *     
     */
    public UserCard getReturn() {
        return _return;
    }

    /**
     * Sets the value of the return property.
     * 
     * @param value
     *     allowed object is
     *     {@link UserCard }
     *     
     */
    public void setReturn(UserCard value) {
        this._return = value;
    }

}
