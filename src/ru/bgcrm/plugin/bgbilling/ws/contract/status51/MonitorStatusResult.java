
package ru.bgcrm.plugin.bgbilling.ws.contract.status51;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for monitorStatusResult complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="monitorStatusResult">
 *   &lt;complexContent>
 *     &lt;extension base="{http://common.bitel.ru}idTitle">
 *       &lt;sequence>
 *         &lt;element name="cid" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="contractComment" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="contractTitile" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="saldo" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="status" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="statusDate" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="statusDateTo" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "monitorStatusResult", propOrder = {
    "cid",
    "contractComment",
    "contractTitile",
    "saldo",
    "status",
    "statusDate",
    "statusDateTo"
})
public class MonitorStatusResult
    extends IdTitle
{

    protected int cid;
    protected String contractComment;
    protected String contractTitile;
    protected String saldo;
    protected String status;
    protected String statusDate;
    protected String statusDateTo;

    /**
     * Gets the value of the cid property.
     * 
     */
    public int getCid() {
        return cid;
    }

    /**
     * Sets the value of the cid property.
     * 
     */
    public void setCid(int value) {
        this.cid = value;
    }

    /**
     * Gets the value of the contractComment property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getContractComment() {
        return contractComment;
    }

    /**
     * Sets the value of the contractComment property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setContractComment(String value) {
        this.contractComment = value;
    }

    /**
     * Gets the value of the contractTitile property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getContractTitile() {
        return contractTitile;
    }

    /**
     * Sets the value of the contractTitile property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setContractTitile(String value) {
        this.contractTitile = value;
    }

    /**
     * Gets the value of the saldo property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSaldo() {
        return saldo;
    }

    /**
     * Sets the value of the saldo property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSaldo(String value) {
        this.saldo = value;
    }

    /**
     * Gets the value of the status property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getStatus() {
        return status;
    }

    /**
     * Sets the value of the status property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setStatus(String value) {
        this.status = value;
    }

    /**
     * Gets the value of the statusDate property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getStatusDate() {
        return statusDate;
    }

    /**
     * Sets the value of the statusDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setStatusDate(String value) {
        this.statusDate = value;
    }

    /**
     * Gets the value of the statusDateTo property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getStatusDateTo() {
        return statusDateTo;
    }

    /**
     * Sets the value of the statusDateTo property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setStatusDateTo(String value) {
        this.statusDateTo = value;
    }

}
