
package ru.bgcrm.plugin.bgbilling.ws.contract.status;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the ru.bgcrm.plugin.bgbilling.ws.contract.status package. 
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

    private final static QName _BGException_QNAME = new QName("http://service.common.status.contract.kernel.bgbilling.bitel.ru/", "BGException");
    private final static QName _CreateBalanceDump_QNAME = new QName("http://service.common.status.contract.kernel.bgbilling.bitel.ru/", "createBalanceDump");
    private final static QName _GetStatusListResponse_QNAME = new QName("http://service.common.status.contract.kernel.bgbilling.bitel.ru/", "getStatusListResponse");
    private final static QName _ContractSearchResponse_QNAME = new QName("http://service.common.status.contract.kernel.bgbilling.bitel.ru/", "contractSearchResponse");
    private final static QName _CreateBalanceDumpResponse_QNAME = new QName("http://service.common.status.contract.kernel.bgbilling.bitel.ru/", "createBalanceDumpResponse");
    private final static QName _ChangeContractStatusResponse_QNAME = new QName("http://service.common.status.contract.kernel.bgbilling.bitel.ru/", "changeContractStatusResponse");
    private final static QName _ContractSearch_QNAME = new QName("http://service.common.status.contract.kernel.bgbilling.bitel.ru/", "contractSearch");
    private final static QName _GetStatusList_QNAME = new QName("http://service.common.status.contract.kernel.bgbilling.bitel.ru/", "getStatusList");
    private final static QName _ChangeContractStatus_QNAME = new QName("http://service.common.status.contract.kernel.bgbilling.bitel.ru/", "changeContractStatus");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: ru.bgcrm.plugin.bgbilling.ws.contract.status
     * 
     */
    public ObjectFactory() {
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
     * Create an instance of {@link GetStatusListResponse }
     * 
     */
    public GetStatusListResponse createGetStatusListResponse() {
        return new GetStatusListResponse();
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
     * Create an instance of {@link GetStatusList }
     * 
     */
    public GetStatusList createGetStatusList() {
        return new GetStatusList();
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
     * Create an instance of {@link Status }
     * 
     */
    public Status createStatus() {
        return new Status();
    }

    /**
     * Create an instance of {@link Id }
     * 
     */
    public Id createId() {
        return new Id();
    }

    /**
     * Create an instance of {@link SearchResult }
     * 
     */
    public SearchResult createSearchResult() {
        return new SearchResult();
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
     * Create an instance of {@link Period }
     * 
     */
    public Period createPeriod() {
        return new Period();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link BGException }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://service.common.status.contract.kernel.bgbilling.bitel.ru/", name = "BGException")
    public JAXBElement<BGException> createBGException(BGException value) {
        return new JAXBElement<BGException>(_BGException_QNAME, BGException.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CreateBalanceDump }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://service.common.status.contract.kernel.bgbilling.bitel.ru/", name = "createBalanceDump")
    public JAXBElement<CreateBalanceDump> createCreateBalanceDump(CreateBalanceDump value) {
        return new JAXBElement<CreateBalanceDump>(_CreateBalanceDump_QNAME, CreateBalanceDump.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetStatusListResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://service.common.status.contract.kernel.bgbilling.bitel.ru/", name = "getStatusListResponse")
    public JAXBElement<GetStatusListResponse> createGetStatusListResponse(GetStatusListResponse value) {
        return new JAXBElement<GetStatusListResponse>(_GetStatusListResponse_QNAME, GetStatusListResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ContractSearchResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://service.common.status.contract.kernel.bgbilling.bitel.ru/", name = "contractSearchResponse")
    public JAXBElement<ContractSearchResponse> createContractSearchResponse(ContractSearchResponse value) {
        return new JAXBElement<ContractSearchResponse>(_ContractSearchResponse_QNAME, ContractSearchResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CreateBalanceDumpResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://service.common.status.contract.kernel.bgbilling.bitel.ru/", name = "createBalanceDumpResponse")
    public JAXBElement<CreateBalanceDumpResponse> createCreateBalanceDumpResponse(CreateBalanceDumpResponse value) {
        return new JAXBElement<CreateBalanceDumpResponse>(_CreateBalanceDumpResponse_QNAME, CreateBalanceDumpResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ChangeContractStatusResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://service.common.status.contract.kernel.bgbilling.bitel.ru/", name = "changeContractStatusResponse")
    public JAXBElement<ChangeContractStatusResponse> createChangeContractStatusResponse(ChangeContractStatusResponse value) {
        return new JAXBElement<ChangeContractStatusResponse>(_ChangeContractStatusResponse_QNAME, ChangeContractStatusResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ContractSearch }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://service.common.status.contract.kernel.bgbilling.bitel.ru/", name = "contractSearch")
    public JAXBElement<ContractSearch> createContractSearch(ContractSearch value) {
        return new JAXBElement<ContractSearch>(_ContractSearch_QNAME, ContractSearch.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetStatusList }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://service.common.status.contract.kernel.bgbilling.bitel.ru/", name = "getStatusList")
    public JAXBElement<GetStatusList> createGetStatusList(GetStatusList value) {
        return new JAXBElement<GetStatusList>(_GetStatusList_QNAME, GetStatusList.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ChangeContractStatus }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://service.common.status.contract.kernel.bgbilling.bitel.ru/", name = "changeContractStatus")
    public JAXBElement<ChangeContractStatus> createChangeContractStatus(ChangeContractStatus value) {
        return new JAXBElement<ChangeContractStatus>(_ChangeContractStatus_QNAME, ChangeContractStatus.class, null, value);
    }

}
