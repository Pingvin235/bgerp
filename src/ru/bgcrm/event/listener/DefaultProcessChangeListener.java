package ru.bgcrm.event.listener;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import ru.bgcrm.cache.ProcessTypeCache;
import ru.bgcrm.dao.expression.Expression;
import ru.bgcrm.dao.expression.ParamValueFunction;
import ru.bgcrm.dao.expression.ProcessChangeFunctions;
import ru.bgcrm.dao.expression.ProcessLinkFunction;
import ru.bgcrm.dao.process.ProcessDAO;
import ru.bgcrm.event.Event;
import ru.bgcrm.event.EventProcessor;
import ru.bgcrm.event.ParamChangedEvent;
import ru.bgcrm.event.ParamChangingEvent;
import ru.bgcrm.event.UserEvent;
import ru.bgcrm.event.link.LinkAddedEvent;
import ru.bgcrm.event.link.LinkAddingEvent;
import ru.bgcrm.event.link.LinkRemovedEvent;
import ru.bgcrm.event.link.LinkRemovingEvent;
import ru.bgcrm.event.process.ProcessChangedEvent;
import ru.bgcrm.event.process.ProcessChangingEvent;
import ru.bgcrm.event.process.ProcessCreatedAsLinkEvent;
import ru.bgcrm.event.process.ProcessMessageAddedEvent;
import ru.bgcrm.model.BGException;
import ru.bgcrm.model.BGMessageException;
import ru.bgcrm.model.process.Process;
import ru.bgcrm.model.process.ProcessType;
import ru.bgcrm.model.user.User;
import ru.bgcrm.servlet.filter.SetRequestParamsFilter;
import ru.bgcrm.struts.action.ProcessCommandExecutor;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.Config;
import ru.bgcrm.util.ParameterMap;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.sql.ConnectionSet;

/**
 * Стандартный слушатель событий с процессом.
 * Возможность определять простейшие действия на события.
 */
//TODO: Многие из событий нельзя определить отдельно, доработать.
public class DefaultProcessChangeListener {

	private static final Logger log = Logger.getLogger(DefaultProcessChangeListener.class);

	public DefaultProcessChangeListener() {
		EventProcessor.subscribe((e, connectionSet) -> {
			processEvent(connectionSet, e, e.getProcess());
		}, ProcessChangingEvent.class);

		EventProcessor.subscribe((e, connectionSet) -> {
			processEvent(connectionSet, e, e.getProcess());
		}, ProcessChangedEvent.class);

		EventProcessor.subscribe((e, connectionSet) -> {
			processEvent(connectionSet, e, e.getProcess());
		}, ProcessCreatedAsLinkEvent.class);

		EventProcessor.subscribe((e, connectionSet) -> {
			processEvent(connectionSet, e, e.getProcess());
		}, ProcessMessageAddedEvent.class);

		EventProcessor.subscribe((e, connectionSet) -> {
			processEvent(connectionSet, e, e.getParameter().getObject(), e.getObjectId());
		}, ParamChangingEvent.class);

		EventProcessor.subscribe((e, connectionSet) -> {
			processEvent(connectionSet, e, e.getParameter().getObject(), e.getObjectId());
		}, ParamChangedEvent.class);

		EventProcessor.subscribe((e, connectionSet) -> {
			processEvent(connectionSet, e, e.getLink().getObjectType(), e.getLink().getObjectId());
		}, LinkAddingEvent.class);

		EventProcessor.subscribe((e, connectionSet) -> {
			processEvent(connectionSet, e, e.getLink().getObjectType(), e.getLink().getObjectId());
		}, LinkAddedEvent.class);

		EventProcessor.subscribe((e, connectionSet) -> {
			processEvent(connectionSet, e, e.getLink().getObjectType(), e.getLink().getObjectId());
		}, LinkRemovingEvent.class);

		EventProcessor.subscribe((e, connectionSet) -> {
			processEvent(connectionSet, e, e.getLink().getObjectType(), e.getLink().getObjectId());
		}, LinkRemovedEvent.class);
	}

	private void processEvent(ConnectionSet connectionSet, UserEvent event, String objectType, int objectId)
			throws Exception {
		if (Process.OBJECT_TYPE.equals(objectType)) {
			Process process = new ProcessDAO(connectionSet.getConnection()).getProcess(objectId);
			processEvent(connectionSet, event, process);
		}
	}

	private void processEvent(ConnectionSet connectionSet, UserEvent event, Process process) throws Exception {
		final int typeId = process.getTypeId();

		ProcessType type = ProcessTypeCache.getProcessType(typeId);
		if (type == null) {
			log.error("Not found process type: " + typeId);
			return;
		}

		DefaultProcessChangingListenerConfig config = type.getProperties().getConfigMap()
				.getConfig(DefaultProcessChangingListenerConfig.class);
		for (ConfigRule rule : config.getRuleList()) {
			rule.processEvent(connectionSet, event, process);
		}
	}

