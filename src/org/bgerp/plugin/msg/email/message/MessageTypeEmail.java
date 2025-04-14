package org.bgerp.plugin.msg.email.message;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.commons.io.IOUtils;
import org.bgerp.app.cfg.ConfigMap;
import org.bgerp.app.cfg.Setup;
import org.bgerp.app.cfg.bean.annotation.Bean;
import org.bgerp.app.event.EventProcessor;
import org.bgerp.app.exception.BGException;
import org.bgerp.app.exception.BGMessageException;
import org.bgerp.app.exception.alarm.AlarmSender;
import org.bgerp.app.l10n.Localization;
import org.bgerp.cache.ProcessTypeCache;
import org.bgerp.dao.FileDataDAO;
import org.bgerp.model.Pageable;
import org.bgerp.model.file.FileData;
import org.bgerp.model.file.tmp.FileInfo;
import org.bgerp.model.file.tmp.SessionTemporaryFiles;
import org.bgerp.model.msg.Message;
import org.bgerp.model.msg.config.MessageTypeConfig;
import org.bgerp.plugin.msg.email.MessageParser;
import org.bgerp.plugin.msg.email.MessageParser.MessageAttach;
import org.bgerp.plugin.msg.email.Plugin;
import org.bgerp.util.Log;
import org.bgerp.util.mail.Addresses;
import org.bgerp.util.mail.MailConfig;
import org.bgerp.util.mail.MailMsg;

import ru.bgcrm.dao.Locker;
import ru.bgcrm.dao.message.MessageDAO;
import ru.bgcrm.dao.message.MessageType;
import ru.bgcrm.dao.process.ProcessDAO;
import ru.bgcrm.dao.user.UserDAO;
import ru.bgcrm.event.process.ProcessMessageAddedEvent;
import ru.bgcrm.model.param.ParameterSearchedObject;
import ru.bgcrm.model.process.Process;
import ru.bgcrm.model.process.ProcessType;
import ru.bgcrm.model.user.User;
import ru.bgcrm.struts.action.ProcessAction;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.TimeUtils;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.sql.ConnectionSet;
import ru.bgcrm.util.sql.SingleConnectionSet;

@Bean(oldClasses = "ru.bgcrm.dao.message.MessageTypeEmail")
public class MessageTypeEmail extends MessageType {
    private static final Log log = Log.getLog();

    @Deprecated
    public static final String RE_PREFIX = "Re: ";
    private static final String AUTOREPLY_SYSTEM_ID = "autoreply";

    private final String encoding;

    private final Pattern processIdPattern;
    private final boolean processedRead;

    private final Pattern quickAnswerPattern;
    private final int quickAnswerEmailParamId;

    private final int autoCreateProcessTypeId;
    private final boolean autoCreateProcessNotification;
    private final String autoCreateProcessNotificationTextMessage;

    private final String replayTo;
    private final MailConfig mailConfig;

    private final String folderIncoming;
    private final String folderSkipped;
    private final String folderProcessed;
    private final String folderSent;
    private final String folderTrash;
    private final MessageContent messageBuilder;

    private final FolderCache incomingCache = new FolderCache(this);

    public MessageTypeEmail(Setup setup, int id, ConfigMap config) throws BGMessageException {
        super(setup, id, config.get("title"), config);

        var l = Localization.getLocalizer(Localization.getLang(), Plugin.ID);

        encoding = MailMsg.getParamMailEncoding(setup);

        mailConfig = new MailConfig(config);

        replayTo = config.get("replayTo");

        folderIncoming = config.get("folderIn");
        folderProcessed = config.get("folderProcessed", "CRM_PROCESSED");
        folderSkipped = config.get("folderSkipped", "CRM_SKIPPED");
        folderSent = config.get("folderSent", "CRM_SENT");
        folderTrash = config.get("folderTrash", "Trash");

        processIdPattern = Pattern.compile(mailConfig.getEmail().replaceAll("\\.", "\\\\.") + "#(\\d+)");
        processedRead = config.getBoolean("processed.read", true);

        quickAnswerPattern = Pattern.compile("QA:(\\d+)");
        quickAnswerEmailParamId = config.getInt("quickAnswerEmailParamId", -1);

        autoCreateProcessTypeId = config.getInt("autoCreateProcess.typeId", -1);
        autoCreateProcessNotification = config.getBoolean("autoCreateProcess.notification", true);
        autoCreateProcessNotificationTextMessage = config.get("autoCreateProcess.notification.text.message", l.l("email.autoCreateProcess.notification.text.message.default"));

        messageBuilder = new MessageContent(setup, encoding, config);

        if (!mailConfig.check() || Utils.isBlankString(folderIncoming) ) {
            throw new BGException("Incorrect message type, email: " + mailConfig.getEmail());
        }
    }

