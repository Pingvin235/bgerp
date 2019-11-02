
package ru.bgcrm.plugin.bgbilling.ws.cerbercrypt.usercard.copy;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import java.util.Date;


/**
 * <p>Java class for userCardCopy complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="userCardCopy">
 *   &lt;complexContent>
 *     &lt;extension base="{http://common.bitel.ru}id">
 *       &lt;sequence>
 *         &lt;element name="comment" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="date1" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="date2" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="parentCard" type="{http://common.cerbercrypt.modules.bgbilling.bitel.ru/}userCard" minOccurs="0"/>
 *         &lt;element name="parentCardId" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "userCardCopy", propOrder = {
    "comment",
    "date1",
    "date2",
    "parentCard",
    "parentCardId"
})
public class UserCardCopy
    extends Id
{

    protected String comment;
    @XmlSchemaType(name = "dateTime")
    protected Date date1;
    @XmlSchemaType(name = "dateTime")
    protected Date date2;
    protected UserCard parentCard;
    protected int parentCardId;

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
     * Gets the value of the date1 property.
     * 
     * @return
     *     possible object is
     *     {@link Date }
     *     
     */
    public Date getDate1() {
        return date1;
    }

    /**
     * Sets the value of the date1 property.
     * 
     * @param value
     *     allowed object is
     *     {@link Date }
     *     
     */
    public void setDate1(Date value) {
        this.date1 = value;
    }

    /**
     * Gets the value of the date2 property.
     * 
     * @return
     *     possible object is
     *     {@link Date }
     *     
     */
    public Date getDate2() {
        return date2;
    }

    /**
     * Sets the value of the date2 property.
     * 
     * @param value
     *     allowed object is
     *     {@link Date }
     *     
     */
    public void setDate2(Date value) {
        this.date2 = value;
    }

    /**
     * Gets the value of the parentCard property.
     * 
     * @return
     *     possible object is
     *     {@link UserCard }
     *     
     */
    public UserCard getParentCard() {
        return parentCard;
    }

    /**
     * Sets the value of the parentCard property.
     * 
     * @param value
     *     allowed object is
     *     {@link UserCard }
     *     
     */
    public void setParentCard(UserCard value) {
        this.parentCard = value;
    }

    /**
     * Gets the value of the parentCardId property.
     * 
     */
    public int getParentCardId() {
        return parentCardId;
    }

    /**
     * Sets the value of the parentCardId property.
     * 
     */
    public void setParentCardId(int value) {
        this.parentCardId = value;
    }

}
