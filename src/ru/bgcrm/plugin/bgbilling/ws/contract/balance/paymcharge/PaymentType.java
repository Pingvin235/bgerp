
package ru.bgcrm.plugin.bgbilling.ws.contract.balance.paymcharge;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for paymentType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="paymentType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://common.bitel.ru}idTitle">
 *       &lt;sequence>
 *       &lt;/sequence>
 *       &lt;attribute name="editable" use="required" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *       &lt;attribute name="parentId" type="{http://www.w3.org/2001/XMLSchema}int" />
 *       &lt;attribute name="type" use="required" type="{http://www.w3.org/2001/XMLSchema}int" />
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "paymentType")
@XmlSeeAlso({
    AbstractPaymentTypes.class
})
public class PaymentType
    extends IdTitle
{

    @XmlAttribute(name = "editable", required = true)
    protected boolean editable;
    @XmlAttribute(name = "parentId")
    protected Integer parentId;
    @XmlAttribute(name = "type", required = true)
    protected int type;

    /**
     * Gets the value of the editable property.
     * 
     */
    public boolean isEditable() {
        return editable;
    }

    /**
     * Sets the value of the editable property.
     * 
     */
    public void setEditable(boolean value) {
        this.editable = value;
    }

    /**
     * Gets the value of the parentId property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getParentId() {
        return parentId;
    }

    /**
     * Sets the value of the parentId property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setParentId(Integer value) {
        this.parentId = value;
    }

    /**
     * Gets the value of the type property.
     * 
     */
    public int getType() {
        return type;
    }

    /**
     * Sets the value of the type property.
     * 
     */
    public void setType(int value) {
        this.type = value;
    }

}
