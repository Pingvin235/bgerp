package ru.bgcrm.plugin.phpbb.dao;

import java.sql.Connection;
import java.util.Date;

import org.bgerp.app.bean.annotation.Bean;
import org.bgerp.app.cfg.ConfigMap;
import org.bgerp.app.cfg.Setup;
import org.bgerp.app.db.sql.pool.ConnectionPool;
import org.bgerp.util.Log;

import ru.bgcrm.cache.ParameterCache;
import ru.bgcrm.cache.ProcessTypeCache;
import ru.bgcrm.dao.ParamValueDAO;
import ru.bgcrm.dao.message.MessageDAO;
import ru.bgcrm.dao.message.MessageType;
import ru.bgcrm.dao.process.ProcessDAO;
import ru.bgcrm.event.EventProcessor;
import ru.bgcrm.event.process.ProcessMessageAddedEvent;
import ru.bgcrm.model.BGException;
import ru.bgcrm.model.BGMessageException;
import ru.bgcrm.model.message.Message;
import ru.bgcrm.model.param.Parameter;
import ru.bgcrm.model.process.Process;
import ru.bgcrm.model.process.ProcessType;
import ru.bgcrm.plugin.phpbb.Plugin;
import ru.bgcrm.plugin.phpbb.model.Forum;
import ru.bgcrm.plugin.phpbb.model.Topic;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.sql.ConnectionSet;
import ru.bgcrm.util.sql.SQLUtils;
import ru.bgcrm.util.sql.SingleConnectionSet;

@Bean
public class MessageTypeForumPost extends MessageType {
    private static final Log log = Log.getLog();

    // TODO: Сохранять в конфигурации!!
    private volatile Date lastCheckTime = new Date();

    private final Parameter userNameParam;
    private final String topicMark;
    private final String topicMarkClosed;
    private final Forum forum;

    public MessageTypeForumPost(Setup setup, int id, ConfigMap config) throws BGException {
        super(setup, id, config.get("title"), config);

        userNameParam = ParameterCache.getParameter(config.getInt("userNameParamId", 0));
        topicMark = config.get("topicMark", " (CRM)");
        topicMarkClosed = config.get("topicMarkClosed", " (CRM:CLOSED)");

        forum = new Forum();
        forum.setId(String.valueOf(id));
        forum.setUrl(config.get("url"));
        forum.setConnectionPool(new ConnectionPool("phpbbForum-" + forum.getId(), config));
    }

    @Override
    public boolean isEditable(Message message) {
        return true;
    }

    @Override
    public boolean isRemovable(Message message) {
        return true;
    }

    @Override
    public String getHeaderJsp() {
        // <endpoint id="user.process.message.header.jsp" file="/WEB-INF/jspf/user/plugin/phpbb/process_message_header.jsp"/>
        return Plugin.ENDPOINT_MESSAGE_HEADER;
    }

    @Override
    public String getEditorJsp() {
        // <endpoint id="user.process.message.editor.jsp" file="/WEB-INF/jspf/user/plugin/phpbb/process_message_editor.jsp"/>
        return Plugin.ENDPOINT_MESSAGE_EDITOR;
    }

    @Override
    public String getProcessMessageHeaderColor(Message message) {
        return "#E9C4F5";
    }

    public String getPostUrl(String postId) {
        return forum.getUrl() + "/viewtopic.php?t=" + postId;
    }

    @Override
    public void messageDelete(ConnectionSet conSet, String... messageIds) throws Exception {
        for (String messageId : messageIds)
            new MessageDAO(conSet.getConnection()).deleteMessage(Utils.parseInt(messageId));
    }

