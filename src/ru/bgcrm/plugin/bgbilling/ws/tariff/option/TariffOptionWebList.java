
package ru.bgcrm.plugin.bgbilling.ws.tariff.option;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import java.util.Date;


/**
 * <p>Java class for tariffOptionWebList complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="tariffOptionWebList">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="contractId" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="date" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="fullOptionList" type="{http://service.common.option.tariff.kernel.bgbilling.bitel.ru/}tariffOption" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "tariffOptionWebList", propOrder = {
    "contractId",
    "date",
    "fullOptionList"
})
public class TariffOptionWebList {

    protected int contractId;
    @XmlSchemaType(name = "dateTime")
    protected Date date;
    protected List<TariffOption> fullOptionList;

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
     * Gets the value of the fullOptionList property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the fullOptionList property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getFullOptionList().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link TariffOption }
     * 
     * 
     */
    public List<TariffOption> getFullOptionList() {
        if (fullOptionList == null) {
            fullOptionList = new ArrayList<TariffOption>();
        }
        return this.fullOptionList;
    }

}