    public String getEmail() {
        return mailConfig.getEmail();
    }

    @Override
    public boolean isAnswerSupport() {
        return true;
    }

    @Override
    public Message getAnswerMessage(Message original) {
        var result = new Message();
        result.setTypeId(original.getTypeId());
        result.setProcessId(original.getProcessId());

        result.setSubject(getAnswerSubject(original.getSubject()));
        result.setText(answerText(original.getText()));

        var addresses = Addresses.parseSafe(original.getTo())
            .exclude(getEmail())
            .addTo(original.getFrom());

        result.setTo(addresses.serialize());

        return result;
    }

    protected String getAnswerSubject(String subject) {
        // fallback, remove later 27.03.2023
        if (Setup.getSetup().getBoolean("email:answer.subject.prepend.with.re")) {
            subject = Utils.maskNull(subject);
            return subject.toLowerCase().contains(RE_PREFIX.toLowerCase()) ? subject : RE_PREFIX + subject;
        }
        return subject;
    }

    @Override
    public boolean isAttachmentSupport() {
        return true;
    }

    @Override
    public boolean isEditable(Message message) {
        return message.getDirection() == Message.DIRECTION_OUTGOING && message.getToTime() == null;
    }

    @Override
    public boolean isRemovable(Message message) {
        return true;
    }

    @Override
    public boolean isProcessChangeSupport() {
        return true;
    }

    @Override
    public String getViewerJsp() {
        return Plugin.ENDPOINT_MESSAGE_VIEWER;
    }

    @Override
    public String getHeaderJsp() {
        return Plugin.ENDPOINT_MESSAGE_HEADER;
    }

    @Override
    public String getMessageDescription(String lang, Message message) {
        var l = Localization.getLocalizer(lang, Plugin.ID);

        var result = new StringBuilder(200);

        result
            .append("EMail: \"")
            .append(message.getSubject())
            .append("\"; ")
            .append(message.getFrom())
            .append(" => ")
            .append(message.getTo())
            .append("; ");
        if (message.getDirection() == Message.DIRECTION_INCOMING) {
            result
                .append(l.l("получено: "))
                .append(TimeUtils.format(message.getFromTime(), TimeUtils.FORMAT_TYPE_YMDHM));
        } else {
            result
                .append(l.l("отправлено: "))
                .append(TimeUtils.format(message.getToTime(), TimeUtils.FORMAT_TYPE_YMDHM));
        }

        return result.toString();
    }

    @Override
    public String getEditorJsp() {
        return Plugin.ENDPOINT_MESSAGE_EDITOR;
    }

    @Override
    public void process() {
        log.info("Starting EMail daemon, box: {}", mailConfig.getEmail());

        readBox();
        sendMessages();
    }

    @Override
    public List<Message> newMessageList(ConnectionSet conSet) throws Exception {
        List<Message> result = new ArrayList<>();

        long time = System.currentTimeMillis();
        try (var store = mailConfig.getImapStore();
            var incomingFolder = store.getFolder(folderIncoming);) {

            log.debug("Get imap store time: {} ms.", System.currentTimeMillis() - time);

            incomingFolder.open(Folder.READ_ONLY);

            result = incomingCache.list(incomingFolder);

            log.debug("New message list time: {} ms.", System.currentTimeMillis() - time);
        }

        unprocessedMessagesCount = result.size();

        return result;
    }

