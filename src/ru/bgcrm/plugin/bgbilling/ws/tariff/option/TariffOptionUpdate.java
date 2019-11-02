
package ru.bgcrm.plugin.bgbilling.ws.tariff.option;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for tariffOptionUpdate complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="tariffOptionUpdate">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="tariffOption" type="{http://service.common.option.tariff.kernel.bgbilling.bitel.ru/}tariffOption" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "tariffOptionUpdate", propOrder = {
    "tariffOption"
})
public class TariffOptionUpdate {

    protected TariffOption tariffOption;

    /**
     * Gets the value of the tariffOption property.
     * 
     * @return
     *     possible object is
     *     {@link TariffOption }
     *     
     */
    public TariffOption getTariffOption() {
        return tariffOption;
    }

    /**
     * Sets the value of the tariffOption property.
     * 
     * @param value
     *     allowed object is
     *     {@link TariffOption }
     *     
     */
    public void setTariffOption(TariffOption value) {
        this.tariffOption = value;
    }

}
