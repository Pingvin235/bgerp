
package ru.bgcrm.plugin.bgbilling.ws.tariff.option;

import java.math.BigDecimal;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for contractTariffOption complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="contractTariffOption">
 *   &lt;complexContent>
 *     &lt;extension base="{http://common.bitel.ru}id">
 *       &lt;sequence>
 *       &lt;/sequence>
 *       &lt;attribute name="activatedMode" use="required" type="{http://www.w3.org/2001/XMLSchema}int" />
 *       &lt;attribute name="activatedTime" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="chargeId" use="required" type="{http://www.w3.org/2001/XMLSchema}int" />
 *       &lt;attribute name="contractId" use="required" type="{http://www.w3.org/2001/XMLSchema}int" />
 *       &lt;attribute name="deactivatedTime" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="optionId" use="required" type="{http://www.w3.org/2001/XMLSchema}int" />
 *       &lt;attribute name="optionTitle" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="summa" type="{http://www.w3.org/2001/XMLSchema}decimal" />
 *       &lt;attribute name="timeFrom" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="timeTo" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="userId" use="required" type="{http://www.w3.org/2001/XMLSchema}int" />
 *       &lt;attribute name="userTitle" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "contractTariffOption")
public class ContractTariffOption
    extends Id
{

    @XmlAttribute(name = "activatedMode", required = true)
    protected int activatedMode;
    @XmlAttribute(name = "activatedTime")
    protected String activatedTime;
    @XmlAttribute(name = "chargeId", required = true)
    protected int chargeId;
    @XmlAttribute(name = "contractId", required = true)
    protected int contractId;
    @XmlAttribute(name = "deactivatedTime")
    protected String deactivatedTime;
    @XmlAttribute(name = "optionId", required = true)
    protected int optionId;
    @XmlAttribute(name = "optionTitle")
    protected String optionTitle;
    @XmlAttribute(name = "summa")
    protected BigDecimal summa;
    @XmlAttribute(name = "timeFrom")
    protected String timeFrom;
    @XmlAttribute(name = "timeTo")
    protected String timeTo;
    @XmlAttribute(name = "userId", required = true)
    protected int userId;
    @XmlAttribute(name = "userTitle")
    protected String userTitle;

    /**
     * Gets the value of the activatedMode property.
     * 
     */
    public int getActivatedMode() {
        return activatedMode;
    }

    /**
     * Sets the value of the activatedMode property.
     * 
     */
    public void setActivatedMode(int value) {
        this.activatedMode = value;
    }

    /**
     * Gets the value of the activatedTime property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getActivatedTime() {
        return activatedTime;
    }

    /**
     * Sets the value of the activatedTime property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setActivatedTime(String value) {
        this.activatedTime = value;
    }

    /**
     * Gets the value of the chargeId property.
     * 
     */
    public int getChargeId() {
        return chargeId;
    }

    /**
     * Sets the value of the chargeId property.
     * 
     */
    public void setChargeId(int value) {
        this.chargeId = value;
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
     * Gets the value of the deactivatedTime property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDeactivatedTime() {
        return deactivatedTime;
    }

    /**
     * Sets the value of the deactivatedTime property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDeactivatedTime(String value) {
        this.deactivatedTime = value;
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
     * Gets the value of the optionTitle property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOptionTitle() {
        return optionTitle;
    }

    /**
     * Sets the value of the optionTitle property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOptionTitle(String value) {
        this.optionTitle = value;
    }

    /**
     * Gets the value of the summa property.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getSumma() {
        return summa;
    }

    /**
     * Sets the value of the summa property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setSumma(BigDecimal value) {
        this.summa = value;
    }

    /**
     * Gets the value of the timeFrom property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTimeFrom() {
        return timeFrom;
    }

    /**
     * Sets the value of the timeFrom property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTimeFrom(String value) {
        this.timeFrom = value;
    }

    /**
     * Gets the value of the timeTo property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTimeTo() {
        return timeTo;
    }

    /**
     * Sets the value of the timeTo property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTimeTo(String value) {
        this.timeTo = value;
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
     * Gets the value of the userTitle property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUserTitle() {
        return userTitle;
    }

    /**
     * Sets the value of the userTitle property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUserTitle(String value) {
        this.userTitle = value;
    }

}
