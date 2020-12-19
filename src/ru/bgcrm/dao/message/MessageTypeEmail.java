package ru.bgcrm.dao.message;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;
import javax.mail.search.FlagTerm;
import javax.mail.search.MessageIDTerm;

import org.apache.commons.io.IOUtils;
import org.bgerp.plugin.msg.email.MessageParser;
import org.bgerp.plugin.msg.email.MessageParser.MessageAttach;

import ru.bgcrm.cache.ProcessTypeCache;
import ru.bgcrm.cache.UserCache;
import ru.bgcrm.dao.FileDataDAO;
import ru.bgcrm.dao.Locker;
import ru.bgcrm.dao.expression.Expression;
import ru.bgcrm.dao.message.config.MessageTypeConfig;
import ru.bgcrm.dao.process.ProcessDAO;
import ru.bgcrm.dao.user.UserDAO;
import ru.bgcrm.event.EventProcessor;
import ru.bgcrm.event.process.ProcessMessageAddedEvent;
import ru.bgcrm.model.BGException;
import ru.bgcrm.model.BGMessageException;
import ru.bgcrm.model.FileData;
import ru.bgcrm.model.SearchResult;
import ru.bgcrm.model.message.Message;
import ru.bgcrm.model.param.ParameterSearchedObject;
import ru.bgcrm.model.process.Process;
import ru.bgcrm.model.process.ProcessType;
import ru.bgcrm.model.user.User;
import ru.bgcrm.struts.action.FileAction.FileInfo;
import ru.bgcrm.struts.action.FileAction.SessionTemporaryFiles;
import ru.bgcrm.struts.action.ProcessAction;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.AlarmErrorMessage;
import ru.bgcrm.util.AlarmSender;
import ru.bgcrm.util.MailConfig;
import ru.bgcrm.util.MailMsg;
import ru.bgcrm.util.ParameterMap;
import ru.bgcrm.util.Setup;
import ru.bgcrm.util.TimeUtils;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.sql.ConnectionSet;
import ru.bgcrm.util.sql.SQLUtils;
import ru.bgcrm.util.sql.SingleConnectionConnectionSet;
import ru.bgerp.util.Log;

public class MessageTypeEmail extends MessageType {
    private static final String AUTOREPLY_SYSTEM_ID = "autoreply";
    public static final String RE_PREFIX = "Re: ";

    private static final RecipientType[] RECIPIENT_TYPES = new RecipientType[] { RecipientType.TO, RecipientType.CC };

    private static final Log log = Log.getLog();
    
    private volatile static ConcurrentHashMap<String, ConcurrentHashMap<String, Message>> incommingFoldersCashe = new ConcurrentHashMap<>();
    private volatile static Set<String> movingIdsMessages = ConcurrentHashMap.newKeySet();
    private static Instant lastDateResetCash = Instant.now();
    private long casheLifetime;

    private final Pattern processIdPattern;
    private final Pattern quickAnswerPattern;
    private final int quickAnswerEmailParamId;
    private final int autoCreateProcessTypeId;
    private final boolean autoCreateProcessNotification;

    private final String replayTo;
    private final MailConfig mailConfig;

    private final String folderIncoming;
    private final String folderSkipped;
    private final String folderProcessed;
    private final String folderSent;
    private final String folderTrash;
    private final String signExpression;
    private final boolean signStandard;

