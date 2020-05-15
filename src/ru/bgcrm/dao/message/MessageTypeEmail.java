package ru.bgcrm.dao.message;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.activation.CommandMap;
import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.BodyPart;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MailDateFormat;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;

import org.apache.commons.io.IOUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;

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

    private static final RecipientType[] RECIPIENT_TYPES = new RecipientType[] { RecipientType.TO, RecipientType.CC };

    private static final Log log = Log.getLog();

    private static final String RE_PREFIX = "Re: ";
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
    private final String signExpression;
    private final boolean signStandard;

    public MessageTypeEmail(int id, ParameterMap config) throws BGException {
        super(id, config.get("title"), config);

        this.mailConfig = new MailConfig(config);

        replayTo = config.get("replayTo");

        folderIncoming = config.get("folderIn");  
        folderProcessed = config.get("folderProcessed");
        folderSkipped = config.get("folderSkipped");
        folderSent = config.get("folderSent");
        signExpression = config.get("signExpression");
        signStandard = config.getBoolean("signStandard", false);
        processIdPattern = Pattern.compile(mailConfig.getEmail().replaceAll("\\.", "\\\\.") + "#(\\d+)");
        // TODO: Сделать конфигурируемым
        quickAnswerPattern = Pattern.compile("QA:(\\d+)");
        quickAnswerEmailParamId = config.getInt("quickAnswerEmailParamId", -1);
        autoCreateProcessTypeId = config.getInt("autoCreateProcess.typeId", -1);
        autoCreateProcessNotification = config.getBoolean("autoCreateProcess.notification", true);

        if (!mailConfig.check() || Utils.isBlankString(folderIncoming) 
                || Utils.isBlankString(folderProcessed)
                || Utils.isBlankString(folderSkipped)) {
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
        List<Message> result = new ArrayList<Message>();

        try {
            long time = System.currentTimeMillis();

            Store store = mailConfig.getImapStore();

            log.debug("Get imap store time: %s %s", System.currentTimeMillis() - time, "ms.");
            
            Folder incomingFolder = store.getFolder(folderIncoming);
            incomingFolder.open(Folder.READ_ONLY);

            javax.mail.Message[] messages = incomingFolder.getMessages();

            log.debug("Message list time: %s %s", System.currentTimeMillis() - time, "ms.");

            incomingFolder.fetch(messages, MailConfig.FETCH_PROFILE);

            log.debug("Prefetch time: %s %s", System.currentTimeMillis() - time, "ms.");

            // обработка только писем в теме которых установлена связка с
            // процессом
            for (javax.mail.Message message : messages) {
                log.debug("Processing: %s", message.getSubject());

                try {
                    result.add(extractMessage(message, false));
                } catch (Exception e) {
                    Message msg = new Message();
                    msg.setTypeId(id);
                    msg.setSubject(e.getMessage() + " [" + message.getSubject() + "]");
                    msg.setFromTime(new Date());
                    result.add(msg);

                    log.error(e.getMessage(), e);
                }
            }

            incomingFolder.close(true);
            store.close();

            log.debug("New message list time: %s %s",  System.currentTimeMillis() - time, "ms.");
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        unprocessedMessagesCount = result.size();

        return result;
    }

    @Override
    public Message newMessageGet(ConnectionSet conSet, String messageId) throws BGException {
        Message result = null;

        try {
            Store store = mailConfig.getImapStore();

            Folder incomingFolder = store.getFolder(folderIncoming);
            incomingFolder.open(Folder.READ_ONLY);

            // сброс некого кэша, в котором после вызова Web сервиса для
            // загрузки файлов может оставаться неверный DataHandler,
            // в итоге приводящий к получению объекта
            // com.sun.xml.internal.messaging.saaj.packaging.mime.internet.MimeMultipart
            // вместо to javax.mail.internet.MimeMultipart
            CommandMap.setDefaultCommandMap(null);

            // обработка только писем в теме которых установлена связка с
            // процессом
            for (javax.mail.Message message : incomingFolder.getMessages()) {
                if (!getSystemId(message).equals(messageId)) {
                    continue;
                }

                // клонирование сообщения для избежания ошибки "Unable to load
                // BODYSTRUCTURE"
                // http://www.oracle.com/technetwork/java/javamail/faq/index.html#imapserverbug
                message = new MimeMessage((MimeMessage) message);
                result = extractMessage(message, true);
                addAttaches(message, result);

                break;
            }

            incomingFolder.close(true);
            store.close();
        } catch (Exception e) {
            throw new BGException(e.getMessage(), e);
        }

        return result;
    }

    @Override
    public void messageDelete(ConnectionSet conSet, String... messageIds) throws BGException {
        try {
            Store store = mailConfig.getImapStore();

            Folder incomingFolder = store.getFolder(folderIncoming);
            incomingFolder.open(Folder.READ_WRITE);

            for (javax.mail.Message message : incomingFolder.getMessages()) {
                if (!Arrays.stream(messageIds).anyMatch(getSystemId(message)::equals))
                    continue;
                message.setFlag(Flags.Flag.DELETED, true);
            }

            incomingFolder.close(true);
            store.close();
        } catch (Exception e) {
            throw new BGException(e.getMessage(), e);
        }
    }

    private void addAttaches(javax.mail.Message message, Message msg) throws Exception {
        for (MessageAttach attach : getAttachContent(message)) {
            FileData file = new FileData();
            file.setTitle(attach.title);
            msg.addAttach(file);
        }
    }

    @Override
    public Message newMessageLoad(Connection con, String messageId) throws BGException {
        Message result = null;

        try {
            Store store = mailConfig.getImapStore();

            Folder incomingFolder = store.getFolder(folderIncoming);
            incomingFolder.open(Folder.READ_WRITE);

            Folder processedFolder = store.getFolder(folderProcessed);
            processedFolder.open(Folder.READ_WRITE);

            Folder skippedFolder = store.getFolder(folderSkipped);
            skippedFolder.open(Folder.READ_WRITE);

            for (javax.mail.Message message : incomingFolder.getMessages()) {
                if (!getSystemId(message).equals(messageId)) {
                    continue;
                }
                result = processMessage(con, incomingFolder, processedFolder, skippedFolder, message);
                break;
            }

            incomingFolder.close(true);
            processedFolder.close(true);
            skippedFolder.close(true);

            store.close();
        } catch (Exception e) {
            throw new BGException(e.getMessage(), e);
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
        Setup setup = Setup.getSetup();

        String encoding = MailMsg.getParamMailEncoding(Setup.getSetup());
        Session session = mailConfig.getSmtpSession(Setup.getSetup());

        Store imapSentStore = null;
        Folder imapSentFolder = null;

        Transport transport = null;

        Connection con = setup.getDBConnectionFromPool();
        try {
            MessageDAO messageDAO = new MessageDAO(con);

            transport = session.getTransport();
            transport.connect();

            if (Utils.notBlankString(folderSent)) {
                imapSentStore = mailConfig.getImapStore();

                imapSentFolder = imapSentStore.getFolder(folderSent);
                imapSentFolder.open(Folder.READ_WRITE);
            }

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
                        subject = subject + " [" + mailConfig.getEmail() + "#" + msg.getProcessId() + "]";
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
                    log.error(e.getMessage(), e);
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
            log.error(e.getMessage(), e);
        } finally {
            SQLUtils.closeConnection(con);

            if (transport != null) {
                try {
                    transport.close();
                } catch (Exception ex) {
                    log.error(ex.getMessage());
                }
            }
            if (imapSentFolder != null) {
                try {
                    imapSentFolder.close(true);
                } catch (Exception ex) {
                    log.error(ex.getMessage());
                }
            }
            if (imapSentStore != null) {
                try {
                    imapSentStore.close();
                } catch (Exception ex) {
                    log.error(ex.getMessage());
                }
            }
        }
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
            text.append("\nСообщение подготовлено системой BGERP ( http://www.bgcrm.ru ).");
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
            Connection con = Setup.getSetup().getDBConnectionFromPool();
            try {
                FileDataDAO fileDao = new FileDataDAO(con);

                for (FileData attach : msg.getAttachList()) {
                    File file = fileDao.getFile(attach);

                    MimeBodyPart attachPart = new MimeBodyPart();
                    attachPart.setDataHandler(new DataHandler(new FileDataSource(file)));
                    attachPart.setFileName(MimeUtility.encodeWord(attach.getTitle()));
                    multipart.addBodyPart(attachPart);

                    if (log.isDebugEnabled()) {
                        log.debug("Attach: " + attach.getTitle());
                    }
                }
            } finally {
                SQLUtils.closeConnection(con);
            }
        }

        message.setContent(multipart);
    }

    private void readBox() {
        Connection con = Setup.getSetup().getDBConnectionFromPool();
        try {
            Store store = mailConfig.getImapStore();

            Folder incomingFolder = store.getFolder(folderIncoming);
            incomingFolder.open(Folder.READ_WRITE);

            Folder processedFolder = store.getFolder(folderProcessed);
            processedFolder.open(Folder.READ_WRITE);

            Folder skippedFolder = store.getFolder(folderSkipped);
            skippedFolder.open(Folder.READ_WRITE);
            
            int unprocessedCount = 0;

            // обработка только писем в теме которых установлена связка с процессом
            // либо быстрый ответ
            for (javax.mail.Message message : incomingFolder.getMessages()) {
                String subject = getMessageSubject(message);
                if (getProcessId(subject) > 0 || getQuickAnswerMessageId(subject) > 0 || autoCreateProcessTypeId > 0) {
                    processMessage(con, incomingFolder, processedFolder, skippedFolder, message);
                    continue;
                }
                log.debug("Skipping message with subject: " + subject);
                unprocessedCount++;
            }
            
            incomingFolder.close(true);
            processedFolder.close(true);
            skippedFolder.close(true);

            store.close();
            
            unprocessedMessagesCount = unprocessedCount;
        } catch (Exception e) {
            log.error("Read box " + mailConfig.getEmail() + ": " + e.getMessage(), e);
        } finally {
            SQLUtils.closeConnection(con);
        }
    }

    /**
     * Обрабатывает сообщение и производит перемещение между папками.
     */
    private Message processMessage(Connection con, Folder incomingFolder, Folder processedFolder, Folder skippedFolder,
            javax.mail.Message message) throws MessagingException {
        Message result = null;

        try {
            // клонирование сообщения для избежания ошибки "Unable to load BODYSTRUCTURE"
            // http://www.oracle.com/technetwork/java/javamail/faq/index.html#imapserverbug
            result = processMessage(con, new MimeMessage((MimeMessage) message));

            incomingFolder.copyMessages(new javax.mail.Message[] { message }, processedFolder);
            con.commit();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            incomingFolder.copyMessages(new javax.mail.Message[] { message }, skippedFolder);
        }

        message.setFlag(Flags.Flag.DELETED, true);

        return result;
    }

    // разбор темы сообщений
    // когда конструкция разбита на несколько частей вида, то стандартный парсер
    // разбирает только первый токен
    // =?koi8-r?Q?Re:_=FA=C1=D0=D2=CF=D3_=D4=C5=D3=D4=CF=D7=CF=CA_=CC?=
    // =?koi8-r?Q?=C9=C3=C5=CE=DA=C9=C9_[info=40bgcrm.ru#2213]?=
    private String getMessageSubject(javax.mail.Message message) throws Exception {
        String subject = Utils.maskNull(message.getSubject());

        int posFrom = -1, posTo = -1;
        do {
            posFrom = subject.indexOf("=?", posTo);
            int posEndEncoding = subject.indexOf("?Q?", posFrom);
            posTo = subject.indexOf("?=", posEndEncoding + 3);

            if (posFrom >= 0 && posTo > posFrom) {
                subject = subject.substring(0, posFrom) + MimeUtility.decodeText(subject.substring(posFrom, posTo + 2))
                        + subject.substring(posTo + 2);
            }
        } while (posFrom >= 0 && posTo > posFrom);

        return subject;
    }

    private static final Pattern datePattern = Pattern
            .compile("\\w{3}, \\d+ \\w{3} \\d{4} \\d{2}:\\d{2}:\\d{2} \\+\\d{4}");
    private static final MailDateFormat mailDateFormat = new MailDateFormat();

    private Message processMessage(Connection con, javax.mail.Message message) throws BGException {
        String subject = "";

        try {
            MessageDAO messageDAO = new MessageDAO(con);
            ProcessDAO processDAO = new ProcessDAO(con);
            FileDataDAO fileDao = new FileDataDAO(con);

            subject = getMessageSubject(message);

            int processId = getProcessId(subject);
            int quickAnsweredMessageId = getQuickAnswerMessageId(subject);

            log.info("Mailbox: " + mailConfig.getEmail() + " found message " + " From: "
                    + (InternetAddress) message.getFrom()[0] + "\t Subject: " + subject + "\t Content-Type: "
                    + message.getContentType() + "\t Content: " + message.getContent() + "\t Process ID: " + processId 
                    + "\t Quick answer message ID: " + quickAnsweredMessageId);

            Message msg = extractMessage(message, true);

            for (MessageAttach attach : getAttachContent(message)) {
                FileData file = new FileData();
                file.setTitle(attach.title);

                OutputStream out = fileDao.add(file);
                IOUtils.copy(attach.inputStream, out);

                msg.addAttach(file);
            }

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
                    new UserDAO(con).searchUserListByEmail(searchResult, 
                            Collections.singletonList(quickAnswerEmailParamId), 
                            ((InternetAddress) message.getFrom()[0]).getAddress().toString());
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
    
    private Message extractMessage(javax.mail.Message message, boolean extractText)
            throws Exception, MessagingException {
        Message msg = new Message();
        msg.setTypeId(id);
        msg.setFrom(((InternetAddress) message.getFrom()[0]).getAddress());

        // адреса пришлось выбирать из заголовков, т.к. getReciepients выдавал
        // только по одному адресу каждого типа
        StringBuilder to = new StringBuilder(10);

        final String[] headersTo = message.getHeader("To");
        if (headersTo != null) {
            for (String header : headersTo) {
                for (InternetAddress addr : InternetAddress.parse(header)) {
                    Utils.addSeparated(to, ", ", addr.getAddress());
                }
            }
        }

        String[] headersCc = message.getHeader("CC");
        if (headersCc != null) {
            StringBuilder ccAddresses = new StringBuilder(100);
            for (String header : headersCc) {
                for (InternetAddress addr : InternetAddress.parse(header)) {
                    Utils.addSeparated(ccAddresses, ", ", addr.getAddress());
                }
            }

            to.append("; CC: ");
            to.append(ccAddresses);
        }

        msg.setTo(to.toString());
        msg.setSystemId(getSystemId(message));

        // приоритентна дата из заголовка Recieved - время получения нашим IMAP
        // сервером
        String[] headers = message.getHeader("Received");
        if (headers != null && headers.length > 0) {
            Matcher m = datePattern.matcher(headers[0]);
            if (m.find()) {
                try {
                    msg.setFromTime(mailDateFormat.parse(m.group()));
                } catch (Exception e) {
                }
            }
        }

        if (msg.getFromTime() == null) {
            msg.setFromTime(message.getSentDate());
        }
        // время прочтения = времени получения, чтобы не считалось непрочитанным
        msg.setToTime(new Date());
        msg.setDirection(ru.bgcrm.model.message.Message.DIRECTION_INCOMING);
        msg.setSubject(getMessageSubject(message));

        if (extractText) {
            msg.setText(getTextContent(message).trim());
        }

        return msg;
    }

    private String getSystemId(javax.mail.Message message) throws MessagingException {
        // ((MimeMessage) message).getMessageID();  or so
        return message.getHeader("Message-ID")[0];
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

    private String getTextContent(javax.mail.Message message) throws Exception {
        String textContent = new String();

        String contentType = message.getContentType().toLowerCase();
        Object content = message.getContent();

        if (log.isDebugEnabled()) {
            log.debug("Extracting content, contentType: " + contentType);
        }

        // если пришел обычный текст
        if (contentType.startsWith("text/plain")) {
            textContent = (String) content;
        }
        // если пришел текст в виде HTML
        else if (contentType.startsWith("text/html")) {
            textContent = htmlToPlainText((String) content);
        }
        // если пришел и обычный текст, и HTML текст
        else if (contentType.startsWith("multipart/alternative")) {
            textContent = getTextFromMultipartAlternative((MimeMultipart) message.getContent());
        }
        // если сообщение с файлами (multipart)
        else if (contentType.startsWith("multipart/mixed") || contentType.startsWith("multipart/related")) {
            textContent = getTextFromMultipartMixed((MimeMultipart) message.getContent());
        } else {
            return "Тип сообщения: " + contentType + " не поддерживается";
            // throw new BGException( "This message type not supported now,
            // skipping.." );
        }

        return textContent;
    }

    private String getTextFromMultipartAlternative(MimeMultipart content) {
        String textContent = new String();
        try {
            for (int i = 0; i < content.getCount(); i++) {
                BodyPart messagePart = content.getBodyPart(i);
                String partContentType = messagePart.getContentType().toLowerCase();
                Object partContent = messagePart.getContent();

                if (log.isDebugEnabled()) {
                    log.debug("Extracting nultipart part, contentType: " + partContentType);
                }

                if (partContentType.startsWith("text/plain")) {
                    textContent = (String) partContent;
                    break;
                } else if (partContentType.startsWith("text/html")) {
                    textContent = htmlToPlainText((String) partContent);
                }
            }
        } catch (Exception ex) {
            log.error("Multipart alternative message error: " + ex.getMessage(), ex);
        }

        return textContent;
    }

    private String getTextFromMultipartMixed(MimeMultipart content) {
        String textContent = new String();
        try {
            for (int i = 0; i < content.getCount(); i++) {
                BodyPart part = content.getBodyPart(i);

                if (partIsAttachedFile(part)) {
                    continue;
                }

                String partContentType = part.getContentType().toLowerCase();
                Object partContent = part.getContent();

                if (log.isDebugEnabled()) {
                    log.debug("Processing multipart part, type: " + partContentType);
                }

                if (partContentType.startsWith("multipart/alternative")) {
                    textContent = getTextFromMultipartAlternative((MimeMultipart) partContent);
                } else if (partContentType.startsWith("text/plain")) {
                    textContent = (String) partContent;
                } else if (partContentType.startsWith("text/html")) {
                    textContent = htmlToPlainText((String) partContent);
                } else if (partContentType.startsWith("multipart/mixed")
                        || partContentType.startsWith("multipart/related")) {
                    textContent = getTextFromMultipartMixed((MimeMultipart) part.getContent());
                }
            }
        } catch (Exception ex) {
            log.error("Multipart mixed message error: " + ex.getMessage(), ex);
        }
        return textContent;
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
        result.setTo(MessageTypeEmail
                .serializeAddresses(parseAddresses(message.getTo(), message.getFrom(), mailConfig.getEmail())));
        result.setFromTime(new Date());
        result.setText(text);
        result.setSubject(message.getSubject());
        if (!result.getSubject().startsWith("Re:")) {
            result.setSubject("Re: " + result.getSubject());
        }

        return result;
    }

    private static class MessageAttach {
        public String title;
        public InputStream inputStream;

        public MessageAttach(String title, InputStream inputStream) {
            this.title = title;
            this.inputStream = inputStream;
        }
    }

    private ArrayList<MessageAttach> getAttachContent(javax.mail.Message message) throws Exception {
        ArrayList<MessageAttach> attachContent = new ArrayList<MessageAttach>();

        String contentType = message.getContentType().toLowerCase();
        if (contentType.startsWith("multipart/mixed") || contentType.startsWith("multipart/alternative")) {
            MimeMultipart content = (MimeMultipart) message.getContent();
            getAttaches(attachContent, content);
        }

        return attachContent;
    }

    private void getAttaches(ArrayList<MessageAttach> attachContent, MimeMultipart content)
            throws MessagingException, UnsupportedEncodingException, IOException {
        for (int i = 0; i < content.getCount(); i++) {
            BodyPart part = content.getBodyPart(i);
            if (part.getContentType().startsWith("multipart/")) {
                getAttaches(attachContent, (MimeMultipart) part.getContent());
            } else if (partIsAttachedFile(part)) {
                String attachTitle = MimeUtility.decodeText(part.getFileName() == null ? "attach" : part.getFileName());
                log.info("Attach: " + attachTitle);

                MessageAttach attachData = new MessageAttach(attachTitle, part.getInputStream());
                attachContent.add(attachData);
            }
        }
    }

    private boolean partIsAttachedFile(Part part) {
        try {
            return Utils.notBlankString(part.getFileName());
        } catch (MessagingException e) {
            return false;
        }
    }

    // http://stackoverflow.com/questions/2513707/how-to-convert-html-to-text-keeping-linebreaks
    private String htmlToPlainText(String text) {
        /*
         * text = text.replace( "</div>", "\n" ); text = text.replace( "</p>",
         * "\n" ); text = text.replaceAll( "<\\w+/?>", "" ); return text;
         */

        return buildStringFromNode(Jsoup.parse(text).body(), "").toString();
    }

    private static StringBuffer buildStringFromNode(Node node, String citation) {
        StringBuffer buffer = new StringBuffer();

        if (node instanceof Element) {
            Element element = (Element) node;
            String tagName = element.tagName();

            if ("blockquote".equals(tagName)) {
                citation = "> " + citation;
            }
        }

        if (node instanceof TextNode) {
            TextNode textNode = (TextNode) node;
            buffer.append(citation + textNode.text().trim());
        }

        for (Node childNode : node.childNodes()) {
            buffer.append(buildStringFromNode(childNode, citation));
        }

        if (node instanceof Element) {
            Element element = (Element) node;
            String tagName = element.tagName();

            if ("p".equals(tagName) || "div".equals(tagName) || "br".equals(tagName)) {
                buffer.append("\n");
            }
        }

        return buffer;
    }

    public static Map<RecipientType, List<InternetAddress>> parseAddresses(String addresses, String addAddress,
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
                    log.error(e.getMessage(), e);
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

    public static String serializeAddresses(Map<RecipientType, List<InternetAddress>> addressMap) {
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