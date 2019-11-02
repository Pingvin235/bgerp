
package ru.bgcrm.plugin.bgbilling.ws.contract.status;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for getStatusList complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="getStatusList">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="onlyManual" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "getStatusList", propOrder = {
    "onlyManual"
})
public class GetStatusList {

    protected boolean onlyManual;

    /**
     * Gets the value of the onlyManual property.
     * 
     */
    public boolean isOnlyManual() {
        return onlyManual;
    }

    /**
     * Sets the value of the onlyManual property.
     * 
     */
    public void setOnlyManual(boolean value) {
        this.onlyManual = value;
    }

}
