
package ru.bgcrm.plugin.bgbilling.ws.tariff.option;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import java.util.Date;


/**
 * <p>Java class for contractTariffOptionHistory complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="contractTariffOptionHistory">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="contractId" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="date" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="web" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "contractTariffOptionHistory", propOrder = {
    "contractId",
    "date",
    "web"
})
public class ContractTariffOptionHistory {

    protected int contractId;
    @XmlSchemaType(name = "dateTime")
    protected Date date;
    protected boolean web;

    /**
     * Gets the value of the contractId property.
     * 
     */
    public int getContractId() {
        return contractId;
    }

    /**
     * Sets the value of the contractId property.
     * 
     */
    public void setContractId(int value) {
        this.contractId = value;
    }

    /**
     * Gets the value of the date property.
     * 
     * @return
     *     possible object is
     *     {@link Date }
     *     
     */
    public Date getDate() {
        return date;
    }

    /**
     * Sets the value of the date property.
     * 
     * @param value
     *     allowed object is
     *     {@link Date }
     *     
     */
    public void setDate(Date value) {
        this.date = value;
    }

    /**
     * Gets the value of the web property.
     * 
     */
    public boolean isWeb() {
        return web;
    }

    /**
     * Sets the value of the web property.
     * 
     */
    public void setWeb(boolean value) {
        this.web = value;
    }

}
