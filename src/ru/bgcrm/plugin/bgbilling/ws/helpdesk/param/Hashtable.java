
package ru.bgcrm.plugin.bgbilling.ws.helpdesk.param;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for hashtable complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="hashtable">
 *   &lt;complexContent>
 *     &lt;extension base="{http://service.common.helpdesk.plugins.bgbilling.bitel.ru/}dictionary">
 *       &lt;sequence>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "hashtable")
@XmlSeeAlso({
    Properties.class
})
public class Hashtable
    extends Dictionary
{


}
