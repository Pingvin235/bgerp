package ru.bgcrm.struts.action;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.struts.action.ActionForward;
import org.bgerp.action.base.BaseAction;
import org.bgerp.app.cfg.Preferences;
import org.bgerp.app.event.EventProcessor;
import org.bgerp.app.exception.BGException;
import org.bgerp.app.exception.BGMessageException;
import org.bgerp.cache.ProcessTypeCache;
import org.bgerp.dao.message.MessageSearchDAO;
import org.bgerp.event.ProcessFilesEvent;
import org.bgerp.model.Pageable;
import org.bgerp.model.process.link.ProcessLinkProcess;
import org.bgerp.util.Dynamic;
import org.bgerp.util.sql.LikePattern;

import com.google.common.collect.Maps;

import javassist.NotFoundException;
import ru.bgcrm.dao.message.MessageDAO;
import ru.bgcrm.dao.message.MessageType;
import ru.bgcrm.dao.message.MessageTypeSearch;
import ru.bgcrm.dao.process.ProcessDAO;
import ru.bgcrm.dao.process.ProcessLinkDAO;
import ru.bgcrm.dao.user.UserDAO;
import ru.bgcrm.event.MessageRemovedEvent;
import ru.bgcrm.event.link.LinkAddedEvent;
import ru.bgcrm.event.process.ProcessMessageAddedEvent;
import ru.bgcrm.model.CommonObjectLink;
import ru.bgcrm.model.message.Message;
import ru.bgcrm.model.message.TagConfig;
import ru.bgcrm.model.message.config.MessageTypeConfig;
import ru.bgcrm.model.process.Process;
import ru.bgcrm.servlet.ActionServlet.Action;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.sql.ConnectionSet;
import ru.bgcrm.util.sql.SingleConnectionSet;

@Action(path = "/user/message")
public class MessageAction extends BaseAction {
    private static final String PATH_JSP = PATH_JSP_USER + "/message";
    /**
     * Maximum messages deleted at once.
     */
    private static final int MAX_MESSAGE_DELETE_QNT = 1000;
    /**
     * Key for storing counter in personalization map.
     */
    public static final String UNPROCESSED_MESSAGES_PERSONAL_KEY = "unprocessedMessages";
    /**
     * Special action for edit and delete not owned messages.
     */
    @Dynamic
    public static final String ACTION_MODIFY_NOT_OWNED = "org.bgerp.action.MessageAction:modifyNotOwned";

    @Override
    public ActionForward unspecified(DynActionForm form, ConnectionSet conSet) throws Exception {
        return message(form, conSet);
    }

    public ActionForward message(DynActionForm form, ConnectionSet conSet) throws Exception {
        int typeId = form.getParamInt("typeId");
        String messageId = form.getParam("messageId");

        Message message = null;

        // processed message
        if (form.getId() > 0) {
            var messageDao = new MessageDAO(conSet.getConnection());

            message = messageDao.getMessageById(form.getId());

            if (message == null)
                throw new NotFoundException("Not found message with ID: " + form.getId());
        }
        // new message, not loaded
        else if (typeId > 0 && Utils.notBlankString(messageId)) {
            var type = getType(typeId);

            message = type.newMessageGet(conSet, messageId);

            if (message == null)
                throw new NotFoundException("Not found message with System ID: " + messageId);

            linksSearch(form, conSet);

            // TODO: Only types with group of the current executor.
            form.setRequestAttribute("typeTreeRoot", ProcessTypeCache.getTypeTreeRoot());
        }

        form.setResponseData("message", message);

        return html(conSet, form, PATH_JSP + "/message.jsp");
    }

