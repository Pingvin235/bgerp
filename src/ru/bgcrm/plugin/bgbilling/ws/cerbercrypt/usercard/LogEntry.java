
package ru.bgcrm.plugin.bgbilling.ws.cerbercrypt.usercard;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import java.util.Date;


/**
 * <p>Java class for logEntry complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="logEntry">
 *   &lt;complexContent>
 *     &lt;extension base="{http://common.bitel.ru}id">
 *       &lt;sequence>
 *         &lt;element name="action" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="contractId" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="date" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="description" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="objectId" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="userId" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="userName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "logEntry", namespace = "http://common.bitel.ru", propOrder = {
    "action",
    "contractId",
    "date",
    "description",
    "objectId",
    "userId",
    "userName"
})
public class LogEntry
    extends Id
{

    protected int action;
    protected int contractId;
    @XmlSchemaType(name = "dateTime")
    protected Date date;
    protected String description;
    protected int objectId;
    protected int userId;
    protected String userName;

    /**
     * Gets the value of the action property.
     * 
     */
    public int getAction() {
        return action;
    }

    /**
     * Sets the value of the action property.
     * 
     */
    public void setAction(int value) {
        this.action = value;
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
     * Gets the value of the description property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the value of the description property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDescription(String value) {
        this.description = value;
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
     * Gets the value of the userId property.
     * 
     */
    public int getUserId() {
        return userId;
    }

    /**
     * Sets the value of the userId property.
     * 
     */
    public void setUserId(int value) {
        this.userId = value;
    }

    /**
     * Gets the value of the userName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUserName() {
        return userName;
    }

    /**
     * Sets the value of the userName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUserName(String value) {
        this.userName = value;
    }

}
