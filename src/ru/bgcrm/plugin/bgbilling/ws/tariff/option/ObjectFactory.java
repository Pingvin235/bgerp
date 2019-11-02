
package ru.bgcrm.plugin.bgbilling.ws.tariff.option;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the ru.bgcrm.plugin.bgbilling.ws.tariff.option package. 
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

    private final static QName _TariffOptionListAvailableResponse_QNAME = new QName("http://service.common.option.tariff.kernel.bgbilling.bitel.ru/", "tariffOptionListAvailableResponse");
    private final static QName _TariffOptionListResponse_QNAME = new QName("http://service.common.option.tariff.kernel.bgbilling.bitel.ru/", "tariffOptionListResponse");
    private final static QName _TariffOptionGet_QNAME = new QName("http://service.common.option.tariff.kernel.bgbilling.bitel.ru/", "tariffOptionGet");
    private final static QName _ContractTariffOptionDeactivate_QNAME = new QName("http://service.common.option.tariff.kernel.bgbilling.bitel.ru/", "contractTariffOptionDeactivate");
    private final static QName _ContractTariffOptionReactivateResponse_QNAME = new QName("http://service.common.option.tariff.kernel.bgbilling.bitel.ru/", "contractTariffOptionReactivateResponse");
    private final static QName _ContractTariffOptionHistoryResponse_QNAME = new QName("http://service.common.option.tariff.kernel.bgbilling.bitel.ru/", "contractTariffOptionHistoryResponse");
    private final static QName _ContractTariffOptionReactivate_QNAME = new QName("http://service.common.option.tariff.kernel.bgbilling.bitel.ru/", "contractTariffOptionReactivate");
    private final static QName _ContractTariffOptionHistory_QNAME = new QName("http://service.common.option.tariff.kernel.bgbilling.bitel.ru/", "contractTariffOptionHistory");
    private final static QName _ContractTariffOptionList_QNAME = new QName("http://service.common.option.tariff.kernel.bgbilling.bitel.ru/", "contractTariffOptionList");
    private final static QName _TariffOptionListAvailable_QNAME = new QName("http://service.common.option.tariff.kernel.bgbilling.bitel.ru/", "tariffOptionListAvailable");
    private final static QName _TariffOptionActivateModeList_QNAME = new QName("http://service.common.option.tariff.kernel.bgbilling.bitel.ru/", "tariffOptionActivateModeList");
    private final static QName _ContractTariffOptionListWebResponse_QNAME = new QName("http://service.common.option.tariff.kernel.bgbilling.bitel.ru/", "contractTariffOptionListWebResponse");
    private final static QName _TariffOptionGetResponse_QNAME = new QName("http://service.common.option.tariff.kernel.bgbilling.bitel.ru/", "tariffOptionGetResponse");
    private final static QName _ContractTariffOptionDeactivateResponse_QNAME = new QName("http://service.common.option.tariff.kernel.bgbilling.bitel.ru/", "contractTariffOptionDeactivateResponse");
    private final static QName _TariffOptionActivateModeListResponse_QNAME = new QName("http://service.common.option.tariff.kernel.bgbilling.bitel.ru/", "tariffOptionActivateModeListResponse");
    private final static QName _TariffOptionUpdate_QNAME = new QName("http://service.common.option.tariff.kernel.bgbilling.bitel.ru/", "tariffOptionUpdate");
    private final static QName _TariffOptionDelete_QNAME = new QName("http://service.common.option.tariff.kernel.bgbilling.bitel.ru/", "tariffOptionDelete");
    private final static QName _TariffOptionWebListResponse_QNAME = new QName("http://service.common.option.tariff.kernel.bgbilling.bitel.ru/", "tariffOptionWebListResponse");
    private final static QName _ContractTariffOptionActivateWhithSum_QNAME = new QName("http://service.common.option.tariff.kernel.bgbilling.bitel.ru/", "contractTariffOptionActivateWhithSum");
    private final static QName _BGException_QNAME = new QName("http://service.common.option.tariff.kernel.bgbilling.bitel.ru/", "BGException");
    private final static QName _ContractTariffOptionListResponse_QNAME = new QName("http://service.common.option.tariff.kernel.bgbilling.bitel.ru/", "contractTariffOptionListResponse");
    private final static QName _TariffOptionWebList_QNAME = new QName("http://service.common.option.tariff.kernel.bgbilling.bitel.ru/", "tariffOptionWebList");
    private final static QName _ContractTariffOptionActivateWhithSumResponse_QNAME = new QName("http://service.common.option.tariff.kernel.bgbilling.bitel.ru/", "contractTariffOptionActivateWhithSumResponse");
    private final static QName _TariffOptionDeleteResponse_QNAME = new QName("http://service.common.option.tariff.kernel.bgbilling.bitel.ru/", "tariffOptionDeleteResponse");
    private final static QName _TariffOptionUpdateResponse_QNAME = new QName("http://service.common.option.tariff.kernel.bgbilling.bitel.ru/", "tariffOptionUpdateResponse");
    private final static QName _ContractTariffOptionListWeb_QNAME = new QName("http://service.common.option.tariff.kernel.bgbilling.bitel.ru/", "contractTariffOptionListWeb");
    private final static QName _TariffOptionList_QNAME = new QName("http://service.common.option.tariff.kernel.bgbilling.bitel.ru/", "tariffOptionList");
    private final static QName _ContractTariffOptionActivateResponse_QNAME = new QName("http://service.common.option.tariff.kernel.bgbilling.bitel.ru/", "contractTariffOptionActivateResponse");
    private final static QName _ContractTariffOptionActivate_QNAME = new QName("http://service.common.option.tariff.kernel.bgbilling.bitel.ru/", "contractTariffOptionActivate");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: ru.bgcrm.plugin.bgbilling.ws.tariff.option
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link TariffOption }
     * 
     */
    public TariffOption createTariffOption() {
        return new TariffOption();
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
     * Create an instance of {@link TariffOptionDeleteResponse }
     * 
     */
    public TariffOptionDeleteResponse createTariffOptionDeleteResponse() {
        return new TariffOptionDeleteResponse();
    }

    /**
     * Create an instance of {@link TariffOptionUpdateResponse }
     * 
     */
    public TariffOptionUpdateResponse createTariffOptionUpdateResponse() {
        return new TariffOptionUpdateResponse();
    }

    /**
     * Create an instance of {@link ContractTariffOptionListWeb }
     * 
     */
    public ContractTariffOptionListWeb createContractTariffOptionListWeb() {
        return new ContractTariffOptionListWeb();
    }

    /**
     * Create an instance of {@link ContractTariffOptionActivateResponse }
     * 
     */
    public ContractTariffOptionActivateResponse createContractTariffOptionActivateResponse() {
        return new ContractTariffOptionActivateResponse();
    }

    /**
     * Create an instance of {@link TariffOptionList }
     * 
     */
    public TariffOptionList createTariffOptionList() {
        return new TariffOptionList();
    }

    /**
     * Create an instance of {@link ContractTariffOptionActivate }
     * 
     */
    public ContractTariffOptionActivate createContractTariffOptionActivate() {
        return new ContractTariffOptionActivate();
    }

    /**
     * Create an instance of {@link TariffOptionWebListResponse }
     * 
     */
    public TariffOptionWebListResponse createTariffOptionWebListResponse() {
        return new TariffOptionWebListResponse();
    }

    /**
     * Create an instance of {@link TariffOptionDelete }
     * 
     */
    public TariffOptionDelete createTariffOptionDelete() {
        return new TariffOptionDelete();
    }

    /**
     * Create an instance of {@link ContractTariffOptionActivateWhithSum }
     * 
     */
    public ContractTariffOptionActivateWhithSum createContractTariffOptionActivateWhithSum() {
        return new ContractTariffOptionActivateWhithSum();
    }

    /**
     * Create an instance of {@link BGException }
     * 
     */
    public BGException createBGException() {
        return new BGException();
    }

    /**
     * Create an instance of {@link ContractTariffOptionListResponse }
     * 
     */
    public ContractTariffOptionListResponse createContractTariffOptionListResponse() {
        return new ContractTariffOptionListResponse();
    }

    /**
     * Create an instance of {@link TariffOptionWebList }
     * 
     */
    public TariffOptionWebList createTariffOptionWebList() {
        return new TariffOptionWebList();
    }

    /**
     * Create an instance of {@link ContractTariffOptionActivateWhithSumResponse }
     * 
     */
    public ContractTariffOptionActivateWhithSumResponse createContractTariffOptionActivateWhithSumResponse() {
        return new ContractTariffOptionActivateWhithSumResponse();
    }

    /**
     * Create an instance of {@link ContractTariffOptionHistory }
     * 
     */
    public ContractTariffOptionHistory createContractTariffOptionHistory() {
        return new ContractTariffOptionHistory();
    }

    /**
     * Create an instance of {@link ContractTariffOptionList }
     * 
     */
    public ContractTariffOptionList createContractTariffOptionList() {
        return new ContractTariffOptionList();
    }

    /**
     * Create an instance of {@link TariffOptionListAvailable }
     * 
     */
    public TariffOptionListAvailable createTariffOptionListAvailable() {
        return new TariffOptionListAvailable();
    }

    /**
     * Create an instance of {@link TariffOptionActivateModeList }
     * 
     */
    public TariffOptionActivateModeList createTariffOptionActivateModeList() {
        return new TariffOptionActivateModeList();
    }

    /**
     * Create an instance of {@link ContractTariffOptionListWebResponse }
     * 
     */
    public ContractTariffOptionListWebResponse createContractTariffOptionListWebResponse() {
        return new ContractTariffOptionListWebResponse();
    }

    /**
     * Create an instance of {@link TariffOptionGetResponse }
     * 
     */
    public TariffOptionGetResponse createTariffOptionGetResponse() {
        return new TariffOptionGetResponse();
    }

    /**
     * Create an instance of {@link ContractTariffOptionDeactivateResponse }
     * 
     */
    public ContractTariffOptionDeactivateResponse createContractTariffOptionDeactivateResponse() {
        return new ContractTariffOptionDeactivateResponse();
    }

    /**
     * Create an instance of {@link TariffOptionActivateModeListResponse }
     * 
     */
    public TariffOptionActivateModeListResponse createTariffOptionActivateModeListResponse() {
        return new TariffOptionActivateModeListResponse();
    }

    /**
     * Create an instance of {@link TariffOptionUpdate }
     * 
     */
    public TariffOptionUpdate createTariffOptionUpdate() {
        return new TariffOptionUpdate();
    }

    /**
     * Create an instance of {@link TariffOptionListAvailableResponse }
     * 
     */
    public TariffOptionListAvailableResponse createTariffOptionListAvailableResponse() {
        return new TariffOptionListAvailableResponse();
    }

    /**
     * Create an instance of {@link TariffOptionListResponse }
     * 
     */
    public TariffOptionListResponse createTariffOptionListResponse() {
        return new TariffOptionListResponse();
    }

    /**
     * Create an instance of {@link ContractTariffOptionDeactivate }
     * 
     */
    public ContractTariffOptionDeactivate createContractTariffOptionDeactivate() {
        return new ContractTariffOptionDeactivate();
    }

    /**
     * Create an instance of {@link TariffOptionGet }
     * 
     */
    public TariffOptionGet createTariffOptionGet() {
        return new TariffOptionGet();
    }

    /**
     * Create an instance of {@link ContractTariffOptionReactivateResponse }
     * 
     */
    public ContractTariffOptionReactivateResponse createContractTariffOptionReactivateResponse() {
        return new ContractTariffOptionReactivateResponse();
    }

    /**
     * Create an instance of {@link ContractTariffOptionHistoryResponse }
     * 
     */
    public ContractTariffOptionHistoryResponse createContractTariffOptionHistoryResponse() {
        return new ContractTariffOptionHistoryResponse();
    }

    /**
     * Create an instance of {@link ContractTariffOptionReactivate }
     * 
     */
    public ContractTariffOptionReactivate createContractTariffOptionReactivate() {
        return new ContractTariffOptionReactivate();
    }

    /**
     * Create an instance of {@link TariffOptionActivateMode }
     * 
     */
    public TariffOptionActivateMode createTariffOptionActivateMode() {
        return new TariffOptionActivateMode();
    }

    /**
     * Create an instance of {@link ContractTariffOption }
     * 
     */
    public ContractTariffOption createContractTariffOption() {
        return new ContractTariffOption();
    }

    /**
     * Create an instance of {@link TariffOption.ModeList }
     * 
     */
    public TariffOption.ModeList createTariffOptionModeList() {
        return new TariffOption.ModeList();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link TariffOptionListAvailableResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://service.common.option.tariff.kernel.bgbilling.bitel.ru/", name = "tariffOptionListAvailableResponse")
    public JAXBElement<TariffOptionListAvailableResponse> createTariffOptionListAvailableResponse(TariffOptionListAvailableResponse value) {
        return new JAXBElement<TariffOptionListAvailableResponse>(_TariffOptionListAvailableResponse_QNAME, TariffOptionListAvailableResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link TariffOptionListResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://service.common.option.tariff.kernel.bgbilling.bitel.ru/", name = "tariffOptionListResponse")
    public JAXBElement<TariffOptionListResponse> createTariffOptionListResponse(TariffOptionListResponse value) {
        return new JAXBElement<TariffOptionListResponse>(_TariffOptionListResponse_QNAME, TariffOptionListResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link TariffOptionGet }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://service.common.option.tariff.kernel.bgbilling.bitel.ru/", name = "tariffOptionGet")
    public JAXBElement<TariffOptionGet> createTariffOptionGet(TariffOptionGet value) {
        return new JAXBElement<TariffOptionGet>(_TariffOptionGet_QNAME, TariffOptionGet.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ContractTariffOptionDeactivate }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://service.common.option.tariff.kernel.bgbilling.bitel.ru/", name = "contractTariffOptionDeactivate")
    public JAXBElement<ContractTariffOptionDeactivate> createContractTariffOptionDeactivate(ContractTariffOptionDeactivate value) {
        return new JAXBElement<ContractTariffOptionDeactivate>(_ContractTariffOptionDeactivate_QNAME, ContractTariffOptionDeactivate.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ContractTariffOptionReactivateResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://service.common.option.tariff.kernel.bgbilling.bitel.ru/", name = "contractTariffOptionReactivateResponse")
    public JAXBElement<ContractTariffOptionReactivateResponse> createContractTariffOptionReactivateResponse(ContractTariffOptionReactivateResponse value) {
        return new JAXBElement<ContractTariffOptionReactivateResponse>(_ContractTariffOptionReactivateResponse_QNAME, ContractTariffOptionReactivateResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ContractTariffOptionHistoryResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://service.common.option.tariff.kernel.bgbilling.bitel.ru/", name = "contractTariffOptionHistoryResponse")
    public JAXBElement<ContractTariffOptionHistoryResponse> createContractTariffOptionHistoryResponse(ContractTariffOptionHistoryResponse value) {
        return new JAXBElement<ContractTariffOptionHistoryResponse>(_ContractTariffOptionHistoryResponse_QNAME, ContractTariffOptionHistoryResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ContractTariffOptionReactivate }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://service.common.option.tariff.kernel.bgbilling.bitel.ru/", name = "contractTariffOptionReactivate")
    public JAXBElement<ContractTariffOptionReactivate> createContractTariffOptionReactivate(ContractTariffOptionReactivate value) {
        return new JAXBElement<ContractTariffOptionReactivate>(_ContractTariffOptionReactivate_QNAME, ContractTariffOptionReactivate.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ContractTariffOptionHistory }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://service.common.option.tariff.kernel.bgbilling.bitel.ru/", name = "contractTariffOptionHistory")
    public JAXBElement<ContractTariffOptionHistory> createContractTariffOptionHistory(ContractTariffOptionHistory value) {
        return new JAXBElement<ContractTariffOptionHistory>(_ContractTariffOptionHistory_QNAME, ContractTariffOptionHistory.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ContractTariffOptionList }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://service.common.option.tariff.kernel.bgbilling.bitel.ru/", name = "contractTariffOptionList")
    public JAXBElement<ContractTariffOptionList> createContractTariffOptionList(ContractTariffOptionList value) {
        return new JAXBElement<ContractTariffOptionList>(_ContractTariffOptionList_QNAME, ContractTariffOptionList.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link TariffOptionListAvailable }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://service.common.option.tariff.kernel.bgbilling.bitel.ru/", name = "tariffOptionListAvailable")
    public JAXBElement<TariffOptionListAvailable> createTariffOptionListAvailable(TariffOptionListAvailable value) {
        return new JAXBElement<TariffOptionListAvailable>(_TariffOptionListAvailable_QNAME, TariffOptionListAvailable.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link TariffOptionActivateModeList }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://service.common.option.tariff.kernel.bgbilling.bitel.ru/", name = "tariffOptionActivateModeList")
    public JAXBElement<TariffOptionActivateModeList> createTariffOptionActivateModeList(TariffOptionActivateModeList value) {
        return new JAXBElement<TariffOptionActivateModeList>(_TariffOptionActivateModeList_QNAME, TariffOptionActivateModeList.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ContractTariffOptionListWebResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://service.common.option.tariff.kernel.bgbilling.bitel.ru/", name = "contractTariffOptionListWebResponse")
    public JAXBElement<ContractTariffOptionListWebResponse> createContractTariffOptionListWebResponse(ContractTariffOptionListWebResponse value) {
        return new JAXBElement<ContractTariffOptionListWebResponse>(_ContractTariffOptionListWebResponse_QNAME, ContractTariffOptionListWebResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link TariffOptionGetResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://service.common.option.tariff.kernel.bgbilling.bitel.ru/", name = "tariffOptionGetResponse")
    public JAXBElement<TariffOptionGetResponse> createTariffOptionGetResponse(TariffOptionGetResponse value) {
        return new JAXBElement<TariffOptionGetResponse>(_TariffOptionGetResponse_QNAME, TariffOptionGetResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ContractTariffOptionDeactivateResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://service.common.option.tariff.kernel.bgbilling.bitel.ru/", name = "contractTariffOptionDeactivateResponse")
    public JAXBElement<ContractTariffOptionDeactivateResponse> createContractTariffOptionDeactivateResponse(ContractTariffOptionDeactivateResponse value) {
        return new JAXBElement<ContractTariffOptionDeactivateResponse>(_ContractTariffOptionDeactivateResponse_QNAME, ContractTariffOptionDeactivateResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link TariffOptionActivateModeListResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://service.common.option.tariff.kernel.bgbilling.bitel.ru/", name = "tariffOptionActivateModeListResponse")
    public JAXBElement<TariffOptionActivateModeListResponse> createTariffOptionActivateModeListResponse(TariffOptionActivateModeListResponse value) {
        return new JAXBElement<TariffOptionActivateModeListResponse>(_TariffOptionActivateModeListResponse_QNAME, TariffOptionActivateModeListResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link TariffOptionUpdate }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://service.common.option.tariff.kernel.bgbilling.bitel.ru/", name = "tariffOptionUpdate")
    public JAXBElement<TariffOptionUpdate> createTariffOptionUpdate(TariffOptionUpdate value) {
        return new JAXBElement<TariffOptionUpdate>(_TariffOptionUpdate_QNAME, TariffOptionUpdate.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link TariffOptionDelete }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://service.common.option.tariff.kernel.bgbilling.bitel.ru/", name = "tariffOptionDelete")
    public JAXBElement<TariffOptionDelete> createTariffOptionDelete(TariffOptionDelete value) {
        return new JAXBElement<TariffOptionDelete>(_TariffOptionDelete_QNAME, TariffOptionDelete.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link TariffOptionWebListResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://service.common.option.tariff.kernel.bgbilling.bitel.ru/", name = "tariffOptionWebListResponse")
    public JAXBElement<TariffOptionWebListResponse> createTariffOptionWebListResponse(TariffOptionWebListResponse value) {
        return new JAXBElement<TariffOptionWebListResponse>(_TariffOptionWebListResponse_QNAME, TariffOptionWebListResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ContractTariffOptionActivateWhithSum }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://service.common.option.tariff.kernel.bgbilling.bitel.ru/", name = "contractTariffOptionActivateWhithSum")
    public JAXBElement<ContractTariffOptionActivateWhithSum> createContractTariffOptionActivateWhithSum(ContractTariffOptionActivateWhithSum value) {
        return new JAXBElement<ContractTariffOptionActivateWhithSum>(_ContractTariffOptionActivateWhithSum_QNAME, ContractTariffOptionActivateWhithSum.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link BGException }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://service.common.option.tariff.kernel.bgbilling.bitel.ru/", name = "BGException")
    public JAXBElement<BGException> createBGException(BGException value) {
        return new JAXBElement<BGException>(_BGException_QNAME, BGException.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ContractTariffOptionListResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://service.common.option.tariff.kernel.bgbilling.bitel.ru/", name = "contractTariffOptionListResponse")
    public JAXBElement<ContractTariffOptionListResponse> createContractTariffOptionListResponse(ContractTariffOptionListResponse value) {
        return new JAXBElement<ContractTariffOptionListResponse>(_ContractTariffOptionListResponse_QNAME, ContractTariffOptionListResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link TariffOptionWebList }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://service.common.option.tariff.kernel.bgbilling.bitel.ru/", name = "tariffOptionWebList")
    public JAXBElement<TariffOptionWebList> createTariffOptionWebList(TariffOptionWebList value) {
        return new JAXBElement<TariffOptionWebList>(_TariffOptionWebList_QNAME, TariffOptionWebList.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ContractTariffOptionActivateWhithSumResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://service.common.option.tariff.kernel.bgbilling.bitel.ru/", name = "contractTariffOptionActivateWhithSumResponse")
    public JAXBElement<ContractTariffOptionActivateWhithSumResponse> createContractTariffOptionActivateWhithSumResponse(ContractTariffOptionActivateWhithSumResponse value) {
        return new JAXBElement<ContractTariffOptionActivateWhithSumResponse>(_ContractTariffOptionActivateWhithSumResponse_QNAME, ContractTariffOptionActivateWhithSumResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link TariffOptionDeleteResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://service.common.option.tariff.kernel.bgbilling.bitel.ru/", name = "tariffOptionDeleteResponse")
    public JAXBElement<TariffOptionDeleteResponse> createTariffOptionDeleteResponse(TariffOptionDeleteResponse value) {
        return new JAXBElement<TariffOptionDeleteResponse>(_TariffOptionDeleteResponse_QNAME, TariffOptionDeleteResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link TariffOptionUpdateResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://service.common.option.tariff.kernel.bgbilling.bitel.ru/", name = "tariffOptionUpdateResponse")
    public JAXBElement<TariffOptionUpdateResponse> createTariffOptionUpdateResponse(TariffOptionUpdateResponse value) {
        return new JAXBElement<TariffOptionUpdateResponse>(_TariffOptionUpdateResponse_QNAME, TariffOptionUpdateResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ContractTariffOptionListWeb }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://service.common.option.tariff.kernel.bgbilling.bitel.ru/", name = "contractTariffOptionListWeb")
    public JAXBElement<ContractTariffOptionListWeb> createContractTariffOptionListWeb(ContractTariffOptionListWeb value) {
        return new JAXBElement<ContractTariffOptionListWeb>(_ContractTariffOptionListWeb_QNAME, ContractTariffOptionListWeb.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link TariffOptionList }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://service.common.option.tariff.kernel.bgbilling.bitel.ru/", name = "tariffOptionList")
    public JAXBElement<TariffOptionList> createTariffOptionList(TariffOptionList value) {
        return new JAXBElement<TariffOptionList>(_TariffOptionList_QNAME, TariffOptionList.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ContractTariffOptionActivateResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://service.common.option.tariff.kernel.bgbilling.bitel.ru/", name = "contractTariffOptionActivateResponse")
    public JAXBElement<ContractTariffOptionActivateResponse> createContractTariffOptionActivateResponse(ContractTariffOptionActivateResponse value) {
        return new JAXBElement<ContractTariffOptionActivateResponse>(_ContractTariffOptionActivateResponse_QNAME, ContractTariffOptionActivateResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ContractTariffOptionActivate }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://service.common.option.tariff.kernel.bgbilling.bitel.ru/", name = "contractTariffOptionActivate")
    public JAXBElement<ContractTariffOptionActivate> createContractTariffOptionActivate(ContractTariffOptionActivate value) {
        return new JAXBElement<ContractTariffOptionActivate>(_ContractTariffOptionActivate_QNAME, ContractTariffOptionActivate.class, null, value);
    }

}
