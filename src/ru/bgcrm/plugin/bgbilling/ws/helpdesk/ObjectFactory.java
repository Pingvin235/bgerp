
package ru.bgcrm.plugin.bgbilling.ws.helpdesk;

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

    private final static QName _FileDownloadResponse_QNAME = new QName("http://service.common.helpdesk.plugins.bgbilling.bitel.ru/", "fileDownloadResponse");
    private final static QName _BGException_QNAME = new QName("http://service.common.helpdesk.plugins.bgbilling.bitel.ru/", "BGException");
    private final static QName _FileDownload_QNAME = new QName("http://service.common.helpdesk.plugins.bgbilling.bitel.ru/", "fileDownload");
    private final static QName _TopicCostUpdateResponse_QNAME = new QName("http://service.common.helpdesk.plugins.bgbilling.bitel.ru/", "topicCostUpdateResponse");
    private final static QName _FileDelete_QNAME = new QName("http://service.common.helpdesk.plugins.bgbilling.bitel.ru/", "fileDelete");
    private final static QName _BGMessageException_QNAME = new QName("http://service.common.helpdesk.plugins.bgbilling.bitel.ru/", "BGMessageException");
    private final static QName _FileDeleteResponse_QNAME = new QName("http://service.common.helpdesk.plugins.bgbilling.bitel.ru/", "fileDeleteResponse");
    private final static QName _TopicCostUpdate_QNAME = new QName("http://service.common.helpdesk.plugins.bgbilling.bitel.ru/", "topicCostUpdate");
    private final static QName _ReserveStatusGet_QNAME = new QName("http://service.common.helpdesk.plugins.bgbilling.bitel.ru/", "reserveStatusGet");
    private final static QName _ReservStatusUpdateResponse_QNAME = new QName("http://service.common.helpdesk.plugins.bgbilling.bitel.ru/", "reservStatusUpdateResponse");
    private final static QName _ReservStatusUpdate_QNAME = new QName("http://service.common.helpdesk.plugins.bgbilling.bitel.ru/", "reservStatusUpdate");
    private final static QName _FileUpload_QNAME = new QName("http://service.common.helpdesk.plugins.bgbilling.bitel.ru/", "fileUpload");
    private final static QName _FileUploadResponse_QNAME = new QName("http://service.common.helpdesk.plugins.bgbilling.bitel.ru/", "fileUploadResponse");
    private final static QName _ReserveStatusGetResponse_QNAME = new QName("http://service.common.helpdesk.plugins.bgbilling.bitel.ru/", "reserveStatusGetResponse");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: ru.bgcrm.plugin.bgbilling.ws.helpdesk
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link FileUploadResponse }
     * 
     */
    public FileUploadResponse createFileUploadResponse() {
        return new FileUploadResponse();
    }

    /**
     * Create an instance of {@link FileDownload }
     * 
     */
    public FileDownload createFileDownload() {
        return new FileDownload();
    }

    /**
     * Create an instance of {@link ReserveStatusGetResponse }
     * 
     */
    public ReserveStatusGetResponse createReserveStatusGetResponse() {
        return new ReserveStatusGetResponse();
    }

    /**
     * Create an instance of {@link TopicCostUpdateResponse }
     * 
     */
    public TopicCostUpdateResponse createTopicCostUpdateResponse() {
        return new TopicCostUpdateResponse();
    }

    /**
     * Create an instance of {@link ReservStatusUpdate }
     * 
     */
    public ReservStatusUpdate createReservStatusUpdate() {
        return new ReservStatusUpdate();
    }

    /**
     * Create an instance of {@link FileUpload }
     * 
     */
    public FileUpload createFileUpload() {
        return new FileUpload();
    }

    /**
     * Create an instance of {@link BGException }
     * 
     */
    public BGException createBGException() {
        return new BGException();
    }

    /**
     * Create an instance of {@link FileDownloadResponse }
     * 
     */
    public FileDownloadResponse createFileDownloadResponse() {
        return new FileDownloadResponse();
    }

    /**
     * Create an instance of {@link TopicCostUpdate }
     * 
     */
    public TopicCostUpdate createTopicCostUpdate() {
        return new TopicCostUpdate();
    }

    /**
     * Create an instance of {@link ReserveStatusGet }
     * 
     */
    public ReserveStatusGet createReserveStatusGet() {
        return new ReserveStatusGet();
    }

    /**
     * Create an instance of {@link ReservStatusUpdateResponse }
     * 
     */
    public ReservStatusUpdateResponse createReservStatusUpdateResponse() {
        return new ReservStatusUpdateResponse();
    }

    /**
     * Create an instance of {@link FileDelete }
     * 
     */
    public FileDelete createFileDelete() {
        return new FileDelete();
    }

    /**
     * Create an instance of {@link FileDeleteResponse }
     * 
     */
    public FileDeleteResponse createFileDeleteResponse() {
        return new FileDeleteResponse();
    }

    /**
     * Create an instance of {@link BGMessageException }
     * 
     */
    public BGMessageException createBGMessageException() {
        return new BGMessageException();
    }

    /**
     * Create an instance of {@link BgServerFile }
     * 
     */
    public BgServerFile createBgServerFile() {
        return new BgServerFile();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link FileDownloadResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://service.common.helpdesk.plugins.bgbilling.bitel.ru/", name = "fileDownloadResponse")
    public JAXBElement<FileDownloadResponse> createFileDownloadResponse(FileDownloadResponse value) {
        return new JAXBElement<FileDownloadResponse>(_FileDownloadResponse_QNAME, FileDownloadResponse.class, null, value);
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
     * Create an instance of {@link JAXBElement }{@code <}{@link FileDownload }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://service.common.helpdesk.plugins.bgbilling.bitel.ru/", name = "fileDownload")
    public JAXBElement<FileDownload> createFileDownload(FileDownload value) {
        return new JAXBElement<FileDownload>(_FileDownload_QNAME, FileDownload.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link TopicCostUpdateResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://service.common.helpdesk.plugins.bgbilling.bitel.ru/", name = "topicCostUpdateResponse")
    public JAXBElement<TopicCostUpdateResponse> createTopicCostUpdateResponse(TopicCostUpdateResponse value) {
        return new JAXBElement<TopicCostUpdateResponse>(_TopicCostUpdateResponse_QNAME, TopicCostUpdateResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link FileDelete }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://service.common.helpdesk.plugins.bgbilling.bitel.ru/", name = "fileDelete")
    public JAXBElement<FileDelete> createFileDelete(FileDelete value) {
        return new JAXBElement<FileDelete>(_FileDelete_QNAME, FileDelete.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link BGMessageException }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://service.common.helpdesk.plugins.bgbilling.bitel.ru/", name = "BGMessageException")
    public JAXBElement<BGMessageException> createBGMessageException(BGMessageException value) {
        return new JAXBElement<BGMessageException>(_BGMessageException_QNAME, BGMessageException.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link FileDeleteResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://service.common.helpdesk.plugins.bgbilling.bitel.ru/", name = "fileDeleteResponse")
    public JAXBElement<FileDeleteResponse> createFileDeleteResponse(FileDeleteResponse value) {
        return new JAXBElement<FileDeleteResponse>(_FileDeleteResponse_QNAME, FileDeleteResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link TopicCostUpdate }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://service.common.helpdesk.plugins.bgbilling.bitel.ru/", name = "topicCostUpdate")
    public JAXBElement<TopicCostUpdate> createTopicCostUpdate(TopicCostUpdate value) {
        return new JAXBElement<TopicCostUpdate>(_TopicCostUpdate_QNAME, TopicCostUpdate.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ReserveStatusGet }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://service.common.helpdesk.plugins.bgbilling.bitel.ru/", name = "reserveStatusGet")
    public JAXBElement<ReserveStatusGet> createReserveStatusGet(ReserveStatusGet value) {
        return new JAXBElement<ReserveStatusGet>(_ReserveStatusGet_QNAME, ReserveStatusGet.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ReservStatusUpdateResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://service.common.helpdesk.plugins.bgbilling.bitel.ru/", name = "reservStatusUpdateResponse")
    public JAXBElement<ReservStatusUpdateResponse> createReservStatusUpdateResponse(ReservStatusUpdateResponse value) {
        return new JAXBElement<ReservStatusUpdateResponse>(_ReservStatusUpdateResponse_QNAME, ReservStatusUpdateResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ReservStatusUpdate }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://service.common.helpdesk.plugins.bgbilling.bitel.ru/", name = "reservStatusUpdate")
    public JAXBElement<ReservStatusUpdate> createReservStatusUpdate(ReservStatusUpdate value) {
        return new JAXBElement<ReservStatusUpdate>(_ReservStatusUpdate_QNAME, ReservStatusUpdate.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link FileUpload }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://service.common.helpdesk.plugins.bgbilling.bitel.ru/", name = "fileUpload")
    public JAXBElement<FileUpload> createFileUpload(FileUpload value) {
        return new JAXBElement<FileUpload>(_FileUpload_QNAME, FileUpload.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link FileUploadResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://service.common.helpdesk.plugins.bgbilling.bitel.ru/", name = "fileUploadResponse")
    public JAXBElement<FileUploadResponse> createFileUploadResponse(FileUploadResponse value) {
        return new JAXBElement<FileUploadResponse>(_FileUploadResponse_QNAME, FileUploadResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ReserveStatusGetResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://service.common.helpdesk.plugins.bgbilling.bitel.ru/", name = "reserveStatusGetResponse")
    public JAXBElement<ReserveStatusGetResponse> createReserveStatusGetResponse(ReserveStatusGetResponse value) {
        return new JAXBElement<ReserveStatusGetResponse>(_ReserveStatusGetResponse_QNAME, ReserveStatusGetResponse.class, null, value);
    }

}
