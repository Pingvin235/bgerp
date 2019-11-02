
package ru.bgcrm.plugin.bgbilling.ws.contract.balance.paymcharge;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for abstractPaymentTypes complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="abstractPaymentTypes">
 *   &lt;complexContent>
 *     &lt;extension base="{http://common.balance.contract.kernel.bgbilling.bitel.ru/}paymentType">
 *       &lt;sequence>
 *         &lt;element name="children" type="{http://common.balance.contract.kernel.bgbilling.bitel.ru/}abstractPaymentTypes" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "abstractPaymentTypes", propOrder = {
    "children"
})
@XmlSeeAlso({
    PaymentTypeItem.class
})
public class AbstractPaymentTypes
    extends PaymentType
{

    @XmlElement(nillable = true)
    protected List<AbstractPaymentTypes> children;

    /**
     * Gets the value of the children property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the children property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getChildren().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link AbstractPaymentTypes }
     * 
     * 
     */
    public List<AbstractPaymentTypes> getChildren() {
        if (children == null) {
            children = new ArrayList<AbstractPaymentTypes>();
        }
        return this.children;
    }

}