    @Override
    public Message newMessageGet(ConnectionSet conSet, String messageId) throws Exception {
        return newMessageGet(messageId, true);
    }

    /**
     * Takes an IMAP message by ID.
     * @param messageId Message-ID header.
     * @param tryRelist try to re-list messages cache if not found.
     * @return
     * @throws IllegalArgumentException {@code tryRelist} is {@code false} and no message found.
     * @throws Exception
     */
    private Message newMessageGet(String messageId, boolean tryRelist) throws Exception {
        Message result = null;

        try (var store = mailConfig.getImapStore();
            var incomingFolder = store.getFolder(folderIncoming)) {
            incomingFolder.open(Folder.READ_ONLY);

            var messages = incomingFolder.getMessages();
            int index = 0;
            try {
                index = incomingCache.idToIndex(messageId);
            }
            // flush cache
            catch (ArrayIndexOutOfBoundsException e) {
                incomingCache.relist(incomingFolder);
                if (tryRelist)
                    return newMessageGet(messageId, false);
                else
                    throw new IllegalArgumentException("Not found message with ID: " + messageId);
            }

            var mp = new MessageParser(messages[index]);

            result = extractMessage(mp, true);
            addAttaches(mp, result);
        }

        return result;
    }

    @Override
    public void messageDelete(ConnectionSet conSet, String... messageIds) throws Exception {
        // message in process, called deletions per one
        if (messageIds.length == 1 && Utils.parseInt(messageIds[0]) > 0) {
            new MessageDAO(conSet.getConnection()).deleteMessage(Utils.parseInt(messageIds[0]));
            return;
        }

        try (var store = mailConfig.getImapStore();
            var incomingFolder = store.getFolder(folderIncoming);
            var trashFolder = store.getFolder(folderTrash)) {

            incomingFolder.open(Folder.READ_WRITE);
            trashFolder.open(Folder.READ_WRITE);

            var messages = incomingFolder.getMessages();

            var list = new ArrayList<javax.mail.Message>(messageIds.length);

            for (var messageId : messageIds) {
                int index = incomingCache.idToIndex(messageId);
                var message = messages[index];

                message.setFlag(Flags.Flag.DELETED, true);
                list.add(message);
            }

            incomingFolder.copyMessages(list.toArray(new javax.mail.Message[0]), trashFolder);

            incomingCache.delete(messageIds);
        }
    }

    private void addAttaches(MessageParser mp, Message msg) throws Exception {
        for (MessageAttach attach : mp.getAttachContent()) {
            FileData file = new FileData();
            file.setTitle(attach.title());
            msg.addAttach(file);
        }
    }

    @Override
    public Message newMessageLoad(Connection con, String messageId) throws Exception {
        try (var store = mailConfig.getImapStore();
            var incomingFolder = store.getFolder(folderIncoming);
            var processedFolder = store.getFolder(folderProcessed);
            var skippedFolder = store.getFolder(folderSkipped);) {
            incomingFolder.open(Folder.READ_WRITE);
            processedFolder.open(Folder.READ_WRITE);
            skippedFolder.open(Folder.READ_WRITE);

            int index = incomingCache.idToIndex(messageId);
            var message = incomingFolder.getMessages()[index];

            return processMessage(con, incomingFolder, processedFolder, skippedFolder, message);
        }
    }

