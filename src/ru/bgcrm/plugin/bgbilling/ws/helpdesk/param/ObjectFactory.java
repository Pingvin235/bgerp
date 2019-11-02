
package ru.bgcrm.plugin.bgbilling.ws.helpdesk.param;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the ru.bgcrm.plugin.bgbilling.ws.helpdesk package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _GetModeList_QNAME = new QName("http://service.common.helpdesk.plugins.bgbilling.bitel.ru/", "getModeList");
    private final static QName _GetPropertiesResponse_QNAME = new QName("http://service.common.helpdesk.plugins.bgbilling.bitel.ru/", "getPropertiesResponse");
    private final static QName _GetModeListResponse_QNAME = new QName("http://service.common.helpdesk.plugins.bgbilling.bitel.ru/", "getModeListResponse");
    private final static QName _GetProperties_QNAME = new QName("http://service.common.helpdesk.plugins.bgbilling.bitel.ru/", "getProperties");
    private final static QName _BGException_QNAME = new QName("http://service.common.helpdesk.plugins.bgbilling.bitel.ru/", "BGException");
    private final static QName _GetContractCurrentModeResponse_QNAME = new QName("http://service.common.helpdesk.plugins.bgbilling.bitel.ru/", "getContractCurrentModeResponse");
    private final static QName _GetContractCurrentMode_QNAME = new QName("http://service.common.helpdesk.plugins.bgbilling.bitel.ru/", "getContractCurrentMode");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: ru.bgcrm.plugin.bgbilling.ws.helpdesk
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link DirectoryItem }
     * 
     */
    public DirectoryItem createDirectoryItem() {
        return new DirectoryItem();
    }

    /**
     * Create an instance of {@link GetContractCurrentModeResponse }
     * 
     */
    public GetContractCurrentModeResponse createGetContractCurrentModeResponse() {
        return new GetContractCurrentModeResponse();
    }

    /**
     * Create an instance of {@link GetContractCurrentMode }
     * 
     */
    public GetContractCurrentMode createGetContractCurrentMode() {
        return new GetContractCurrentMode();
    }

    /**
     * Create an instance of {@link BGException }
     * 
     */
    public BGException createBGException() {
        return new BGException();
    }

    /**
     * Create an instance of {@link GetModeListResponse }
     * 
     */
    public GetModeListResponse createGetModeListResponse() {
        return new GetModeListResponse();
    }

    /**
     * Create an instance of {@link GetProperties }
     * 
     */
    public GetProperties createGetProperties() {
        return new GetProperties();
    }

    /**
     * Create an instance of {@link GetPropertiesResponse }
     * 
     */
    public GetPropertiesResponse createGetPropertiesResponse() {
        return new GetPropertiesResponse();
    }

    /**
     * Create an instance of {@link GetModeList }
     * 
     */
    public GetModeList createGetModeList() {
        return new GetModeList();
    }

    /**
     * Create an instance of {@link Hashtable }
     * 
     */
    public Hashtable createHashtable() {
        return new Hashtable();
    }

    /**
     * Create an instance of {@link Properties }
     * 
     */
    public Properties createProperties() {
        return new Properties();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetModeList }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://service.common.helpdesk.plugins.bgbilling.bitel.ru/", name = "getModeList")
    public JAXBElement<GetModeList> createGetModeList(GetModeList value) {
        return new JAXBElement<GetModeList>(_GetModeList_QNAME, GetModeList.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetPropertiesResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://service.common.helpdesk.plugins.bgbilling.bitel.ru/", name = "getPropertiesResponse")
    public JAXBElement<GetPropertiesResponse> createGetPropertiesResponse(GetPropertiesResponse value) {
        return new JAXBElement<GetPropertiesResponse>(_GetPropertiesResponse_QNAME, GetPropertiesResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetModeListResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://service.common.helpdesk.plugins.bgbilling.bitel.ru/", name = "getModeListResponse")
    public JAXBElement<GetModeListResponse> createGetModeListResponse(GetModeListResponse value) {
        return new JAXBElement<GetModeListResponse>(_GetModeListResponse_QNAME, GetModeListResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetProperties }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://service.common.helpdesk.plugins.bgbilling.bitel.ru/", name = "getProperties")
    public JAXBElement<GetProperties> createGetProperties(GetProperties value) {
        return new JAXBElement<GetProperties>(_GetProperties_QNAME, GetProperties.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link BGException }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://service.common.helpdesk.plugins.bgbilling.bitel.ru/", name = "BGException")
    public JAXBElement<BGException> createBGException(BGException value) {
        return new JAXBElement<BGException>(_BGException_QNAME, BGException.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetContractCurrentModeResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://service.common.helpdesk.plugins.bgbilling.bitel.ru/", name = "getContractCurrentModeResponse")
    public JAXBElement<GetContractCurrentModeResponse> createGetContractCurrentModeResponse(GetContractCurrentModeResponse value) {
        return new JAXBElement<GetContractCurrentModeResponse>(_GetContractCurrentModeResponse_QNAME, GetContractCurrentModeResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetContractCurrentMode }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://service.common.helpdesk.plugins.bgbilling.bitel.ru/", name = "getContractCurrentMode")
    public JAXBElement<GetContractCurrentMode> createGetContractCurrentMode(GetContractCurrentMode value) {
        return new JAXBElement<GetContractCurrentMode>(_GetContractCurrentMode_QNAME, GetContractCurrentMode.class, null, value);
    }

}