	private static class DefaultProcessChangingListenerConfig extends Config {
		private static final Logger log = Logger.getLogger(DefaultProcessChangingListenerConfig.class);

		private final List<ConfigRule> ruleList = new ArrayList<ConfigRule>();

		public DefaultProcessChangingListenerConfig(ParameterMap config) {
			super(config);

			for (Map.Entry<Integer, ParameterMap> me : config.subIndexed("onProcessEvent.").entrySet()) {
				try {
					ruleList.add(new ConfigRule(me.getValue()));
				} catch (Exception e) {
					log.error(e.getMessage(), e);
				}
			}
			
			if (log.isDebugEnabled())
				log.debug("Rules parsed: " + ruleList);
		}

		public List<ConfigRule> getRuleList() {
			return ruleList;
		}
	}

	private static class ConfigRule {
		public static final int EVENT_STATUS_CHANGED = 1;
		public static final int EVENT_CLOSED = 2;
		public static final int EVENT_CREATED = 3;
		public static final int EVENT_CREATED_AS_LINK = 4;
		public static final int EVENT_STATUS_CHANGING = 5;
		public static final int EVENT_ALL = 6;
		public static final int EVENT_DESCRIPTION_ADDING = 7;
		public static final int EVENT_DESCRIPTION_ADDED = 8;
		public static final int EVENT_DESCRIPTION_CHANGING = 9;
		public static final int EVENT_DESCRIPTION_CHANGED = 10;
		public static final int EVENT_PARAM_CHANGING = 11;
		public static final int EVENT_PARAM_CHANGED = 12;
		public static final int EVENT_LINK_ADDING = 14;
		public static final int EVENT_LINK_ADDED = 15;
		public static final int EVENT_LINK_REMOVING = 16;
		public static final int EVENT_LINK_REMOVED = 17;
		public static final int EVENT_MESSAGE_ADDED = 18;
		public static final int EVENT_EXECUTORS_CHANGING = 19;
		public static final int EVENT_EXECUTORS_CHANGED = 20;
		public static final int EVENT_CREATE_FINISHED = 21;

		private final Map<Integer, Object> eventMap = new HashMap<Integer, Object>();
		private final Map<Integer, Object> eventExcludeMap = new HashMap<Integer, Object>();

		private String ifExpression;
		private String doExpression;
		private final List<String> commands;
		private Rule checkRule;

		public ConfigRule(ParameterMap config) {
			extractEvents(config, Utils.toList(config.get("events", "*"), ";"), eventMap);
			extractEvents(config, Utils.toList(config.get("eventsExclude", ""), ";"), eventExcludeMap);

			// если ключи checkExpression и checkErrorMessage не определены - выйдет исключение и переменная будет null 
			try {
				checkRule = new Rule(config);
			} catch (Exception e) {
			}

			this.ifExpression = config.get("ifExpression");
			this.doExpression = config.get(Expression.DO_EXPRESSION_CONFIG_KEY);

			this.commands = Utils.toList(config.get("commands", ""), ";");
		}

		private void extractEvents(ParameterMap config, List<String> eventList, Map<Integer, Object> eventMap) {
			for (String token : eventList) {
				if (token.startsWith("statusChanged")) {
					eventMap.put(EVENT_STATUS_CHANGED, Utils.toIntegerSet(StringUtils.substringAfter(token, ":")));
				} else if (token.startsWith("statusChanging")) {
					eventMap.put(EVENT_STATUS_CHANGING, Utils.toIntegerSet(StringUtils.substringAfter(token, ":")));
				} else if (token.startsWith("closed")) {
					eventMap.put(EVENT_CLOSED, Utils.toIntegerSet(StringUtils.substringAfter(token, ":")));
				} else if (token.equals("created")) {
					eventMap.put(EVENT_CREATED, "");
				} else if (token.equals("createdAsLink")) {
					eventMap.put(EVENT_CREATED_AS_LINK, "");
				} else if (token.equals("createFinished")) {
					eventMap.put(EVENT_CREATE_FINISHED, "");
				} else if (token.equals("descriptionAdding")) {
					eventMap.put(EVENT_DESCRIPTION_ADDING, "");
				} else if (token.equals("descriptionAdded")) {
					eventMap.put(EVENT_DESCRIPTION_ADDED, "");
				} else if (token.equals("descriptionChanging")) {
					eventMap.put(EVENT_DESCRIPTION_CHANGING, "");
				} else if (token.equals("descriptionChanged")) {
					eventMap.put(EVENT_DESCRIPTION_CHANGED, "");
				} else if (token.startsWith("linkAdding")) {
					eventMap.put(EVENT_LINK_ADDING, "");
				} else if (token.startsWith("linkAdded")) {
					eventMap.put(EVENT_LINK_ADDED, "");
				} else if (token.startsWith("paramChanging")) {
					eventMap.put(EVENT_PARAM_CHANGING, Utils.toIntegerSet(StringUtils.substringAfter(token, ":")));
				} else if (token.startsWith("paramChanged")) {
					eventMap.put(EVENT_PARAM_CHANGED, Utils.toIntegerSet(StringUtils.substringAfter(token, ":")));
				} else if (token.startsWith("linkRemoving")) {
					eventMap.put(EVENT_LINK_REMOVING, "");
				} else if (token.startsWith("linkRemoved")) {
					eventMap.put(EVENT_LINK_REMOVED, "");
				} else if (token.equals("messageAdded")) {
					eventMap.put(EVENT_MESSAGE_ADDED, "");
				} else if (token.equals("executorsChanging")) {
					eventMap.put(EVENT_EXECUTORS_CHANGING, "");
				} else if (token.equals("executorsChanged")) {
					eventMap.put(EVENT_EXECUTORS_CHANGED, "");
				} else if (token.equals("*")) {
					eventMap.put(EVENT_ALL, "");
				}
			}
		}