    private void sendMessages() {
        Session session = mailConfig.getSmtpSession(setup);

        try (var con = setup.getDBConnectionFromPool();
            var transport = session.getTransport();
            var imapStore = mailConfig.getImapStore();
            var imapSentFolder = imapStore.getFolder(folderSent);) {

            var messageDAO = new MessageDAO(con);

            transport.connect();
            imapSentFolder.open(Folder.READ_WRITE);

            List<Message> toSendList = messageDAO.getUnsendMessageList(id, 100);
            for (Message msg : toSendList) {
                log.info("Sending message ID: {}, subject: {}, to: {}", msg.getId(), msg.getSubject(), msg.getTo());

                if (Locker.checkLock(msg.getLockEdit())) {
                    log.info("Skipping message on lock");
                    continue;
                }

                try {
                    MimeMessage message = new MimeMessage(session);
                    message.setFrom(new InternetAddress(mailConfig.getFrom()));
                    if (Utils.notBlankString(replayTo))
                        message.setReplyTo(InternetAddress.parse(replayTo));

                    for (Map.Entry<RecipientType, InternetAddress[]> me : Addresses.parseSafe(msg.getTo()).recipients().entrySet())
                        message.addRecipients(me.getKey(), me.getValue());

                    String subject = msg.getSubject();

                    int processId = getProcessId(subject);
                    if (processId <= 0 && msg.getProcessId() > 0)
                        subject = getSubjectWithProcessIdSuffix(msg);

                    message.setSubject(subject, encoding);
                    message.setSentDate(new Date());

                    messageBuilder.create(message, Localization.getLang(), msg);

                    transport.sendMessage(message, message.getAllRecipients());

                    if (imapSentFolder != null) {
                        message.setFlag(Flags.Flag.SEEN, true);
                        imapSentFolder.appendMessages(new javax.mail.Message[] { message });
                        log.info("Saved copy to folder: {}", folderSent);
                    }
                } catch (Exception e) {
                    log.error(e);
                }

                if (AUTOREPLY_SYSTEM_ID.equals(msg.getSystemId())) {
                    messageDAO.deleteMessage(msg.getId());
                } else {
                    msg.setToTime(new Date());
                    messageDAO.updateMessageProcess(msg);
                }

                con.commit();
            }
        } catch (Exception e) {
            log.error(e);
        }
    }

    private String getSubjectWithProcessIdSuffix(Message msg) {
        return msg.getSubject() + " [" + mailConfig.getEmail() + "#" + msg.getProcessId() + "]";
    }

    private void readBox() {
        try (Connection con = setup.getDBConnectionFromPool();
            Store store = mailConfig.getImapStore();
            Folder incomingFolder = store.getFolder(folderIncoming);
            Folder processedFolder = store.getFolder(folderProcessed);
            Folder skippedFolder = store.getFolder(folderSkipped);) {

            checkFolders(processedFolder, skippedFolder, store.getFolder(folderTrash));

            incomingFolder.open(Folder.READ_WRITE);
            processedFolder.open(Folder.READ_WRITE);
            skippedFolder.open(Folder.READ_WRITE);

            javax.mail.Message[] messages = null;

            var list = incomingCache.list(incomingFolder);
            for (int i = 0; i < list.size(); i++) {
                var message = list.get(i);
                var subject = message.getSubject();
                if (getProcessId(subject) > 0 || getQuickAnswerMessageId(subject) > 0 || autoCreateProcessTypeId > 0) {
                    if (messages == null) {
                        messages = incomingFolder.getMessages();
                    }
                    processMessage(con, incomingFolder, processedFolder, skippedFolder, messages[i]);
                    continue;
                }
                log.debug("Skipping message with subject: {}", subject);
            }

            unprocessedMessagesCount = list.size();
        } catch (Exception e) {
            log.error("Reading box " + mailConfig.getEmail() + ": " + e.getMessage(), e);
        }
    }

