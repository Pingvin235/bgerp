package ru.bgcrm.event.listener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.bgerp.app.cfg.Config;
import org.bgerp.app.cfg.ConfigMap;
import org.bgerp.app.cfg.bean.Bean;
import org.bgerp.app.event.EventProcessor;
import org.bgerp.app.event.iface.EventListener;
import org.bgerp.app.exception.BGException;
import org.bgerp.app.exception.BGMessageException;
import org.bgerp.app.exception.BGMessageExceptionWithoutL10n;
import org.bgerp.cache.ProcessTypeCache;
import org.bgerp.dao.expression.Expression;
import org.bgerp.event.base.UserEvent;
import org.bgerp.util.Log;

import ru.bgcrm.dao.process.ProcessDAO;
import ru.bgcrm.event.ParamChangedEvent;
import ru.bgcrm.event.ParamChangingEvent;
import ru.bgcrm.event.link.LinkAddedEvent;
import ru.bgcrm.event.link.LinkAddingEvent;
import ru.bgcrm.event.link.LinkRemovedEvent;
import ru.bgcrm.event.link.LinkRemovingEvent;
import ru.bgcrm.event.process.ProcessChangedEvent;
import ru.bgcrm.event.process.ProcessChangingEvent;
import ru.bgcrm.event.process.ProcessCreatedAsLinkEvent;
import ru.bgcrm.event.process.ProcessMessageAddedEvent;
import ru.bgcrm.event.process.ProcessRemovedEvent;
import ru.bgcrm.model.process.Process;
import ru.bgcrm.model.process.ProcessType;
import ru.bgcrm.struts.action.ProcessCommandExecutor;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.sql.ConnectionSet;

/**
 * Default configurable event processor.
 *
 * @author Shamil Vakhitov
 */
public class DefaultProcessChangeListener {
    private static final Log log = Log.getLog();

    public DefaultProcessChangeListener() {
        EventProcessor.subscribe((e, conSet) -> processEvent(conSet, e, e.getProcess()), ProcessChangingEvent.class);

        EventProcessor.subscribe((e, conSet) -> processEvent(conSet, e, e.getProcess()), ProcessChangedEvent.class);

        EventProcessor.subscribe((e, conSet) -> processEvent(conSet, e, e.getProcess()),
                ProcessCreatedAsLinkEvent.class);

        EventProcessor.subscribe((e, conSet) -> processEvent(conSet, e, e.getProcess()),
                ProcessMessageAddedEvent.class);

        EventProcessor.subscribe((e, conSet) -> processEvent(conSet, e, e.getProcess()),
                ProcessRemovedEvent.class);

        EventProcessor.subscribe((e, conSet) -> processEvent(conSet, e, e.getParameter().getObjectType(), e.getObjectId()),
                ParamChangingEvent.class);

        EventProcessor.subscribe((e, conSet) -> processEvent(conSet, e, e.getParameter().getObjectType(), e.getObjectId()),
                ParamChangedEvent.class);

        EventProcessor.subscribe(
                (e, conSet) -> processEvent(conSet, e, e.getLink().getObjectType(), e.getLink().getObjectId()),
                LinkAddingEvent.class);

        EventProcessor.subscribe(
                (e, conSet) -> processEvent(conSet, e, e.getLink().getObjectType(), e.getLink().getObjectId()),
                LinkAddedEvent.class);

        EventProcessor.subscribe(
                (e, conSet) -> processEvent(conSet, e, e.getLink().getObjectType(), e.getLink().getObjectId()),
                LinkRemovingEvent.class);

        EventProcessor.subscribe(
                (e, conSet) -> processEvent(conSet, e, e.getLink().getObjectType(), e.getLink().getObjectId()),
                LinkRemovedEvent.class);
    }

    private void processEvent(ConnectionSet conSet, UserEvent event, String objectType, int objectId)
            throws Exception {
        if (Process.OBJECT_TYPE.equals(objectType)) {
            Process process = new ProcessDAO(conSet.getConnection()).getProcess(objectId);
            processEvent(conSet, event, process);
        }
    }

    private void processEvent(ConnectionSet conSet, UserEvent event, Process process) throws Exception {
        final int typeId = process.getTypeId();

        ProcessType type = ProcessTypeCache.getProcessType(typeId);
        if (type == null) {
            log.error("Not found process type: " + typeId);
            return;
        }

        var config = type.getProperties().getConfigMap().getConfig(DefaultProcessChangingListenerConfig.class);
        for (ConfigRule rule : config.getRuleList()) {
            rule.processEvent(conSet, event, process);
        }
    }

    private static class DefaultProcessChangingListenerConfig extends Config {
        private static final Log log = Log.getLog();

        private final List<ConfigRule> ruleList = new ArrayList<>();

