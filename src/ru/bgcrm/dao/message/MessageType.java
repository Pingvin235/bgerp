package ru.bgcrm.dao.message;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.sql.Connection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import ru.bgcrm.dao.FileDataDAO;
import ru.bgcrm.dynamic.DynamicClassManager;
import ru.bgcrm.model.BGException;
import ru.bgcrm.model.CommonObjectLink;
import ru.bgcrm.model.FileData;
import ru.bgcrm.model.IdTitle;
import ru.bgcrm.model.message.Message;
import ru.bgcrm.struts.action.FileAction.FileInfo;
import ru.bgcrm.struts.action.FileAction.SessionTemporaryFiles;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.ParameterMap;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.sql.ConnectionSet;

public abstract class MessageType extends IdTitle {
    private static final Logger log = Logger.getLogger(MessageType.class);

    private final LinkedHashMap<Integer, MessageTypeSearch> searchMap = new LinkedHashMap<Integer, MessageTypeSearch>();
    private MessageTypeContactSaver contactSaver;
    protected final ParameterMap configMap;
    // количество необработанных сообщений, null - если неизвестно
    protected volatile Integer unprocessedMessagesCount; 

    protected MessageType(int id, String title, ParameterMap config) throws BGException {
        this.configMap = config;

        this.id = id;
        this.title = title;
        if (Utils.isBlankString(title)) {
            throw new BGException("Title of message type is empty.");
        }

        for (Map.Entry<Integer, ParameterMap> me : config.subIndexed("search.").entrySet()) {
            int searchId = me.getKey();
            ParameterMap searchConf = me.getValue();

            MessageTypeSearch search = null;

            String className = searchConf.get("class");
            if (Utils.notBlankString(className)) {
                try {
                    Class<?> clazz = DynamicClassManager.getClass(className);
                    if (MessageTypeSearch.class.isAssignableFrom(clazz)) {
                        Constructor<?> constr = clazz.getConstructor(ParameterMap.class);
                        search = (MessageTypeSearch) constr.newInstance(searchConf);

                        searchMap.put(searchId, search);
                    }
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
        }

        ParameterMap saver = config.sub("saver.");
        String className = saver.get("class");

        if (Utils.notBlankString(className)) {
            try {
                Class<?> clazz = DynamicClassManager.getClass(className);
                if (MessageTypeContactSaver.class.isAssignableFrom(clazz)) {
                    Constructor<?> constr = clazz.getConstructor(ParameterMap.class);
                    contactSaver = (MessageTypeContactSaver) constr.newInstance(saver);
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    public ParameterMap getConfigMap() {
        return configMap;
    }

    public Map<Integer, MessageTypeSearch> getSearchMap() {
        return searchMap;
    }

    public MessageTypeContactSaver getContactSaver() {
        return contactSaver;
    }
    
    public Integer getUnprocessedMessagesCount() {
        return configMap.getBoolean("unprocessedMessageNotify", true) ?
                unprocessedMessagesCount :
                    null;
    }

    // извлечение и отправка сообщений
    public void process() {}

    public boolean isAnswerSupport() {
        return false;
    }

    public Message getAnswerMessage(Message original) {
        var result = new Message();
        result.setTypeId(original.getTypeId());
        result.setProcessId(original.getProcessId());

        /* Extracted from JSP:
            <% pageContext.setAttribute( "rChar", "\r" );
            pageContext.setAttribute( "newLineChar", "\n" );
            pageContext.setAttribute( "singleQuot", "'" );
            %> 
            
            <c:set var="subject" value="${message.subject}"/>
            <c:if test="${not fn:startsWith( subject, 'Re:' ) }">
                <c:set var="subject" value="Re: ${subject}"/>
            </c:if>

            <c:set var="answerText" value=">${message.text}"/>
            <c:set var="answerText" value="${fn:replace( answerText, rChar, '' )}"/>
            <c:set var="answerText" value="${fn:replace( answerText, newLineChar, newLineChar.concat( '>' ) )}"/>
        */

        var subject = Utils.maskNull(original.getSubject());
        subject = subject.startsWith("Re:") ? subject : "Re: " + subject;
        result.setSubject(subject);

        var text = original.getText();
        text = ">" + text
            .replace("\r", "")
            .replace("\n", "\n>");
        result.setText(text);

        /* Extracted from JSP:
        <%
            Message message = (Message)request.getAttribute("message");
            if (request.getAttribute( "messageType" ) instanceof MessageTypeEmail) {
                MessageTypeEmail type = (MessageTypeEmail)request.getAttribute( "messageType" );
                try {
                    String answerTo = MessageTypeEmail.serializeAddresses(MessageTypeEmail.parseAddresses(message.getTo(), message.getFrom(), type.getEmail()));
                    pageContext.setAttribute( "answerTo", answerTo );
                }
                catch( Exception e )
                {}
            }
        %>*/
        result.setTo(original.getFrom());

        return result;
    }

    public boolean isEditable(Message message) {
        return false;
    }

    public boolean isRemovable(Message message) {
        return false;
    }

    public boolean isProcessChangeSupport() {
        return false;
    }

    public boolean isSpecialEditor() {
        return false;
    }
    
    public boolean isAttachmentSupport() {
        return true;
    }

    public String getProcessMessageHeaderColor(Message message) {
        return message.isIncoming() ? "#c3f6b6" : "#aceae7";
    }

    public List<Message> newMessageList(ConnectionSet conSet) throws BGException {
        return Collections.emptyList();
    }

    public Message newMessageGet(ConnectionSet conSet, String messageId) throws BGException {
        return null;
    }

    public void messageDelete(ConnectionSet conSet, String... messageIds) throws BGException {
        throw new UnsupportedOperationException();
    }

    public Message newMessageLoad(Connection con, String messageId) throws BGException {
        return null;
    }

    public abstract void updateMessage(Connection con, DynActionForm form, Message message) throws Exception;

    public String getMessageDescription(Message message) {
        return "";
    }

    public List<CommonObjectLink> searchObjectsForLink(Message message) {
        return Collections.emptyList();
    }

    public Message messageLinkedToProcess(Message message) throws BGException {
        return null;
    }

    protected Map<Integer, FileInfo> processMessageAttaches(Connection con, DynActionForm form, Message message)
            throws BGException {
        try {
            // перемещение временных загруженных файлов в постоянное хранилище
            FileDataDAO fileDao = new FileDataDAO(con);

            // удаление лишних сообщений
            List<FileData> attachList = message.getAttachList();

            Set<Integer> existFileIds = form.getSelectedValues("fileId");
            for (int i = 0; i < attachList.size(); i++) {
                FileData file = attachList.get(i);
                if (!existFileIds.contains(file.getId())) {
                    fileDao.delete(file);
                    attachList.remove(i--);
                }
            }

            Map<Integer, FileInfo> tmpFiles = SessionTemporaryFiles.getFiles(form, "tmpFileId");
            for (FileInfo fileInfo : tmpFiles.values()) {
                FileData file = new FileData();

                file.setTitle(fileInfo.title);

                OutputStream out = fileDao.add(file);

                IOUtils.copy(fileInfo.inputStream, out);
                out.close();
                fileInfo.inputStream.close();

                message.addAttach(file);
            }
            return tmpFiles;
        } catch (FileNotFoundException e) {
            throw new BGException(e);
        } catch (IOException e) {
            throw new BGException(e);
        }
    }
}