    /**
     * Processes an IMAP message and moves it over folders.
     * @param con DB connection.
     * @param incomingFolder INBOX.
     * @param processedFolder IMAP folder for successfully processed messages.
     * @param skippedFolder IMAP folder for case of error on processing.
     * @param message processed message.
     * @throws MessagingException
     */
    private Message processMessage(Connection con, Folder incomingFolder, Folder processedFolder, Folder skippedFolder, javax.mail.Message message)
            throws MessagingException {
        final String key = "email.process.error";

        Message result = null;
        String subject = "???";

        try {
            // клонирование сообщения для избежания ошибки "Unable to load BODYSTRUCTURE"
            // http://www.oracle.com/technetwork/java/javamail/faq/index.html#imapserverbug
            // MessageParser mp = new MessageParser(new MimeMessage((MimeMessage) message));
            MessageParser mp = new MessageParser(message);

            subject = mp.getMessageSubject();

            // test subject
            if (Utils.maskNull(subject).contains("test." + key))
                throw new Exception("Test alarm exception");

            Message msg = extractMessage(mp, true);

            FileDataDAO fileDao = new FileDataDAO(con);
            for (MessageAttach attach : mp.getAttachContent()) {
                FileData file = new FileData();
                file.setTitle(attach.title());

                OutputStream out = fileDao.add(file);
                IOUtils.copy(attach.inputStream(), out);

                msg.addAttach(file);
            }

            result = processMessage(con, msg);

            incomingFolder.copyMessages(new javax.mail.Message[] { message }, processedFolder);
            con.commit();
        } catch (Exception e) {
            log.error(e);
            incomingFolder.copyMessages(new javax.mail.Message[] { message }, skippedFolder);

            String s = subject;
            AlarmSender.send(key, 0, "Email processing error", () -> "Subject: " + s, e, () -> {
                try {
                    var bos = new ByteArrayOutputStream(1000);
                    message.writeTo(bos);
                    return List.of(new FileData("message.eml", bos.toByteArray()));
                } catch (Exception ex) {
                    log.error(ex);
                }
                return null;
            });
        }

        message.setFlag(Flags.Flag.DELETED, true);

        return result;
    }

    private Message processMessage(Connection con, Message msg) throws Exception {
        MessageDAO messageDAO = new MessageDAO(con);
        ProcessDAO processDAO = new ProcessDAO(con);

        String subject = msg.getSubject();
        int processId = getProcessId(subject);
        int quickAnsweredMessageId = getQuickAnswerMessageId(subject);

        log.info("Mailbox: {} found message From: {} Subject: {} Content: {} Process ID: {} Quick answer message ID: {} ", mailConfig.getEmail(),
                msg.getFrom(), subject, msg.getText(), processId, quickAnsweredMessageId);

        Process process = null;

        if (processId > 0) {
            // TODO: Think about writing to foreign processes.
            process = processDAO.getProcess(processId);
            if (process == null)
                log.error("Not found process with code: {}", processId);
            else
                setMessageProcessed(msg, processId);
        }
        // сообщение переделывается в исходящее и отправляется
        else if (quickAnsweredMessageId > 0) {
            Message quickAnsweredMessage = messageDAO.getMessageById(quickAnsweredMessageId);
            if (quickAnsweredMessage == null) {
                log.error("Message not found: {}", quickAnsweredMessageId);
            }
            else {
                MessageTypeEmail quickAnsweredMessageType =
                        (MessageTypeEmail) setup.getConfig(MessageTypeConfig.class)
                        .getTypeMap()
                        .get(quickAnsweredMessage.getTypeId());

                // изменение входящего сообщения в исходящее
                msg.setTypeId(quickAnsweredMessage.getTypeId());
                msg.setDirection(Message.DIRECTION_OUTGOING);
                msg.setProcessId(quickAnsweredMessage.getProcessId());
                msg.setTo(quickAnsweredMessage.getFrom());
                msg.setFromTime(new Date());
                msg.setToTime(null);

                msg.setSubject(getAnswerSubject(quickAnsweredMessage.getSubject()));

                // поиск пользователя по Email
                Pageable<ParameterSearchedObject<User>> searchResult = new Pageable<>();
                new UserDAO(con).searchUserListByEmail(searchResult, Collections.singletonList(quickAnswerEmailParamId), msg.getFrom());
                ParameterSearchedObject<User> user = Utils.getFirst(searchResult.getList());

                if (user != null) {
                    log.info("Creating quick answer on message: {}", quickAnsweredMessageId);
                    quickAnsweredMessageType.updateMessageInt(con, new DynActionForm(user.getObject()), msg);
                    return msg;
                }
            }
        } else if (autoCreateProcessTypeId > 0) {
            process = new Process();
            process.setTypeId(autoCreateProcessTypeId);
            process.setDescription(msg.getSubject());
            ProcessAction.processCreate(DynActionForm.SYSTEM_FORM, con, process);

            log.info("Created process: {}", process.getId());

            setMessageProcessed(msg, process.getId());

            if (autoCreateProcessNotification)
                messageDAO.updateMessage(messageLinkedToProcess(msg));
        }

        messageDAO.updateMessage(msg);

        if (process != null) {
            ProcessType type = ProcessTypeCache.getProcessType(process.getTypeId());
            if (type == null) {
                log.error("Not found process type with id: {}", process.getTypeId());
            } else {
                EventProcessor.processEvent(new ProcessMessageAddedEvent(DynActionForm.SYSTEM_FORM, msg, process),
                        new SingleConnectionSet(con));
            }
        }

        return msg;
    }

