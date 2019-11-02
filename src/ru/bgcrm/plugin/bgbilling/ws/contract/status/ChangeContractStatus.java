
package ru.bgcrm.plugin.bgbilling.ws.contract.status;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import java.util.Date;


/**
 * <p>Java class for changeContractStatus complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="changeContractStatus">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="cid" type="{http://www.w3.org/2001/XMLSchema}int" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="statusId" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="dateFrom" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="dateTo" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="comment" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "changeContractStatus", propOrder = {
    "cid",
    "statusId",
    "dateFrom",
    "dateTo",
    "comment"
})
public class ChangeContractStatus {

    @XmlElement(nillable = true)
    protected List<Integer> cid;
    protected int statusId;
    @XmlSchemaType(name = "dateTime")
    protected Date dateFrom;
    @XmlSchemaType(name = "dateTime")
    protected Date dateTo;
    protected String comment;

    /**
     * Gets the value of the cid property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the cid property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getCid().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Integer }
     * 
     * 
     */
    public List<Integer> getCid() {
        if (cid == null) {
            cid = new ArrayList<Integer>();
        }
        return this.cid;
    }

    /**
     * Gets the value of the statusId property.
     * 
     */
    public int getStatusId() {
        return statusId;
    }

    /**
     * Sets the value of the statusId property.
     * 
     */
    public void setStatusId(int value) {
        this.statusId = value;
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

    /**
     * Gets the value of the comment property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getComment() {
        return comment;
    }

    /**
     * Sets the value of the comment property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setComment(String value) {
        this.comment = value;
    }

}
