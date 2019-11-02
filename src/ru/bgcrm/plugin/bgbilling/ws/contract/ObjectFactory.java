
package ru.bgcrm.plugin.bgbilling.ws.contract;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the ru.bgcrm.plugin.bgbilling.ws.contract package. 
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

    private final static QName _BGException_QNAME = new QName("http://service.common.api.contract.kernel.bgbilling.bitel.ru/", "BGException");
    private final static QName _ContractGroupRemove_QNAME = new QName("http://service.common.api.contract.kernel.bgbilling.bitel.ru/", "contractGroupRemove");
    private final static QName _ContractGroupAdd_QNAME = new QName("http://service.common.api.contract.kernel.bgbilling.bitel.ru/", "contractGroupAdd");
    private final static QName _ContractGroupRemoveResponse_QNAME = new QName("http://service.common.api.contract.kernel.bgbilling.bitel.ru/", "contractGroupRemoveResponse");
    private final static QName _ContractGroupAddResponse_QNAME = new QName("http://service.common.api.contract.kernel.bgbilling.bitel.ru/", "contractGroupAddResponse");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: ru.bgcrm.plugin.bgbilling.ws.contract
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link ContractGroupAdd }
     * 
     */
    public ContractGroupAdd createContractGroupAdd() {
        return new ContractGroupAdd();
    }

    /**
     * Create an instance of {@link ContractGroupRemove }
     * 
     */
    public ContractGroupRemove createContractGroupRemove() {
        return new ContractGroupRemove();
    }

    /**
     * Create an instance of {@link BGException }
     * 
     */
    public BGException createBGException() {
        return new BGException();
    }

    /**
     * Create an instance of {@link ContractGroupAddResponse }
     * 
     */
    public ContractGroupAddResponse createContractGroupAddResponse() {
        return new ContractGroupAddResponse();
    }

    /**
     * Create an instance of {@link ContractGroupRemoveResponse }
     * 
     */
    public ContractGroupRemoveResponse createContractGroupRemoveResponse() {
        return new ContractGroupRemoveResponse();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link BGException }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://service.common.api.contract.kernel.bgbilling.bitel.ru/", name = "BGException")
    public JAXBElement<BGException> createBGException(BGException value) {
        return new JAXBElement<BGException>(_BGException_QNAME, BGException.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ContractGroupRemove }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://service.common.api.contract.kernel.bgbilling.bitel.ru/", name = "contractGroupRemove")
    public JAXBElement<ContractGroupRemove> createContractGroupRemove(ContractGroupRemove value) {
        return new JAXBElement<ContractGroupRemove>(_ContractGroupRemove_QNAME, ContractGroupRemove.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ContractGroupAdd }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://service.common.api.contract.kernel.bgbilling.bitel.ru/", name = "contractGroupAdd")
    public JAXBElement<ContractGroupAdd> createContractGroupAdd(ContractGroupAdd value) {
        return new JAXBElement<ContractGroupAdd>(_ContractGroupAdd_QNAME, ContractGroupAdd.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ContractGroupRemoveResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://service.common.api.contract.kernel.bgbilling.bitel.ru/", name = "contractGroupRemoveResponse")
    public JAXBElement<ContractGroupRemoveResponse> createContractGroupRemoveResponse(ContractGroupRemoveResponse value) {
        return new JAXBElement<ContractGroupRemoveResponse>(_ContractGroupRemoveResponse_QNAME, ContractGroupRemoveResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ContractGroupAddResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://service.common.api.contract.kernel.bgbilling.bitel.ru/", name = "contractGroupAddResponse")
    public JAXBElement<ContractGroupAddResponse> createContractGroupAddResponse(ContractGroupAddResponse value) {
        return new JAXBElement<ContractGroupAddResponse>(_ContractGroupAddResponse_QNAME, ContractGroupAddResponse.class, null, value);
    }

}
