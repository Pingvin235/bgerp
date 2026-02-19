package ru.bgcrm.plugin.bgbilling.event.listener;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bgerp.app.cfg.Setup;
import org.bgerp.app.event.EventProcessor;
import org.bgerp.app.exception.BGException;
import org.bgerp.app.exception.BGMessageException;
import org.bgerp.cache.UserCache;
import org.bgerp.dao.message.MessageSearchDAO;
import org.bgerp.model.Pageable;
import org.bgerp.model.msg.Message;
import org.bgerp.model.msg.config.MessageTypeConfig;

import ru.bgcrm.dao.message.MessageDAO;
import ru.bgcrm.dao.message.MessageType;
import ru.bgcrm.dao.process.ProcessLinkDAO;
import ru.bgcrm.event.ParamChangedEvent;
import ru.bgcrm.event.client.ProcessOpenEvent;
import ru.bgcrm.event.process.ProcessChangingEvent;
import ru.bgcrm.model.CommonObjectLink;
import ru.bgcrm.model.Pair;
import ru.bgcrm.model.process.Process;
import ru.bgcrm.model.process.ProcessExecutor;
import ru.bgcrm.model.user.User;
import ru.bgcrm.plugin.bgbilling.message.MessageTypeHelpDesk;
import ru.bgcrm.plugin.bgbilling.proto.dao.HelpDeskDAO;
import ru.bgcrm.plugin.bgbilling.proto.model.helpdesk.HdTopic;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.sql.ConnectionSet;

/*
 * Слушатель для интеграции с HelpDesk ами.
 */
public class HelpDeskListener {
    public HelpDeskListener() {
        EventProcessor.subscribe(this::processChanged, ProcessChangingEvent.class);
        EventProcessor.subscribe(this::paramChanged, ParamChangedEvent.class);
    }

    @SuppressWarnings("unchecked")
    private void paramChanged(ParamChangedEvent e, ConnectionSet conSet) throws BGMessageException {
        // TODO: Предварительно можно сделать отсев по кодам параметров, которые могут быть связаны с хелпдеском.
        if (!Process.OBJECT_TYPE.equals(e.getParameter().getObjectType())) {
            return;
        }

        int paramId = e.getParameter().getId();

        Pair<MessageTypeHelpDesk, Integer> pair = getTypeAndTopic(conSet.getConnection(), e.getObjectId());
        if (pair == null) {
            for (MessageTypeHelpDesk mt : getMessageTypes()) {
                if (paramId == mt.getStatusParamId() || paramId == mt.getCostParamId()) {
                    throw new BGMessageException(
                            "Данные параметры разрешено править только для процессов, связанных с HelpDesk темами.");
                }
            }
            return;
        }

        Integer topicId = pair.getSecond();

        MessageTypeHelpDesk mt = pair.getFirst();
        HelpDeskDAO hdDao = new HelpDeskDAO(e.getUser(), mt.getDbInfo());

        var topicWithMessages = hdDao.getTopicWithMessages(topicId);

        HdTopic topic = topicWithMessages != null ? topicWithMessages.getFirst() : null;
        if (topic == null) {
            throw new BGException("Не найден топик HelpDesk с кодом: " + topicId);
        }

        // статус (категория)
        if (paramId == mt.getStatusParamId()) {
            hdDao.setTopicStatus(topic.getContractId(), topicId, Utils.getFirst(((Map<Integer, String>) e.getValue()).keySet()));
        }
        // стоимость
        else if (paramId == mt.getCostParamId()) {
            hdDao.setTopicCost(topic.getContractId(), topicId, Utils.parseBigDecimal((String) e.getValue(), BigDecimal.ZERO));
        }
        // автозакрытие
        else if (paramId == mt.getAutoCloseParamId()) {
            hdDao.setTopicAutoClose(topic.getContractId(), topicId, ((Set<Integer>) e.getValue()).size() > 0);
        }
    }

