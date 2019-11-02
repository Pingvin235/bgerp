
package ru.bgcrm.plugin.bgbilling.ws.helpdesk;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for reservStatusUpdate complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="reservStatusUpdate">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="topicId" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="reserveStatus" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "reservStatusUpdate", propOrder = {
    "topicId",
    "reserveStatus"
})
public class ReservStatusUpdate {

    protected int topicId;
    protected int reserveStatus;

    /**
     * Gets the value of the topicId property.
     * 
     */
    public int getTopicId() {
        return topicId;
    }

    /**
     * Sets the value of the topicId property.
     * 
     */
    public void setTopicId(int value) {
        this.topicId = value;
    }

    /**
     * Gets the value of the reserveStatus property.
     * 
     */
    public int getReserveStatus() {
        return reserveStatus;
    }

    /**
     * Sets the value of the reserveStatus property.
     * 
     */
    public void setReserveStatus(int value) {
        this.reserveStatus = value;
    }

}