    public MessageTypeEmail(int id, ParameterMap config) throws BGException {
        super(id, config.get("title"), config);

        this.mailConfig = new MailConfig(config);

        replayTo = config.get("replayTo");

        folderIncoming = config.get("folderIn");  
        folderProcessed = config.get("folderProcessed", "CRM_PROCESSED");
        folderSkipped = config.get("folderSkipped", "CRM_SKIPPED");
        folderSent = config.get("folderSent", "CRM_SENT");
        folderTrash = config.get("folderTrash", "TRASH");
        signExpression = config.get("signExpression");
        signStandard = config.getBoolean("signStandard", false);
        processIdPattern = Pattern.compile(mailConfig.getEmail().replaceAll("\\.", "\\\\.") + "#(\\d+)");
        // TODO: Сделать конфигурируемым
        quickAnswerPattern = Pattern.compile("QA:(\\d+)");
        quickAnswerEmailParamId = config.getInt("quickAnswerEmailParamId", -1);
        autoCreateProcessTypeId = config.getInt("autoCreateProcess.typeId", -1);
        autoCreateProcessNotification = config.getBoolean("autoCreateProcess.notification", true);
        casheLifetime = config.getLong("casheLifetimeHour", 12);

        if (!mailConfig.check() || Utils.isBlankString(folderIncoming) ) {
            throw new BGException("Incorrect message type, email: " + mailConfig.getEmail());
        }
    }  

    public String getEmail() {
        return mailConfig.getEmail();
    }

    @Override
    public void process() {
        log.info("Starting EMail daemon, box: " + mailConfig.getEmail());

        readBox();
        sendMessages();
    }

    @Override
    public boolean isAnswerSupport() {
        return true;
    }

    @Override
    public boolean isEditable(Message message) {
        // исходящее но не отправленно ещё сообщение
        return message.getDirection() == Message.DIRECTION_OUTGOING && message.getToTime() == null;
    }

    @Override
    public boolean isProcessChangeSupport() {
        return true;
    }

    @Override
    public List<Message> newMessageList(ConnectionSet conSet) throws BGException {
        ConcurrentHashMap<String, Message> cache = incommingFoldersCashe.get(getEmail());
        if (cache == null) {
            CompletableFuture.runAsync(() -> {
                readBox();
            });
            try {
                Thread.sleep(5000); // will give time to load some messages
            } catch (InterruptedException e) {}
            cache = incommingFoldersCashe.get(getEmail()); // and return them
        }

        if (cache != null) {
            unprocessedMessagesCount = cache.size();
            return cache.values().stream().sorted((m1, m2) -> m1.getFromTime().compareTo(m2.getFromTime())).collect(Collectors.toList());
        }
        unprocessedMessagesCount = 0;
        return Collections.emptyList();
    }

    @Override
    public Message newMessageGet(ConnectionSet conSet, String messageId) throws BGException {
        ConcurrentHashMap<String, Message> cache = incommingFoldersCashe.get(getEmail());
        if (cache != null) {
            return cache.get(messageId);
        }
        return null;
    }

    @Override
    public void messageDelete(ConnectionSet conSet, String... messageIds) throws BGException {
        ConcurrentHashMap<String, Message> cache = incommingFoldersCashe.get(getEmail());
        if (cache != null) {
            for (String id : messageIds) {
                cache.remove(id);
                movingIdsMessages.add(id);
            }
        }
        CompletableFuture.runAsync(() -> {
            try (Store store = mailConfig.getImapStore();
                    Folder incomingFolder = store.getFolder(folderIncoming);
                    Folder trashFolder = store.getFolder(folderTrash);) {

                incomingFolder.open(Folder.READ_WRITE);
                trashFolder.open(Folder.READ_WRITE);

                int count = messageIds.length;
                for (javax.mail.Message message : incomingFolder.getMessages()) {
                    if (!Arrays.stream(messageIds).anyMatch(new MessageParser(message).getSystemId()::equals))
                        continue;
                    incomingFolder.copyMessages(new javax.mail.Message[] { message }, trashFolder);
                    message.setFlag(Flags.Flag.DELETED, true);
                    count--;
                    if (count == 0) // enough, if targets finished
                        break;
                }
            } catch (Exception e) {
                log.error(e);
            } finally {
                movingIdsMessages.removeAll(Arrays.asList(messageIds));
            }
        });
    }

    private void addAttaches(MessageParser mp, Message msg) throws Exception {
        for (MessageAttach attach : mp.getAttachContent()) {
            FileData file = new FileData();
            file.setTitle(attach.title);
            msg.addAttach(file);
        }
    }

