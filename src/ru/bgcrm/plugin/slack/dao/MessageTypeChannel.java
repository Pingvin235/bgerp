package ru.bgcrm.plugin.slack.dao;

import java.sql.Connection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;

import org.apache.commons.lang.StringUtils;
import org.bgerp.model.Pageable;
import org.bgerp.util.Log;

import ru.bgcrm.cache.ParameterCache;
import ru.bgcrm.cache.ProcessTypeCache;
import ru.bgcrm.cache.UserCache;
import ru.bgcrm.dao.ParamValueDAO;
import ru.bgcrm.dao.PropertiesDAO;
import ru.bgcrm.dao.expression.Expression;
import ru.bgcrm.dao.message.MessageDAO;
import ru.bgcrm.dao.message.MessageType;
import ru.bgcrm.dao.message.config.MessageTypeConfig;
import ru.bgcrm.dao.process.ProcessLinkDAO;
import ru.bgcrm.event.EventProcessor;
import ru.bgcrm.event.process.ProcessMessageAddedEvent;
import ru.bgcrm.model.BGException;
import ru.bgcrm.model.BGMessageException;
import ru.bgcrm.model.CommonObjectLink;
import ru.bgcrm.model.Pair;
import ru.bgcrm.model.message.Message;
import ru.bgcrm.model.param.Parameter;
import ru.bgcrm.model.process.Process;
import ru.bgcrm.model.process.ProcessType;
import ru.bgcrm.model.user.User;
import ru.bgcrm.plugin.slack.Plugin;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.ParameterMap;
import ru.bgcrm.util.Setup;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.Config.InitStopException;
import ru.bgcrm.util.sql.ConnectionSet;
import ru.bgcrm.util.sql.SingleConnectionSet;

/**
 * Message type and settings of Slack.
 *
 * @author Shamil Vakhitov
 */
public class MessageTypeChannel extends MessageType {
    private static final Log log = Log.getLog();

    private static final String LAST_CHECK_PARAM = Plugin.ID + ":lastCheck";

    private final String token;
    private final String purposeExpression;
    private final String postExpression;
    private final Parameter accountParam;
    private final String stringExpressionMessageExtract;

    public MessageTypeChannel(Setup setup, int id, ParameterMap config) throws BGException {
        super(setup, id, config.get("title"), config);
        token = config.get("authToken");
        if (token == null) {
            log.info("authToken is not defined.");
            throw new InitStopException();
        }
        this.purposeExpression = config.get("puproseExpression");
        this.postExpression = config.get("postExpression");
        this.accountParam = ParameterCache.getParameter(config.getInt("accountParamId", 0));
        this.stringExpressionMessageExtract = config.get(Expression.STRING_MAKE_EXPRESSION_CONFIG_KEY + "MessageExtract");
    }

    public String getToken() {
        return token;
    }

    public String getPurposeExpression() {
        return purposeExpression;
    }

    public String getPostExpression() {
        return postExpression;
    }

    public Parameter getAccountParam() {
        return accountParam;
    }

    @Override
    public boolean isEditable(Message message) {
        return false;
    }

    @Override
    public boolean isRemovable(Message message) {
        return false;
    }

    @Override
    public boolean isAttachmentSupport() {
        return false;
    }

    @Override
    public String getHeaderJsp() {
        // "user.process.message.header.jsp", List.of(PATH_JSP_USER + "/process_link_list.jsp")
        return Plugin.ENDPOINT_MESSAGE_HEADER;
    }

    @Override
    public String getProcessMessageHeaderColor(Message message) {
        return "#E9C4F5";
    }

    @Override
    public void messageDelete(ConnectionSet conSet, String... messageId) throws BGException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateMessage(Connection con, DynActionForm form, Message message) throws Exception {
        int processId = message.getProcessId();
        SlackProto proto = new SlackProto(getMessageType().getToken());

        CommonObjectLink link = Utils.getFirst(new ProcessLinkDAO(con).getObjectLinksWithType(processId, Plugin.LINK_TYPE_CHANNEL));
        if (link == null)
            throw new BGMessageException("К процессу не привязан канал Slack.");

        // подстановка пользователя Slack, от которого должно быть отправлено сообщение
        String username = null;
        if (accountParam != null) {
            String slackAccount = new ParamValueDAO(con).getParamText(message.getUserId(), accountParam.getId());
            if (Utils.notBlankString(slackAccount))
                username = slackAccount;
        }

        JsonNode result = proto.chatPostMessage(link.getLinkedObjectTitle(), message.getText(), username, null).get("message");
        message.setFrom("");
        message.setTo("");
        message.setSystemId(result.get("ts").asText());

        new MessageDAO(con).updateMessage(message);
    }

