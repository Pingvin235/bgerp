package ru.bgcrm.plugin.slack.dao;

import java.sql.Connection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;

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
import ru.bgcrm.model.SearchResult;
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
import ru.bgcrm.util.sql.ConnectionSet;
import ru.bgcrm.util.sql.SQLUtils;
import ru.bgcrm.util.sql.SingleConnectionConnectionSet;

/**
 * Тип сообщения и одновременно хранилище настроек slack.
 */
public class MessageTypeChannel extends MessageType {
	private static final Logger log = Logger.getLogger(MessageTypeChannel.class);
	
	private static final String LAST_CHECK_PARAM = Plugin.ID + ":lastCheck";
	
	private final String token;
	private final String puproseExpression;
	private final String postExpression;
	private final Parameter accountParam;
	private final String stringExpressionMessageExtract;

	public MessageTypeChannel(int id, ParameterMap config) throws BGException {
		super(id, config.get("title"), config);
		token = config.get("authToken");
		if (token == null)
			throw new BGException("authToken is not defined.");
		this.puproseExpression = config.get("puproseExpression");
		this.postExpression = config.get("postExpression");
		this.accountParam = ParameterCache.getParameter(config.getInt("accountParamId", 0));
		this.stringExpressionMessageExtract = config.get(Expression.STRING_MAKE_EXPRESSION_CONFIG_KEY + "MessageExtract");
	}
	
	public String getToken() {
		return token;
	}
	
	public String getPuproseExpression() {
		return puproseExpression;
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
	public String getProcessMessageHeaderColor(Message message) {
		return "#E9C4F5";
	}

	@Override
	public void messageDelete(ConnectionSet conSet, String... messageId) throws BGException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void updateMessage(Connection con, DynActionForm form, Message message) throws BGException {
		try {
			int processId = message.getProcessId();
			SlackProto proto = new SlackProto(getMessageType().getToken(), true);
			
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
		catch (BGException e) {
			throw e;
		} 
		catch (Exception e) {
			throw new BGException(e);
		}
	}

	@Override
	public void process() {
		if (log.isDebugEnabled()) {
			log.debug("Processing Slack: " + id);
		}
		if (stringExpressionMessageExtract == null) {
			log.info("No stringExpressionMessageExtract - skipping processing");
			return;
		}
		
		Connection con = Setup.getSetup().getDBConnectionFromPool();
		try {
			PropertiesDAO propDAO = new PropertiesDAO(con);
			MessageDAO messageDAO = new MessageDAO(con);
			ParamValueDAO paramDAO = new ParamValueDAO(con);
			ProcessLinkDAO linkDAO = new ProcessLinkDAO(con);
			
			// время последнего выбранного сообщения
			long lastCheck = Utils.parseLong(propDAO.get(LAST_CHECK_PARAM), 0);
			if (log.isDebugEnabled())
				log.debug("Last check: " + lastCheck);
			
			SlackProto proto = new SlackProto(token, true);
			
			// ключ - Slack код пользователя, значение - логин 
			Map<String, String> accountMap = null;
			if (accountParam != null) {
				accountMap = new HashMap<>();
				for (JsonNode n : proto.userList().get("members"))
					accountMap.put(n.get("id").asText(), n.get("name").asText());
			}
			
			long newLastCheck = lastCheck;
			
			// перебор открытых каналов
			for (JsonNode channel : proto.channelList(true).get(proto.getChannelsPrefix())) {
				String channelId = channel.get("id").asText();
				String name = channel.get("name").asText();
						
				if (log.isDebugEnabled())
					log.debug("Channel: " + name);
				
				SearchResult<Pair<String, Process>> searchResult = new SearchResult<Pair<String, Process>>();
				linkDAO.searchLinkedProcessList(searchResult, Plugin.LINK_TYPE_CHANNEL, 1, channelId, null, null, null, null);
				
				Pair<String, Process> pair = Utils.getFirst(searchResult.getList());
				if (pair == null)
					continue;
				
				Process process = pair.getSecond();
			
				for (JsonNode m : proto.channelHistory(channelId, lastCheck > 0 ? String.valueOf(lastCheck) : null).get("messages")) {
					if (log.isDebugEnabled())
						log.debug("Processing message: " + m.toString());
					
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
									type.getProperties().getActualScriptName(), new SingleConnectionConnectionSet(con));
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
		} finally {
			SQLUtils.closeConnection(con);
		}
	}
	
	public static MessageTypeChannel getMessageType() throws BGMessageException {
		MessageTypeChannel type = Setup.getSetup().getConfig(MessageTypeConfig.class).getMessageType(MessageTypeChannel.class);
		if (type == null)
			throw new BGMessageException("Не сконфигурирован тип сообщения Slack");
		return type;
	}
}
