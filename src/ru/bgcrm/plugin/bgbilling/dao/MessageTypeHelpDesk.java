package ru.bgcrm.plugin.bgbilling.dao;

import static ru.bgcrm.dao.process.Tables.TABLE_PROCESS;
import static ru.bgcrm.dao.process.Tables.TABLE_PROCESS_LINK;

import java.io.ByteArrayOutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.io.IOUtils;
import org.bgerp.app.bean.annotation.Bean;
import org.bgerp.app.cfg.ConfigMap;
import org.bgerp.app.cfg.Setup;
import org.bgerp.app.exception.BGException;
import org.bgerp.dao.message.MessageSearchDAO;
import org.bgerp.dao.param.ParamValueDAO;
import org.bgerp.dao.process.ProcessLinkSearchDAO;
import org.bgerp.model.Pageable;
import org.bgerp.model.file.FileInfo;
import org.bgerp.model.file.SessionTemporaryFiles;
import org.bgerp.util.Log;

import ru.bgcrm.cache.ProcessTypeCache;
import ru.bgcrm.dao.message.MessageDAO;
import ru.bgcrm.dao.message.MessageType;
import ru.bgcrm.dao.process.ProcessDAO;
import ru.bgcrm.dao.process.ProcessLinkDAO;
import ru.bgcrm.dao.process.StatusChangeDAO;
import ru.bgcrm.event.EventProcessor;
import ru.bgcrm.event.process.ProcessMessageAddedEvent;
import ru.bgcrm.model.CommonObjectLink;
import ru.bgcrm.model.FileData;
import ru.bgcrm.model.Pair;
import ru.bgcrm.model.message.Message;
import ru.bgcrm.model.process.Process;
import ru.bgcrm.model.process.ProcessExecutor;
import ru.bgcrm.model.process.ProcessGroup;
import ru.bgcrm.model.process.ProcessType;
import ru.bgcrm.model.process.StatusChange;
import ru.bgcrm.model.user.User;
import ru.bgcrm.plugin.bgbilling.DBInfo;
import ru.bgcrm.plugin.bgbilling.DBInfoManager;
import ru.bgcrm.plugin.bgbilling.Plugin;
import ru.bgcrm.plugin.bgbilling.proto.dao.HelpDeskDAO;
import ru.bgcrm.plugin.bgbilling.proto.model.Contract;
import ru.bgcrm.plugin.bgbilling.proto.model.helpdesk.HdMessage;
import ru.bgcrm.plugin.bgbilling.proto.model.helpdesk.HdTopic;
import ru.bgcrm.struts.action.LinkAction;
import ru.bgcrm.struts.action.ProcessAction;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.sql.SingleConnectionSet;

@Bean
public class MessageTypeHelpDesk extends MessageType {
    private static final Log log = Log.getLog();

    private final String billingId;
    // импорт одного топика, для отладки
    private final int topicId;

    private final User user;
    private final int processTypeId;
    // текстовый параметр со стоимостью
    private final int costParamId;
    // списковый параметр со статусом
    private final int statusParamId;
    // списковый параметр - признак вхождения в пакет
    private final int packageParamId;
    // списковый параметр - признак автозакрытия
    private final int autoCloseParamId;
    // в какой статус закрывать процесс, если тема HD закрылась
    private final int closeStatusId;
    // в какой статус закрывать процесс, если тема HD открылась
    private final int openStatusId;
    // первое сообщение добавлять в описание процесса
    private final boolean addFirstMessageInDescription;
    // событие о новых сообщениях
    private final boolean newMessageEvent;
    // при переходе в эти статусы помечать все сообщения прочитанными
    private final Set<Integer> markMessagesReadStatusIds;
    // количество выбираемых тем
    private final int pageSize;