		public void processEvent(ConnectionSet conSet, UserEvent e, Process process) throws Exception {
			if (!checkEvent(e, process, eventMap)) {
				return;
			}

			if (checkEvent(e, process, eventExcludeMap)) {
				return;
			}

			if (Utils.notBlankString(ifExpression)
					&& !initExpression(conSet, e, process).check(ifExpression)) {
				log.debug("Skipping rule by ifExpression.");
				return;
			}

			if (checkRule != null) {
				checkRule.check(initExpression(conSet, e, process), e);
			}

			if (log.isDebugEnabled()) {
				log.debug("Process commands: " + commands);
			}

			ProcessCommandExecutor.processDoCommands(conSet.getConnection(), e.getForm(), process, e, commands);

			if (Utils.notBlankString(doExpression)) {
				if (log.isDebugEnabled()) {
					log.debug("Do expression: " + doExpression);
				}

				try {
					initExpression(conSet, e, process).executeScript(doExpression);
				} 
				catch (Exception ex) {
					if (ex.getCause() != null && ex.getCause() instanceof BGMessageException)
						throw (BGMessageException)ex.getCause();
					throw new BGException(ex);
				}
			}
		}

		private boolean checkEvent(UserEvent e, Process process, Map<Integer, Object> eventMap) throws BGException {
			if (eventMap.containsKey(EVENT_ALL)) {
				return true;
			}

			// проверяется не по instanceof, т.к. часто события расширяют друг друга
			Class<?> eventClass = e.getClass();

			if (eventClass == ProcessChangingEvent.class) {
				ProcessChangingEvent event = (ProcessChangingEvent) e;

				if (event.isStatus()) {
					@SuppressWarnings("unchecked")
					Set<Integer> statusIds = (Set<Integer>) eventMap.get(EVENT_STATUS_CHANGING);
					return statusIds != null
							&& (statusIds.size() == 0 || statusIds.contains(event.getStatusChange().getStatusId()));
				} else if (event.isDescriptionAdd()) {
					return eventMap.containsKey(EVENT_DESCRIPTION_ADDING);
				} else if (event.isDescription()) {
					return eventMap.containsKey(EVENT_DESCRIPTION_CHANGING);
				} else if (event.isExecutors()) {
					return eventMap.containsKey(EVENT_EXECUTORS_CHANGING);
				} else {
					return false;
				}
			} else if (eventClass == ProcessChangedEvent.class) {
				ProcessChangedEvent event = (ProcessChangedEvent) e;

				if (event.isStatus()) {
					@SuppressWarnings("unchecked")
					Set<Integer> statusIds = (Set<Integer>) eventMap.get(EVENT_STATUS_CHANGED);
					return statusIds != null && (statusIds.size() == 0 || statusIds.contains(process.getStatusId()));
				} else if (event.isDescriptionAdd()) {
					return eventMap.containsKey(EVENT_DESCRIPTION_ADDED);
				} else if (event.isDescription()) {
					return eventMap.containsKey(EVENT_DESCRIPTION_CHANGED);
				} else if (event.isExecutors()) {
					return eventMap.containsKey(EVENT_EXECUTORS_CHANGED);
				} else if (event.isCreated()) {
					return eventMap.containsKey(EVENT_CREATED);
				} else if (event.isCreateFinished()) {
					return eventMap.containsKey(EVENT_CREATE_FINISHED);
				} else {
					return false;
				}
			} else if (eventClass == ProcessMessageAddedEvent.class) {
				return eventMap.containsKey(EVENT_MESSAGE_ADDED);
			} else if (eventClass == ParamChangingEvent.class) {
				ParamChangingEvent event = (ParamChangingEvent) e;

				@SuppressWarnings("unchecked")
				Set<Integer> typeIds = (Set<Integer>) eventMap.get(EVENT_PARAM_CHANGING);

				return typeIds != null && (typeIds.size() == 0 || typeIds.contains(event.getParameter().getId()));
			} else if (eventClass == ParamChangedEvent.class) {
				ParamChangedEvent event = (ParamChangedEvent) e;

				@SuppressWarnings("unchecked")
				Set<Integer> typeIds = (Set<Integer>) eventMap.get(EVENT_PARAM_CHANGED);

				return typeIds != null && (typeIds.size() == 0 || typeIds.contains(event.getParameter().getId()));
			} else if (eventClass == ProcessCreatedAsLinkEvent.class) {
				return eventMap.containsKey(EVENT_CREATED_AS_LINK);
			} else if (eventClass == LinkRemovingEvent.class) {
				return eventMap.containsKey(EVENT_LINK_REMOVING);
			} else if (eventClass == LinkRemovedEvent.class) {
				return eventMap.containsKey(EVENT_LINK_REMOVED);
			} else if (eventClass == LinkAddingEvent.class) {
				return eventMap.containsKey(EVENT_LINK_ADDING);
			} else if (eventClass == LinkAddedEvent.class) {
				return eventMap.containsKey(EVENT_LINK_ADDED);
			}

			return false;
		}
	}

