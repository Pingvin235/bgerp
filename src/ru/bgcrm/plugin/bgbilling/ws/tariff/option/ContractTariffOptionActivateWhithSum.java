
package ru.bgcrm.plugin.bgbilling.ws.tariff.option;

import java.math.BigDecimal;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for contractTariffOptionActivateWhithSum complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="contractTariffOptionActivateWhithSum">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="contractId" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="optionId" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="modeId" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="chargeSum" type="{http://www.w3.org/2001/XMLSchema}decimal" minOccurs="0"/>
 *         &lt;element name="web" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "contractTariffOptionActivateWhithSum", propOrder = {
    "contractId",
    "optionId",
    "modeId",
    "chargeSum",
    "web"
})
public class ContractTariffOptionActivateWhithSum {

    protected int contractId;
    protected int optionId;
    protected int modeId;
    protected BigDecimal chargeSum;
    protected boolean web;

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
     * Gets the value of the optionId property.
     * 
     */
    public int getOptionId() {
        return optionId;
    }

    /**
     * Sets the value of the optionId property.
     * 
     */
    public void setOptionId(int value) {
        this.optionId = value;
    }

    /**
     * Gets the value of the modeId property.
     * 
     */
    public int getModeId() {
        return modeId;
    }

    /**
     * Sets the value of the modeId property.
     * 
     */
    public void setModeId(int value) {
        this.modeId = value;
    }

    /**
     * Gets the value of the chargeSum property.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getChargeSum() {
        return chargeSum;
    }

    /**
     * Sets the value of the chargeSum property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setChargeSum(BigDecimal value) {
        this.chargeSum = value;
    }

    /**
     * Gets the value of the web property.
     * 
     */
    public boolean isWeb() {
        return web;
    }

    /**
     * Sets the value of the web property.
     * 
     */
    public void setWeb(boolean value) {
        this.web = value;
    }

}
