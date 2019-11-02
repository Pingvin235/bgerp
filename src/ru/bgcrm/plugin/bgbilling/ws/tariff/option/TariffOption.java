
package ru.bgcrm.plugin.bgbilling.ws.tariff.option;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlList;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import java.util.Date;


/**
 * <p>Java class for tariffOption complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="tariffOption">
 *   &lt;complexContent>
 *     &lt;extension base="{http://common.bitel.ru}idTitle">
 *       &lt;sequence>
 *         &lt;element name="modeList" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="item" type="{http://service.common.option.tariff.kernel.bgbilling.bitel.ru/}tariffOptionActivateMode" maxOccurs="unbounded" minOccurs="0"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="depends" minOccurs="0">
 *           &lt;simpleType>
 *             &lt;list itemType="{http://www.w3.org/2001/XMLSchema}int" />
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="description" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="incompatible" minOccurs="0">
 *           &lt;simpleType>
 *             &lt;list itemType="{http://www.w3.org/2001/XMLSchema}int" />
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="tariffIdSet" minOccurs="0">
 *           &lt;simpleType>
 *             &lt;list itemType="{http://www.w3.org/2001/XMLSchema}int" />
 *           &lt;/simpleType>
 *         &lt;/element>
 *       &lt;/sequence>
 *       &lt;attribute name="comment" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="contractGroups" use="required" type="{http://www.w3.org/2001/XMLSchema}long" />
 *       &lt;attribute name="dateFrom" type="{http://www.w3.org/2001/XMLSchema}dateTime" />
 *       &lt;attribute name="dateTo" type="{http://www.w3.org/2001/XMLSchema}dateTime" />
 *       &lt;attribute name="enable" use="required" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *       &lt;attribute name="hideForContractGroups" use="required" type="{http://www.w3.org/2001/XMLSchema}long" />
 *       &lt;attribute name="hideForContractGroupsMode" use="required" type="{http://www.w3.org/2001/XMLSchema}int" />
 *       &lt;attribute name="hideForWeb" use="required" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "tariffOption", propOrder = {
    "modeList",
    "depends",
    "description",
    "incompatible",
    "tariffIdSet"
})
public class TariffOption
    extends IdTitle
{

    protected TariffOption.ModeList modeList;
    @XmlList
    @XmlElement(type = Integer.class)
    protected List<Integer> depends;
    protected String description;
    @XmlList
    @XmlElement(type = Integer.class)
    protected List<Integer> incompatible;
    @XmlList
    @XmlElement(type = Integer.class)
    protected List<Integer> tariffIdSet;
    @XmlAttribute(name = "comment")
    protected String comment;
    @XmlAttribute(name = "contractGroups", required = true)
    protected long contractGroups;
    @XmlAttribute(name = "dateFrom")
    @XmlSchemaType(name = "dateTime")
    protected Date dateFrom;
    @XmlAttribute(name = "dateTo")
    @XmlSchemaType(name = "dateTime")
    protected Date dateTo;
    @XmlAttribute(name = "enable", required = true)
    protected boolean enable;
    @XmlAttribute(name = "hideForContractGroups", required = true)
    protected long hideForContractGroups;
    @XmlAttribute(name = "hideForContractGroupsMode", required = true)
    protected int hideForContractGroupsMode;
    @XmlAttribute(name = "hideForWeb", required = true)
    protected boolean hideForWeb;

    /**
     * Gets the value of the modeList property.
     * 
     * @return
     *     possible object is
     *     {@link TariffOption.ModeList }
     *     
     */
    public TariffOption.ModeList getModeList() {
        return modeList;
    }

    /**
     * Sets the value of the modeList property.
     * 
     * @param value
     *     allowed object is
     *     {@link TariffOption.ModeList }
     *     
     */
    public void setModeList(TariffOption.ModeList value) {
        this.modeList = value;
    }

    /**
     * Gets the value of the depends property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the depends property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getDepends().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Integer }
     * 
     * 
     */
    public List<Integer> getDepends() {
        if (depends == null) {
            depends = new ArrayList<Integer>();
        }
        return this.depends;
    }

    /**
     * Gets the value of the description property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the value of the description property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDescription(String value) {
        this.description = value;
    }

    /**
     * Gets the value of the incompatible property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the incompatible property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getIncompatible().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Integer }
     * 
     * 
     */
    public List<Integer> getIncompatible() {
        if (incompatible == null) {
            incompatible = new ArrayList<Integer>();
        }
        return this.incompatible;
    }

    /**
     * Gets the value of the tariffIdSet property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the tariffIdSet property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getTariffIdSet().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Integer }
     * 
     * 
     */
    public List<Integer> getTariffIdSet() {
        if (tariffIdSet == null) {
            tariffIdSet = new ArrayList<Integer>();
        }
        return this.tariffIdSet;
    }

    /**
     * Gets the value of the comment property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getComment() {
        return comment;
    }

    /**
     * Sets the value of the comment property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setComment(String value) {
        this.comment = value;
    }

    /**
     * Gets the value of the contractGroups property.
     * 
     */
    public long getContractGroups() {
        return contractGroups;
    }

    /**
     * Sets the value of the contractGroups property.
     * 
     */
    public void setContractGroups(long value) {
        this.contractGroups = value;
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
     * Gets the value of the enable property.
     * 
     */
    public boolean isEnable() {
        return enable;
    }

    /**
     * Sets the value of the enable property.
     * 
     */
    public void setEnable(boolean value) {
        this.enable = value;
    }

    /**
     * Gets the value of the hideForContractGroups property.
     * 
     */
    public long getHideForContractGroups() {
        return hideForContractGroups;
    }

    /**
     * Sets the value of the hideForContractGroups property.
     * 
     */
    public void setHideForContractGroups(long value) {
        this.hideForContractGroups = value;
    }

    /**
     * Gets the value of the hideForContractGroupsMode property.
     * 
     */
    public int getHideForContractGroupsMode() {
        return hideForContractGroupsMode;
    }

    /**
     * Sets the value of the hideForContractGroupsMode property.
     * 
     */
    public void setHideForContractGroupsMode(int value) {
        this.hideForContractGroupsMode = value;
    }

    /**
     * Gets the value of the hideForWeb property.
     * 
     */
    public boolean isHideForWeb() {
        return hideForWeb;
    }

    /**
     * Sets the value of the hideForWeb property.
     * 
     */
    public void setHideForWeb(boolean value) {
        this.hideForWeb = value;
    }


    /**
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;sequence>
     *         &lt;element name="item" type="{http://service.common.option.tariff.kernel.bgbilling.bitel.ru/}tariffOptionActivateMode" maxOccurs="unbounded" minOccurs="0"/>
     *       &lt;/sequence>
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "item"
    })
    public static class ModeList {

        protected List<TariffOptionActivateMode> item;

        /**
         * Gets the value of the item property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the item property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getItem().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link TariffOptionActivateMode }
         * 
         * 
         */
        public List<TariffOptionActivateMode> getItem() {
            if (item == null) {
                item = new ArrayList<TariffOptionActivateMode>();
            }
            return this.item;
        }

    }

}
