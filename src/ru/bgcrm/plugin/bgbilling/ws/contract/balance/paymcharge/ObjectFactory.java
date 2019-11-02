
package ru.bgcrm.plugin.bgbilling.ws.contract.balance.paymcharge;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the ru.bgcrm.plugin.bgbilling.ws.contract.balance.paymcharge package. 
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

    private final static QName _GetChargeTree_QNAME = new QName("http://common.balance.contract.kernel.bgbilling.bitel.ru/", "getChargeTree");
    private final static QName _GetChargeTreeResponse_QNAME = new QName("http://common.balance.contract.kernel.bgbilling.bitel.ru/", "getChargeTreeResponse");
    private final static QName _PaymentListResponse_QNAME = new QName("http://common.balance.contract.kernel.bgbilling.bitel.ru/", "paymentListResponse");
    private final static QName _ChargeListResponse_QNAME = new QName("http://common.balance.contract.kernel.bgbilling.bitel.ru/", "chargeListResponse");
    private final static QName _BGException_QNAME = new QName("http://common.balance.contract.kernel.bgbilling.bitel.ru/", "BGException");
    private final static QName _ChargeList_QNAME = new QName("http://common.balance.contract.kernel.bgbilling.bitel.ru/", "chargeList");
    private final static QName _GetPaymentTree_QNAME = new QName("http://common.balance.contract.kernel.bgbilling.bitel.ru/", "getPaymentTree");
    private final static QName _GetPaymentTreeResponse_QNAME = new QName("http://common.balance.contract.kernel.bgbilling.bitel.ru/", "getPaymentTreeResponse");
    private final static QName _PaymentList_QNAME = new QName("http://common.balance.contract.kernel.bgbilling.bitel.ru/", "paymentList");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: ru.bgcrm.plugin.bgbilling.ws.contract.balance.paymcharge
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link ChargeListResponse }
     * 
     */
    public ChargeListResponse createChargeListResponse() {
        return new ChargeListResponse();
    }

    /**
     * Create an instance of {@link PaymentListResponse }
     * 
     */
    public PaymentListResponse createPaymentListResponse() {
        return new PaymentListResponse();
    }

    /**
     * Create an instance of {@link GetChargeTreeResponse }
     * 
     */
    public GetChargeTreeResponse createGetChargeTreeResponse() {
        return new GetChargeTreeResponse();
    }

    /**
     * Create an instance of {@link GetChargeTree }
     * 
     */
    public GetChargeTree createGetChargeTree() {
        return new GetChargeTree();
    }

    /**
     * Create an instance of {@link GetPaymentTreeResponse }
     * 
     */
    public GetPaymentTreeResponse createGetPaymentTreeResponse() {
        return new GetPaymentTreeResponse();
    }

    /**
     * Create an instance of {@link GetPaymentTree }
     * 
     */
    public GetPaymentTree createGetPaymentTree() {
        return new GetPaymentTree();
    }

    /**
     * Create an instance of {@link ChargeList }
     * 
     */
    public ChargeList createChargeList() {
        return new ChargeList();
    }

    /**
     * Create an instance of {@link BGException }
     * 
     */
    public BGException createBGException() {
        return new BGException();
    }

    /**
     * Create an instance of {@link PaymentList }
     * 
     */
    public PaymentList createPaymentList() {
        return new PaymentList();
    }

    /**
     * Create an instance of {@link AbstractPaymentTypes }
     * 
     */
    public AbstractPaymentTypes createAbstractPaymentTypes() {
        return new AbstractPaymentTypes();
    }

    /**
     * Create an instance of {@link PaymentType }
     * 
     */
    public PaymentType createPaymentType() {
        return new PaymentType();
    }

    /**
     * Create an instance of {@link PaymentTypeItem }
     * 
     */
    public PaymentTypeItem createPaymentTypeItem() {
        return new PaymentTypeItem();
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
     * Create an instance of {@link JAXBElement }{@code <}{@link GetChargeTree }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://common.balance.contract.kernel.bgbilling.bitel.ru/", name = "getChargeTree")
    public JAXBElement<GetChargeTree> createGetChargeTree(GetChargeTree value) {
        return new JAXBElement<GetChargeTree>(_GetChargeTree_QNAME, GetChargeTree.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetChargeTreeResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://common.balance.contract.kernel.bgbilling.bitel.ru/", name = "getChargeTreeResponse")
    public JAXBElement<GetChargeTreeResponse> createGetChargeTreeResponse(GetChargeTreeResponse value) {
        return new JAXBElement<GetChargeTreeResponse>(_GetChargeTreeResponse_QNAME, GetChargeTreeResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link PaymentListResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://common.balance.contract.kernel.bgbilling.bitel.ru/", name = "paymentListResponse")
    public JAXBElement<PaymentListResponse> createPaymentListResponse(PaymentListResponse value) {
        return new JAXBElement<PaymentListResponse>(_PaymentListResponse_QNAME, PaymentListResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ChargeListResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://common.balance.contract.kernel.bgbilling.bitel.ru/", name = "chargeListResponse")
    public JAXBElement<ChargeListResponse> createChargeListResponse(ChargeListResponse value) {
        return new JAXBElement<ChargeListResponse>(_ChargeListResponse_QNAME, ChargeListResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link BGException }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://common.balance.contract.kernel.bgbilling.bitel.ru/", name = "BGException")
    public JAXBElement<BGException> createBGException(BGException value) {
        return new JAXBElement<BGException>(_BGException_QNAME, BGException.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ChargeList }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://common.balance.contract.kernel.bgbilling.bitel.ru/", name = "chargeList")
    public JAXBElement<ChargeList> createChargeList(ChargeList value) {
        return new JAXBElement<ChargeList>(_ChargeList_QNAME, ChargeList.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetPaymentTree }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://common.balance.contract.kernel.bgbilling.bitel.ru/", name = "getPaymentTree")
    public JAXBElement<GetPaymentTree> createGetPaymentTree(GetPaymentTree value) {
        return new JAXBElement<GetPaymentTree>(_GetPaymentTree_QNAME, GetPaymentTree.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetPaymentTreeResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://common.balance.contract.kernel.bgbilling.bitel.ru/", name = "getPaymentTreeResponse")
    public JAXBElement<GetPaymentTreeResponse> createGetPaymentTreeResponse(GetPaymentTreeResponse value) {
        return new JAXBElement<GetPaymentTreeResponse>(_GetPaymentTreeResponse_QNAME, GetPaymentTreeResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link PaymentList }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://common.balance.contract.kernel.bgbilling.bitel.ru/", name = "paymentList")
    public JAXBElement<PaymentList> createPaymentList(PaymentList value) {
        return new JAXBElement<PaymentList>(_PaymentList_QNAME, PaymentList.class, null, value);
    }

}