    /**
     * Important, called also in {@link #processCreate(DynActionForm, ConnectionSet)}
     */
    public ActionForward messageUpdateProcess(DynActionForm form, Connection con) throws Exception {
        MessageTypeConfig config = setup.getConfig(MessageTypeConfig.class);

        MessageDAO messageDao = new MessageDAO(con);

        Message message = null;
        if (form.getId() > 0)
            message = new MessageDAO(con).getMessageById(form.getId());
        else {
            int typeId = form.getParamInt("messageTypeId");
            String messageId = form.getParam("messageId");

            MessageType type = config.getTypeMap().get(typeId);

            message = type.newMessageLoad(con, messageId);
        }

        if (message == null)
            throw new BGMessageException("Сообщение не найдено.");

        form.setResponseData("id", message.getId());

        int processId = form.getParamInt("processId", -1);
        // TODO: Check, how setting process ID = 0 is handled for E-Mails.
        if (processId >= 0) {
            message.setProcessId(processId);

            if (processId > 0) {
                int contactSaveMode = form.getParamInt("contactSaveMode");

                MessageType type = config.getTypeMap().get(message.getTypeId());

                Process process = new ProcessDAO(con).getProcess(processId);
                if (process == null)
                    throw new BGException("Process not found.");

                if (form.getParamBoolean("notification", false))
                    messageDao.updateMessage(type.messageLinkedToProcess(message));
                else if (contactSaveMode > 0)
                    type.getContactSaver().saveContact(form, con, message, process, contactSaveMode);

                EventProcessor.processEvent(new ProcessMessageAddedEvent(form, message, process), new SingleConnectionSet(con));
            }
        }

        messageDao.updateMessageProcess(message);

        if (message.getId() > 0)
            form.setResponseData("messageId", message.getId());

        return json(con, form);
    }

    public ActionForward messageUpdateTags(DynActionForm form, Connection con) throws Exception {
        new MessageDAO(con).updateMessageTags(form.getId(), form.getParamValues("tagId"), true);

        return json(con, form);
    }

    public ActionForward messageToggleTags(DynActionForm form, Connection con) throws SQLException {
        new MessageDAO(con).toggleMessageTags(form.getId(), form.getParamValues("tagId"), form.getParamBoolean("add"));

        return json(con, form);
    }

    public ActionForward messageUpdateRead(DynActionForm form, Connection con) throws Exception {
        var dao = new MessageDAO(con);

        Message m = dao.getMessageById(form.getId());
        if (m == null)
            throw new NotFoundException("Not found message with ID: " + form.getId());

        if (form.getParamBoolean("value")) {
            m.setToTime(new Date());
            if (m.getUserId() <= 0)
                m.setUserId(form.getUserId());
        }
        else
            m.setToTime(null);

        dao.updateMessage(m);

        return json(con, form);
    }

    public ActionForward messageUpdateProcessToCopy(DynActionForm form, Connection con) throws Exception {
        MessageDAO messageDao = new MessageDAO(con);
        ProcessDAO processDao = new ProcessDAO(con);
        ProcessLinkDAO linkDao = new ProcessLinkDAO(con);

        Message message = messageDao.getMessageById(form.getId());
        if (message == null)
            throw new BGMessageException("Сообщение не найдено.");

        Process process = processDao.getProcess(message.getProcessId());
        if (process == null)
            throw new BGMessageException("Процесс не найден.");

        String linkType = form.getParam("linkType");

        Process newProcess = new Process();
        newProcess.setTypeId(process.getTypeId());
        newProcess.setStatusId(process.getStatusId());
        newProcess.setStatusUserId(form.getUserId());
        newProcess.setDescription(message.getSubject());
        newProcess.setCreateUserId(form.getUserId());
        newProcess.setCreateUserId(form.getUserId());

        processDao.updateProcess(newProcess);

        processDao.updateProcessGroups(process.getGroups(), newProcess.getId());
        processDao.updateProcessExecutors(process.getExecutors(), newProcess.getId());

        if (StringUtils.isBlank(linkType))
            linkDao.copyLinks(process.getId(), newProcess.getId(), null);
        else {
            linkDao.addLink(new ProcessLinkProcess(process.getId(), linkType, newProcess.getId()));
            if (Process.LINK_TYPE_DEPEND.equals(linkType)) {
                for (var linkedProcess : linkDao.getLinkedProcessList(process.getId(), Process.LINK_TYPE_MADE, false, null)) {
                    linkDao.addLink(new ProcessLinkProcess.Made(linkedProcess.getId(), newProcess.getId()));
                }
            }
        }

        message.setProcessId(newProcess.getId());
        message.setText(l.l("Перенесено из процесса #{}", process.getId()) + "\n\n" + message.getText());

        messageDao.updateMessage(message);

        form.setResponseData("process", newProcess);

        return json(con, form);
    }

