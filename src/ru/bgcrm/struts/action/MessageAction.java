package ru.bgcrm.struts.action;

import java.io.IOException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import ru.bgcrm.cache.ProcessTypeCache;
import ru.bgcrm.cache.UserCache;
import ru.bgcrm.cache.UserNewsCache;
import ru.bgcrm.dao.CommonDAO;
import ru.bgcrm.dao.message.MessageDAO;
import ru.bgcrm.dao.message.MessageType;
import ru.bgcrm.dao.message.MessageTypeSearch;
import ru.bgcrm.dao.message.config.MessageTypeConfig;
import ru.bgcrm.dao.process.ProcessDAO;
import ru.bgcrm.dao.process.ProcessLinkDAO;
import ru.bgcrm.dao.user.UserDAO;
import ru.bgcrm.event.EventProcessor;
import ru.bgcrm.event.MessageRemovedEvent;
import ru.bgcrm.model.BGException;
import ru.bgcrm.model.BGMessageException;
import ru.bgcrm.model.CommonObjectLink;
import ru.bgcrm.model.SearchResult;
import ru.bgcrm.model.config.TagConfig;
import ru.bgcrm.model.message.Message;
import ru.bgcrm.model.process.Process;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.Preferences;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.sql.ConnectionSet;

public class MessageAction extends BaseAction {
    public static final String UNPROCESSED_MESSAGES_PERSONAL_KEY = "unprocessedMessages";

    @Override
    protected ActionForward unspecified(ActionMapping mapping, DynActionForm form, ConnectionSet conSet)
            throws Exception {
        return message(mapping, form, conSet);
    }

    public ActionForward message(ActionMapping mapping, DynActionForm form, ConnectionSet conSet) throws Exception {
        var request = form.getHttpRequest();
        var config = setup.getConfig(MessageTypeConfig.class);

        var replyToId = form.getParamInt("replyToId");
        if (replyToId > 0) {
            var message = new MessageDAO(conSet.getConnection()).getMessageById(replyToId);
            if (message == null)
                throw new BGException("Message not found: " + replyToId);
            
            var type = config.getTypeMap().get(message.getTypeId());
            if (type == null) 
                throw new BGException("Message type not found: " + replyToId);

            form.getResponse().setData("message", type.getAnswerMessage(message));

            return data(conSet, mapping, form, "processMessageEdit");
        }
        else {
            Message message = null;
            MessageType type = null;
            
            restoreRequestParams(conSet.getConnection(), form, true, false, "messageTypeAdd");

            int typeId = form.getParamInt("typeId");
            String messageId = form.getParam("messageId");

            // открытие существующего сообщения
            if (form.getId() > 0) {
                var messageDao = new MessageDAO(conSet.getConnection());

                message = messageDao.getMessageById(form.getId());
                type = config.getTypeMap().get(message.getTypeId());
                
                var tagConfig = setup.getConfig(TagConfig.class);
                if (tagConfig != null && !tagConfig.getTagList().isEmpty())
                    form.setResponseData("messageTagIds", messageDao.getMessageTags(form.getId()));
            }
            // нвового сообщения
            else if (typeId > 0 && Utils.notBlankString(messageId)) {
                type = config.getTypeMap().get(typeId);
                if (type == null) 
                    throw new BGException("Не найден тип сообщения.");

                message = type.newMessageGet(conSet, messageId);
            }

            if (message != null)
                form.getResponse().setData("message", message);

            // немного нечёткая логика, это для случая открытия сообщения на обработу
            if (type != null) {
                int searchId = form.getParamInt("searchId", 1);

                // автоматически ищет первым поиском
                if (CollectionUtils.isNotEmpty(type.getSearchMap().values())) {
                    MessageTypeSearch search = type.getSearchMap().get(searchId);

                    Set<CommonObjectLink> searchedList = new LinkedHashSet<CommonObjectLink>();
                    search.search(form, conSet, message, searchedList);
                    request.setAttribute("searchedList", searchedList);
                }

                request.setAttribute("typeTreeRoot", ProcessTypeCache.getTypeTreeRoot());
            }

            return data(conSet, mapping, form, "message");
        }
    }

