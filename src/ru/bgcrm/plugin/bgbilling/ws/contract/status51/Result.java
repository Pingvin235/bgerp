
package ru.bgcrm.plugin.bgbilling.ws.contract.status51;

import java.util.ArrayList;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for result complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="result">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="data" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="item" type="{http://www.w3.org/2001/XMLSchema}anyType" maxOccurs="unbounded" minOccurs="0"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="page" type="{http://common.bitel.ru}page" minOccurs="0"/>
 *         &lt;element name="attributes" type="{http://common.status.contract.kernel.bgbilling.bitel.ru/}list" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "result", namespace = "http://common.bitel.ru", propOrder = {
    "data",
    "page",
    "attributes"
})
public class Result {

    protected Result.Data data;
    protected Page page;
    protected ru.bgcrm.plugin.bgbilling.ws.contract.status51.List attributes;

    /**
     * Gets the value of the data property.
     * 
     * @return
     *     possible object is
     *     {@link Result.Data }
     *     
     */
    public Result.Data getData() {
        return data;
    }

    /**
     * Sets the value of the data property.
     * 
     * @param value
     *     allowed object is
     *     {@link Result.Data }
     *     
     */
    public void setData(Result.Data value) {
        this.data = value;
    }

    /**
     * Gets the value of the page property.
     * 
     * @return
     *     possible object is
     *     {@link Page }
     *     
     */
    public Page getPage() {
        return page;
    }

    /**
     * Sets the value of the page property.
     * 
     * @param value
     *     allowed object is
     *     {@link Page }
     *     
     */
    public void setPage(Page value) {
        this.page = value;
    }

    /**
     * Gets the value of the attributes property.
     * 
     * @return
     *     possible object is
     *     {@link ru.bgcrm.plugin.bgbilling.ws.contract.status51.List }
     *     
     */
    public ru.bgcrm.plugin.bgbilling.ws.contract.status51.List getAttributes() {
        return attributes;
    }

    /**
     * Sets the value of the attributes property.
     * 
     * @param value
     *     allowed object is
     *     {@link ru.bgcrm.plugin.bgbilling.ws.contract.status51.List }
     *     
     */
    public void setAttributes(ru.bgcrm.plugin.bgbilling.ws.contract.status51.List value) {
        this.attributes = value;
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
     *         &lt;element name="item" type="{http://www.w3.org/2001/XMLSchema}anyType" maxOccurs="unbounded" minOccurs="0"/>
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
    public static class Data {

        protected java.util.List<Object> item;

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
         * {@link Object }
         * 
         * 
         */
        public java.util.List<Object> getItem() {
            if (item == null) {
                item = new ArrayList<Object>();
            }
            return this.item;
        }

    }

}