    public ActionForward messageDelete(DynActionForm form, ConnectionSet conSet) throws Exception {
        MessageTypeConfig config = setup.getConfig(MessageTypeConfig.class);

        int cnt = 0;

        Map<MessageType, List<String>> typeSystemIds = new HashMap<>(10);
        for (String pair : form.getParamValuesListStr("typeId-systemId")) {
            // to avoid too long processing
            if (++cnt > MAX_MESSAGE_DELETE_QNT)
                break;

            int typeId = Utils.parseInt(StringUtils.substringBefore(pair, "-"));

            MessageType type = config.getTypeMap().get(typeId);
            if (type == null)
                throw new BGException("Not found message type with ID: {}", typeId);

            typeSystemIds
                .computeIfAbsent(type, (unused) -> new ArrayList<>(10))
                .add(StringUtils.substringAfter(pair, "-"));
        }

        // if no the special permission checking all the messages for ownerships
        if (!form.getUser().checkPerm(ACTION_MODIFY_NOT_OWNED)) {
            MessageDAO messageDao = new MessageDAO(conSet.getConnection());
            for (Integer sysId : typeSystemIds.values().stream().flatMap(List::stream).map(Utils::parseInt).collect(Collectors.toSet())) {
                Message message = messageDao.getMessageById(sysId);
                if (message != null && message.getUserId() != form.getUserId()) {
                    throw new BGException("Deletion not own messages is not allowed");
                }
            }
        }

        for (Map.Entry<MessageType, List<String>> me : typeSystemIds.entrySet())
            me.getKey().messageDelete(conSet, me.getValue().toArray(new String[me.getValue().size()]));

        EventProcessor.processEvent(new MessageRemovedEvent(form, form.getId()), conSet);

        return html(conSet, form, PATH_JSP + "/message.jsp");
    }

    public ActionForward messageList(DynActionForm form, final ConnectionSet conSet) throws Exception {
        restoreRequestParams(conSet.getConnection(), form, true, true, "order", "typeId");

        boolean processed = form.getParamBoolean("processed", false);
        final boolean reverseOrder = form.getParamBoolean("order", true);

        Set<Integer> allowedTypeIds = Utils.toIntegerSet(form.getPermission().get("allowedTypeIds", ""));

        var config = setup.getConfig(MessageTypeConfig.class);
        form.setRequestAttribute("config", config);

        var typeMap =  Maps.filterKeys(
                config.getTypeMap(),
                k -> allowedTypeIds.isEmpty() || allowedTypeIds.contains(k));

        int typeId = form.getParamInt("typeId", -1);

        if (processed) {
            new MessageSearchDAO(conSet.getConnection())
                .withTypeId(typeId).withDirection(Message.DIRECTION_INCOMING)
                .withProcessed(true)
                .withRead(form.getParamBoolean("read", null))
                .withAttach(form.getParamBoolean("attach", null))
                .withDateFrom(form.getParamDate("dateFrom", null), form.getParamDate("dateTo", null))
                .withFrom(LikePattern.SUB.get(form.getParam("from")))
                .order(reverseOrder ? MessageSearchDAO.Order.FROM_TIME_DESC : MessageSearchDAO.Order.FROM_TIME)
                .search(new Pageable<>(form));
        } else {
            // when external system isn't available, an empty table of messages should be however shown
            try {
                var executors = Executors.newFixedThreadPool(typeId <= 0 ? typeMap.size() : 1);

                List<Message> result = Collections.synchronizedList(new ArrayList<>(1000));

                for (final MessageType type : typeMap.values()) {
                    if (typeId > 0 && typeId != type.getId())
                        continue;

                    executors.execute(() -> {
                        try {
                            result.addAll(type.newMessageList(conSet));
                        } catch (Exception e) {
                            log.error(e);
                        }
                    });
                }

                executors.shutdown();

                if (!executors.awaitTermination(2, TimeUnit.MINUTES))
                    log.error("Timeout waiting threads");

                Collections.sort(result, (Message o1, Message o2) -> {
                    if (reverseOrder) {
                        Message tmp = o1;
                        o1 = o2;
                        o2 = tmp;
                    }
                    return o1.getFromTime() == null ? -1 : o1.getFromTime().compareTo(o2.getFromTime());
                });

                form.setResponseData("list", result);

                Preferences prefs = new Preferences();
                prefs.put(UNPROCESSED_MESSAGES_PERSONAL_KEY, String.valueOf(config.getUnprocessedMessagesCount()));
                new UserDAO(conSet.getConnection()).updatePersonalization(form.getUser(), prefs);
            } catch (Exception e) {
                log.error(e);
            }
        }

        form.setRequestAttribute("typeMap", typeMap);

        return html(conSet, form, PATH_JSP + "/list.jsp");
    }