	public static final class DefaultProcessorChangeContextEvent extends UserEvent {
		private final Map<String, Object> context;

		public DefaultProcessorChangeContextEvent(DynActionForm form, Map<String, Object> context) {
			super(form);
			this.context = context;
		}

		public Map<String, Object> getContext() {
			return context;
		}
	}

	public static Expression initExpression(ConnectionSet conSet, UserEvent event, Process process)
			throws Exception {
		DynActionForm form = event.getForm();
		
		Map<String, Object> context = getProcessJexlContext(conSet, form, event, process);
		int sizeBefore = context.size();
		// добавление плагинами объектов в контекст
		EventProcessor.processEvent(new DefaultProcessorChangeContextEvent(form, context), conSet);
		log.debug("Context sizes, before: " + sizeBefore + "; after: " + context.size()); 
		return new Expression(context);
	}

	// TODO: Перенести функцию, используется повсеместно.
	public static Map<String, Object> getProcessJexlContext(ConnectionSet conSet, DynActionForm form, 
			UserEvent event, Process process) {
		Connection con = conSet.getConnection();
		
		Map<String, Object> context = new HashMap<>(100);
		context.put(User.OBJECT_TYPE, form.getUser());
		context.put(User.OBJECT_TYPE + ParamValueFunction.PARAM_FUNCTION_SUFFIX,
				new ParamValueFunction(con, form.getUserId()));
		context.put(Process.OBJECT_TYPE, process);
		context.put(Process.OBJECT_TYPE + ParamValueFunction.PARAM_FUNCTION_SUFFIX,
				new ParamValueFunction(con, process.getId()));
		context.put(ProcessLinkFunction.PROCESS_LINK_FUNCTION, new ProcessLinkFunction(con, process.getId()));
		context.put(ConnectionSet.KEY, conSet);
		context.put(DynActionForm.KEY, form);
		if (event != null)
		    context.put(Event.KEY, event);
		
		context.put(null, new ProcessChangeFunctions(process, form, con));
		
        context.putAll(SetRequestParamsFilter.getContextVariables(form.getHttpRequest()));
		
		return context;
	}

	public static class Rule {
		private final String expression;
		private final String checkErrorMessage;
		private final boolean showEvent;

		public Rule(ParameterMap rule) throws BGException {
			expression = rule.get(Expression.CHECK_EXPRESSION_CONFIG_KEY);
			checkErrorMessage = rule.get("checkErrorMessage");
			showEvent = rule.getBoolean("checkErrorShowEvent", false);

			if (Utils.isBlankString(expression) || Utils.isBlankString(checkErrorMessage)) {
				throw new BGException(
						"Expression incorrect: " + expression + "; checkErrorMessage: " + checkErrorMessage);
			}
		}

		public void check(Expression expr, UserEvent e) throws BGMessageException {
			if (!expr.check(expression)) {
				if (showEvent) {
					throw new BGMessageException(checkErrorMessage + "\n (" + e + ")");
				} else {
					throw new BGMessageException(checkErrorMessage);
				}
			}
		}
	}
}