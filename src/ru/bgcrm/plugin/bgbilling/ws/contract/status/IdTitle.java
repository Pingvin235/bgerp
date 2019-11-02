
package ru.bgcrm.plugin.bgbilling.ws.contract.status;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for idTitle complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="idTitle">
 *   &lt;complexContent>
 *     &lt;extension base="{http://common.bitel.ru}id">
 *       &lt;sequence>
 *       &lt;/sequence>
 *       &lt;attribute name="title" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "idTitle", namespace = "http://common.bitel.ru")
@XmlSeeAlso({
    Status.class
})
public class IdTitle
    extends Id
{

    @XmlAttribute(name = "title")
    protected String title;

    /**
     * Gets the value of the title property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets the value of the title property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTitle(String value) {
        this.title = value;
    }

}
