
package ru.bgcrm.plugin.bgbilling.ws.tariff.option;

import java.math.BigDecimal;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import java.util.Date;


/**
 * <p>Java class for tariffOptionActivateMode complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="tariffOptionActivateMode">
 *   &lt;complexContent>
 *     &lt;extension base="{http://common.bitel.ru}idTitle">
 *       &lt;sequence>
 *       &lt;/sequence>
 *       &lt;attribute name="chargeProportional" use="required" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *       &lt;attribute name="chargeSumma" type="{http://www.w3.org/2001/XMLSchema}decimal" />
 *       &lt;attribute name="chargeTypeId" use="required" type="{http://www.w3.org/2001/XMLSchema}int" />
 *       &lt;attribute name="chargeTypeTitle" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="dateFrom" type="{http://www.w3.org/2001/XMLSchema}dateTime" />
 *       &lt;attribute name="dateTo" type="{http://www.w3.org/2001/XMLSchema}dateTime" />
 *       &lt;attribute name="deactivationMode" use="required" type="{http://www.w3.org/2001/XMLSchema}int" />
 *       &lt;attribute name="modeTitle" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="optionId" use="required" type="{http://www.w3.org/2001/XMLSchema}int" />
 *       &lt;attribute name="periodCol" use="required" type="{http://www.w3.org/2001/XMLSchema}int" />
 *       &lt;attribute name="periodMode" use="required" type="{http://www.w3.org/2001/XMLSchema}int" />
 *       &lt;attribute name="reactivationMode" use="required" type="{http://www.w3.org/2001/XMLSchema}int" />
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "tariffOptionActivateMode")
public class TariffOptionActivateMode
    extends IdTitle
{

    @XmlAttribute(name = "chargeProportional", required = true)
    protected boolean chargeProportional;
    @XmlAttribute(name = "chargeSumma")
    protected BigDecimal chargeSumma;
    @XmlAttribute(name = "chargeTypeId", required = true)
    protected int chargeTypeId;
    @XmlAttribute(name = "chargeTypeTitle")
    protected String chargeTypeTitle;
    @XmlAttribute(name = "dateFrom")
    @XmlSchemaType(name = "dateTime")
    protected Date dateFrom;
    @XmlAttribute(name = "dateTo")
    @XmlSchemaType(name = "dateTime")
    protected Date dateTo;
    @XmlAttribute(name = "deactivationMode", required = true)
    protected int deactivationMode;
    @XmlAttribute(name = "modeTitle")
    protected String modeTitle;
    @XmlAttribute(name = "optionId", required = true)
    protected int optionId;
    @XmlAttribute(name = "periodCol", required = true)
    protected int periodCol;
    @XmlAttribute(name = "periodMode", required = true)
    protected int periodMode;
    @XmlAttribute(name = "reactivationMode", required = true)
    protected int reactivationMode;

    /**
     * Gets the value of the chargeProportional property.
     * 
     */
    public boolean isChargeProportional() {
        return chargeProportional;
    }

    /**
     * Sets the value of the chargeProportional property.
     * 
     */
    public void setChargeProportional(boolean value) {
        this.chargeProportional = value;
    }

    /**
     * Gets the value of the chargeSumma property.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getChargeSumma() {
        return chargeSumma;
    }

    /**
     * Sets the value of the chargeSumma property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setChargeSumma(BigDecimal value) {
        this.chargeSumma = value;
    }

    /**
     * Gets the value of the chargeTypeId property.
     * 
     */
    public int getChargeTypeId() {
        return chargeTypeId;
    }

    /**
     * Sets the value of the chargeTypeId property.
     * 
     */
    public void setChargeTypeId(int value) {
        this.chargeTypeId = value;
    }

    /**
     * Gets the value of the chargeTypeTitle property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getChargeTypeTitle() {
        return chargeTypeTitle;
    }

    /**
     * Sets the value of the chargeTypeTitle property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setChargeTypeTitle(String value) {
        this.chargeTypeTitle = value;
    }

    /**
     * Gets the value of the dateFrom property.
     * 
     * @return
     *     possible object is
     *     {@link Date }
     *     
     */
    public Date getDateFrom() {
        return dateFrom;
    }

    /**
     * Sets the value of the dateFrom property.
     * 
     * @param value
     *     allowed object is
     *     {@link Date }
     *     
     */
    public void setDateFrom(Date value) {
        this.dateFrom = value;
    }

    /**
     * Gets the value of the dateTo property.
     * 
     * @return
     *     possible object is
     *     {@link Date }
     *     
     */
    public Date getDateTo() {
        return dateTo;
    }

    /**
     * Sets the value of the dateTo property.
     * 
     * @param value
     *     allowed object is
     *     {@link Date }
     *     
     */
    public void setDateTo(Date value) {
        this.dateTo = value;
    }

    /**
     * Gets the value of the deactivationMode property.
     * 
     */
    public int getDeactivationMode() {
        return deactivationMode;
    }

    /**
     * Sets the value of the deactivationMode property.
     * 
     */
    public void setDeactivationMode(int value) {
        this.deactivationMode = value;
    }

    /**
     * Gets the value of the modeTitle property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getModeTitle() {
        return modeTitle;
    }

    /**
     * Sets the value of the modeTitle property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setModeTitle(String value) {
        this.modeTitle = value;
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
     * Gets the value of the periodCol property.
     * 
     */
    public int getPeriodCol() {
        return periodCol;
    }

    /**
     * Sets the value of the periodCol property.
     * 
     */
    public void setPeriodCol(int value) {
        this.periodCol = value;
    }

    /**
     * Gets the value of the periodMode property.
     * 
     */
    public int getPeriodMode() {
        return periodMode;
    }

    /**
     * Sets the value of the periodMode property.
     * 
     */
    public void setPeriodMode(int value) {
        this.periodMode = value;
    }

    /**
     * Gets the value of the reactivationMode property.
     * 
     */
    public int getReactivationMode() {
        return reactivationMode;
    }

    /**
     * Sets the value of the reactivationMode property.
     * 
     */
    public void setReactivationMode(int value) {
        this.reactivationMode = value;
    }

}