    @Override
    public void updateMessage(Connection con, DynActionForm form, Message message) throws Exception {
        int systemId = Utils.parseInt(message.getSystemId());
        if (systemId <= 0)
            throw new BGMessageException("Код темы форума должен быть числовым.");

        MessageDAO messageDao = new MessageDAO(con);
        Message existingMessage = messageDao.getMessageBySystemId(id, String.valueOf(systemId));
        if (existingMessage != null && existingMessage.getId() != message.getId())
            throw new BGMessageException("Тема уже привязана, сообщение: " + existingMessage.getId() + "; процесс: "
                    + existingMessage.getProcessId());

        Process process = new ProcessDAO(con).getProcess(message.getProcessId());
        boolean processClosed = process != null && process.getCloseTime() != null;

        Connection forumCon = forum.getConnectionPool().getDBConnectionFromPool();
        try {
            ForumDAO forumDao = new ForumDAO(forumCon);
            Topic topic = forumDao.getTopic(systemId);
            if (topic == null)
                throw new BGMessageException("Указанная тема не найдена в форуме.");

            String markAdd = topicMark;
            String markRemove = topicMarkClosed;
            if (processClosed) {
                markAdd = topicMarkClosed;
                markRemove = topicMark;
            }

            if (!topic.getTitle().contains(markAdd)) {
                log.info("Marking topic: " + topic.getId());

                String titleWithMark = topic.getTitle().replace(markRemove, "") + markAdd;
                topic.setTitle(titleWithMark);
                forumDao.updateTopicTitle(topic.getId(), titleWithMark);

                forumCon.commit();
            }

            message.setFromTime(topic.getLastPostTime());
            // TODO: Может сделать позднее контроль прочитанных.
            message.setToTime(new Date());
            message.setSubject(topic.getTitle());
            message.setDirection(Message.DIRECTION_INCOMING);
            message.setFrom("");
            message.setTo("");
            message.setText("");
            messageDao.updateMessage(message);
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        } finally {
            SQLUtils.closeConnection(forumCon);
        }

        lastCheckTime = new Date();
    }

    @Override
    public void process() {
        log.debug("Processing forum: {}", forum.getId());

        Connection con = setup.getDBConnectionFromPool();
        Connection forumCon = forum.getConnectionPool().getDBConnectionFromPool();
        try {
            ForumDAO forumDao = new ForumDAO(forumCon);
            MessageDAO messageDao = new MessageDAO(con);
            ProcessDAO processDao = new ProcessDAO(con);

            for (Topic topic : forumDao.getTopicListChangedAfter(lastCheckTime)) {
                log.debug("New messages in topic: {}", topic.getId());

                Message message = messageDao.getMessageBySystemId(id, String.valueOf(topic.getId()));
                if (message == null || message.getProcessId() <= 0) {
                    log.debug("Message not found or process isn't linked.");
                    continue;
                }

                Process process = processDao.getProcess(message.getProcessId());

                final Date lastPostTime = topic.getLastPostTime();

                // последнее сообщение было после последней проверки и
                // последнего изменения статуса
                if (lastPostTime.after(lastCheckTime) && lastPostTime.after(process.getStatusTime())) {
                    log.info("New message in process: " + process.getId());

                    // обновление темы и даты сообщения, установка маркера и т.п.
                    updateMessage(con, null, message);

                    boolean generateEvent = true;

                    if (userNameParam != null) {
                        ParamValueDAO paramDao = new ParamValueDAO(con);

                        for (int executorId : process.getExecutorIds()) {
                            String forumName = paramDao.getParamText(executorId, userNameParam.getId());
                            if (topic.getLastPosterName().equals(forumName)) {
                                log.info("It's executor's message");

                                generateEvent = false;
                                break;
                            }
                        }
                    }

                    if (generateEvent) {
                        ProcessType type = ProcessTypeCache.getProcessType(process.getTypeId());
                        if (type == null) {
                            log.error("Not found process type with id:" + process.getTypeId());
                        } else {
                            Message msg = new Message();
                            msg.setText("Сообщения в форуме.");
                            EventProcessor.processEvent(new ProcessMessageAddedEvent(DynActionForm.SYSTEM_FORM, msg, process),
                                    new SingleConnectionSet(con));
                        }
                    }
                }

                con.commit();
            }
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        } finally {
            SQLUtils.closeConnection(forumCon, con);
        }
    }
}