    public ActionForward messageUpdateProcess(ActionMapping mapping, DynActionForm form, Connection con) throws BGException {
        MessageTypeConfig config = setup.getConfig(MessageTypeConfig.class);

        MessageDAO messageDao = new MessageDAO(con);

        Message message = null;
        if (form.getId() > 0)
            message = new MessageDAO(con).getMessageById(form.getId());
        else {
            int typeId = form.getParamInt("typeId");
            String messageId = form.getParam("messageId");

            MessageType type = config.getTypeMap().get(typeId);

            message = type.newMessageLoad(con, messageId);
        }

        if (message == null)
            throw new BGException("Сообщение не найдено.");

        message.setProcessed(true);

        form.getResponse().setData("id", message.getId());

        int processId = form.getParamInt("processId", -1);
        if (processId >= 0) {
            message.setProcessId(processId);

            if (processId > 0) {
                int contactSaveMode = form.getParamInt("contactSaveMode");

                MessageType type = config.getTypeMap().get(message.getTypeId());

                Process process = new ProcessDAO(con).getProcess(processId);
                if (process == null)
                    throw new BGException("Процесс не найден.");

                if (form.getParamBoolean("notification", false))
                    messageDao.updateMessage(type.messageLinkedToProcess(message));
                else if (contactSaveMode > 0)
                    type.getContactSaver().saveContact(form, con, message, process, contactSaveMode);
            }
        }

        messageDao.updateMessageProcess(message);

        return status(con, form);
    }
    
    public ActionForward messageUpdateTags(ActionMapping mapping, DynActionForm form, ConnectionSet conSet) throws Exception {
        Connection con = conSet.getConnection();

        new MessageDAO(con).updateMessageTags(form.getId(), form.getSelectedValues("tagId"));

        return status(conSet, form);
    }

    public ActionForward messageUpdateProcessToCopy(ActionMapping mapping, DynActionForm form, Connection con) throws Exception {
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
        else
            linkDao.addLink(new CommonObjectLink(process.getId(), linkType, newProcess.getId(), ""));

        message.setProcessId(newProcess.getId());
        message.setText(l.l("Перенесено из процесса #%s", process.getId()) + "\n\n" + message.getText());

        messageDao.updateMessage(message);

        form.setResponseData("process", newProcess);

        return status(con, form);
    }

    public ActionForward messageDelete(ActionMapping mapping, DynActionForm form, ConnectionSet conSet)
            throws Exception {
        MessageTypeConfig config = setup.getConfig(MessageTypeConfig.class);

        Map<MessageType, List<String>> typeSystemIds = new HashMap<>(10);
        for (String pair : form.getParamArray("typeId-systemId")) {
            int typeId = Utils.parseInt(StringUtils.substringBefore(pair, "-"));
            
            MessageType type = config.getTypeMap().get(typeId);
            if (type == null)
                throw new BGException("Не найден тип сообщения.");

            List<String> systemIds = typeSystemIds.get(type);
            if (systemIds == null)
                typeSystemIds.put(type, systemIds = new ArrayList<>(10));
            
            systemIds.add(StringUtils.substringAfter(pair, "-"));
        }
        
        // если нет разрешения на удаления чужих, проверим, чтобы все сообщения принадлежали ему.
        if (UserCache.getPerm(form.getUserId(), "ru.bgcrm.struts.action.MessageAction:deleteEditOtherUsersNotes") == null) {
            MessageDAO messageDao = new MessageDAO(conSet.getConnection());
            for (Integer sysId : typeSystemIds.values().stream().flatMap(List::stream).map(Utils::parseInt).collect(Collectors.toSet())) {
                Message message = messageDao.getMessageById(sysId);
                if (message != null && message.getUserId() != form.getUserId()) {
                    throw new BGMessageException("Удаление чужих сообщений запрещено!");
                }
            }
        }
        
        for (Map.Entry<MessageType, List<String>> me : typeSystemIds.entrySet())
            me.getKey().messageDelete(conSet, me.getValue().toArray(new String[me.getValue().size()]));

        EventProcessor.processEvent(new MessageRemovedEvent(form, form.getId()), conSet);

        return data(conSet, mapping, form, "message");
    }

