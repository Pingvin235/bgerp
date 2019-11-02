
package ru.bgcrm.plugin.bgbilling.ws.cerbercrypt.usercard.copy;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import java.util.Date;


/**
 * <p>Java class for userCard complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="userCard">
 *   &lt;complexContent>
 *     &lt;extension base="{http://common.bitel.ru}id">
 *       &lt;sequence>
 *         &lt;element name="basecardId" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="basecardTitle" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="comment" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="contractId" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="date1" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="date2" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="needSync" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="number" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="objectId" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="slavecardsNumber" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="subscrDate" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="userdeviceId" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "userCard", propOrder = {
    "basecardId",
    "basecardTitle",
    "comment",
    "contractId",
    "date1",
    "date2",
    "needSync",
    "number",
    "objectId",
    "slavecardsNumber",
    "subscrDate",
    "userdeviceId"
})
public class UserCard
    extends Id
{

    protected int basecardId;
    protected String basecardTitle;
    protected String comment;
    protected int contractId;
    protected String date1;
    protected String date2;
    protected boolean needSync;
    protected long number;
    protected int objectId;
    protected int slavecardsNumber;
    @XmlSchemaType(name = "dateTime")
    protected Date subscrDate;
    protected int userdeviceId;

    /**
     * Gets the value of the basecardId property.
     * 
     */
    public int getBasecardId() {
        return basecardId;
    }

    /**
     * Sets the value of the basecardId property.
     * 
     */
    public void setBasecardId(int value) {
        this.basecardId = value;
    }

    /**
     * Gets the value of the basecardTitle property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBasecardTitle() {
        return basecardTitle;
    }

    /**
     * Sets the value of the basecardTitle property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBasecardTitle(String value) {
        this.basecardTitle = value;
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
     * Gets the value of the date1 property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDate1() {
        return date1;
    }

    /**
     * Sets the value of the date1 property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDate1(String value) {
        this.date1 = value;
    }

    /**
     * Gets the value of the date2 property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDate2() {
        return date2;
    }

    /**
     * Sets the value of the date2 property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDate2(String value) {
        this.date2 = value;
    }

    /**
     * Gets the value of the needSync property.
     * 
     */
    public boolean isNeedSync() {
        return needSync;
    }

    /**
     * Sets the value of the needSync property.
     * 
     */
    public void setNeedSync(boolean value) {
        this.needSync = value;
    }

    /**
     * Gets the value of the number property.
     * 
     */
    public long getNumber() {
        return number;
    }

    /**
     * Sets the value of the number property.
     * 
     */
    public void setNumber(long value) {
        this.number = value;
    }

    /**
     * Gets the value of the objectId property.
     * 
     */
    public int getObjectId() {
        return objectId;
    }

    /**
     * Sets the value of the objectId property.
     * 
     */
    public void setObjectId(int value) {
        this.objectId = value;
    }

    /**
     * Gets the value of the slavecardsNumber property.
     * 
     */
    public int getSlavecardsNumber() {
        return slavecardsNumber;
    }

    /**
     * Sets the value of the slavecardsNumber property.
     * 
     */
    public void setSlavecardsNumber(int value) {
        this.slavecardsNumber = value;
    }

    /**
     * Gets the value of the subscrDate property.
     * 
     * @return
     *     possible object is
     *     {@link Date }
     *     
     */
    public Date getSubscrDate() {
        return subscrDate;
    }

    /**
     * Sets the value of the subscrDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link Date }
     *     
     */
    public void setSubscrDate(Date value) {
        this.subscrDate = value;
    }

    /**
     * Gets the value of the userdeviceId property.
     * 
     */
    public int getUserdeviceId() {
        return userdeviceId;
    }

    /**
     * Sets the value of the userdeviceId property.
     * 
     */
    public void setUserdeviceId(int value) {
        this.userdeviceId = value;
    }

}