    public ActionForward newMessageLoad(DynActionForm form, ConnectionSet conSet) throws Exception {
        MessageTypeConfig config = setup.getConfig(MessageTypeConfig.class);

        int typeId = form.getParamInt("typeId");
        String messageId = form.getParam("messageId");

        MessageType type = config.getTypeMap().get(typeId);
        if (type == null)
            throw new BGException("Message type not found:" + typeId);

        type.newMessageLoad(conSet.getConnection(), messageId);

        return json(conSet, form);
    }

    private void linksSearch(DynActionForm form, ConnectionSet conSet) throws Exception {
        var type = getType(form.getParamInt("typeId"));
        var message = type.newMessageGet(conSet, form.getParam("messageId"));

        final var searchMap = type.getSearchMap();

        int searchId = form.getParamInt("searchId");
        // explicitly defined search
        if (searchId > 0) {
            if (CollectionUtils.isNotEmpty(searchMap.values())) {
                MessageTypeSearch search = type.getSearchMap().get(searchId);

                Set<CommonObjectLink> searchedList = new LinkedHashSet<>();
                search.search(form, conSet, message, searchedList);
                form.setResponseData("searchedList", searchedList);
            }
        }
        // only searches without JSP
        else {
            var searches = searchMap.values().stream()
                .filter(s -> Utils.isBlankString(s.getJsp()))
                .collect(Collectors.toList());
            if (!searches.isEmpty()) {
                Set<CommonObjectLink> searchedList = Collections.synchronizedSet(new LinkedHashSet<>());

                var executors = Executors.newFixedThreadPool(searches.size());

                for (var search : searches) {
                    executors.execute(() -> {
                        try {
                            search.search(form, conSet, message, searchedList);
                        } catch (Exception e) {
                            log.error(e);
                        }
                    });
                }

                executors.shutdown();
                if (!executors.awaitTermination(2, TimeUnit.MINUTES))
                    log.error("Timeout waiting threads");

                form.setResponseData("searchedList", searchedList);
            }
        }
    }

    public ActionForward processCreate(DynActionForm form, ConnectionSet conSet) throws Exception {
        var con = conSet.getConnection();

        var process = ProcessAction.processCreateAndGet(form, con);

        var linkDao = new ProcessLinkDAO(con, form);
        for (String link : form.getParamValuesListStr("link")) {
            var tokens = link.split("\\*");
            if (tokens.length != 3) {
                log.warn("Incorrect link: '{}'", link);
                continue;
            }

            var olink = new CommonObjectLink(Process.OBJECT_TYPE, process.getId(), tokens[0], Utils.parseInt(tokens[1]),
                    tokens[2]);
            linkDao.addLink(olink);

            EventProcessor.processEvent(new LinkAddedEvent(form, olink), conSet);
        }

        form.setParam("processId", String.valueOf(process.getId()));
        // takes param messageTypeId, messageId from form object, and places ID or the created message to response
        messageUpdateProcess(form, con);

        return json(con, form);
    }

    private MessageType getType(int typeId) {
        var config = setup.getConfig(MessageTypeConfig.class);

        var type = config.getTypeMap().get(typeId);
        if (type == null)
            throw new BGException("Message type not found: " + typeId);

        return type;
    }

