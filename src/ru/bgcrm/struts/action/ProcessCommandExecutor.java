package ru.bgcrm.struts.action;

import java.sql.Connection;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import ru.bgcrm.cache.ParameterCache;
import ru.bgcrm.cache.UserCache;
import ru.bgcrm.dao.NewsDAO;
import ru.bgcrm.dao.ParamValueDAO;
import ru.bgcrm.dao.expression.Expression;
import ru.bgcrm.dao.process.ProcessDAO;
import ru.bgcrm.dao.process.ProcessLinkDAO;
import ru.bgcrm.event.EventProcessor;
import ru.bgcrm.event.UserEvent;
import ru.bgcrm.event.listener.DefaultProcessChangeListener;
import ru.bgcrm.event.process.ProcessDoActionEvent;
import ru.bgcrm.event.process.ProcessMessageAddedEvent;
import ru.bgcrm.model.BGException;
import ru.bgcrm.model.BGMessageException;
import ru.bgcrm.model.News;
import ru.bgcrm.model.Pair;
import ru.bgcrm.model.SearchResult;
import ru.bgcrm.model.param.Parameter;
import ru.bgcrm.model.param.ParameterEmailValue;
import ru.bgcrm.model.process.Process;
import ru.bgcrm.model.process.ProcessExecutor;
import ru.bgcrm.model.process.ProcessGroup;
import ru.bgcrm.model.process.ProcessType;
import ru.bgcrm.model.process.StatusChange;
import ru.bgcrm.model.process.config.ProcessReferenceConfig;
import ru.bgcrm.model.user.Group;
import ru.bgcrm.model.user.User;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.MailMsg;
import ru.bgcrm.util.Preferences;
import ru.bgcrm.util.Setup;
import ru.bgcrm.util.TimeUtils;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.sql.SingleConnectionConnectionSet;

public class ProcessCommandExecutor {
    public static final String COMMAND_ADD_GROUPS = "addGroups";
    public static final String COMMAND_SET_STATUS = "setStatus";
    public static final String COMMAND_ADD_EXECUTORS = "addExecutors";
    public static final String COMMAND_SET_PARAM = "setParam";

