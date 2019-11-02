
package ru.bgcrm.plugin.bgbilling.ws.contract.status51;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for contractSearch complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="contractSearch">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="arg0" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="arg1" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="arg2" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="arg3" type="{http://www.w3.org/2001/XMLSchema}int" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="arg4" type="{http://www.w3.org/2001/XMLSchema}int" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="arg5" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="arg6" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="arg7" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="arg8" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="arg9" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="arg10" type="{http://common.bitel.ru}page" minOccurs="0"/>
 *         &lt;element name="arg11" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="arg12" type="{http://www.w3.org/2001/XMLSchema}decimal" minOccurs="0"/>
 *         &lt;element name="arg13" type="{http://www.w3.org/2001/XMLSchema}int" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="arg14" type="{http://www.w3.org/2001/XMLSchema}float"/>
 *         &lt;element name="arg15" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="arg16" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "contractSearch", propOrder = {
    "arg0",
    "arg1",
    "arg2",
    "arg3",
    "arg4",
    "arg5",
    "arg6",
    "arg7",
    "arg8",
    "arg9",
    "arg10",
    "arg11",
    "arg12",
    "arg13",
    "arg14",
    "arg15",
    "arg16"
})
public class ContractSearch {

    protected int arg0;
    protected int arg1;
    protected String arg2;
    @XmlElement(type = Integer.class)
    protected List<Integer> arg3;
    @XmlElement(type = Integer.class)
    protected List<Integer> arg4;
    protected String arg5;
    protected String arg6;
    protected String arg7;
    protected String arg8;
    protected int arg9;
    protected Page arg10;
    protected int arg11;
    protected BigDecimal arg12;
    @XmlElement(type = Integer.class)
    protected List<Integer> arg13;
    protected float arg14;
    protected int arg15;
    protected int arg16;

    /**
     * Gets the value of the arg0 property.
     * 
     */
    public int getArg0() {
        return arg0;
    }

    /**
     * Sets the value of the arg0 property.
     * 
     */
    public void setArg0(int value) {
        this.arg0 = value;
    }

    /**
     * Gets the value of the arg1 property.
     * 
     */
    public int getArg1() {
        return arg1;
    }

    /**
     * Sets the value of the arg1 property.
     * 
     */
    public void setArg1(int value) {
        this.arg1 = value;
    }

    /**
     * Gets the value of the arg2 property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getArg2() {
        return arg2;
    }

    /**
     * Sets the value of the arg2 property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setArg2(String value) {
        this.arg2 = value;
    }

    /**
     * Gets the value of the arg3 property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the arg3 property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getArg3().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Integer }
     * 
     * 
     */
    public List<Integer> getArg3() {
        if (arg3 == null) {
            arg3 = new ArrayList<Integer>();
        }
        return this.arg3;
    }

    /**
     * Gets the value of the arg4 property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the arg4 property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getArg4().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Integer }
     * 
     * 
     */
    public List<Integer> getArg4() {
        if (arg4 == null) {
            arg4 = new ArrayList<Integer>();
        }
        return this.arg4;
    }

    /**
     * Gets the value of the arg5 property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getArg5() {
        return arg5;
    }

    /**
     * Sets the value of the arg5 property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setArg5(String value) {
        this.arg5 = value;
    }

    /**
     * Gets the value of the arg6 property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getArg6() {
        return arg6;
    }

    /**
     * Sets the value of the arg6 property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setArg6(String value) {
        this.arg6 = value;
    }

    /**
     * Gets the value of the arg7 property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getArg7() {
        return arg7;
    }

    /**
     * Sets the value of the arg7 property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setArg7(String value) {
        this.arg7 = value;
    }

    /**
     * Gets the value of the arg8 property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getArg8() {
        return arg8;
    }

    /**
     * Sets the value of the arg8 property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setArg8(String value) {
        this.arg8 = value;
    }

    /**
     * Gets the value of the arg9 property.
     * 
     */
    public int getArg9() {
        return arg9;
    }

    /**
     * Sets the value of the arg9 property.
     * 
     */
    public void setArg9(int value) {
        this.arg9 = value;
    }

    /**
     * Gets the value of the arg10 property.
     * 
     * @return
     *     possible object is
     *     {@link Page }
     *     
     */
    public Page getArg10() {
        return arg10;
    }

    /**
     * Sets the value of the arg10 property.
     * 
     * @param value
     *     allowed object is
     *     {@link Page }
     *     
     */
    public void setArg10(Page value) {
        this.arg10 = value;
    }

    /**
     * Gets the value of the arg11 property.
     * 
     */
    public int getArg11() {
        return arg11;
    }

    /**
     * Sets the value of the arg11 property.
     * 
     */
    public void setArg11(int value) {
        this.arg11 = value;
    }

    /**
     * Gets the value of the arg12 property.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getArg12() {
        return arg12;
    }

    /**
     * Sets the value of the arg12 property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setArg12(BigDecimal value) {
        this.arg12 = value;
    }

    /**
     * Gets the value of the arg13 property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the arg13 property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getArg13().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Integer }
     * 
     * 
     */
    public List<Integer> getArg13() {
        if (arg13 == null) {
            arg13 = new ArrayList<Integer>();
        }
        return this.arg13;
    }

    /**
     * Gets the value of the arg14 property.
     * 
     */
    public float getArg14() {
        return arg14;
    }

    /**
     * Sets the value of the arg14 property.
     * 
     */
    public void setArg14(float value) {
        this.arg14 = value;
    }

    /**
     * Gets the value of the arg15 property.
     * 
     */
    public int getArg15() {
        return arg15;
    }

    /**
     * Sets the value of the arg15 property.
     * 
     */
    public void setArg15(int value) {
        this.arg15 = value;
    }

    /**
     * Gets the value of the arg16 property.
     * 
     */
    public int getArg16() {
        return arg16;
    }

    /**
     * Sets the value of the arg16 property.
     * 
     */
    public void setArg16(int value) {
        this.arg16 = value;
    }

}
