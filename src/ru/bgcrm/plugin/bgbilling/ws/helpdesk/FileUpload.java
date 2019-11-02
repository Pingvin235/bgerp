
package ru.bgcrm.plugin.bgbilling.ws.helpdesk;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for fileUpload complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="fileUpload">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="topicId" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="filename" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="size" type="{http://www.w3.org/2001/XMLSchema}long"/>
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
@XmlType(name = "fileUpload", propOrder = {
    "topicId",
    "filename",
    "size",
    "fileData"
})
public class FileUpload {

    protected String topicId;
    protected String filename;
    protected long size;
    protected byte[] fileData;

    /**
     * Gets the value of the topicId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTopicId() {
        return topicId;
    }

    /**
     * Sets the value of the topicId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTopicId(String value) {
        this.topicId = value;
    }

    /**
     * Gets the value of the filename property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFilename() {
        return filename;
    }

    /**
     * Sets the value of the filename property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFilename(String value) {
        this.filename = value;
    }

    /**
     * Gets the value of the size property.
     * 
     */
    public long getSize() {
        return size;
    }

    /**
     * Sets the value of the size property.
     * 
     */
    public void setSize(long value) {
        this.size = value;
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