    @SuppressWarnings("unchecked")
    public static void processDoCommands(Connection con, DynActionForm form, Process process, UserEvent event, List<String> commands)
            throws Exception {
        ProcessType type = ProcessAction.getProcessType(process.getTypeId());

        for (String command : commands) {
            //linked:<typeId>:...
            if (command.startsWith("linkedProcess:")) {
                command = StringUtils.substringAfter(command, ":");
                int typeId = Utils.parseInt(StringUtils.substringBefore(command, ":"));
                if (typeId <= 0) {
                    throw new BGException("Не указан тип привязанного процесса в команде " + command);
                }

                process = Utils.getFirst(new ProcessLinkDAO(con).getLinkedProcessList(process.getId(), null, false, Collections.singleton(typeId)));
                if (process == null) {
                    throw new BGException("Не найден привязанный процесс: " + command);
                }

                command = StringUtils.substringAfter(command, ":");
            }

            // устаревший формат, убрать в доке
            if ("setCurrentExecutor".equals(command)) {
                command = "addExecutors:{@ctxUserId}";
            }

            // устаревший формат, убрать в конфигурации типов процессов
            if (command.startsWith(COMMAND_ADD_EXECUTORS)) {
                command = command.replaceAll("current", "{@ctxUserId}");
            }

            command = Preferences.insertVariablesValues(command, form.getUser().getConfigMap(), null, true);

            //TODO: Поддержка ролей.
            if (command.startsWith("addGroupsInRole")) {
                String groupIds = StringUtils.substringBetween(command, ":");
                int roleId = Utils.parseInt(StringUtils.substringAfterLast(command, ":"), 0);

                Set<ProcessGroup> processGroups = new HashSet<ProcessGroup>(process.getGroups());
                Set<ProcessGroup> addingProcessGroups = ProcessGroup.toProcessGroupSet(Utils.toIntegerSet(groupIds), roleId);
                if (type.getProperties().getAllowedGroups().size() > 0) {
                    addingProcessGroups = new HashSet<ProcessGroup>(
                            CollectionUtils.intersection(addingProcessGroups, type.getProperties().getAllowedGroups()));
                }

                if (processGroups.addAll(addingProcessGroups)) {
                    ProcessAction.processGroupsUpdate(form, con, process, processGroups);
                }
            } else if (command.startsWith(COMMAND_ADD_GROUPS)) {
                String param = StringUtils.substringAfter(command, ":");

                Set<ProcessGroup> processGroups = new HashSet<ProcessGroup>(process.getGroups());
                Set<ProcessGroup> addingProcessGroups = ProcessGroup.toProcessGroupSet(Utils.toIntegerSet(param));
                if (type.getProperties().getAllowedGroups().size() > 0) {
                    addingProcessGroups = new HashSet<ProcessGroup>(
                            CollectionUtils.intersection(addingProcessGroups, type.getProperties().getAllowedGroups()));
                }

                if (processGroups.addAll(addingProcessGroups)) {
                    ProcessAction.processGroupsUpdate(form, con, process, processGroups);
                }
            } else if (command.startsWith(COMMAND_SET_STATUS)) {
                int status = Utils.parseInt(StringUtils.substringAfter(command, ":"));

                StatusChange change = new StatusChange();
                change.setDate(new Date());
                change.setProcessId(process.getId());
                change.setUserId(form.getUserId());
                change.setStatusId(status);
                change.setComment("Автоматическая смена статуса");

                ProcessAction.processStatusUpdate(form, con, process, change);
            } else if (command.startsWith("checkExecutorsInGroups")) {
                Set<Integer> groupIds = Utils.toIntegerSet(StringUtils.substringAfter(command, ":"));

                GROUP_LOOP: for (Integer groupId : groupIds) {
                    for (ProcessExecutor processExecutor : process.getExecutors()) {
                        if (processExecutor.getGroupId() == groupId) {
                            continue GROUP_LOOP;
                        }
                    }

                    Group group = UserCache.getUserGroup(groupId);

                    throw new BGMessageException("Не установлен исполнитель из группы:\n " + group.getTitle());
                }
            } else if (command.startsWith("addExecutorsInGroupsInRole")) {
                String curCommand = StringUtils.substringAfter(command, ":");

                Set<Integer> groupIds = Utils.toIntegerSet(StringUtils.substringBefore(curCommand, ":"));
                Set<Integer> userIds = Utils.toIntegerSet(StringUtils.substringBetween(curCommand, ":"));
                int roleId = Utils.parseInt(StringUtils.substringAfterLast(curCommand, ":"), -1);

                ProcessGroup processGroup = getProcessGroup(process, groupIds, roleId);

                // добавление в текущих исполнителей группороли
                Set<ProcessExecutor> executors = ProcessExecutor.getProcessExecutors(process.getExecutors(),
                        Collections.singleton(processGroup));
                executors.addAll(ProcessExecutor.toProcessExecutorSet(userIds, processGroup));

                ProcessAction.processExecutorsUpdate(form, con, process, Collections.singleton(processGroup), executors);
            }
            // обновление исполнитлей для одной из предложенных групп среди групп процесса
            else if (command.startsWith("addExecutorsInGroups")) {
                Set<Integer> groupIds = Utils.toIntegerSet(StringUtils.substringBetween(command, ":"));
                Set<Integer> userIds = Utils.toIntegerSet(StringUtils.substringAfterLast(command, ":"));

                ProcessGroup processGroup = getProcessGroup(process, groupIds);

                // добавление в текущих исполнителей группороли
                Set<ProcessExecutor> executors = ProcessExecutor.getProcessExecutors(process.getExecutors(),
                        Collections.singleton(processGroup));
                executors.addAll(ProcessExecutor.toProcessExecutorSet(userIds, processGroup));

                ProcessAction.processExecutorsUpdate(form, con, process, Collections.singleton(processGroup), executors);
            } else if (command.startsWith(COMMAND_ADD_EXECUTORS)) {
                String param = StringUtils.substringAfter(command, ":");

                Set<Integer> addingExecutorIds = Utils.toIntegerSet(param);

                // определение единственной группороли в которую добавляются исполнители
                ProcessGroup processGroup = null;
                for (ProcessGroup pg : process.getGroups()) {
                    for (Integer executorId : addingExecutorIds) {
                        User user = UserCache.getUser(executorId);
                        if (user.getGroupIds().contains(pg.getGroupId())) {
                            if (processGroup != null && processGroup.getGroupId() != pg.getGroupId()) {
                                throw new BGMessageException("Устанавливаемые исполнители относится к нескольким группам процесса.");
                            }
                            processGroup = pg;
                        }
                    }
                }

                if (processGroup == null) {
                    throw new BGMessageException("Устанавливаемые исполнители не входят в группы, исполняющие процесс.");
                }

                // добавление в текущих исполнителей группороли
                Set<ProcessExecutor> executors = ProcessExecutor.getProcessExecutors(process.getExecutors(),
                        Collections.singleton(processGroup));
                executors.addAll(ProcessExecutor.toProcessExecutorSet(addingExecutorIds, processGroup));

                ProcessAction.processExecutorsUpdate(form, con, process, Collections.singleton(processGroup), executors);
            } else if (command.startsWith("setExecutorsInGroupsIfNot")) {
                Set<Integer> groupIds = Utils.toIntegerSet(StringUtils.substringBetween(command, ":"));
                Set<Integer> userIds = Utils.toIntegerSet(StringUtils.substringAfterLast(command, ":"));

                ProcessGroup processGroup = getProcessGroup(process, groupIds);

                // добавление в текущих исполнителей группороли
                Set<ProcessExecutor> executors = ProcessExecutor.getProcessExecutors(process.getExecutors(),
                        Collections.singleton(processGroup));
                if (executors.size() == 0) {
                    executors = ProcessExecutor.toProcessExecutorSet(userIds, processGroup);
                }

                ProcessAction.processExecutorsUpdate(form, con, process, Collections.singleton(processGroup), executors);
            } else if (command.startsWith("setExecutorsInGroupsInRole")) {
                String curCommand = StringUtils.substringAfter(command, ":");

                Set<Integer> groupIds = Utils.toIntegerSet(StringUtils.substringBefore(curCommand, ":"));
                Set<Integer> userIds = Utils.toIntegerSet(StringUtils.substringBetween(curCommand, ":"));
                int roleId = Utils.parseInt(StringUtils.substringAfterLast(curCommand, ":"), -1);

                ProcessGroup processGroup = getProcessGroup(process, groupIds, roleId);
                // добавление в текущих исполнителей группороли
                Set<ProcessExecutor> executors = ProcessExecutor.toProcessExecutorSet(userIds, processGroup);

                ProcessAction.processExecutorsUpdate(form, con, process, Collections.singleton(processGroup), executors);
            } else if (command.startsWith("setExecutorsInGroups")) {
                Set<Integer> groupIds = Utils.toIntegerSet(StringUtils.substringBetween(command, ":"));
                Set<Integer> userIds = Utils.toIntegerSet(StringUtils.substringAfterLast(command, ":"));

                ProcessGroup processGroup = getProcessGroup(process, groupIds);

                // добавление в текущих исполнителей группороли
                Set<ProcessExecutor> executors = ProcessExecutor.toProcessExecutorSet(userIds, processGroup);

                ProcessAction.processExecutorsUpdate(form, con, process, Collections.singleton(processGroup), executors);
            } else if (command.equals("clearGroups")) {
                ProcessAction.processGroupsUpdate(form, con, process, new HashSet<ProcessGroup>());
            } else if (command.equals("clearExecutors")) {
                ProcessAction.processExecutorsUpdate(form, con, process, process.getGroups(), new HashSet<ProcessExecutor>());
            } else if (command.equals("refreshCurrentQueue")) {
                form.getResponse().addEvent(new ru.bgcrm.event.client.ProcessCurrentQueueRefreshEvent());
            } else if (command.equals("open")) {
                form.getResponse().addEvent(new ru.bgcrm.event.client.ProcessOpenEvent(process.getId()));
            } else if (command.equals("close")) {
                form.getResponse().addEvent(new ru.bgcrm.event.client.ProcessCloseEvent(process.getId()));
            } else if (command.startsWith("decreasePriority")) {
                Integer decPriority = Utils.parseInt(StringUtils.substringAfter(command, ":"));
                if (decPriority > 0) {
                    Integer newPriority = process.getPriority() - decPriority;
                    if (newPriority < 0) {
                        newPriority = 0;
                    }
                    ProcessAction.processPriorityUpdate(form, process, con, newPriority);
                }
            } else if (command.startsWith("increasePriority")) {
                Integer incPriority = Utils.parseInt(StringUtils.substringAfter(command, ":"));
                if (incPriority > 0) {
                    Integer newPriority = process.getPriority() + incPriority;
                    if (newPriority > 9) {
                        newPriority = 9;
                    }
                    ProcessAction.processPriorityUpdate(form, process, con, newPriority);
                }
            } else if (command.startsWith("emailNotifyExecutors")) {
                String[] tokens = command.split(":");

                if (tokens.length < 2) {
                    throw new BGException("Ошибка параметров " + command);
                }

                Integer paramId = Utils.parseInt(tokens[1]);
                String subject = tokens.length > 2 ? tokens[2] : "Изменился процесс";

                // исполнители исключая пользователя, совершившего действие
                Set<Integer> executorIds = new HashSet<Integer>(process.getExecutorIds());
                executorIds.remove(event.getForm().getUserId());

                if (executorIds.size() == 0) {
                    continue;
                }
                String exprText = null;
                if (tokens.length > 3) {
                    exprText = type.getProperties().getConfigMap().get(tokens[3]);
                }

                emailSend(con, form, event, process, type, subject, exprText, paramId, executorIds,
                        "Изменился процесс, в котором вы числитесь исполнителем.");
            } else if (command.startsWith("emailNotifyUsers")) {
                String[] tokens = command.split(":");

                if (tokens.length < 4) {
                    throw new BGException("Ошибка параметров " + command);
                }

                Integer paramId = Utils.parseInt(tokens[1]);
                Set<Integer> userIds = Utils.toIntegerSet(tokens[2]);
                String subject = tokens.length > 3 ? tokens[3] : "Изменился процесс";

                // исключая пользователя, совершившего действие
                userIds.remove(event.getForm().getUserId());

                if (userIds.size() == 0) {
                    continue;
                }
                String exprText = null;
                if (tokens.length > 4) {
                    exprText = type.getProperties().getConfigMap().get(tokens[4]);
                }

                emailSend(con, form, event, process, type, subject, exprText, paramId, userIds,
                        "Изменился процесс, на обновления которого вы подписаны.");
            } else if (command.startsWith("newsNotifyExecutors") || command.startsWith("newsPopupNotifyExecutors")) {
                String subject = Utils.maskEmpty(StringUtils.substringAfterLast(command, ":"), "Изменился процесс ");

                String text = "Изменился процесс, в котором вы числитесь исполнителем.\n\n" + "Описание:<br/>" + process.getDescription();

                ProcessReferenceConfig config = type.getProperties().getConfigMap().getConfig(ProcessReferenceConfig.class);
                process.setReference(config.getReference(con, form, process, "newsNotifySubject", "processCard"));

                if (Utils.notBlankString(process.getReference())) {
                    subject += process.getReference();
                } else {
                    subject += type.getTitle() + "#" + process.getId();
                }

                NewsDAO newsDao = new NewsDAO(con);

                News news = new News();
                news.setCreateDate(new Date());
                news.setLifeTime(200);
                news.setReadTime(400);
                news.setUserId(event.getForm().getUserId());
                news.setPopup(command.startsWith("newsPopup"));

                Set<Integer> userIds = new HashSet<Integer>(process.getExecutorIds());

                news.setTitle(subject);

                text += "<br/><a href='#UNDEF' onClick='openProcess( " + process.getId() + " )'>Перейти к процессу</a>";

                news.setDescription(text);

                userIds.remove(event.getForm().getUserId());

                if (userIds.size() > 0 && Utils.notBlankString(news.getTitle())) {
                    newsDao.updateNewsUsers(news, userIds);
                }
            } else if (command.startsWith("createProcessLinkForSame")) {
                int createTypeId = Utils.parseInt(StringUtils.substringAfter(command, ":"));
                if (createTypeId <= 0) {
                    throw new BGException("Не определён тип для создания");
                }

                SearchResult<Pair<String, Process>> searchResult = new SearchResult<Pair<String, Process>>();
                new ProcessLinkDAO(con).searchLinkedProcessList(searchResult, Process.LINK_TYPE_DEPEND, process.getId(), null, null, null, null,
                        null);

                Pair<String, Process> linked = Utils.getFirst(searchResult.getList());
                if (linked == null) {
                    throw new BGMessageException("Не найден процесс, зависящий от данного.");
                }

                ProcessLinkAction.linkProcessCreate(con, form, linked.getSecond(), -1, null, createTypeId, "", -1);
            } else if (command.startsWith("createProcessLink")) {
                int createTypeId = Utils.parseInt(StringUtils.substringAfter(command, ":"));
                if (createTypeId <= 0) {
                    throw new BGException("Не определён тип для создания");
                }

                ProcessLinkAction.linkProcessCreate(con, form, process, -1, null, createTypeId, "", -1);
            } else if (command.startsWith("setRelativeDateParam")) {
                int paramId = Utils.parseInt(StringUtils.substringBetween(command, ":"));
                Parameter param = ParameterCache.getParameter(paramId);
                if (param == null || (!param.getType().equals(Parameter.TYPE_DATE) && !param.getType().equals(Parameter.TYPE_DATETIME))) {
                    throw new BGException("Не найден параметр с кодом либо неверен:" + paramId);
                }

                int days = Utils.parseInt(StringUtils.substringAfterLast(command, ":"), 1);

                Calendar now = new GregorianCalendar();
                TimeUtils.clear_HOUR_MIN_MIL_SEC(now);
                now.add(Calendar.DAY_OF_YEAR, days);

                ParamValueDAO paramDao = new ParamValueDAO(con);
                if (param.getType().equals(Parameter.TYPE_DATE)) {
                    paramDao.updateParamDate(process.getId(), paramId, now.getTime());
                } else {
                    paramDao.updateParamDateTime(process.getId(), paramId, now.getTime());
                }
            } else if (command.startsWith(COMMAND_SET_PARAM)) {
                int paramId = Utils.parseInt(StringUtils.substringBetween(command, ":"));
                Parameter param = ParameterCache.getParameter(paramId);
                String value = Utils.substringAfter(command, ":", 2);

                switch (Parameter.Type.fromString(param.getType())) {
                case DATE:
                    new ParamValueDAO(con).updateParamDate(process.getId(), param.getId(), TimeUtils.parse(value, param.getDateParamFormat(), null));
                    break;
                case DATETIME:
                    new ParamValueDAO(con).updateParamDateTime(process.getId(), param.getId(),
                            TimeUtils.parse(value, param.getDateParamFormat(), null));
                    break;
                case ADDRESS:
                case BLOB:
                case EMAIL:
                case FILE:
                case LIST:
                case LISTCOUNT:
                case PHONE:
                case TEXT:
                case TREE:
                    throw new BGException("Неподдерживаемый тип параметра для макроса:" + command);
                }
            } else {
                EventProcessor.processEvent(new ProcessDoActionEvent(form, process, command), type.getProperties().getActualScriptName(),
                        new SingleConnectionConnectionSet(con));
            }
        }
    }

