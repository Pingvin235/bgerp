
package ru.bgcrm.plugin.bgbilling.ws.helpdesk;

import java.math.BigDecimal;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for topicCostUpdate complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="topicCostUpdate">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="topicId" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="sum" type="{http://www.w3.org/2001/XMLSchema}decimal" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "topicCostUpdate", propOrder = {
    "topicId",
    "sum"
})
public class TopicCostUpdate {

    protected int topicId;
    protected BigDecimal sum;

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
     * Gets the value of the sum property.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getSum() {
        return sum;
    }

    /**
     * Sets the value of the sum property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setSum(BigDecimal value) {
        this.sum = value;
    }

}
