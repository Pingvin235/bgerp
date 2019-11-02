
package ru.bgcrm.plugin.bgbilling.ws.tariff.option;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for contractTariffOptionActivate complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="contractTariffOptionActivate">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="contractId" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="optionId" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="modeId" type="{http://www.w3.org/2001/XMLSchema}int"/>
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
@XmlType(name = "contractTariffOptionActivate", propOrder = {
    "contractId",
    "optionId",
    "modeId",
    "web"
})
public class ContractTariffOptionActivate {

    protected int contractId;
    protected int optionId;
    protected int modeId;
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