    private static void emailSend(Connection con, DynActionForm form, UserEvent event, Process process, ProcessType type, String subject,
            String exprText, int paramId, Set<Integer> userIds, String defaultSubject) throws BGException {
        // вычисление subject с помощью JEXL
        String subjectExpr = type.getProperties().getConfigMap().get(subject);
        if (Utils.notBlankString(subjectExpr)) {
            subject = getMessageChangeText(con, form, event, process, subjectExpr);
        } else {
            subject += " #" + process.getId();

            String description = StringUtils.substringBefore(process.getDescription(), "\n");
            subject += " [" + description + "]";
        }

        // вычисление текста сообщения с помощью JEXL
        String text = null;
        if (exprText == null) {
            text = defaultSubject + "\n\n" + "Описание:\n" + process.getDescription();

            if (event != null && event instanceof ProcessMessageAddedEvent && ((ProcessMessageAddedEvent) event).getMessage() != null) {
                text += "\n\nСообщение:\n" + ((ProcessMessageAddedEvent) event).getMessage().getText();
            }
        } else {
            text = getMessageChangeText(con, form, event, process, exprText);
        }

        Parameter param = ParameterCache.getParameter(paramId);
        if (param == null || !Parameter.TYPE_EMAIL.equals(param.getType())) {
            throw new BGMessageException("Параметр с кодом " + paramId + " не найден или его тип не EMail.");
        }

        ParamValueDAO paramDao = new ParamValueDAO(con);
        for (Integer executorId : userIds) {
            for (ParameterEmailValue value : paramDao.getParamEmail(executorId, paramId).values()) {
                new MailMsg(Setup.getSetup()).sendMessage(value.getValue(), subject, text);
            }
        }
    }