    private void setMessageProcessed(Message msg, int processId) {
        log.debug("setMessageProcessed processId: {}, processedRead: {}", processId, processedRead);

        msg.setProcessId(processId);
        if (processedRead) {
            msg.setToTime(new Date());
            msg.setUserId(User.USER_SYSTEM_ID);
        }
    }

    private Message extractMessage(MessageParser mp, boolean extractText) throws Exception {
        Message msg = new Message();
        msg.setTypeId(id);
        msg.setDirection(org.bgerp.model.msg.Message.DIRECTION_INCOMING);
        msg.setFrom(mp.getFrom());
        msg.setTo(mp.getTo());
        msg.setSystemId(mp.getMessageId());
        msg.setFromTime(mp.getFromTime());
        msg.setSubject(mp.getMessageSubject());

        if (extractText)
            msg.setText(mp.getTextContent());

        return msg;
    }

    /** Выделяет из темы письма код привязанного процесса. */
    private int getProcessId(String subject) {
        Matcher m = processIdPattern.matcher(subject);
        if (m.find())
            return Utils.parseInt(m.group(1));
        return -1;
    }

    /** Выделяет из письма код сообщения для быстрого ответа. */
    private int getQuickAnswerMessageId(String subject) {
        Matcher m = quickAnswerPattern.matcher(subject);
        if (m.find())
            return Utils.parseInt(m.group(1));
        return -1;
    }

    @Override
    public void updateMessage(Connection con, DynActionForm form, Message message) throws Exception {
        String to = form.getParam("to", "");

        if (Utils.isBlankString(to))
            throw new BGMessageException(Localization.getLocalizer(Localization.getLang(), Plugin.ID), "Undefined recipient address.");

        // checking recipient addresses
        var addresses = Addresses.parse(form.l.getLang(), to);
        addresses.put(RecipientType.CC, Addresses.parse(form.l.getLang(), form.getParam("toCc", "")).get(RecipientType.TO));

        message.setTo(addresses.serialize());

        updateMessageInt(con, form, message);
    }

    private void updateMessageInt(Connection con, DynActionForm form, Message message) throws Exception {
        message.setSystemId("");
        message.setFrom(mailConfig.getEmail());

        Map<Integer, FileInfo> tmpFiles = processMessageAttaches(con, form, message);

        new MessageDAO(con).updateMessage(message);

        SessionTemporaryFiles.deleteFiles(form, tmpFiles.keySet());
    }

    @Override
    public Message messageLinkedToProcess(Message message) {
        Message result = new Message();

        String text = message.getText().replace("\r", "");
        text = ">" + text.replace("\n", "\n>");

        text = autoCreateProcessNotificationTextMessage + text;

        result.setSystemId(AUTOREPLY_SYSTEM_ID);
        result.setDirection(Message.DIRECTION_OUTGOING);
        result.setTypeId(id);
        result.setProcessId(message.getProcessId());
        result.setFrom(mailConfig.getEmail());
        result.setTo(Addresses.parseSafe(message.getTo()).addTo(message.getFrom()).exclude(mailConfig.getEmail()).serialize());
        result.setFromTime(new Date());
        result.setText(text);
        result.setSubject(getAnswerSubject(message.getSubject()));

        return result;
    }

    /** Create folders, if they don't exist */
    private void checkFolders(Folder... folders) throws MessagingException {
        for (Folder folder : folders) {
            if (!folder.exists()) {
                folder.create(Folder.HOLDS_MESSAGES);
            }
        }
    }
}