    @Override
    public void process() {
        log.debug("Processing Slack: {}", id);

        if (stringExpressionMessageExtract == null) {
            log.info("No stringExpressionMessageExtract - skipping processing");
            return;
        }

        try (var con = setup.getDBConnectionFromPool()) {
            PropertiesDAO propDAO = new PropertiesDAO(con);
            MessageDAO messageDAO = new MessageDAO(con);
            ParamValueDAO paramDAO = new ParamValueDAO(con);
            ProcessLinkDAO linkDAO = new ProcessLinkDAO(con);

            // время последнего выбранного сообщения
            long lastCheck = Utils.parseLong(propDAO.get(LAST_CHECK_PARAM), 0);
            log.debug("Last check: {}", lastCheck);

            SlackProto proto = new SlackProto(token);

            // ключ - Slack код пользователя, значение - логин
            Map<String, String> accountMap = null;
            if (accountParam != null) {
                accountMap = new HashMap<>();
                for (JsonNode n : proto.userList().get("members"))
                    accountMap.put(n.get("id").asText(), n.get("name").asText());
            }

            long newLastCheck = lastCheck;

            // перебор открытых каналов
            for (JsonNode channel : proto.channelList(true)) {
                String channelId = channel.get("id").asText();
                String name = channel.get("name").asText();

                log.debug("Channel: {}", name);

                Pageable<Pair<String, Process>> searchResult = new Pageable<Pair<String, Process>>();
                linkDAO.searchLinkedProcessList(searchResult, Plugin.LINK_TYPE_CHANNEL, 1, channelId, null, null, null, null);

                Pair<String, Process> pair = Utils.getFirst(searchResult.getList());
                if (pair == null)
                    continue;

                Process process = pair.getSecond();

                for (JsonNode m : proto.channelHistory(channelId, lastCheck > 0 ? String.valueOf(lastCheck) : null).get("messages")) {
                    log.debug("Processing message: {}", m);

                    String text = m.get("text").asText();
                    long ts = Utils.parseLong(StringUtils.substringBefore(m.get("ts").asText(), "."));
                    if (ts > newLastCheck)
                        newLastCheck = ts;

                    Map<String, Object> ctx = new HashMap<>();
                    ctx.put("message", text);
                    ctx.put("m", m);

                    if ((text = new Expression(ctx).getString(stringExpressionMessageExtract)) != null) {
                        int userId = User.USER_SYSTEM_ID;
                        if (accountParam != null) {
                            String slackLogin = accountMap.get(m.get("user").asText());
                            Set<Integer> userIds = paramDAO.searchObjectByParameterText(accountParam.getId(), slackLogin);
                            if (!userIds.isEmpty())
                                userId = Utils.getFirst(userIds);
                        }

                        Message msg = new Message();
                        msg.setDirection(Message.DIRECTION_INCOMING);
                        msg.setTypeId(id);
                        msg.setText(text);
                        msg.setFromTime(new Date(ts * 1000));
                        msg.setFrom("");
                        msg.setTo("");
                        msg.setProcessId(process.getId());
                        msg.setProcessed(true);
                        msg.setToTime(new Date());
                        msg.setUserId(userId);

                        messageDAO.updateMessage(msg);

                        log.info("Created message: " + msg.getId());

                        //TODO: Код дублирован из Email!
                        ProcessType type = ProcessTypeCache.getProcessType(process.getTypeId());
                        if (type == null) {
                            log.error("Not found process type with id:" + process.getTypeId());
                        } else {
                            DynActionForm form = DynActionForm.SERVER_FORM;
                            User user = UserCache.getUser(userId);
                            if (user != null)
                                form = new DynActionForm(user);
                            EventProcessor.processEvent(new ProcessMessageAddedEvent(form, msg, process),
                                    type.getProperties().getActualScriptName(), new SingleConnectionSet(con));
                        }
                    }
                }
            }

            if (newLastCheck > 0) {
                // + 1с, чтобы постоянно не выбиралось последнее сообщение, дробная часть ts отброшена
                propDAO.set(LAST_CHECK_PARAM, String.valueOf(newLastCheck + 1));
            }

            con.commit();
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }
    }

    public static MessageTypeChannel getMessageType() throws BGMessageException {
        MessageTypeChannel type = Setup.getSetup().getConfig(MessageTypeConfig.class).getMessageType(MessageTypeChannel.class);
        if (type == null)
            throw new BGMessageException("Не сконфигурирован тип сообщения Slack");
        return type;
    }
}
