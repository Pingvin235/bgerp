
package ru.bgcrm.plugin.bgbilling.ws.contract.status;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import java.util.Date;


/**
 * <p>Java class for period complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="period">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="fromDate" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="toDate" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="dateFrom" type="{http://www.w3.org/2001/XMLSchema}dateTime" />
 *       &lt;attribute name="dateTo" type="{http://www.w3.org/2001/XMLSchema}dateTime" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "period", namespace = "http://common.bitel.ru", propOrder = {
    "fromDate",
    "toDate"
})
public class Period {

    @XmlSchemaType(name = "dateTime")
    protected Date fromDate;
    @XmlSchemaType(name = "dateTime")
    protected Date toDate;
    @XmlAttribute(name = "dateFrom")
    @XmlSchemaType(name = "dateTime")
    protected Date dateFrom;
    @XmlAttribute(name = "dateTo")
    @XmlSchemaType(name = "dateTime")
    protected Date dateTo;

    /**
     * Gets the value of the fromDate property.
     * 
     * @return
     *     possible object is
     *     {@link Date }
     *     
     */
    public Date getFromDate() {
        return fromDate;
    }

    /**
     * Sets the value of the fromDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link Date }
     *     
     */
    public void setFromDate(Date value) {
        this.fromDate = value;
    }

    /**
     * Gets the value of the toDate property.
     * 
     * @return
     *     possible object is
     *     {@link Date }
     *     
     */
    public Date getToDate() {
        return toDate;
    }

    /**
     * Sets the value of the toDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link Date }
     *     
     */
    public void setToDate(Date value) {
        this.toDate = value;
    }

    /**
     * Gets the value of the dateFrom property.
     * 
     * @return
     *     possible object is
     *     {@link Date }
     *     
     */
    public Date getDateFrom() {
        return dateFrom;
    }

    /**
     * Sets the value of the dateFrom property.
     * 
     * @param value
     *     allowed object is
     *     {@link Date }
     *     
     */
    public void setDateFrom(Date value) {
        this.dateFrom = value;
    }

    /**
     * Gets the value of the dateTo property.
     * 
     * @return
     *     possible object is
     *     {@link Date }
     *     
     */
    public Date getDateTo() {
        return dateTo;
    }

    /**
     * Sets the value of the dateTo property.
     * 
     * @param value
     *     allowed object is
     *     {@link Date }
     *     
     */
    public void setDateTo(Date value) {
        this.dateTo = value;
    }

}