    @Override
    public Message newMessageLoad(Connection con, String messageId) throws BGException {
        Message result = null;
        
        ConcurrentHashMap<String, Message> cache = incommingFoldersCashe.get(getEmail());
        if (cache != null) {
            result = cache.remove(messageId);
            
            if (result != null) {
                final HashMap<String, Deque<FileOutputStream>> outputMap = new HashMap<>();
                try {
                    MessageDAO messageDAO = new MessageDAO(con);
                    FileDataDAO fileDao = new FileDataDAO(con);
                    for (FileData fileData : result.getAttachList()) {
                        var queue = outputMap.get(fileData.getTitle());
                        if (queue == null) {
                            queue = new ArrayDeque<FileOutputStream>();
                            outputMap.put(fileData.getTitle(), queue ); // creating the id, side effect we get output, so store and use them later for save files  
                        }
                        queue.offer(fileDao.add(fileData));
                    }
                    messageDAO.updateMessage(result);

                    con.commit();
                } catch (Exception e) {
                    log.error(e);
                }
                
                movingIdsMessages.add(messageId);
                CompletableFuture.runAsync(() -> { // moving from incoming to processed, download attach
                    try (Store store = mailConfig.getImapStore();
                         Folder incomingFolder = store.getFolder(folderIncoming);
                         Folder processedFolder = store.getFolder(folderProcessed);
                         Folder skippedFolder = store.getFolder(folderSkipped); ) {
                        
                        incomingFolder.open(Folder.READ_WRITE);
                        processedFolder.open(Folder.READ_WRITE);
                        skippedFolder.open(Folder.READ_WRITE);

                        for (javax.mail.Message message : incomingFolder.search(new MessageIDTerm(messageId))) {
                            MessageParser mp = new MessageParser(message);

                            if (!mp.getSystemId().equals(messageId)) {
                                continue;
                            }

                            try {
                                for (MessageAttach attach : mp.getAttachContent()) {
                                    var queue = outputMap.get(attach.title);
                                    if (queue != null && queue.size() > 0) {
                                        FileOutputStream out = queue.poll();
                                        IOUtils.copy(attach.inputStream, out);
                                        out.close();
                                    }
                                    else {
                                        log.error( "Don't have FileOutputStream for attach - " + attach.title );
                                    }
                                }
                                incomingFolder.copyMessages(new javax.mail.Message[] { message }, processedFolder);
                            } catch (Exception e) {
                                log.error(e);
                                incomingFolder.copyMessages(new javax.mail.Message[] { message }, skippedFolder);
                            }
                            
                            message.setFlag(Flags.Flag.DELETED, true);
                            break;
                        }
                    } catch (Exception e) {
                        log.error(e);
                    } 
                    finally {
                        movingIdsMessages.remove(messageId);
                        SQLUtils.closeConnection(con);
                    }
                });
            }
        }

        return result;
    }

    public String getMessageDescription(Message message) {
        StringBuilder result = new StringBuilder(200);

        result.append("EMail: \"");
        result.append(message.getSubject());
        result.append("\"; ");
        result.append(message.getFrom());
        result.append(" => ");
        result.append(message.getTo());
        result.append("; ");
        if (message.getDirection() == Message.DIRECTION_INCOMING) {
            result.append("получено: ");
            result.append(TimeUtils.format(message.getFromTime(), TimeUtils.FORMAT_TYPE_YMDHM));
        } else {
            result.append("отправлено: ");
            result.append(TimeUtils.format(message.getToTime(), TimeUtils.FORMAT_TYPE_YMDHM));
        }

        return result.toString();
    }

