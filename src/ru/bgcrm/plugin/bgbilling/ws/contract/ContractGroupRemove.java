
package ru.bgcrm.plugin.bgbilling.ws.contract;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for contractGroupRemove complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="contractGroupRemove">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="contractId" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="contractGroupId" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "contractGroupRemove", propOrder = {
    "contractId",
    "contractGroupId"
})
public class ContractGroupRemove {

    protected int contractId;
    protected int contractGroupId;

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
     * Gets the value of the contractGroupId property.
     * 
     */
    public int getContractGroupId() {
        return contractGroupId;
    }

    /**
     * Sets the value of the contractGroupId property.
     * 
     */
    public void setContractGroupId(int value) {
        this.contractGroupId = value;
    }

}