    private void processChanged(ProcessChangingEvent e, ConnectionSet conSet) throws Exception {
        // при закрытии/открытии темы - закрытие в HelpDesk
        if (e.isOpening() || e.isClosing()) {
            Pair<MessageTypeHelpDesk, Integer> pair = getTypeAndTopic(conSet.getConnection(), e.getProcess().getId());
            if (pair != null) {
                new HelpDeskDAO(e.getUser(), pair.getFirst().getDbInfo()).setTopicState(pair.getSecond(), e.isClosing());
            }
        }
        // изменение исполнителей - установка в HelpDesk
        else if (e.isExecutors()) {
            Pair<MessageTypeHelpDesk, Integer> pair = getTypeAndTopic(conSet.getConnection(), e.getProcess().getId());
            if (pair != null) {
                Set<Integer> executors = ProcessExecutor.getExecutorsWithRole(e.getProcessExecutors(), 0);
                if (executors.size() > 1) {
                    throw new BGMessageException(
                            "Для процесса связанного с HelpDesk запрещена установка более одного исполнителя с ролью 0.");
                }

                User user = null;

                Integer userId = Utils.getFirst(executors);
                if (userId != null) {
                    user = UserCache.getUser(userId);
                }

                HelpDeskDAO hdDao = new HelpDeskDAO(e.getUser(), pair.getFirst().getDbInfo());
                if (user == null) {
                    hdDao.setTopicExecutor(pair.getSecond(), 0);
                } else if (user.getId() == e.getUser().getId()) {
                    hdDao.setTopicExecutorMe(pair.getSecond());
                } else {
                    int billingUserId = pair.getFirst().getDbInfo().loadUsers(e.getUser()).getBillingUserId(user.getId());
                    if (billingUserId <= 0) {
                        throw new BGException("Исполнителю " + user.getTitle() + " не сопоставлен пользователь биллинга.");
                    }
                    hdDao.setTopicExecutor(pair.getSecond(), billingUserId);
                }
            }
        }

        if (e.isStatus()) {
            // TODO: Также можно предварительно фильтровать по статусам.
            Pair<MessageTypeHelpDesk, Integer> pair = getTypeAndTopic(conSet.getConnection(), e.getProcess().getId());
            if (pair != null) {
                MessageTypeHelpDesk mt = pair.getFirst();
                if (mt.getMarkMessagesReadStatusIds().contains(e.getStatusChange().getStatusId())) {
                    Pageable<Message> searchResult = new Pageable<>();

                    new MessageSearchDAO(conSet.getConnection())
                        .withTypeId(mt.getId())
                        .withDirection(Message.DIRECTION_INCOMING)
                        .withProcessIds(Set.of(e.getProcess().getId()))
                        .search(searchResult);

                    MessageDAO messageDao = new MessageDAO(conSet.getConnection());

                    HelpDeskDAO hdDao = new HelpDeskDAO(e.getUser(), mt.getDbInfo());
                    for (Message msg : searchResult.getList()) {
                        if (msg.getToTime() != null) {
                            continue;
                        }

                        hdDao.markMessageRead(Utils.parseInt(msg.getSystemId()));

                        msg.setToTime(new Date());
                        msg.setUserId(e.getUser().getId());

                        messageDao.updateMessageProcess(msg);
                    }
                }
                e.getForm().getResponse().addEvent(new ProcessOpenEvent(e.getProcess().getId()));
            }
        }
    }

    private Pair<MessageTypeHelpDesk, Integer> getTypeAndTopic(Connection con, int processId) {
        ProcessLinkDAO linkDao = new ProcessLinkDAO(con);

        List<CommonObjectLink> linkList = linkDao.getObjectLinksWithType(processId, "bgbilling-helpdesk%");
        if (linkList.size() > 0) {
            Set<MessageTypeHelpDesk> messageTypes = getMessageTypes();
            for (MessageTypeHelpDesk mt : messageTypes) {
                for (CommonObjectLink link : linkList) {
                    if (link.getLinkObjectType().equals(mt.getObjectType())) {
                        return new Pair<>(mt, link.getLinkObjectId());
                    }
                }
            }
        }

        return null;
    }

    private Set<MessageTypeHelpDesk> getMessageTypes() {
        Set<MessageTypeHelpDesk> result = new HashSet<>();

        MessageTypeConfig config = Setup.getSetup().getConfig(MessageTypeConfig.class);
        for (MessageType type : config.getTypeMap().values()) {
            if (!(type instanceof MessageTypeHelpDesk)) {
                continue;
            }
            result.add((MessageTypeHelpDesk) type);
        }

        return result;
    }
}