    private void sendMessages() {
        String encoding = MailMsg.getParamMailEncoding(Setup.getSetup());
        Session session = mailConfig.getSmtpSession(Setup.getSetup());
        Connection con = Setup.getSetup().getDBConnectionFromPool();
        
        try (Transport transport = session.getTransport();
                Store imapStore = mailConfig.getImapStore();
                Folder imapSentFolder = imapStore.getFolder(folderSent);) {
            MessageDAO messageDAO = new MessageDAO(con);

            transport.connect();
            imapSentFolder.open(Folder.READ_WRITE);

            List<Message> toSendList = messageDAO.getUnsendMessageList(id, 100);
            for (Message msg : toSendList) {
                log.info("Send message subject: " + msg.getSubject() + " to " + msg.getTo());

                if (Locker.checkLock(msg.getLockEdit())) {
                    log.info("Skipping message on lock");
                    continue;
                }

                try {
                    MimeMessage message = new MimeMessage(session);
                    message.setFrom(new InternetAddress(mailConfig.getFrom()));
                    if (Utils.notBlankString(replayTo)) {
                        message.setReplyTo(InternetAddress.parse(replayTo));
                    }

                    Map<RecipientType, List<InternetAddress>> addrMap = parseAddresses(msg.getTo(), null, null);
                    for (RecipientType type : RECIPIENT_TYPES) {
                        List<InternetAddress> addrList = addrMap.get(type);
                        if (addrList == null) {
                            continue;
                        }

                        for (InternetAddress addr : addrList) {
                            message.addRecipient(type, addr);
                        }
                    }

                    String subject = msg.getSubject();

                    int processId = getProcessId(subject);
                    if (processId <= 0 && msg.getProcessId() > 0) {
                        subject = getSubjectWithProcessIdSuffix(msg);
                    }

                    message.setSubject(subject, encoding);
                    message.setSentDate(new Date());
                    createEmailText(messageDAO, message, encoding, msg);

                    transport.sendMessage(message, message.getAllRecipients());

                    if (imapSentFolder != null) {
                        message.setFlag(Flags.Flag.SEEN, true);
                        imapSentFolder.appendMessages(new javax.mail.Message[] { message });
                        log.info("Saved copy to folder: " + folderSent);
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
        } finally {
            SQLUtils.closeConnection(con);
        }
    }

    public String getSubjectWithProcessIdSuffix(Message msg) {
        return msg.getSubject() + " [" + mailConfig.getEmail() + "#" + msg.getProcessId() + "]";
    }

    private void createEmailText(MessageDAO messageDAO, MimeMessage message, String encoding, Message msg)
            throws Exception {
        MessageTypeConfig typeConfig = Setup.getSetup().getConfig(MessageTypeConfig.class);

        StringBuilder text = new StringBuilder();
        text.append(msg.getText());
        text.append("\n");
        text.append("\n-- ");

        if (Utils.notBlankString(signExpression)) {
            Map<String, Object> context = new HashMap<String, Object>();
            context.put(User.OBJECT_TYPE, UserCache.getUser(msg.getUserId()));
            context.put("message", msg);

            text.append(new Expression(context).getString(signExpression));
        }

        if (signStandard) {
            text.append("\nСообщение подготовлено системой BGERP (https://bgerp.ru).");
            text.append("\nНе изменяйте, пожалуйста, тему сообщения и не цитируйте данное сообщение в ответе!");
            text.append("\nИсторию переписки вы можете посмотреть в приложенном файле History.txt");
        }

        StringBuilder history = new StringBuilder();
        // у исходящего сообщения может быть привязан только один инцидент
        int processId = msg.getProcessId();
        if (processId > 0) {
            history.append("История сообщений по процессу #");
            history.append(processId);
            /*
             * history.append( " (" ); history.append( setup.get(
             * "helpdesk.process.url", "" ) ); history.append( "&processId=?" );
             * history.append( "):" );
             */
            history.append(":\n------------------------------------------");

            String to = msg.getTo();

            List<Message> messageList = messageDAO.getProcessMessageList(processId, msg.getId());
            for (Message historyItem : messageList) {
                // пока в истории отображаем только то, что относилось к данному
                // адресу
                if (!historyItem.getFrom().equals(to) && !historyItem.getTo().equals(to)) {
                    continue;
                }

                MessageType type = typeConfig.getTypeMap().get(historyItem.getTypeId());

                history.append("\n\n");
                history.append(type.getMessageDescription(historyItem));
                history.append("\n------------------------------------------");
                history.append("\n");
                history.append(historyItem.getText());
            }
        }

        MimeBodyPart textPart = new MimeBodyPart();
        textPart.setText(text.toString(), encoding);

        MimeBodyPart historyPart = new MimeBodyPart();
        historyPart.setText(history.toString(), encoding);
        historyPart.setFileName("History.txt");

        MimeMultipart multipart = new MimeMultipart();
        multipart.addBodyPart(textPart);
        multipart.addBodyPart(historyPart);

        if (msg.getAttachList().size() > 0) {
            try (var con = Setup.getSetup().getDBConnectionFromPool()) {
                FileDataDAO fileDao = new FileDataDAO(con);

                for (FileData attach : msg.getAttachList()) {
                    File file = fileDao.getFile(attach);

                    MimeBodyPart attachPart = new MimeBodyPart();
                    attachPart.setHeader("Content-Type", "charset=\"UTF-8\"; format=\"flowed\"");
                    attachPart.setDataHandler(new DataHandler(new FileDataSource(file)));
                    attachPart.setFileName(MimeUtility.encodeWord(attach.getTitle(), encoding, null));
                    multipart.addBodyPart(attachPart);

                    log.debug("Attach: %s", attach.getTitle());
                }
            }
        }

        message.setContent(multipart);
    }

    private synchronized void readBox() {
        Connection con = Setup.getSetup().getDBConnectionFromPool();
        try (Store store = mailConfig.getImapStore();
                Folder incomingFolder = store.getFolder(folderIncoming);
                Folder processedFolder = store.getFolder(folderProcessed);
                Folder skippedFolder = store.getFolder(folderSkipped);) {
            
            incomingFolder.open(Folder.READ_WRITE);
            processedFolder.open(Folder.READ_WRITE);
            skippedFolder.open(Folder.READ_WRITE);
            
            boolean isFirst = !incommingFoldersCashe.containsKey( getEmail() ); 
            if (isFirst) { // check folders
                Folder trashFolder = store.getFolder(folderTrash);
                checkFolders( processedFolder, skippedFolder, trashFolder);
            }
            boolean isLifetimeEnd = Duration.between(lastDateResetCash, Instant.now()).abs().getSeconds() >= casheLifetime * 60 * 60;
            
            javax.mail.Message[] messages = null;
            if (isFirst || isLifetimeEnd) { // first time - all of them
                log.info( "Cache reset" );
                lastDateResetCash = Instant.now();
                messages = incomingFolder.getMessages();
                incommingFoldersCashe.put(getEmail(), new ConcurrentHashMap<>());
            }
            else {  // next times - only unread
                messages = (MimeMessage[]) incomingFolder.search(new FlagTerm(new Flags(Flags.Flag.SEEN), false));
            }
            
            ConcurrentHashMap<String, Message> cache = incommingFoldersCashe.get( getEmail() );
            
            int unprocessedCount = 0;
            for (javax.mail.Message message : messages) {
                MessageParser mp = new MessageParser(message);

                String messageId = mp.getSystemId();
                if (movingIdsMessages.contains(messageId)) // check, may be that message in transaction to the other folder
                    continue;
                
                String subject = mp.getMessageSubject();
                if (getProcessId(subject) > 0 || getQuickAnswerMessageId(subject) > 0 || autoCreateProcessTypeId > 0) {
                    processMessage(con, incomingFolder, processedFolder, skippedFolder, message); // обработка только писем в теме которых установлена связка с процессом либо быстрый ответ
                    continue;
                } else { // to cache
                    try {
                        mp = new MessageParser(new MimeMessage((MimeMessage) message)); // don't sure that is necessary now - https://javaee.github.io/javamail/FAQ#imapserverbug
                        Message result = extractMessage(mp, true);
                        addAttaches(mp, result);
                        cache.put(mp.getSystemId(), result);
                        message.setFlag(Flags.Flag.SEEN, true); // mark it as read
                    } catch (Exception e) {
                        Message result = new Message();
                        result.setTypeId(id);
                        result.setSubject(e.getMessage() + " [" + message.getSubject() + "]");
                        result.setFromTime(new Date());
                        cache.put(mp.getSystemId(), result);
                        // don't mark, who know may be next time will be more successful  v(‘.’)v
                        log.error(e);
                    }
                }
                log.debug("Skipping message with subject: " + subject);
                unprocessedCount++;
            }
            
            unprocessedMessagesCount = unprocessedCount;

            /* Optimization for the future.
            if( isFirst ) {
               ExecutorService es = Executors.newCachedThreadPool();
               final IdleManager idleManager = new IdleManager(mailConfig.getImapSession(), es);
               
               Folder folder = store.getFolder(folderIncoming);
               folder.open(Folder.READ_WRITE);
               folder.addMessageCountListener(new MessageCountAdapter() {
                   public void messagesAdded(MessageCountEvent ev) {
                       Folder folder = (Folder)ev.getSource();
                       javax.mail.Message[] msgs = ev.getMessages();
                       System.out.println("Folder: " + folder + " got " + msgs.length + " new messages");
                       try {
                           // process new messages
                           idleManager.watch(folder); // keep watching for new messages
                       } catch (MessagingException mex) {
                           // handle exception related to the Folder
                       }
                   }
               });
               idleManager.watch(folder);
           }*/
        } catch (Exception e) {
            log.error("Read box " + mailConfig.getEmail() + ": " + e.getMessage(), e);
        } finally {
            SQLUtils.closeConnection(con);
        }
    }

    /**
     * Обрабатывает сообщение и производит перемещение между папками.
     */
    private Message processMessage(Connection con, Folder incomingFolder, Folder processedFolder, Folder skippedFolder, javax.mail.Message message) throws MessagingException {
        Message result = null;

        try {
            // клонирование сообщения для избежания ошибки "Unable to load BODYSTRUCTURE"
            // http://www.oracle.com/technetwork/java/javamail/faq/index.html#imapserverbug
            MessageParser mp = new MessageParser(new MimeMessage((MimeMessage) message));
            Message msg = extractMessage(mp, true);
            
            FileDataDAO fileDao = new FileDataDAO(con);
            for (MessageAttach attach : mp.getAttachContent()) {
                FileData file = new FileData();
                file.setTitle(attach.title);

                OutputStream out = fileDao.add(file);
                IOUtils.copy(attach.inputStream, out);

                msg.addAttach(file);
            }
            
            result = processMessage(con, msg );

            incomingFolder.copyMessages(new javax.mail.Message[] { message }, processedFolder);
            con.commit();
        } catch (Exception e) {
            log.error(e);
            incomingFolder.copyMessages(new javax.mail.Message[] { message }, skippedFolder);
        }

        message.setFlag(Flags.Flag.DELETED, true);

        return result;
    }

    private Message processMessage(Connection con, Message msg) throws BGException {
        String subject = "";

        try {
            MessageDAO messageDAO = new MessageDAO(con);
            ProcessDAO processDAO = new ProcessDAO(con);

            subject = msg.getSubject();
            int processId = getProcessId(subject);
            int quickAnsweredMessageId = getQuickAnswerMessageId(subject);

            log.info("Mailbox: " + mailConfig.getEmail() + " found message " + " From: "
                    + msg.getFrom() + "\t Subject: " + subject + "\t Content: " + msg.getText() + "\t Process ID: " + processId 
                    + "\t Quick answer message ID: " + quickAnsweredMessageId); // + "\t Content-Type: " + message.getContentType()

            Process process = null;

            // определение кода процесса
            if (processId > 0) {
                // TODO: Подумать, чтобы не хулиганили и не писали в чужие процессы..
                // Может к коду процесса чего добавить.
                process = processDAO.getProcess(processId);
                if (process == null)
                    log.error("Not found process with code: " + processId);
                else
                    setMessageProcessed(msg, processId);
            }
            // сообщение переделывается в исходящее и отправляется 
            else if (quickAnsweredMessageId > 0) {
                Message quickAnsweredMessage = messageDAO.getMessageById(quickAnsweredMessageId);
                if (quickAnsweredMessage == null) {
                    log.error("Message not found: " + quickAnsweredMessageId);
                }
                else {
                    MessageType quickAnsweredMessageType = 
                            Setup.getSetup().getConfig(MessageTypeConfig.class)
                            .getTypeMap().get(quickAnsweredMessage.getTypeId());
                                        
                    // изменение входящего сообщения в исходящее
                    msg.setTypeId(quickAnsweredMessage.getTypeId());
                    msg.setDirection(Message.DIRECTION_OUTGOING);
                    msg.setProcessId(quickAnsweredMessage.getProcessId());
                    msg.setProcessed(true);
                    msg.setTo(quickAnsweredMessage.getFrom());
                    msg.setFromTime(new Date());
                    msg.setToTime(null);
                    
                    String quickAnswerSubject = quickAnsweredMessage.getSubject();
                    if (!quickAnswerSubject.startsWith(RE_PREFIX))
                        quickAnswerSubject = RE_PREFIX + quickAnswerSubject;
                    
                    msg.setSubject(quickAnswerSubject);
                    
                    // поиск пользователя по E-Mail
                    SearchResult<ParameterSearchedObject<User>> searchResult = new SearchResult<>();
                    new UserDAO(con).searchUserListByEmail(searchResult, Collections.singletonList(quickAnswerEmailParamId), msg.getFrom());
                    ParameterSearchedObject<User> user = Utils.getFirst(searchResult.getList());
                    
                    if (user != null) {
                        log.info("Creating quick answer on message: " + quickAnsweredMessageId);
                        quickAnsweredMessageType.updateMessage(con, new DynActionForm(user.getObject()), msg);
                        return msg;
                    }
                }
            } else if (autoCreateProcessTypeId > 0) {
                process = new Process();
                process.setTypeId(autoCreateProcessTypeId);
                process.setDescription(msg.getSubject());
                ProcessAction.processCreate(DynActionForm.SERVER_FORM, con, process);

                log.info("Created process: %s", process.getId());

                setMessageProcessed(msg, process.getId());

                if (autoCreateProcessNotification)
                    messageDAO.updateMessage(messageLinkedToProcess(msg));
            }

            messageDAO.updateMessage(msg);

            if (process != null) {
                ProcessType type = ProcessTypeCache.getProcessType(process.getTypeId());
                if (type == null) {
                    log.error("Not found process type with id:" + process.getTypeId());
                } else {
                    EventProcessor.processEvent(new ProcessMessageAddedEvent(DynActionForm.SERVER_FORM, msg, process),
                            type.getProperties().getActualScriptName(), new SingleConnectionConnectionSet(con));
                }
            }

            return msg;
        } catch (Exception e) {
            String key = "email.parse.error";
            long time = System.currentTimeMillis();

            if (AlarmSender.needAlarmSend(key, time, 0)) {
                AlarmSender.sendAlarm(new AlarmErrorMessage(key, "Ошибка разбора E-Mail", "Тема письма " + subject), time);
            }

            log.error(e);

            throw new BGException(e);
        }
    }

    private void setMessageProcessed(Message msg, int processId) {
        msg.setProcessId(processId);
        msg.setProcessed(true);
        msg.setToTime(new Date());
        msg.setUserId(User.USER_SYSTEM_ID);
    }
    
    private Message extractMessage(MessageParser mp, boolean extractText)
            throws Exception, MessagingException {
        Message msg = new Message();
        msg.setTypeId(id);
        msg.setFrom(mp.getFrom());
        msg.setTo(mp.getTo());
        msg.setSystemId(mp.getSystemId());
        msg.setFromTime(mp.getFromTime());
        
        // время прочтения = времени получения, чтобы не считалось непрочитанным
        msg.setToTime(new Date());
        msg.setDirection(ru.bgcrm.model.message.Message.DIRECTION_INCOMING);
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
    public void updateMessage(Connection con, DynActionForm form, Message message) throws BGException {
        message.setSystemId("");
        message.setFrom(mailConfig.getEmail());

        if (Utils.isBlankString(message.getTo())) {
            throw new BGMessageException("Не указан EMail получателя.");
        }

        try {
            parseAddresses(message.getTo(), null, null);
        } catch (Exception ex) {
            throw new BGMessageException("Некорректный EMail получателя. " + ex.getMessage());
        }

        Map<Integer, FileInfo> tmpFiles = processMessageAttaches(con, form, message);

        new MessageDAO(con).updateMessage(message);

        SessionTemporaryFiles.deleteFiles(form, tmpFiles.keySet());
    }

    @Override
    public Message messageLinkedToProcess(Message message) throws BGException {
        Message result = new Message();

        String text = message.getText().replace("\r", "");
        text = ">" + text.replace("\n", "\n>");

        text = "Уважаемый клиент, ваше обращение зарегистрировано!\n"
                + "Для него назначен исполнитель и в ближайшее возможное время вам будет дан ответ.\n"
                + "Пожалуйста, при возникновении дополнительных сообщений по данному вопросу отвечайте на это письмо,\n"
                + "так чтобы в теме письма сохранялся числовой идентификатор обращения.\n"
                + "Это позволит нам быстрее обработать ваш запрос.\n\n" + text;

        result.setSystemId(AUTOREPLY_SYSTEM_ID);
        result.setDirection(Message.DIRECTION_OUTGOING);
        result.setTypeId(id);
        result.setProcessId(message.getProcessId());
        result.setFrom(mailConfig.getEmail());
        result.setTo(serializeAddresses(parseAddresses(message.getTo(), message.getFrom(), mailConfig.getEmail())));
        result.setFromTime(new Date());
        result.setText(text);
        result.setSubject(message.getSubject());
        if (!result.getSubject().startsWith("Re:")) {
            result.setSubject("Re: " + result.getSubject());
        }

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

    private Map<RecipientType, List<InternetAddress>> parseAddresses(String addresses, String addAddress,
            String excludeAddress) throws BGException {
        Map<RecipientType, List<InternetAddress>> result = new HashMap<RecipientType, List<InternetAddress>>();

        try {
            for (String token : addresses.split("\\s*;\\s*")) {
                int pos = token.indexOf(':');

                String prefix = null;
                if (pos > 0) {
                    prefix = token.substring(0, pos);
                    token = token.substring(pos + 1);
                }

                try {
                    RecipientType type = null;
                    if (Utils.isBlankString(prefix)) {
                        type = RecipientType.TO;
                    } else if (prefix.equalsIgnoreCase("CC")) {
                        type = RecipientType.CC;
                    } else {
                        throw new BGMessageException("Не поддерживаемый префикс: " + prefix);
                    }

                    List<InternetAddress> addressList = new ArrayList<InternetAddress>();
                    for (InternetAddress addr : InternetAddress.parse(token)) {
                        if (excludeAddress != null && addr.getAddress().equals(excludeAddress)) {
                            continue;
                        }
                        addressList.add(addr);
                    }

                    if (addressList.size() > 0) {
                        result.put(type, addressList);
                    }
                } catch (Exception e) {
                    log.error(e);
                }
            }

            if (addAddress != null) {
                List<InternetAddress> toAddressList = result.get(RecipientType.TO);
                if (toAddressList == null) {
                    result.put(RecipientType.TO, toAddressList = new ArrayList<InternetAddress>(1));
                }
                toAddressList.add(0, new InternetAddress(addAddress));
            }
        } catch (Exception e) {
        }

        return result;
    }

    private String serializeAddresses(Map<RecipientType, List<InternetAddress>> addressMap) {
        StringBuilder result = new StringBuilder();

        for (RecipientType type : RECIPIENT_TYPES) {
            List<InternetAddress> addressList = addressMap.get(type);
            if (addressList == null) {
                continue;
            }

            StringBuilder part = new StringBuilder();
            for (InternetAddress addr : addressList) {
                Utils.addCommaSeparated(part, addr.getAddress());
            }

            if (type != RecipientType.TO) {
                part.insert(0, type.toString() + ": ");
            }
            Utils.addSeparated(result, "; ", part.toString());
        }

        return result.toString();
    }
}