    public MessageTypeHelpDesk(Setup setup, int id, ConfigMap config) throws BGException {
        super(setup, id, config.get("title"), config);
        this.billingId = config.get("billingId");
        this.topicId = config.getInt("topicId");

        String userName = config.get("user", "");
        String userPassword = config.get("pswd", "");
        if (Utils.isBlankString(userName) || Utils.isBlankString(userPassword)) {
            throw new BGException("Billing user or password undefined.");
        }

        user = new User();
        user.setLogin(userName);
        user.setPassword(userPassword);

        processTypeId = config.getInt("processTypeId", 0);
        if (processTypeId <= 0) {
            throw new BGException("processTypeId not defined");
        }

        costParamId = config.getInt("costParamId", 0);
        statusParamId = config.getInt("statusParamId", 0);
        closeStatusId = config.getInt("closeStatusId", 0);
        openStatusId = config.getInt("openStatusId", 0);
        autoCloseParamId = config.getInt("autoCloseParamId", 0);
        packageParamId = config.getInt("packageParamId", 0);
        pageSize = config.getInt("pageSize", 100000);
        newMessageEvent = config.getBoolean("newMessageEvent", false);
        markMessagesReadStatusIds = Utils.toIntegerSet(config.get("markMessagesReadStatusIds", ""));
        addFirstMessageInDescription = config.getBoolean("addFirstMessageInDescription", false);
    }

    @Override
    public String getHeaderJsp() {
        return Plugin.ENDPOINT_MESSAGE_HEADER;
    }

    public User getUser() {
        return user;
    }

    public String getBillingId() {
        return billingId;
    }

    public int getProcessTypeId() {
        return processTypeId;
    }

    public int getCostParamId() {
        return costParamId;
    }

    public int getStatusParamId() {
        return statusParamId;
    }

    public int getCloseStatusId() {
        return closeStatusId;
    }

    public int getPackageParamId() {
        return packageParamId;
    }

    public int getAutoCloseParamId() {
        return autoCloseParamId;
    }

    public Set<Integer> getMarkMessagesReadStatusIds() {
        return markMessagesReadStatusIds;
    }

    public DBInfo getDbInfo() throws BGException {
        return DBInfoManager.getDbInfo(billingId);
    }

    public String getObjectType() {
        return "bgbilling-helpdesk:" + billingId;
    }

    @Override
    public void process() {
        log.info("Starting {}", MessageTypeHelpDesk.class);

        try (Connection con = Setup.getSetup().getDBConnectionFromPool()) {
            ProcessType processType = ProcessTypeCache.getProcessType(processTypeId);
            if (processType == null) {
                log.error("Not found process type with id: {}", processTypeId);
                return;
            }

            DBInfo dbInfo = getDbInfo();

            ProcessDAO processDao = new ProcessDAO(con);

            final String objectType = getObjectType();

            // выбрать активные процессы, чтобы закрыть те, что привязаны к неактивным уже топикам
            Map<Integer, Integer> openHdProcessTopicIds = new TreeMap<>();

            String query = "SELECT p.id, pl.object_id FROM " + TABLE_PROCESS + " AS p "
                    + "INNER JOIN " + TABLE_PROCESS_LINK + " AS pl ON p.id=pl.process_id AND pl.object_type=? "
                    + "WHERE p.close_dt IS NULL ";

            PreparedStatement ps = con.prepareStatement(query);
            ps.setString(1, objectType);

            ResultSet rs = ps.executeQuery();
            while (rs.next())
                openHdProcessTopicIds.put(rs.getInt(1), rs.getInt(2));
            ps.close();

            DynActionForm form = new DynActionForm(user);
            HelpDeskDAO hdDao = new HelpDeskDAO(user, dbInfo);

            if (dbInfo.versionCompare("7.2") >= 0) {
                Pageable<Pair<HdTopic, List<HdMessage>>> result = new Pageable<>();
                result.getPage().setPageIndex(1);
                result.getPage().setPageSize(pageSize);

                log.info("topicId: {}", topicId);

                hdDao.searchTopicsWithMessages(result, topicId);

                for (Pair<HdTopic, List<HdMessage>> pair : result.getList()) {
                    HdTopic topic = pair.getFirst();

                    Process process = processTopic(con, openHdProcessTopicIds, processType, objectType, form, hdDao, topic);

                    updateProcessFromTopic(con, processType, process, topic, pair.getSecond());

                    con.commit();
                }
            } else
                throw new IllegalStateException("Unsupported BGBilling version");


            // оставшиеся процессы привязаны к уже закрытым темам хелпдеска - нужно их закрыть
            for (Integer processId : openHdProcessTopicIds.keySet()) {
                log.info("Closing process: {}", processId);

                Process process = processDao.getProcess(processId);

                CommonObjectLink topicLink = Utils.getFirst(new ProcessLinkDAO(con).getObjectLinksWithType(processId, getObjectType()));
                if (topicLink == null) {
                    log.error("Not linked topic to process: {}", processId);
                    continue;
                }

                int topicId = topicLink.getLinkObjectId();

                var topicWithMessages = hdDao.getTopicWithMessages(topicId);

                HdTopic topic = topicWithMessages != null ? topicWithMessages.getFirst() : null;
                if (topic == null)
                    log.warn("Topic not found: {}", topicId);
                else
                    // загрузка параметров
                    updateProcessFromTopic(con, processType, process, topic, null);

                StatusChange status = new StatusChange();
                status.setDate(new Date());
                status.setComment("Закрыто вслед за темой в HelpDesk.");
                status.setProcessId(processId);
                status.setStatusId(closeStatusId);

                // вызов не через ProcessAction, чтобы по событию повторно тема не закрылась
                new StatusChangeDAO(con).changeStatus(process, processType, status);

                con.commit();
            }
        } catch (Exception e) {
            log.error(e);
        } finally {
            log.info("Finished {}", MessageTypeHelpDesk.class);
        }
    }

