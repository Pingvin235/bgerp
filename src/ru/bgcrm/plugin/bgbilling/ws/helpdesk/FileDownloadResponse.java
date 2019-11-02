
package ru.bgcrm.plugin.bgbilling.ws.helpdesk;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for fileDownloadResponse complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="fileDownloadResponse">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="serverFile" type="{http://service.common.helpdesk.plugins.bgbilling.bitel.ru/}bgServerFile" minOccurs="0"/>
 *         &lt;element name="fileData" type="{http://www.w3.org/2001/XMLSchema}base64Binary" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "fileDownloadResponse", propOrder = {
    "serverFile",
    "fileData"
})
public class FileDownloadResponse {

    protected BgServerFile serverFile;
    protected byte[] fileData;

    /**
     * Gets the value of the serverFile property.
     * 
     * @return
     *     possible object is
     *     {@link BgServerFile }
     *     
     */
    public BgServerFile getServerFile() {
        return serverFile;
    }

    /**
     * Sets the value of the serverFile property.
     * 
     * @param value
     *     allowed object is
     *     {@link BgServerFile }
     *     
     */
    public void setServerFile(BgServerFile value) {
        this.serverFile = value;
    }

    /**
     * Gets the value of the fileData property.
     * 
     * @return
     *     possible object is
     *     byte[]
     */
    public byte[] getFileData() {
        return fileData;
    }

    /**
     * Sets the value of the fileData property.
     * 
     * @param value
     *     allowed object is
     *     byte[]
     */
    public void setFileData(byte[] value) {
        this.fileData = value;
    }

}