    public ActionForward messageUpdate(ActionMapping mapping, DynActionForm form, ConnectionSet conSet)
            throws Exception {
        MessageTypeConfig config = setup.getConfig(MessageTypeConfig.class);

        MessageType type = config.getTypeMap().get(form.getParamInt("typeId"));
        if (type == null)
            throw new BGException("Не определён тип сообщения.");
        
        // сохранение типа сообщения, чтобы в следующий раз выбрать в редакторе его
        if (form.getId() <= 0) {
            form.setParam("messageTypeAdd", String.valueOf(type.getId()));
            restoreRequestParams(conSet.getConnection(), form, false, true, "messageTypeAdd");
        }

        Message message = new Message();
        if (form.getId() > 0)
            message = new MessageDAO(conSet.getConnection()).getMessageById(form.getId());

        if (message.getId() > 0 && message.getUserId() != form.getUserId()) {
            if (UserCache.getPerm(form.getUserId(), "ru.bgcrm.struts.action.MessageAction:deleteEditOtherUsersNotes") == null) {
                throw new BGMessageException("Редактирование чужих сообщений запрещено!");
            }
        }
        
        message.setId(form.getId());
        message.setUserId(form.getUserId());
        message.setTypeId(type.getId());
        message.setDirection(Message.DIRECTION_OUTGOING);
        message.setFromTime(new Date());
        message.setProcessId(form.getParamInt("processId"));
        message.setSubject(form.getParam("subject"));
        message.setTo(form.getParam("to"));
        message.setText(form.getParam("text"));

        String systemId = form.getParam("systemId");
        if (Utils.notBlankString(systemId))
            message.setSystemId(systemId);

        type.updateMessage(conSet.getConnection(), form, message);
        
        message.getAttachList().forEach(a -> { // remove output Fix
            try {
                if( a.getOutputStream() != null ) {
                    a.getOutputStream().close();
                    a.setOutputStream(null);
                }
            } catch (IOException e) {
                log.error(e.getMessage(),e);
            }
        });

        if (form.getParamBoolean("updateTags")) {
            form.setParam("id", String.valueOf(message.getId()));
            messageUpdateTags(mapping, form, conSet);
        }

        form.getResponse().setData("message", message);

        return status(conSet, form);
    }

    private static final ThreadPoolExecutor threadPool = new ThreadPoolExecutor(0, 10, 10, TimeUnit.MINUTES,
            new LinkedBlockingQueue<Runnable>());