        public DefaultProcessChangingListenerConfig(ConfigMap config) {
            super(null);

            // the prefix 'on.process.event' instead of 'onProcessEvent' was tested, but looks worse
            for (Map.Entry<Integer, ConfigMap> me : config.subIndexed("onProcessEvent.").entrySet()) {
                try {
                    ruleList.add(new ConfigRule(me.getValue()));
                } catch (Exception e) {
                    log.error(e);
                }
            }

            log.debug("Rules parsed: {}", ruleList);
        }

        public List<ConfigRule> getRuleList() {
            return ruleList;
        }
    }

    private static class ConfigRule {
        private static final String EVENT_STATUS_CHANGED = "statusChanged";
        private static final String EVENT_CLOSED = "closed";
        private static final String EVENT_CREATED = "created";
        private static final String EVENT_REMOVED = "removed";
        private static final String EVENT_CREATED_AS_LINK = "createdAsLink";
        private static final String EVENT_STATUS_CHANGING = "statusChanging";
        private static final String EVENT_ALL = "*";
        private static final String EVENT_DESCRIPTION_ADDING = "descriptionAdding";
        private static final String EVENT_DESCRIPTION_ADDED = "descriptionAdded";
        private static final String EVENT_DESCRIPTION_CHANGING = "descriptionChanging";
        private static final String EVENT_DESCRIPTION_CHANGED = "descriptionChanged";
        private static final String EVENT_PARAM_CHANGING = "paramChanging";
        private static final String EVENT_PARAM_CHANGED = "paramChanged";
        private static final String EVENT_LINK_ADDING = "linkAdding";
        private static final String EVENT_LINK_ADDED = "linkAdded";
        private static final String EVENT_LINK_REMOVING = "linkRemoving";
        private static final String EVENT_LINK_REMOVED = "linkRemoved";
        private static final String EVENT_MESSAGE_ADDED = "messageAdded";
        private static final String EVENT_EXECUTORS_CHANGING = "executorsChanging";
        private static final String EVENT_EXECUTORS_CHANGED = "executorsChanged";
        private static final String EVENT_CREATE_FINISHED = "createFinished";

        private final Map<String, Object> eventMap = new HashMap<>();
        private final Map<String, Object> eventExcludeMap = new HashMap<>();

        // if the class is defined, then the rest of fields are not used
        private final String className;

        private final String ifExpression;
        private final Rule checkRule;
        private final String doExpression;
        private final List<String> commands;

        public ConfigRule(ConfigMap config) {
            extractEvents(config, Utils.toList(config.get("events", "*"), ";"), eventMap);
            extractEvents(config, Utils.toList(config.get("eventsExclude", ""), ";"), eventExcludeMap);

            className = config.get("class");

            ifExpression = config.get("ifExpression");
            checkRule = extractCheckRule(config);
            doExpression = config.get(Expression.DO_EXPRESSION_CONFIG_KEY);

            //  deprecated way of doing commands
            String commands = config.get("commands", "");
            this.commands = Utils.toList(commands, ";");
            if (Utils.notBlankString(commands))
                log.warn("Used process type expression commands: {}, qty: {}", commands, this.commands.size());
        }

        private void extractEvents(ConfigMap config, List<String> eventList, Map<String, Object> eventMap) {
            for (String token : eventList) {
                if (token.startsWith(EVENT_STATUS_CHANGED)) {
                    eventMap.put(EVENT_STATUS_CHANGED, Utils.toIntegerSet(StringUtils.substringAfter(token, ":")));
                } else if (token.startsWith(EVENT_STATUS_CHANGING)) {
                    eventMap.put(EVENT_STATUS_CHANGING, Utils.toIntegerSet(StringUtils.substringAfter(token, ":")));
                } else if (token.startsWith(EVENT_CLOSED)) {
                    eventMap.put(EVENT_CLOSED, Utils.toIntegerSet(StringUtils.substringAfter(token, ":")));
                } else if (token.equals(EVENT_CREATED)) {
                    eventMap.put(EVENT_CREATED, "");
                } else if (token.equals(EVENT_REMOVED)) {
                    eventMap.put(EVENT_REMOVED, "");
                } else if (token.equals(EVENT_CREATED_AS_LINK)) {
                    eventMap.put(EVENT_CREATED_AS_LINK, "");
                } else if (token.equals(EVENT_CREATE_FINISHED)) {
                    eventMap.put(EVENT_CREATE_FINISHED, "");
                } else if (token.equals(EVENT_DESCRIPTION_ADDING)) {
                    eventMap.put(EVENT_DESCRIPTION_ADDING, "");
                } else if (token.equals(EVENT_DESCRIPTION_ADDED)) {
                    eventMap.put(EVENT_DESCRIPTION_ADDED, "");
                } else if (token.equals(EVENT_DESCRIPTION_CHANGING)) {
                    eventMap.put(EVENT_DESCRIPTION_CHANGING, "");
                } else if (token.equals(EVENT_DESCRIPTION_CHANGED)) {
                    eventMap.put(EVENT_DESCRIPTION_CHANGED, "");
                } else if (token.startsWith(EVENT_LINK_ADDING)) {
                    eventMap.put(EVENT_LINK_ADDING, "");
                } else if (token.startsWith(EVENT_LINK_ADDED)) {
                    eventMap.put(EVENT_LINK_ADDED, "");
                } else if (token.startsWith(EVENT_PARAM_CHANGING)) {
                    eventMap.put(EVENT_PARAM_CHANGING, Utils.toIntegerSet(StringUtils.substringAfter(token, ":")));
                } else if (token.startsWith(EVENT_PARAM_CHANGED)) {
                    eventMap.put(EVENT_PARAM_CHANGED, Utils.toIntegerSet(StringUtils.substringAfter(token, ":")));
                } else if (token.startsWith(EVENT_LINK_REMOVING)) {
                    eventMap.put(EVENT_LINK_REMOVING, "");
                } else if (token.startsWith(EVENT_LINK_REMOVED)) {
                    eventMap.put(EVENT_LINK_REMOVED, "");
                } else if (token.equals(EVENT_MESSAGE_ADDED)) {
                    eventMap.put(EVENT_MESSAGE_ADDED, "");
                } else if (token.equals(EVENT_EXECUTORS_CHANGING)) {
                    eventMap.put(EVENT_EXECUTORS_CHANGING, "");
                } else if (token.equals(EVENT_EXECUTORS_CHANGED)) {
                    eventMap.put(EVENT_EXECUTORS_CHANGED, "");
                } else if (token.equals(EVENT_ALL)) {
                    eventMap.put(EVENT_ALL, "");
                }
            }
        }