    private Process processTopic(Connection con, Map<Integer, Integer> activeHdProcessTopicIds, ProcessType processType, final String objectType,
            DynActionForm form, HelpDeskDAO hdDao, HdTopic topic) throws Exception {
        log.debug("Processing topic: {}", topic.getId());

        Pageable<Process> searchResult = new Pageable<>();
        new ProcessLinkSearchDAO(con, form).withLinkObjectType(objectType).withLinkObjectId(topic.getId()).search(searchResult);

        Process process = Utils.getFirst(searchResult.getList());

        if (process == null) {
            log.info("Creating process for topic: {}/{}", billingId, topic.getId());

            String description = topic.getTitle();
            if (Utils.notBlankString(topic.getContact()))
                description += "\nКонтакт: " + topic.getContact();

            process = new Process();
            process.setDescription(description);
            process.setTypeId(processTypeId);

            ProcessAction.processCreate(form, con, process);

            // привязка к топику
            LinkAction.addLink(form, con,
                    new CommonObjectLink(Process.OBJECT_TYPE, process.getId(), objectType, topic.getId(), ""));

            // привязка к договору
            LinkAction.addLink(form, con, new CommonObjectLink(Process.OBJECT_TYPE, process.getId(),
                    Contract.OBJECT_TYPE + ":" + billingId, topic.getContractId(), topic.getContractTitle()));
        } else {
            activeHdProcessTopicIds.remove(process.getId());

            if (process.getCloseTime() != null) {
                var pair = hdDao.getTopicWithMessages(topic.getId());

                // если тема оказалась закрытой, например её закрыли во время работы этой задачи, то переоткрытие
                if (pair != null && !pair.getFirst().isClosed()) {
                    log.info("Opening process: {} for topic: {}", process.getId(), topic.getId());

                    StatusChange status = new StatusChange();
                    status.setDate(new Date());
                    status.setComment("Открыто вслед за темой в HelpDesk.");
                    status.setProcessId(process.getId());
                    status.setStatusId(openStatusId);

                    // вызов не через ProcessAction, чтобы по событию повторно тема не открылась
                    new StatusChangeDAO(con).changeStatus(process, processType, status);

                    con.commit();
                } else
                    log.info("Topic is already closed or not found: {}", topic.getId());
            }
        }
        return process;
    }