    private static ProcessGroup getProcessGroup(Process process, Set<Integer> groupIds) throws BGMessageException {
        return getProcessGroup(process, groupIds, -1);
    }

    public static ProcessGroup getProcessGroup(Process process, Set<Integer> groupIds, int roleId) throws BGMessageException {
        ProcessGroup processGroup = null;
        Set<ProcessGroup> processGroupSet;
        if (roleId > -1) {
            processGroupSet = process.getProcessGroupWithRole(roleId);
        } else {
            processGroupSet = process.getGroups();
        }

        for (ProcessGroup pg : processGroupSet) {
            if (groupIds.contains(pg.getGroupId())) {
                if (processGroup != null && processGroup.getGroupId() != pg.getGroupId()) {
                    throw new BGMessageException("Среди групп процесса, в этой роли, несколько соответствуют предложенному набору.");
                }
                processGroup = pg;
            }
        }

        if (processGroup == null) {
            throw new BGMessageException("Среди групп процесса, в этой роли, нет предложенных.");
        }
        return processGroup;
    }

    protected static String getMessageChangeText(Connection con, DynActionForm form, UserEvent event, Process process, String exprText)
            throws BGException {
        Map<String, Object> context = DefaultProcessChangeListener.getProcessJexlContext(new SingleConnectionConnectionSet(con), form, event,
                process);

        context.put("lastChangeLogItem", new ProcessDAO(con).getLastProcessChangeLog(process));
        if (event != null && event instanceof ProcessMessageAddedEvent && ((ProcessMessageAddedEvent) event).getMessage() != null)
            context.put("message", ((ProcessMessageAddedEvent) event).getMessage());
        else
            context.put("message", null);

        return new Expression(context).getString(exprText);
    }
}