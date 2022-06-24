package ru.bgcrm.dao.message;

import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.sql.Connection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.bgerp.event.ProcessFileGetEvent;
import org.bgerp.util.Log;

import ru.bgcrm.dao.FileDataDAO;
import ru.bgcrm.dynamic.DynamicClassManager;
import ru.bgcrm.event.EventProcessor;
import ru.bgcrm.model.BGException;
import ru.bgcrm.model.CommonObjectLink;
import ru.bgcrm.model.FileData;
import ru.bgcrm.model.IdTitle;
import ru.bgcrm.model.message.Message;
import ru.bgcrm.struts.action.FileAction.FileInfo;
import ru.bgcrm.struts.action.FileAction.SessionTemporaryFiles;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.ParameterMap;
import ru.bgcrm.util.Setup;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.sql.ConnectionSet;
import ru.bgcrm.util.sql.SingleConnectionSet;

public abstract class MessageType extends IdTitle {
    private static final Log log = Log.getLog();

    private final LinkedHashMap<Integer, MessageTypeSearch> searchMap = new LinkedHashMap<>();

    private MessageTypeContactSaver contactSaver;

    protected final Setup setup;
    protected final ParameterMap configMap;

    protected volatile boolean reading;
    protected volatile Integer unprocessedMessagesCount;

    protected MessageType(Setup setup, int id, String title, ParameterMap config) throws BGException {
        this.setup = setup;
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
                    log.error(e);
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
                log.error(e);
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

    /**
     * Message type is currently running {@link #newMessageList(ConnectionSet)}.
     * @return
     */
    public boolean isReading() {
        return reading;
    }

    /**
     * Count of unprocessed messages.
     * @return value or null if unknown.
     */
    public Integer getUnprocessedMessagesCount() {
        return configMap.getBoolean("unprocessedMessageNotify", true) ?
                unprocessedMessagesCount :
                    null;
    }

    /**
     * Sends and reads message list.
     */
    public void process() {}

    // TODO: Rename to isAnswerSupported
    public boolean isAnswerSupport() {
        return false;
    }

    public Message getAnswerMessage(Message original) {
        var result = new Message();
        result.setTypeId(original.getTypeId());
        result.setProcessId(original.getProcessId());

        var subject = Utils.maskNull(original.getSubject());
        subject = subject.startsWith("Re:") ? subject : "Re: " + subject;
        result.setSubject(subject);

        var text = original.getText();
        text = ">" + text
            .replace("\r", "")
            .replace("\n", "\n>");
        result.setText(text);

        result.setTo(original.getFrom());

        return result;
    }

    public boolean isEditable(Message message) {
        return false;
    }

    public boolean isRemovable(Message message) {
        return false;
    }

    // TODO: Rename to ísProcessChangeSupported
    public boolean isProcessChangeSupport() {
        return false;
    }

    /**
     * @return Plugin's endpoint for unprocessed message viewing.
     */
    public String getViewerJsp() {
        return null;
    }

    /**
     * @return Plugin's endpoint for process message header.
     */
    public String getHeaderJsp() {
        return null;
    }

    /**
     * @return possibility to mark message as read/unread using kernel logic.
     */
    public boolean isReadable() {
        return true;
    }

    /**
     * Plugin's endpoint for process message editor.
     * @return
     */
    public String getEditorJsp() {
        return null;
    }

    public boolean isAttachmentSupport() {
        return true;
    }

    public String getProcessMessageHeaderColor(Message message) {
        return message.isIncoming() ? "#c3f6b6" : "#aceae7";
    }

    /**
     * List of unprocessed messages from storage, for example - E-Mails from IMAP folder.
     * @param conSet
     * @return
     * @throws Exception
     */
    public List<Message> newMessageList(ConnectionSet conSet) throws Exception {
        return Collections.emptyList();
    }

    /**
     * Gets unprocessed message from storage.
     * @param conSet
     * @param messageId unique ID.
     * @return
     * @throws Exception
     */
    public Message newMessageGet(ConnectionSet conSet, String messageId) throws Exception {
        return null;
    }

    /**
     * Deletes both processed and unprocessed messages.
     * @param conSet
     * @param messageIds set with int DB IDs or type related string IDs.
     * @throws Exception
     */
    public void messageDelete(ConnectionSet conSet, String... messageIds) throws Exception {
        throw new UnsupportedOperationException();
    }

    /**
     * Gets unprocessed message from storage and persists it in DB.
     * @param con
     * @param messageId
     * @return
     * @throws Exception
     */
    public Message newMessageLoad(Connection con, String messageId) throws Exception {
        return null;
    }

    public abstract void updateMessage(Connection con, DynActionForm form, Message message) throws Exception;

    /**
     * Generates short message description.
     * @param lang language.
     * @param message message with the type.
     * @return
     */
    public String getMessageDescription(String lang, Message message) {
        return "";
    }

    public List<CommonObjectLink> searchObjectsForLink(Message message) {
        return Collections.emptyList();
    }

    public Message messageLinkedToProcess(Message message) throws Exception {
        return null;
    }

    protected Map<Integer, FileInfo> processMessageAttaches(Connection con, DynActionForm form, Message message)
            throws Exception {
        var fileDao = new FileDataDAO(con);

        // attaching of already existing
        List<FileData> attachList = message.getAttachList();
        attachList.clear();
        attachList.addAll(fileDao.list(form.getSelectedValuesList("fileId")));

        // newly uploaded files
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

        // announced
        for (String fileId : form.getSelectedValuesListStr("announcedFileId")) {
            var event = new ProcessFileGetEvent(form, message.getProcessId(), fileId);
            EventProcessor.processEvent(event, new SingleConnectionSet(con));

            if (event.getFileData() == null) {
                log.debug("Not found bytes for announcedFileId: " + fileId);
                continue;
            }

            var file = new FileData();
            file.setTitle(event.getFileTitle());

            var out = fileDao.add(file);
            IOUtils.write(event.getFileData(), out);
            out.close();

            message.addAttach(file);
        }

        return tmpFiles;
    }
}