    // protected boolean isTopicClosed(HelpDeskDAO hdDao, int topicId) throws BGException {
    //     Pageable<HdTopic> result2 = new Pageable<HdTopic>();
    //     result2.getPage().setPageIndex(1);
    //     result2.getPage().setPageSize(pageSize);
    //     hdDao.seachTopicList(result2, null, true, false, topicId);
    //     //getTopicMessageListgetTopicMessageList
    //     HdTopic topic2 = Utils.getFirst(result2.getList());
    //     //если тема закрыта, то список должен вернуться пустым
    //     return topic2 != null;
    // }

    private HdTopic updateProcessFromTopic(Connection con, ProcessType processType, Process process, HdTopic topic, List<HdMessage> hdMessages)
            throws Exception {
        DBInfo dbInfo = getDbInfo();

        ProcessDAO processDao = new ProcessDAO(con);
        MessageDAO messageDao = new MessageDAO(con);
        ParamValueDAO paramDao = new ParamValueDAO(con);

        HelpDeskDAO hdDao = new HelpDeskDAO(user, dbInfo);

        // ключ - Id сообщения в биллинге, значение - в ЦРМ, если есть
        Map<Integer, Message> messageMap = new HashMap<Integer, Message>();

        // все сообщения из данного HD топика в данном процессе
        Pageable<Message> messages = new Pageable<>();
        new MessageSearchDAO(con)
            .withProcessIds(Set.of(process.getId()))
            .withTypeId(id)
            .search(messages);

        for (Message message : messages.getList())
            messageMap.put(Utils.parseInt(message.getSystemId()), message);

        // обработка сообщений и точных данных, повторная выборка позволяет получить актуальное состояние
        // т.к. до этого во время синхронизации иногда что-то менялось и синхронизация сбрасывала изменения в BGERP (исполнителя)
        if (hdMessages == null) {
            Pair<HdTopic, List<HdMessage>> pair = hdDao.getTopicWithMessages(topic.getId());
            if (pair != null) {
                topic = pair.getFirst();
                hdMessages = pair.getSecond();
            }
        }

        // соотнесение исполнителей
        if (topic.getUserId() > 0) {
            int crmUserId = dbInfo.getCrmUserId(topic.getUserId());
            if (crmUserId > 0) {
                ProcessGroup group = process.getGroups().stream()
                    .filter(pg -> pg.getGroupId() == 0)
                    .findFirst().orElse(null);
                if (group == null) {
                    log.warn("Not found process group with role=0");
                }
                else {
                    Set<ProcessExecutor> executors = process.getExecutors();
                    executors.add(new ProcessExecutor(crmUserId, group.getGroupId(), group.getRoleId()));

                    processDao.updateProcessExecutors(executors, process.getId());
                }
            }
        } else
            processDao.updateProcessExecutors(Set.of(), process.getId());

        // статус - ошибка, херашибка и т.п.
        if (topic.getStatusId() > 0)
            paramDao.updateParamList(process.getId(), statusParamId, Collections.singleton(topic.getStatusId()));

        Set<Integer> on = Collections.singleton(1);
        Set<Integer> empty = Collections.emptySet();

        // автозакрытие
        paramDao.updateParamList(process.getId(), autoCloseParamId, topic.isAutoClose() ? on : empty);

        // входит в пакет
        if (packageParamId > 0)
            paramDao.updateParamList(process.getId(), packageParamId, topic.isInPackage() ? on : empty);

        // стоимость
        paramDao.updateParamText(process.getId(), costParamId, Utils.format(topic.getCost()));

        boolean firstMessageAddInDescription = addFirstMessageInDescription && messageMap.size() == 0;

        // добавление недостающих сообщений
        for (HdMessage topicMessage : hdMessages) {
            Message message = messageMap.get(topicMessage.getId());
            // сообщения нет
            if (message == null) {
                message = new Message();
                message.setTypeId(id);
                message.setProcessId(process.getId());
                message.setSystemId(String.valueOf(topicMessage.getId()));
                message.setDirection(topicMessage.getDirection());

                if (message.getDirection() == Message.DIRECTION_OUTGOING) {
                    int crmUserId = dbInfo.getCrmUserId(topicMessage.getUserIdFrom());
                    if (crmUserId > 0)
                        message.setUserId(crmUserId);
                }

                topicMessage = hdDao.getMessage(topic.getId(), topicMessage.getId());

                if (firstMessageAddInDescription) {
                    // возможно, что-то поменяли в базе
                    process = processDao.getProcess(process.getId());

                    process.setDescription(process.getDescription() + "\n" + topicMessage.getText());
                    processDao.updateProcess(process);

                    firstMessageAddInDescription = false;
                }

                message.setText(topicMessage.getText());
                message.setFromTime(topicMessage.getTimeFrom());
                message.setFrom("");
                message.setTo("");
                for (FileData attach : topicMessage.getAttachList())
                    message.addAttach(attach);

                messageDao.updateMessage(message);

                if (newMessageEvent && message.getDirection() == Message.DIRECTION_INCOMING)
                    // событие о новом сообщении
                    EventProcessor.processEvent(new ProcessMessageAddedEvent(DynActionForm.SYSTEM_FORM, message, process),
                            new SingleConnectionSet(con));
            }

            // отметка сообщения прочитанным
            if (topicMessage.getTimeTo() != null && message.getToTime() == null) {
                message.setToTime(topicMessage.getTimeTo());
                if (message.getDirection() == Message.DIRECTION_INCOMING) {
                    int crmUserId = dbInfo.getCrmUserId(topicMessage.getUserIdTo());
                    if (crmUserId > 0)
                        message.setUserId(crmUserId);
                }
                messageDao.updateMessageProcess(message);
            }
        }
        return topic;
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

    @Override
    public boolean isEditable(Message message) {
        // исходящее но не прочитанное ещё сообщение
        return message.getDirection() == Message.DIRECTION_OUTGOING && message.getToTime() == null;
    }

    @Override
    public boolean isReadable() {
        return false;
    }

    @Override
    public void updateMessage(Connection con, DynActionForm form, Message message) throws Exception {
        ProcessLinkDAO linkDao = new ProcessLinkDAO(con);

        int processId = message.getProcessId();

        CommonObjectLink topicLink = Utils.getFirst(linkDao.getObjectLinksWithType(processId, getObjectType()));
        if (topicLink == null) {
            throw new BGException("К процессу не привязан топик HelpDesk.");
        }

        int topicId = topicLink.getLinkObjectId();

        HelpDeskDAO hdDao = new HelpDeskDAO(form.getUser(), getDbInfo());

        HdMessage msg = new HdMessage();
        msg.setId(Utils.parseInt(message.getSystemId()));
        msg.setDirection(Message.DIRECTION_OUTGOING);
        msg.setText(message.getText());

        hdDao.updateMessage(topicId, msg);

        message.setSystemId(String.valueOf(msg.getId()));

        Map<Integer, FileInfo> tmpFiles = SessionTemporaryFiles.getFiles(form, "tmpFileId");
        for (FileInfo fileInfo : tmpFiles.values()) {
            ByteArrayOutputStream out = new ByteArrayOutputStream(1000000);
            IOUtils.copy(fileInfo.inputStream, out);
            out.close();
            fileInfo.inputStream.close();

            hdDao.putAttach(msg.getId(), fileInfo.title, out.toByteArray());
        }

        // вложение выбираются из хелпдеска
        message.getAttachList().clear();

        msg = hdDao.getMessage(topicId, msg.getId());
        for (FileData attach : msg.getAttachList()) {
            message.addAttach(attach);
        }

        message.setFrom("");
        message.setTo("");

        new MessageDAO(con).updateMessage(message);

        SessionTemporaryFiles.deleteFiles(form, tmpFiles.keySet());
    }
}