    public ActionForward messageList(ActionMapping mapping, DynActionForm form, final ConnectionSet conSet)
            throws BGException {
        restoreRequestParams(conSet.getConnection(), form, true, true, "order", "typeId");
        
        boolean processed = form.getParamBoolean("processed", false);
        final boolean reverseOrder = form.getParamBoolean("order", true);
        
        Set<Integer> allowedTypeIds = Utils.toIntegerSet(form.getPermission().get("allowedTypeIds", ""));
        
        SortedMap<Integer, MessageType> typeMap =  Maps.filterKeys(
                setup.getConfig(MessageTypeConfig.class).getTypeMap(), 
                k -> allowedTypeIds.isEmpty() || allowedTypeIds.contains(k));

        if (processed) {
            MessageDAO messageDao = new MessageDAO(conSet.getConnection());
            messageDao.searchMessageList(new SearchResult<Message>(form), null, form.getParamInt("typeId"),
                    Message.DIRECTION_INCOMING, true, form.getParamBoolean("attach", null),
                    form.getParamDate("dateFrom", null), form.getParamDate("dateTo", null),
                    CommonDAO.getLikePatternSub(form.getParam("from", null)), reverseOrder);
        } else {
            final List<Message> resultConc = new CopyOnWriteArrayList<>();
            final AtomicInteger unprocessedCount = new AtomicInteger();
            final Map<Integer, Integer> unprocessedCountMap = new ConcurrentHashMap<>();

            int typeId = form.getParamInt("typeId", -1);

            List<Callable<Object>> todo = new ArrayList<>();

            for (final MessageType type : typeMap.values()) {
                if (typeId <= 0 || typeId == type.getId()) {
                    todo.add(Executors.callable(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                resultConc.addAll(type.newMessageList(conSet));
                                Integer count = type.getUnprocessedMessagesCount();
                                if (count != null && count > 0) {
                                    unprocessedCount.addAndGet(count);
                                    unprocessedCountMap.put(type.getId(), count);
                                }
                            } catch (Exception e) {
                                log.error(e.getMessage(), e);
                            }
                        }
                    }));
                }
            }

            try {
                threadPool.invokeAll(todo);
            } catch (Exception e) {
                throw new BGException(e);
            }

            List<Message> result = new ArrayList<>(resultConc);

            Collections.sort(result, new Comparator<Message>() {
                @Override
                public int compare(Message o1, Message o2) {
                    if (reverseOrder) {
                        Message tmp = o1;
                        o1 = o2;
                        o2 = tmp;
                    }

                    return o1.getFromTime() == null ? -1 : o1.getFromTime().compareTo(o2.getFromTime());
                }
            });

            form.getResponse().setData("list", result);
            form.getResponse().setData("unprocessedCountMap", unprocessedCountMap);
            
            Preferences prefs = new Preferences();
            prefs.put(UNPROCESSED_MESSAGES_PERSONAL_KEY, String.valueOf(unprocessedCount.get()));
            new UserDAO(conSet.getConnection()).updatePersonalization(form.getUser(), prefs);
            
            // полный сброс кэша, довольно дорогая операция
            UserNewsCache.flush(conSet.getConnection());
        }
        
        form.getHttpRequest().setAttribute("typeMap", typeMap);

        return data(conSet, mapping, form, "messageList");
    }

    public ActionForward processMessageList(ActionMapping mapping, DynActionForm form, ConnectionSet conSet)
            throws Exception {
        int tagId = form.getParamInt("tagId");
        int processId = form.getParamInt("processId");
        List<Integer> processIds = Lists.newArrayList(processId);

        Set<String> linkProcess = Utils.toSet(form.getParam("linkProcess"));
        if (!linkProcess.isEmpty()) {
            List<Integer> linkProcessIds = new ProcessLinkDAO(conSet.getSlaveConnection())
                .getObjectLinksWithType(processId, "process%").stream()
                .filter(l -> linkProcess.contains(l.getLinkedObjectType()))
                .map(CommonObjectLink::getLinkedObjectId).collect(Collectors.toList());
            processIds.addAll(linkProcessIds);
        }

        log.debug("processIds: %s", processIds);

        MessageDAO messageDao = new MessageDAO(conSet.getConnection());
        messageDao.searchMessageList(new SearchResult<>(form),
                processIds, null, null, null, tagId == TagConfig.Tag.TAG_ATTACH_ID ? true : null,
                form.getParamDate("dateFrom", null), form.getParamDate("dateTo", null), form.getParam("from", null),
                true, tagId > 0 ? Collections.singleton(tagId) : null);
        
        Map<Integer, Set<Integer>> messageTagMap = messageDao.getProcessMessageTagMap(processIds);
        form.setResponseData("messageTagMap", messageTagMap);

        Set<Integer> tagIds = messageTagMap.values().stream().flatMap(mt -> mt.stream()).collect(Collectors.toSet());
        form.setResponseData("tagIds", tagIds);
        
        return data(conSet, mapping, form, "processMessageList");
    }

    public ActionForward newMessageLoad(ActionMapping mapping, DynActionForm form, ConnectionSet conSet)
            throws BGException {
        MessageTypeConfig config = setup.getConfig(MessageTypeConfig.class);

        int typeId = form.getParamInt("typeId");
        String messageId = form.getParam("messageId");

        MessageType type = config.getTypeMap().get(typeId);
        if (type == null)
            throw new BGException("Не найден тип сообщений:" + typeId);

        type.newMessageLoad(conSet.getConnection(), messageId);

        return status(conSet, form);
    }
}