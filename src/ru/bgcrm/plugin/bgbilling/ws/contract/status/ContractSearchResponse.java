
package ru.bgcrm.plugin.bgbilling.ws.contract.status;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for contractSearchResponse complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="contractSearchResponse">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="return" type="{http://common.bitel.ru}searchResult" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "contractSearchResponse", propOrder = {
    "_return"
})
public class ContractSearchResponse {

    @XmlElement(name = "return")
    protected SearchResult _return;

    /**
     * Gets the value of the return property.
     * 
     * @return
     *     possible object is
     *     {@link SearchResult }
     *     
     */
    public SearchResult getReturn() {
        return _return;
    }

    /**
     * Sets the value of the return property.
     * 
     * @param value
     *     allowed object is
     *     {@link SearchResult }
     *     
     */
    public void setReturn(SearchResult value) {
        this._return = value;
    }

}
