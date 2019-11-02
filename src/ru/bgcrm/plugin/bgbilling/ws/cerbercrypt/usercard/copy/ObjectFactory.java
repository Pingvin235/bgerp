
package ru.bgcrm.plugin.bgbilling.ws.cerbercrypt.usercard.copy;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the ru.bgcrm.plugin.bgbilling.ws.cerbercrypt.usercard.copy package. 
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

    private final static QName _UpdateUserCardCopyResponse_QNAME = new QName("http://common.cerbercrypt.modules.bgbilling.bitel.ru/", "updateUserCardCopyResponse");
    private final static QName _GetUserCardCopyListByDateResponse_QNAME = new QName("http://common.cerbercrypt.modules.bgbilling.bitel.ru/", "getUserCardCopyListByDateResponse");
    private final static QName _GetUserCardCopyList_QNAME = new QName("http://common.cerbercrypt.modules.bgbilling.bitel.ru/", "getUserCardCopyList");
    private final static QName _DeleteUserCardCopy_QNAME = new QName("http://common.cerbercrypt.modules.bgbilling.bitel.ru/", "deleteUserCardCopy");
    private final static QName _GetUserCardCopyListResponse_QNAME = new QName("http://common.cerbercrypt.modules.bgbilling.bitel.ru/", "getUserCardCopyListResponse");
    private final static QName _LogList_QNAME = new QName("http://common.cerbercrypt.modules.bgbilling.bitel.ru/", "logList");
    private final static QName _DeleteUserCardCopyResponse_QNAME = new QName("http://common.cerbercrypt.modules.bgbilling.bitel.ru/", "deleteUserCardCopyResponse");
    private final static QName _UpdateUserCardCopy_QNAME = new QName("http://common.cerbercrypt.modules.bgbilling.bitel.ru/", "updateUserCardCopy");
    private final static QName _BGException_QNAME = new QName("http://common.cerbercrypt.modules.bgbilling.bitel.ru/", "BGException");
    private final static QName _GetUserCardCopyListByDate_QNAME = new QName("http://common.cerbercrypt.modules.bgbilling.bitel.ru/", "getUserCardCopyListByDate");
    private final static QName _LogListResponse_QNAME = new QName("http://common.cerbercrypt.modules.bgbilling.bitel.ru/", "logListResponse");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: ru.bgcrm.plugin.bgbilling.ws.cerbercrypt.usercard.copy
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link Id }
     * 
     */
    public Id createId() {
        return new Id();
    }

    /**
     * Create an instance of {@link LogEntry }
     * 
     */
    public LogEntry createLogEntry() {
        return new LogEntry();
    }

    /**
     * Create an instance of {@link GetUserCardCopyListByDate }
     * 
     */
    public GetUserCardCopyListByDate createGetUserCardCopyListByDate() {
        return new GetUserCardCopyListByDate();
    }

    /**
     * Create an instance of {@link LogListResponse }
     * 
     */
    public LogListResponse createLogListResponse() {
        return new LogListResponse();
    }

    /**
     * Create an instance of {@link LogList }
     * 
     */
    public LogList createLogList() {
        return new LogList();
    }

    /**
     * Create an instance of {@link DeleteUserCardCopyResponse }
     * 
     */
    public DeleteUserCardCopyResponse createDeleteUserCardCopyResponse() {
        return new DeleteUserCardCopyResponse();
    }

    /**
     * Create an instance of {@link UpdateUserCardCopy }
     * 
     */
    public UpdateUserCardCopy createUpdateUserCardCopy() {
        return new UpdateUserCardCopy();
    }

    /**
     * Create an instance of {@link BGException }
     * 
     */
    public BGException createBGException() {
        return new BGException();
    }

    /**
     * Create an instance of {@link GetUserCardCopyList }
     * 
     */
    public GetUserCardCopyList createGetUserCardCopyList() {
        return new GetUserCardCopyList();
    }

    /**
     * Create an instance of {@link GetUserCardCopyListResponse }
     * 
     */
    public GetUserCardCopyListResponse createGetUserCardCopyListResponse() {
        return new GetUserCardCopyListResponse();
    }

    /**
     * Create an instance of {@link DeleteUserCardCopy }
     * 
     */
    public DeleteUserCardCopy createDeleteUserCardCopy() {
        return new DeleteUserCardCopy();
    }

    /**
     * Create an instance of {@link UpdateUserCardCopyResponse }
     * 
     */
    public UpdateUserCardCopyResponse createUpdateUserCardCopyResponse() {
        return new UpdateUserCardCopyResponse();
    }

    /**
     * Create an instance of {@link GetUserCardCopyListByDateResponse }
     * 
     */
    public GetUserCardCopyListByDateResponse createGetUserCardCopyListByDateResponse() {
        return new GetUserCardCopyListByDateResponse();
    }

    /**
     * Create an instance of {@link UserCard }
     * 
     */
    public UserCard createUserCard() {
        return new UserCard();
    }

    /**
     * Create an instance of {@link UserCardCopy }
     * 
     */
    public UserCardCopy createUserCardCopy() {
        return new UserCardCopy();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link UpdateUserCardCopyResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://common.cerbercrypt.modules.bgbilling.bitel.ru/", name = "updateUserCardCopyResponse")
    public JAXBElement<UpdateUserCardCopyResponse> createUpdateUserCardCopyResponse(UpdateUserCardCopyResponse value) {
        return new JAXBElement<UpdateUserCardCopyResponse>(_UpdateUserCardCopyResponse_QNAME, UpdateUserCardCopyResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetUserCardCopyListByDateResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://common.cerbercrypt.modules.bgbilling.bitel.ru/", name = "getUserCardCopyListByDateResponse")
    public JAXBElement<GetUserCardCopyListByDateResponse> createGetUserCardCopyListByDateResponse(GetUserCardCopyListByDateResponse value) {
        return new JAXBElement<GetUserCardCopyListByDateResponse>(_GetUserCardCopyListByDateResponse_QNAME, GetUserCardCopyListByDateResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetUserCardCopyList }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://common.cerbercrypt.modules.bgbilling.bitel.ru/", name = "getUserCardCopyList")
    public JAXBElement<GetUserCardCopyList> createGetUserCardCopyList(GetUserCardCopyList value) {
        return new JAXBElement<GetUserCardCopyList>(_GetUserCardCopyList_QNAME, GetUserCardCopyList.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link DeleteUserCardCopy }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://common.cerbercrypt.modules.bgbilling.bitel.ru/", name = "deleteUserCardCopy")
    public JAXBElement<DeleteUserCardCopy> createDeleteUserCardCopy(DeleteUserCardCopy value) {
        return new JAXBElement<DeleteUserCardCopy>(_DeleteUserCardCopy_QNAME, DeleteUserCardCopy.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetUserCardCopyListResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://common.cerbercrypt.modules.bgbilling.bitel.ru/", name = "getUserCardCopyListResponse")
    public JAXBElement<GetUserCardCopyListResponse> createGetUserCardCopyListResponse(GetUserCardCopyListResponse value) {
        return new JAXBElement<GetUserCardCopyListResponse>(_GetUserCardCopyListResponse_QNAME, GetUserCardCopyListResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link LogList }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://common.cerbercrypt.modules.bgbilling.bitel.ru/", name = "logList")
    public JAXBElement<LogList> createLogList(LogList value) {
        return new JAXBElement<LogList>(_LogList_QNAME, LogList.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link DeleteUserCardCopyResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://common.cerbercrypt.modules.bgbilling.bitel.ru/", name = "deleteUserCardCopyResponse")
    public JAXBElement<DeleteUserCardCopyResponse> createDeleteUserCardCopyResponse(DeleteUserCardCopyResponse value) {
        return new JAXBElement<DeleteUserCardCopyResponse>(_DeleteUserCardCopyResponse_QNAME, DeleteUserCardCopyResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link UpdateUserCardCopy }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://common.cerbercrypt.modules.bgbilling.bitel.ru/", name = "updateUserCardCopy")
    public JAXBElement<UpdateUserCardCopy> createUpdateUserCardCopy(UpdateUserCardCopy value) {
        return new JAXBElement<UpdateUserCardCopy>(_UpdateUserCardCopy_QNAME, UpdateUserCardCopy.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link BGException }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://common.cerbercrypt.modules.bgbilling.bitel.ru/", name = "BGException")
    public JAXBElement<BGException> createBGException(BGException value) {
        return new JAXBElement<BGException>(_BGException_QNAME, BGException.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetUserCardCopyListByDate }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://common.cerbercrypt.modules.bgbilling.bitel.ru/", name = "getUserCardCopyListByDate")
    public JAXBElement<GetUserCardCopyListByDate> createGetUserCardCopyListByDate(GetUserCardCopyListByDate value) {
        return new JAXBElement<GetUserCardCopyListByDate>(_GetUserCardCopyListByDate_QNAME, GetUserCardCopyListByDate.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link LogListResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://common.cerbercrypt.modules.bgbilling.bitel.ru/", name = "logListResponse")
    public JAXBElement<LogListResponse> createLogListResponse(LogListResponse value) {
        return new JAXBElement<LogListResponse>(_LogListResponse_QNAME, LogListResponse.class, null, value);
    }

}
