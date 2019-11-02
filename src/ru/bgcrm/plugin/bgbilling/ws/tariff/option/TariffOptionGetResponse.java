
package ru.bgcrm.plugin.bgbilling.ws.tariff.option;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for tariffOptionGetResponse complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="tariffOptionGetResponse">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="return" type="{http://service.common.option.tariff.kernel.bgbilling.bitel.ru/}tariffOption" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "tariffOptionGetResponse", propOrder = {
    "_return"
})
public class TariffOptionGetResponse {

    @XmlElement(name = "return")
    protected TariffOption _return;

    /**
     * Gets the value of the return property.
     * 
     * @return
     *     possible object is
     *     {@link TariffOption }
     *     
     */
    public TariffOption getReturn() {
        return _return;
    }

    /**
     * Sets the value of the return property.
     * 
     * @param value
     *     allowed object is
     *     {@link TariffOption }
     *     
     */
    public void setReturn(TariffOption value) {
        this._return = value;
    }

}