    public ActionForward processMessageList(DynActionForm form, ConnectionSet conSet) throws Exception {
        int tagId = form.getParamInt("tagId");
        int processId = form.getParamInt("processId");
        Set<Integer> processIds = new TreeSet<>(Collections.singleton(processId));

        Set<String> linkProcess = Utils.toSet(form.getParam("linkProcess"));
        if (!linkProcess.isEmpty()) {
            List<Integer> linkProcessIds = new ProcessLinkDAO(conSet.getSlaveConnection())
                .getObjectLinksWithType(processId, "process%").stream()
                .filter(l -> linkProcess.contains(l.getLinkObjectType()))
                .map(CommonObjectLink::getLinkObjectId).collect(Collectors.toList());
            processIds.addAll(linkProcessIds);
        }

        log.debug("processIds: {}", processIds);

        Set<Integer> allowedTypeIds = Utils.toIntegerSet(form.getPermission().get("allowedTypeIds", ""));

        new MessageSearchDAO(conSet.getConnection())
            .withProcessIds(processIds)
            .withTypeIds(allowedTypeIds)
            .withAttach(tagId == TagConfig.Tag.TAG_ATTACH_ID ? true : null)
            .withDateFrom(form.getParamDate("dateFrom"), form.getParamDate("dateTo"))
            .order(MessageSearchDAO.Order.PINNED_FIRST)
            .order(MessageSearchDAO.Order.FROM_TIME_DESC)
            .withTagId(tagId)
            .search(new Pageable<>(form));

        Map<Integer, Set<Integer>> messageTagMap = new MessageDAO(conSet.getConnection()).getProcessMessageTagMap(processIds);
        form.setResponseData("messageTagMap", messageTagMap);

        Set<Integer> tagIds = messageTagMap.values().stream().flatMap(mt -> mt.stream()).collect(Collectors.toSet());
        form.setResponseData("tagIds", tagIds);

        return html(conSet, form, PATH_JSP + "/process_message_list.jsp");
    }

    public ActionForward processMessageEdit(DynActionForm form, ConnectionSet conSet) throws Exception {
        MessageDAO dao = new MessageDAO(conSet.getSlaveConnection());

        restoreRequestParams(conSet.getConnection(), form, true, false, "messageTypeAdd");

        Message message = null;

        var replyToId = form.getParamInt("replyToId");
        if (replyToId > 0) {
            message = dao.getMessageById(replyToId);
            if (message == null)
                throw new BGException("Message not found: " + replyToId);
            message = getType(message.getTypeId()).getAnswerMessage(message);
        }
        else if (form.getId() > 0) {
            message = dao.getMessageById(form.getId());
        }

        var tagConfig = setup.getConfig(TagConfig.class);
        if (tagConfig != null)
            form.setResponseData("messageTagIds", dao.getMessageTags(form.getId()));

        if (message != null)
            form.setResponseData("message", message);

        var event = new ProcessFilesEvent(form, form.getParamInt("processId"));
        EventProcessor.processEvent(event, conSet);
        form.setRequestAttribute("files", event.getFiles());
        form.setRequestAttribute("announcedFiles", event.getAnnouncedFiles());

        return html(conSet, form, PATH_JSP + "/process_message_edit.jsp");
    }

    public ActionForward messageUpdate(DynActionForm form, Connection con) throws Exception {
        var type = getType(form.getParamInt("typeId"));

        // preserving message type for choosing in next usage of editor
        if (form.getId() <= 0) {
            form.setParam("messageTypeAdd", String.valueOf(type.getId()));
            restoreRequestParams(con, form, false, true, "messageTypeAdd");
        }

        Message message = new Message();
        if (form.getId() > 0)
            message = new MessageDAO(con).getMessageById(form.getId());

        if (message.getId() > 0 && message.getUserId() != form.getUserId() && !form.getUser().checkPerm(ACTION_MODIFY_NOT_OWNED)) {
            throw new BGException("Editing of not own messages is not allowed");
        }

        Set<Integer> allowedTypeIds = Utils.toIntegerSet(form.getPermission().get("allowedTypeIds", ""));
        if (message.getId() <= 0 && !allowedTypeIds.isEmpty() && !allowedTypeIds.contains(type.getId())) {
            throw new BGException("Message with the given type is not allowed to be created");
        }

        message.setId(form.getId());
        message.setUserId(form.getUserId());
        message.setTypeId(type.getId());
        message.setDirection(Message.DIRECTION_OUTGOING);
        message.setFromTime(new Date());
        message.setProcessId(form.getParamInt("processId"));
        message.setSubject(form.getParam("subject"));
        message.setText(form.getParam("text"));

        String systemId = form.getParam("systemId");
        if (Utils.notBlankString(systemId))
            message.setSystemId(systemId);

        type.updateMessage(con, form, message);

        if (form.getParamBoolean("updateTags"))
            new MessageDAO(con).updateMessageTags(message.getId(), form.getParamValues("tagId"), false);

        form.setResponseData("message", message);

        return json(con, form);
    }
}