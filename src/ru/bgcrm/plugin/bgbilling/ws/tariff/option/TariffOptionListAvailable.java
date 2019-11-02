
package ru.bgcrm.plugin.bgbilling.ws.tariff.option;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import java.util.Date;


/**
 * <p>Java class for tariffOptionListAvailable complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="tariffOptionListAvailable">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="contractId" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="date" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="currentContractOptionList" type="{http://service.common.option.tariff.kernel.bgbilling.bitel.ru/}contractTariffOption" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="onlyAvailable" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
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
@XmlType(name = "tariffOptionListAvailable", propOrder = {
    "contractId",
    "date",
    "currentContractOptionList",
    "onlyAvailable",
    "web"
})
public class TariffOptionListAvailable {

    protected int contractId;
    @XmlSchemaType(name = "dateTime")
    protected Date date;
    protected List<ContractTariffOption> currentContractOptionList;
    protected boolean onlyAvailable;
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
     * Gets the value of the currentContractOptionList property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the currentContractOptionList property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getCurrentContractOptionList().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ContractTariffOption }
     * 
     * 
     */
    public List<ContractTariffOption> getCurrentContractOptionList() {
        if (currentContractOptionList == null) {
            currentContractOptionList = new ArrayList<ContractTariffOption>();
        }
        return this.currentContractOptionList;
    }

    /**
     * Gets the value of the onlyAvailable property.
     * 
     */
    public boolean isOnlyAvailable() {
        return onlyAvailable;
    }

    /**
     * Sets the value of the onlyAvailable property.
     * 
     */
    public void setOnlyAvailable(boolean value) {
        this.onlyAvailable = value;
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