        /**
         * Extracts checking rule.
         * @param config
         * @return if no {@code checkExpression} or {@code checkErrorMessage} is defined, than checkRule will be null
         */
        private Rule extractCheckRule(ConfigMap config) {
            try {
                return new Rule(config);
            } catch (Exception e) {
                return null;
            }
        }

        public void processEvent(ConnectionSet conSet, UserEvent e, Process process) throws Exception {
            if (!checkEvent(e, process, eventMap))
                return;

            if (checkEvent(e, process, eventExcludeMap))
                return;

            if (Utils.notBlankString(className)) {
                log.debug("Processing rule with class: {}", className);
                @SuppressWarnings("unchecked")
                Class<? extends EventListener<UserEvent>> listenerClass = (Class<? extends EventListener<UserEvent>>) Bean.getClass(className);
                listenerClass.getDeclaredConstructor().newInstance().notify(e, conSet);
            } else {
                if (Utils.notBlankString(ifExpression) && !Expression.init(conSet, e, process).executeCheck(ifExpression)) {
                    log.debug("Skipping rule by ifExpression.");
                    return;
                }

                if (checkRule != null)
                    checkRule.check(Expression.init(conSet, e, process), e);

                if (Utils.notBlankString(doExpression)) {
                    log.debug("Do expression: {}", doExpression);

                    try {
                        Expression.init(conSet, e, process).execute(doExpression);
                    }
                    catch (Exception ex) {
                        if (ex.getCause() instanceof BGMessageException)
                            throw (BGMessageException)ex.getCause();
                        throw new BGException(ex);
                    }
                }

                if (!commands.isEmpty()) {
                    log.warn("Processing commands: {}", commands);

                    ProcessCommandExecutor.processDoCommands(conSet.getConnection(), e.getForm(), process, e, commands);
                }
            }
        }

        private boolean checkEvent(UserEvent e, Process process, Map<String, Object> eventMap) {
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
            } else if (eventClass == ProcessRemovedEvent.class) {
                return eventMap.containsKey(EVENT_REMOVED);
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

    private static class Rule {
        private final String expression;
        private final String checkErrorMessage;
        private final boolean showEvent;

        private Rule(ConfigMap rule) {
            expression = rule.get(Expression.CHECK_EXPRESSION_CONFIG_KEY);
            checkErrorMessage = rule.get(Expression.CHECK_ERROR_MESSAGE_CONFIG_KEY);
            showEvent = rule.getBoolean("checkErrorShowEvent", false);

            if (Utils.isBlankString(expression) || Utils.isBlankString(checkErrorMessage)) {
                throw new BGException(
                        "Expression incorrect: " + expression + "; checkErrorMessage: " + checkErrorMessage);
            }
        }

        private void check(Expression expr, UserEvent e) throws BGMessageException {
            if (!expr.executeCheck(expression)) {
                if (showEvent) {
                    throw new BGMessageExceptionWithoutL10n(checkErrorMessage + "\n (" + e + ")");
                } else {
                    throw new BGMessageExceptionWithoutL10n(checkErrorMessage);
                }
            }
        }
    }
}