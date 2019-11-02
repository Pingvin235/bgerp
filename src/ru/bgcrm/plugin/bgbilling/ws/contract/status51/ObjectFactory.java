
package ru.bgcrm.plugin.bgbilling.ws.contract.status51;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the ru.bgcrm.plugin.bgbilling.ws.contract.status51 package. 
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

    private final static QName _ChangeContractStatusResponse_QNAME = new QName("http://common.status.contract.kernel.bgbilling.bitel.ru/", "changeContractStatusResponse");
    private final static QName _ChangeContractStatus_QNAME = new QName("http://common.status.contract.kernel.bgbilling.bitel.ru/", "changeContractStatus");
    private final static QName _ContractSearch_QNAME = new QName("http://common.status.contract.kernel.bgbilling.bitel.ru/", "contractSearch");
    private final static QName _CreateBalanceDump_QNAME = new QName("http://common.status.contract.kernel.bgbilling.bitel.ru/", "createBalanceDump");
    private final static QName _BGException_QNAME = new QName("http://common.status.contract.kernel.bgbilling.bitel.ru/", "BGException");
    private final static QName _Result_QNAME = new QName("http://common.bitel.ru", "result");
    private final static QName _CreateBalanceDumpResponse_QNAME = new QName("http://common.status.contract.kernel.bgbilling.bitel.ru/", "createBalanceDumpResponse");
    private final static QName _ContractSearchResponse_QNAME = new QName("http://common.status.contract.kernel.bgbilling.bitel.ru/", "contractSearchResponse");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: ru.bgcrm.plugin.bgbilling.ws.contract.status51
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link Result }
     * 
     */
    public Result createResult() {
        return new Result();
    }

    /**
     * Create an instance of {@link CreateBalanceDumpResponse }
     * 
     */
    public CreateBalanceDumpResponse createCreateBalanceDumpResponse() {
        return new CreateBalanceDumpResponse();
    }

    /**
     * Create an instance of {@link ContractSearchResponse }
     * 
     */
    public ContractSearchResponse createContractSearchResponse() {
        return new ContractSearchResponse();
    }

    /**
     * Create an instance of {@link CreateBalanceDump }
     * 
     */
    public CreateBalanceDump createCreateBalanceDump() {
        return new CreateBalanceDump();
    }

    /**
     * Create an instance of {@link BGException }
     * 
     */
    public BGException createBGException() {
        return new BGException();
    }

    /**
     * Create an instance of {@link ChangeContractStatus }
     * 
     */
    public ChangeContractStatus createChangeContractStatus() {
        return new ChangeContractStatus();
    }

    /**
     * Create an instance of {@link ContractSearch }
     * 
     */
    public ContractSearch createContractSearch() {
        return new ContractSearch();
    }

    /**
     * Create an instance of {@link ChangeContractStatusResponse }
     * 
     */
    public ChangeContractStatusResponse createChangeContractStatusResponse() {
        return new ChangeContractStatusResponse();
    }

    /**
     * Create an instance of {@link MonitorStatusResult }
     * 
     */
    public MonitorStatusResult createMonitorStatusResult() {
        return new MonitorStatusResult();
    }

    /**
     * Create an instance of {@link MapEntry }
     * 
     */
    public MapEntry createMapEntry() {
        return new MapEntry();
    }

    /**
     * Create an instance of {@link List }
     * 
     */
    public List createList() {
        return new List();
    }

    /**
     * Create an instance of {@link Collection }
     * 
     */
    public Collection createCollection() {
        return new Collection();
    }

    /**
     * Create an instance of {@link IntMapEntry }
     * 
     */
    public IntMapEntry createIntMapEntry() {
        return new IntMapEntry();
    }

    /**
     * Create an instance of {@link Id }
     * 
     */
    public Id createId() {
        return new Id();
    }

    /**
     * Create an instance of {@link IdTitle }
     * 
     */
    public IdTitle createIdTitle() {
        return new IdTitle();
    }

    /**
     * Create an instance of {@link Page }
     * 
     */
    public Page createPage() {
        return new Page();
    }

    /**
     * Create an instance of {@link KeyValue }
     * 
     */
    public KeyValue createKeyValue() {
        return new KeyValue();
    }

    /**
     * Create an instance of {@link Result.Data }
     * 
     */
    public Result.Data createResultData() {
        return new Result.Data();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ChangeContractStatusResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://common.status.contract.kernel.bgbilling.bitel.ru/", name = "changeContractStatusResponse")
    public JAXBElement<ChangeContractStatusResponse> createChangeContractStatusResponse(ChangeContractStatusResponse value) {
        return new JAXBElement<ChangeContractStatusResponse>(_ChangeContractStatusResponse_QNAME, ChangeContractStatusResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ChangeContractStatus }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://common.status.contract.kernel.bgbilling.bitel.ru/", name = "changeContractStatus")
    public JAXBElement<ChangeContractStatus> createChangeContractStatus(ChangeContractStatus value) {
        return new JAXBElement<ChangeContractStatus>(_ChangeContractStatus_QNAME, ChangeContractStatus.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ContractSearch }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://common.status.contract.kernel.bgbilling.bitel.ru/", name = "contractSearch")
    public JAXBElement<ContractSearch> createContractSearch(ContractSearch value) {
        return new JAXBElement<ContractSearch>(_ContractSearch_QNAME, ContractSearch.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CreateBalanceDump }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://common.status.contract.kernel.bgbilling.bitel.ru/", name = "createBalanceDump")
    public JAXBElement<CreateBalanceDump> createCreateBalanceDump(CreateBalanceDump value) {
        return new JAXBElement<CreateBalanceDump>(_CreateBalanceDump_QNAME, CreateBalanceDump.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link BGException }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://common.status.contract.kernel.bgbilling.bitel.ru/", name = "BGException")
    public JAXBElement<BGException> createBGException(BGException value) {
        return new JAXBElement<BGException>(_BGException_QNAME, BGException.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Result }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://common.bitel.ru", name = "result")
    public JAXBElement<Result> createResult(Result value) {
        return new JAXBElement<Result>(_Result_QNAME, Result.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CreateBalanceDumpResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://common.status.contract.kernel.bgbilling.bitel.ru/", name = "createBalanceDumpResponse")
    public JAXBElement<CreateBalanceDumpResponse> createCreateBalanceDumpResponse(CreateBalanceDumpResponse value) {
        return new JAXBElement<CreateBalanceDumpResponse>(_CreateBalanceDumpResponse_QNAME, CreateBalanceDumpResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ContractSearchResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://common.status.contract.kernel.bgbilling.bitel.ru/", name = "contractSearchResponse")
    public JAXBElement<ContractSearchResponse> createContractSearchResponse(ContractSearchResponse value) {
        return new JAXBElement<ContractSearchResponse>(_ContractSearchResponse_QNAME, ContractSearchResponse.class, null, value);
    }

}
