
package ru.bgcrm.plugin.bgbilling.ws.helpdesk;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for fileDownload complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="fileDownload">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="fileId" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="contractId" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "fileDownload", propOrder = {
    "fileId",
    "contractId"
})
public class FileDownload {

    protected int fileId;
    protected int contractId;

    /**
     * Gets the value of the fileId property.
     * 
     */
    public int getFileId() {
        return fileId;
    }

    /**
     * Sets the value of the fileId property.
     * 
     */
    public void setFileId(int value) {
        this.fileId = value;
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

}
