
package ru.bgcrm.plugin.bgbilling.ws.tariff.option;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for contractTariffOptionDeactivate complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="contractTariffOptionDeactivate">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="contractId" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="contractOptionId" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "contractTariffOptionDeactivate", propOrder = {
    "contractId",
    "contractOptionId"
})
public class ContractTariffOptionDeactivate {

    protected int contractId;
    protected int contractOptionId;

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
     * Gets the value of the contractOptionId property.
     * 
     */
    public int getContractOptionId() {
        return contractOptionId;
    }

    /**
     * Sets the value of the contractOptionId property.
     * 
     */
    public void setContractOptionId(int value) {
        this.contractOptionId = value;
    }

}
