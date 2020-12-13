package ru.bgcrm.struts.action;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import ru.bgcrm.cache.ParameterCache;
import ru.bgcrm.cache.ProcessTypeCache;
import ru.bgcrm.cache.UserCache;
import ru.bgcrm.dao.ParamValueDAO;
import ru.bgcrm.dao.message.MessageDAO;
import ru.bgcrm.dao.message.MessageTypeNote;
import ru.bgcrm.dao.message.config.MessageTypeConfig;
import ru.bgcrm.dao.process.ProcessDAO;
import ru.bgcrm.dao.process.ProcessTypeDAO;
import ru.bgcrm.dao.process.StatusChangeDAO;
import ru.bgcrm.event.EventProcessor;
import ru.bgcrm.event.UserEvent;
import ru.bgcrm.event.listener.TemporaryObjectOpenListener;
import ru.bgcrm.event.process.ProcessChangedEvent;
import ru.bgcrm.event.process.ProcessChangingEvent;
import ru.bgcrm.event.process.ProcessMessageAddedEvent;
import ru.bgcrm.event.process.ProcessRemovedEvent;
import ru.bgcrm.event.process.ProcessRequestEvent;
import ru.bgcrm.model.BGException;
import ru.bgcrm.model.BGIllegalArgumentException;
import ru.bgcrm.model.BGMessageException;
import ru.bgcrm.model.CommonObjectLink;
import ru.bgcrm.model.EntityLogItem;
import ru.bgcrm.model.SearchResult;
import ru.bgcrm.model.message.Message;
import ru.bgcrm.model.param.Parameter;
import ru.bgcrm.model.process.Process;
import ru.bgcrm.model.process.ProcessExecutor;
import ru.bgcrm.model.process.ProcessGroup;
import ru.bgcrm.model.process.ProcessType;
import ru.bgcrm.model.process.StatusChange;
import ru.bgcrm.model.process.TransactionProperties;
import ru.bgcrm.model.process.TypeProperties;
import ru.bgcrm.model.process.Wizard;
import ru.bgcrm.model.process.config.LinkProcessCreateConfig;
import ru.bgcrm.model.process.config.ProcessReferenceConfig;
import ru.bgcrm.model.process.wizard.WizardData;
import ru.bgcrm.model.user.Group;
import ru.bgcrm.model.user.User;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.ParameterMap;
import ru.bgcrm.util.PatternFormatter;
import ru.bgcrm.util.TimeUtils;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.sql.SingleConnectionConnectionSet;
import ru.bgerp.util.Log;

public class ProcessAction extends BaseAction {
    private static final Log log = Log.getLog();

    @Override
    protected ActionForward unspecified(ActionMapping mapping, DynActionForm actionForm, Connection con) throws Exception {
        return process(mapping, actionForm, con);
    }

    public ActionForward process(ActionMapping mapping, DynActionForm form, Connection con) throws Exception {
        ProcessDAO processDAO = new ProcessDAO(con, form.getUser());

        Process process = processDAO.getProcess(form.getId());
        if (process != null) {
            ProcessType type = ProcessTypeCache.getProcessType(process.getTypeId());
            form.getResponse().setData("process", process);

            form.getHttpRequest().setAttribute("processType", type);

            // генерация описания процесса
            if (type != null) {
                ProcessReferenceConfig config = type.getProperties().getConfigMap().getConfig(ProcessReferenceConfig.class);
                process.setReference(config.getReference(con, form, process, "processCard"));
            }

            // передача мастера
            if (Utils.notBlankString(form.getParam("wizard")) || form.getId() < 0) {
                Wizard wizard = type.getProperties().getCreateWizard();
                if (wizard != null) {
                    form.getHttpRequest().setAttribute("wizardData",
                            new WizardData(con, form, wizard, process, form.getId() < 0 ? wizard.getCreateStepList() : wizard.getStepList()));
                }
            }
        }

        return data(con, mapping, form, "process");
    }

    public static boolean applyProcessTypePermission(List<ProcessType> typeList, DynActionForm form) {
        boolean onlyPermittedTypes = form.getPermission().getBoolean("onlyPermittedTypes", false);
        if (onlyPermittedTypes) {
            var user = form.getUser();

            List<Integer> typeForRemove = new ArrayList<Integer>();
            Iterator<ProcessType> iterator = typeList.iterator();
            while (iterator.hasNext()) {
                ProcessType type = iterator.next();

                /* Undocumented, remove later, 03.05.2020
                if (type.getProperties().getConfigMap().getBoolean("allowForNonExecutorsGroup", false)) {
                    continue;
                } */

                if (CollectionUtils.intersection(type.getProperties().getAllowedGroupsSet(), user.getGroupIds()).isEmpty()
                        && CollectionUtils.intersection(type.getProperties().getGroupsSet(), user.getGroupIds()).isEmpty()) {
                    typeForRemove.add(type.getId());
                    // TODO: Only when type properties are inherited?
                    typeForRemove.addAll(type.getAllChildIds());
                }
            }
            iterator = typeList.iterator();
            while (iterator.hasNext()) {
                ProcessType type = iterator.next();
                if (typeForRemove.contains(type.getId())) {
                    iterator.remove();
                }
            }
        }
        return onlyPermittedTypes;
    }

    public ActionForward processCreateGroups(ActionMapping mapping, DynActionForm form, Connection con) {
        int typeId = form.getParamInt("typeId", 0);
        ProcessType type = ProcessTypeCache.getProcessType(typeId);

        if (type != null) {
            List<Group> groups = new ArrayList<Group>();
            for (int groupId : Utils.toIntegerSet(type.getProperties().getConfigMap().get("onCreateSelectGroup"))) {
                groups.add(UserCache.getUserGroup(groupId));
            }

            form.getResponse().setData("groups", groups);
        }

        return data(con, mapping, form, "processCreateGroup");
    }

    public static Process processCreate(DynActionForm form, Connection con) throws Exception {
        Process process = new Process();

        process.setTypeId(Utils.parseInt(form.getParam("typeId")));
        process.setDescription(Utils.maskNull(form.getParam("description")));

        processCreate(form, con, process, form.getParamInt("groupId", 0));

        return process;
    }

    public static void processCreate(DynActionForm form, Connection con, Process process) throws Exception {
        processCreate(form, con, process, -1);
    }

    public static void processCreate(DynActionForm form, Connection con, Process process, int groupId) throws Exception {
        ProcessDAO processDAO = new ProcessDAO(con);
        StatusChangeDAO changeDao = new StatusChangeDAO(con);

        ProcessType type = ProcessTypeCache.getProcessType(process.getTypeId());
        if (type == null) {
            throw new BGMessageException("Process type does not exist: %s", process.getTypeId());
        }

        TypeProperties typeProperties = type.getProperties();

        process.setStatusId(0);
        process.setCreateUserId(form.getUser().getId());

        processDAO.updateProcess(process);

        StatusChange change = new StatusChange();
        change.setDate(new Date());
        change.setProcessId(process.getId());
        change.setUserId(form.getUserId());
        change.setComment(form.l.l("Процесс создан"));

        change.setStatusId(type.getProperties().getCreateStatus());
        if (!ProcessTypeCache.getStatusMap().containsKey(change.getStatusId())) {
            throw new BGException("Для типа процесса не определён существующий начальный статус");
        }

        changeDao.changeStatus(process, type, change);

        if (groupId > 0) {
            // если вручную указали группу из списка в конфига типа процесса onCreateSelectGroup, то выбраем ее
            Set<ProcessGroup> processGroups = new HashSet<ProcessGroup>();
            processGroups.add(new ProcessGroup(groupId, 0));
            process.setProcessGroups(processGroups);
        } else {
            // иначе выставляем то что указано в конфигурации типа процесса
            process.setProcessGroups(new HashSet<ProcessGroup>(typeProperties.getGroups()));
        }
        processDAO.updateProcessGroups(process.getProcessGroups(), process.getId());

        // wizard=0 в обработке сообщений
        if (form.getParamBoolean("wizard", true)) {
            doCreateWizard(form, con, process, type);
        }

        EventProcessor.processEvent(new ProcessChangedEvent(form, process, ProcessChangedEvent.MODE_CREATED),
                type.getProperties().getActualScriptName(), new SingleConnectionConnectionSet(con));

        form.getResponse().setData("process", process);
    }

    protected static void doCreateWizard(DynActionForm form, Connection con, Process process, ProcessType type) throws BGException {
        // временный процесс - с отрицательным кодом
        Wizard wizard = type.getProperties().getCreateWizard();
        if (wizard != null && !wizard.getCreateStepList().isEmpty()) {
            new ProcessDAO(con).processIdInvert(process);

            TemporaryObjectOpenListener.flushUserData(form.getUserId());
        }
    }

    /** 
     * Создаёт процесс и возвращает его код для перехода в редактор.
     */
    public ActionForward processCreate(ActionMapping mapping, DynActionForm form, Connection con) throws Exception {
        ProcessAction.processCreate(form, con);

        return status(con, form);
    }

    public ActionForward processDeleteTmp(ActionMapping mapping, DynActionForm form, Connection con) throws Exception {
        ProcessDAO processDao = new ProcessDAO(con);

        if (form.getId() > 0) {
            throw new BGIllegalArgumentException();
        }

        Process process = getProcess(processDao, form.getId());
        processDao.deleteProcess(process.getId());

        TemporaryObjectOpenListener.flushUserData(form.getUserId());

        return status(con, form);
    }

    public ActionForward processDelete(ActionMapping mapping, DynActionForm form, Connection con) throws Exception {
        ProcessDAO processDao = new ProcessDAO(con);

        Process process = getProcess(processDao, form.getId());
        processDao.deleteProcess(process.getId());

        processDoEvent(form, process, new ProcessRemovedEvent(form, process), con);

        return status(con, form);
    }

    public ActionForward processFinishCreateTmp(ActionMapping mapping, DynActionForm form, Connection con) throws Exception {
        ProcessDAO processDao = new ProcessDAO(con);

        Process process = getProcess(processDao, form.getId());
        ProcessType type = getProcessType(process.getTypeId());

        processDao.processIdInvert(process);

        EventProcessor.processEvent(new ProcessChangedEvent(form, process, ProcessChangedEvent.MODE_CREATE_FINISHED),
                type.getProperties().getActualScriptName(), new SingleConnectionConnectionSet(con));

        TemporaryObjectOpenListener.flushUserData(form.getUserId());

        return status(con, form);
    }

    public ActionForward processDoCommands(ActionMapping mapping, DynActionForm form, Connection con) throws Exception {
        Process process = getProcess(new ProcessDAO(con), form.getId());

        // FIXME: Security issue, send action ID instead with possibility there define doExpression or use these old commands.
        List<String> commands = Utils.toList(form.getParam("commands"), ";");
        if (commands.size() == 0) {
            throw new BGException("Пустой список команд");
        }

        ProcessCommandExecutor.processDoCommands(con, form, process, null, commands);

        return status(con, form);
    }

    public ActionForward processStatusUpdate(ActionMapping mapping, DynActionForm form, Connection con) throws Exception {
        Process process = getProcess(new ProcessDAO(con), form.getId());

        int statusId = Utils.parseInt(form.getParam("statusId"));
        if ("prev".equals(form.getParam("statusId"))) {
            SearchResult<StatusChange> searchResult = new SearchResult<StatusChange>();
            new StatusChangeDAO(con).searchProcessStatus(searchResult, process.getId(), null);
            if (searchResult.getList().size() < 2) {
                throw new BGMessageException("У процесса не было предыдущего статуса.");
            }

            statusId = searchResult.getList().get(1).getStatusId();
        }

        StatusChange change = new StatusChange();
        change.setDate(new Date());
        change.setProcessId(process.getId());
        change.setStatusId(statusId);
        change.setUserId(form.getUserId());
        change.setComment(form.getParam("comment", ""));

        processStatusUpdate(form, con, process, change);

        return status(con, form);
    }

    public static void processStatusUpdate(DynActionForm form, Connection con, Process process, StatusChange change) throws Exception {
        StatusChangeDAO changeDao = new StatusChangeDAO(con, true);

        ProcessType type = getProcessType(process.getTypeId());

        // указаны обязательные при смене статуса к заполнению параметры
        final String requireParamName = "requireFillParamIdsBeforeStatusSet." + change.getStatusId();

        if (Utils.isBlankString(change.getComment())) {
            Set<Integer> requireStatusChangeComment = Utils
                    .toIntegerSet(type.getProperties().getConfigMap().get("requireChangeCommentStatusIds", ""));
            if (requireStatusChangeComment.contains(change.getStatusId())) {
                throw new BGMessageException("Перевод в данный статус обязан содержать комментарий.");
            }
        }

        ParamValueDAO paramValueDao = new ParamValueDAO(con);

        Set<Integer> requireBeforeParams = Utils.toIntegerSet(type.getProperties().getConfigMap().get(requireParamName, ""));
        for (int requireParamId : requireBeforeParams) {
            Parameter requireParam = ParameterCache.getParameter(requireParamId);
            if (requireParam == null) {
                throw new BGMessageException(
                        "Параметр с кодом " + requireParamId + " не существует.\nУказан в " + requireParamName + " конфигурации типа процесса.");
            }

            if (!paramValueDao.isParameterFilled(process.getId(), requireParam)) {
                throw new BGMessageException("Параметр '" + requireParam.getTitle() + "' не заполнен.");
            }

            EventProcessor.processEvent(new ProcessChangingEvent(form, process, change, ProcessChangingEvent.MODE_STATUS_CHANGING),
                    requireParam.getScript(), new SingleConnectionConnectionSet(con), false);
        }

        processDoEvent(form, process, new ProcessChangingEvent(form, process, change, ProcessChangingEvent.MODE_STATUS_CHANGING), con);

        TransactionProperties transactionProperties = type.getProperties().getTransactionProperties(process.getStatusId(), change.getStatusId());
        if (process.getStatusId() != 0 && !transactionProperties.isEnable())
            throw new BGMessageException("Переход со статуса %s на статус %s невозможен", process.getStatusId(), change.getStatusId());

        changeDao.changeStatus(process, type, change);

        processDoEvent(form, process, new ProcessChangedEvent(form, process, ProcessChangedEvent.MODE_STATUS_CHANGED), con);
    }

    public ActionForward processStatusHistory(ActionMapping mapping, DynActionForm form, Connection con) throws Exception {
        new StatusChangeDAO(con).searchProcessStatus(new SearchResult<StatusChange>(form), form.getId(), form.getSelectedValues("statusId"));

        return data(con, mapping, form, "processStatusHistory");
    }

    public ActionForward processPriorityUpdate(ActionMapping mapping, DynActionForm form, Connection con) throws Exception {
        ProcessDAO processDAO = new ProcessDAO(con, form.getUser(), true);

        Process process = getProcess(processDAO, form.getId());
        int priority = Utils.parseInt(form.getParam("priority"));

        processPriorityUpdate(form, process, con, priority);

        return status(con, form);
    }

    public static void processPriorityUpdate(DynActionForm form, Process process, Connection con, Integer priority) throws Exception {
        ProcessDAO processDAO = new ProcessDAO(con, form.getUser(), true);

        processDoEvent(form, process, new ProcessChangingEvent(form, process, priority, ProcessChangingEvent.MODE_PRIORITY_CHANGING), con);
        process.setPriority(priority);
        processDAO.updateProcess(process);
        processDoEvent(form, process, new ProcessChangedEvent(form, process, ProcessChangedEvent.MODE_PRIORITY_CHANGED), con);
    }

    public ActionForward processTypeEdit(ActionMapping mapping, DynActionForm form, Connection con) throws Exception {
        form.getHttpRequest().setAttribute("typeTreeRoot", ProcessTypeCache.getTypeTreeRoot());

        return data(con, mapping, form, "processTypeChange");
    }

    public ActionForward processTypeUpdate(ActionMapping mapping, DynActionForm form, Connection con) throws Exception {
        ProcessDAO processDAO = new ProcessDAO(con, form.getUser(), true);
        Process process = getProcess(processDAO, form.getId());
        int typeId = Utils.parseInt(form.getParam("typeId"));
        processTypeUpdate(form, process, con, typeId);

        return status(con, form);
    }

    private static void processTypeUpdate(DynActionForm form, Process process, Connection con, Integer typeId) throws Exception {
        ProcessDAO processDAO = new ProcessDAO(con, form.getUser(), true);
        processDoEvent(form, process, new ProcessChangingEvent(form, process, typeId, ProcessChangingEvent.MODE_TYPE_CHANGING), con);
        process.setTypeId(typeId);

        processDAO.updateProcess(process);

        processDoEvent(form, process, new ProcessChangedEvent(form, process, ProcessChangedEvent.MODE_TYPE_CHANGED), con);
    }

    public ActionForward processDescriptionUpdate(ActionMapping mapping, DynActionForm form, Connection con) throws Exception {
        ProcessDAO processDAO = new ProcessDAO(con, form.getUser(), true);

        Process process = getProcess(processDAO, form.getId());
        String description = form.getParam("description");

        processDoEvent(form, process, new ProcessChangingEvent(form, process, description, ProcessChangingEvent.MODE_DESCRIPTION_CHANGING), con);
        process.setDescription(description);
        processDAO.updateProcess(process);
        processDoEvent(form, process, new ProcessChangedEvent(form, process, ProcessChangedEvent.MODE_DESCRIPTION_CHANGED), con);

        return status(con, form);
    }

    public ActionForward processDescriptionAdd(ActionMapping mapping, DynActionForm form, Connection con) throws Exception {
        ProcessDAO processDAO = new ProcessDAO(con, form.getUser(), true);

        final Process process = getProcess(processDAO, form.getId());
        final String description = form.getParam("description");

        if (Utils.isBlankString(description)) {
            throw new BGIllegalArgumentException();
        }

        ProcessType type = getProcessType(process.getTypeId());

        String pattern = type.getProperties().getConfigMap().get("descriptionAddPattern", "(${description}\n)(${text})\t[(${time}) (${user})]");
        final String timePattern = type.getProperties().getConfigMap().get("descriptionAddPattern.timePattern", TimeUtils.FORMAT_TYPE_YMDHMS);

        if (!pattern.contains("${description}")) {
            pattern = "(${description}\n)" + pattern;
        }

        pattern = pattern.replaceAll("\\\\n", "\n").replaceAll("\\\\t", "\t");

        String newDescription = PatternFormatter.processPattern(pattern, new PatternFormatter.PatternItemProcessor() {
            @Override
            public String processPatternItem(String variable) {
                if ("time".equals(variable)) {
                    return TimeUtils.format(new Date(), timePattern);
                } else if ("user".equals(variable)) {
                    return form.getUser().getTitle();
                } else if ("text".equals(variable)) {
                    return description;
                } else if ("description".equals(variable)) {
                    return process.getDescription();
                }
                return "";
            }
        });

        processDoEvent(form, process, new ProcessChangingEvent(form, process, description, ProcessChangingEvent.MODE_DESCRIPTION_ADDING), con);
        process.setDescription(newDescription);
        processDAO.updateProcess(process);
        processDoEvent(form, process, new ProcessChangedEvent(form, process, ProcessChangedEvent.MODE_DESCRIPTION_ADDED), con);

        return status(con, form);
    }

    public ActionForward processGroupsUpdate(ActionMapping mapping, DynActionForm form, Connection con) throws Exception {
        ProcessDAO processDao = new ProcessDAO(con, form.getUser(), true);

        Process process = getProcess(processDao, form.getId());
        Set<ProcessGroup> allowedGroups = ProcessTypeCache.getProcessType(process.getTypeId()).getProperties().getAllowedGroups();

        Set<String> groupRoleSet = form.getSelectedValuesStr("groupRole");
        Set<ProcessGroup> processGroupList = new LinkedHashSet<ProcessGroup>();

        for (String item : groupRoleSet) {
            ProcessGroup processGroup = new ProcessGroup();

            if (item.indexOf(":") > -1) {
                processGroup.setGroupId(Utils.parseInt(StringUtils.substringBefore(item, ":")));
                processGroup.setRoleId(Utils.parseInt(StringUtils.substringAfter(item, ":")));
            } else {
                processGroup.setGroupId(Integer.parseInt(item));
            }

            processGroupList.add(processGroup);
        }

        if (allowedGroups.size() > 0) {
            for (ProcessGroup item : processGroupList) {
                boolean exist = false;

                for (ProcessGroup allowedItem : allowedGroups) {
                    if (item.getGroupId() == allowedItem.getGroupId() && item.getRoleId() == allowedItem.getRoleId()) {
                        exist = true;
                        break;
                    }
                }

                if (!exist)
                    throw new BGException(
                            "Запрещено добавлять группу " + UserCache.getUserGroup(item.getGroupId()).getTitle() + " с ролью " + item.getRoleId());
            }
        }

        processGroupsUpdate(form, con, process, processGroupList);

        return status(con, form);
    }

    public static void processGroupsUpdate(DynActionForm form, Connection con, Process process, Set<ProcessGroup> processGroups) throws Exception {
        ProcessDAO processDao = new ProcessDAO(con, form.getUser(), true);

        processDoEvent(form, process, new ProcessChangingEvent(form, process, processGroups, ProcessChangingEvent.MODE_GROUPS_CHANGING), con);

        process.setGroups(processGroups);
        processDao.updateProcessGroups(processGroups, process.getId());
        
        // удаление исполнителей, привязанных к удалённым группоролям
        boolean updated = false;
        Set<ProcessExecutor> processExecutors = process.getExecutors();
        Iterator<ProcessExecutor> processExecutorsIt = processExecutors.iterator();
        
        while (processExecutorsIt.hasNext()) {
            ProcessExecutor executor = processExecutorsIt.next();
            if (!processGroups.contains(new ProcessGroup(executor.getGroupId(), executor.getRoleId()))) {
                log.debug("Removing executorId: " + executor.getUserId() + "; groupId:" + executor.getGroupId() + "; roleId: " + executor.getRoleId());
                processExecutorsIt.remove();
                updated = true;
            }
        }
        
        if (updated) 
            processDao.updateProcessExecutors(processExecutors, process.getId());

        processDoEvent(form, process, new ProcessChangedEvent(form, process, ProcessChangedEvent.MODE_GROUPS_CHANGED), con);
    }

    public ActionForward processExecutorsUpdate(ActionMapping mapping, DynActionForm form, Connection con) throws Exception {
        Process process = getProcess(new ProcessDAO(con, form.getUser(), true), form.getId());

        // группороли в которых обновляются исполнители
        Set<ProcessGroup> updateGroups = ProcessGroup.parseFromStringSet(form.getSelectedValuesStr("group"));
        Set<ProcessExecutor> executors = ProcessExecutor.parseUnsafe(form.getSelectedValuesStr("executor"), updateGroups);
        
        processExecutorsUpdate(form, con, process, updateGroups, executors);

        return status(con, form);
    }

    @SuppressWarnings("unchecked")
    public static void processExecutorsUpdate(DynActionForm form, Connection con, Process process, Set<ProcessGroup> processGroups,
            Set<ProcessExecutor> processExecutors) throws Exception {
        ProcessDAO processDao = new ProcessDAO(con, form.getUser(), true);
        ParameterMap perm = form.getPermission();

        // различные проверки
        Set<Integer> allowOnlyGroupIds = Utils.toIntegerSet(perm.get("allowOnlyGroups"));
        if (allowOnlyGroupIds.size() != 0) {
            Collection<Integer> denyGroupIds = CollectionUtils.subtract(ProcessGroup.toGroupSet(processGroups), allowOnlyGroupIds);
            if (denyGroupIds.size() > 0)
                throw new BGMessageException("Запрещена правка исполнителей в группах:\n"
                        + Utils.getObjectList(UserCache.getUserGroupList(), new HashSet<Integer>(denyGroupIds)));
        }

        checkExecutorRestriction(process);

        Set<Integer> allowOnlyUsers = Utils.toIntegerSet(perm.get("allowOnlyUsers"));
        if (allowOnlyUsers.size() > 0) {
            // коды исполнителей которые добавляются либо удаляются
            Collection<Integer> changingExecutorIds = CollectionUtils.disjunction(process.getExecutorIds(),
                    ProcessExecutor.toExecutorSet(processExecutors));

            Collection<Integer> denyUserIds = CollectionUtils.subtract(changingExecutorIds, allowOnlyUsers);
            if (denyUserIds.size() > 0)
                throw new BGMessageException(
                        "Запрещена правка исполнителей:\n" + Utils.getObjectList(UserCache.getUserList(), new HashSet<Integer>(denyUserIds)));
        }

        Set<Integer> allowOnlyProcessTypeIds = Utils.toIntegerSet(perm.get("allowOnlyProcessTypeIds"));
        if (allowOnlyProcessTypeIds.size() > 0 && !CollectionUtils.containsAny(allowOnlyProcessTypeIds, Arrays.asList(process.getTypeId())))
            throw new BGMessageException("Запрещена правка исполнителей у данного типа процесса!");

        // проверка обновляемых групп
        for (ProcessGroup processGroup : processGroups) {
            // удаление не привязанных к группе пользователей
            if (processGroup.getGroupId() <= 0)
                continue;

            Group group = UserCache.getUserGroup(processGroup.getGroupId());
            if (group == null)
                throw new BGException("Не найдена группа с кодом: " + processGroup.getGroupId());

            if (!process.getGroups().contains(processGroup))
                throw new BGMessageException("Группа: " + group.getTitle() + " с ролью: " + processGroup.getRoleId() + " не участвует в процессе.");
        }

        // текущие исполнители
        Set<ProcessExecutor> executors = new LinkedHashSet<ProcessExecutor>(process.getExecutors());

        // удаление исполнителей привязанных к обновляемым группоролям, они будут заменены
        Iterator<ProcessExecutor> currentExecutorsIt = executors.iterator();
        while (currentExecutorsIt.hasNext()) {
            ProcessExecutor executor = currentExecutorsIt.next();
            if (processGroups.contains(new ProcessGroup(executor.getGroupId(), executor.getRoleId())))
                currentExecutorsIt.remove();
        }

        // обновляемые исполнители
        executors.addAll(processExecutors);

        processDoEvent(form, process, new ProcessChangingEvent(form, process, executors, ProcessChangingEvent.MODE_EXECUTORS_CHANGING), con);

        process.setExecutors(executors);
        processDao.updateProcessExecutors(executors, process.getId());

        processDoEvent(form, process, new ProcessChangedEvent(form, process, ProcessChangedEvent.MODE_EXECUTORS_CHANGED), con);
    }

    /**
     * Проверяет ограничение на количество исполнителей в процессе
     * @param process
     * @throws BGMessageException
     */
    private static void checkExecutorRestriction(Process process) throws BGMessageException {
        Set<Integer> executorIds = process.getExecutorIds();

        ProcessType processType = ProcessTypeCache.getProcessType(process.getTypeId());
        for (Map.Entry<Integer, ParameterMap> entry : processType.getProperties().getConfigMap().subIndexed("executorRestriction.").entrySet()) {
            ParameterMap paramMap = entry.getValue();
            int groupId = paramMap.getInt("groupId", 0);
            int maxCount = paramMap.getInt("maxCount", 0);

            if (groupId > 0 && maxCount > 0) {
                int count = 0;
                List<User> userList = UserCache.getUserList(new HashSet<Integer>(Arrays.asList(new Integer[] { groupId })));

                for (Integer executorId : executorIds) {
                    User executor = UserCache.getUser(executorId);
                    if (userList.contains(executor)) {
                        count++;
                        if (count > maxCount) {
                            Group group = UserCache.getUserGroup(groupId);
                            StringBuilder sb = new StringBuilder();
                            sb.append("Максимальное количество исполнителей для группы\"");
                            sb.append(group.getTitle());
                            sb.append("\" равно ");
                            sb.append(maxCount);
                            sb.append(".");
                            throw new BGMessageException(sb.toString());
                        }
                    }
                }
            }
        }
    }

    private static void processDoEvent(DynActionForm form, Process process, UserEvent event, Connection con) throws Exception {
        ProcessType type = ProcessTypeCache.getProcessType(process.getTypeId());
        if (type != null) {
            EventProcessor.processEvent(event, type.getProperties().getActualScriptName(), new SingleConnectionConnectionSet(con));
        }
    }

    protected Process getProcess(ProcessDAO processDao, int id) throws BGException {
        Process process = processDao.getProcess(id);
        if (process == null) {
            throw new BGMessageException("Процесс не найдён.");
        }
        return process;
    }

    /**
     * Возвращает тип процесса из кэша по его коду либо генерит исключение, если его нет.
     * @param typeId
     * @return
     * @throws BGMessageException
     */
    public static ProcessType getProcessType(int typeId) throws BGMessageException {
        ProcessType type = ProcessTypeCache.getProcessType(typeId);
        if (type == null) {
            throw new BGMessageException("Не найден тип процесса: %s", typeId);
        }
        return type;
    }

    @Deprecated
    public static Process linkProcessCreate(Connection con, DynActionForm form, Process linkedProcess, int typeId, String objectType,
            int createTypeId, String description, int groupId) throws Exception {
        return ProcessLinkAction.linkProcessCreate(con, form, linkedProcess, typeId, objectType, createTypeId, description, groupId);
    }

    public ActionForward processRequest(ActionMapping mapping, DynActionForm form, Connection con) throws Exception {
        ProcessTypeDAO pDao = new ProcessTypeDAO(con);
        int typeId = form.getParamInt("typeId");

        if (typeId == 0) {
            int createTypeId = form.getParamInt("createTypeId");
            int parentTypeId = form.getParamInt("parentTypeId");

            if (createTypeId == 0 || parentTypeId == 0) {
                throw new BGException("Ошибка параметров запроса");
            }

            typeId = ProcessTypeCache.getProcessType(parentTypeId).getProperties().getConfigMap().getConfig(LinkProcessCreateConfig.class)
                    .getItem(createTypeId).getProcessTypeId();

            if (typeId == 0) {
                throw new BGException("Ошибка параметров запроса");
            }
        }

        ProcessType type = pDao.getProcessType(typeId);

        while (type.isUseParentProperties()) {
            type = pDao.getProcessType(type.getParentId());
        }

        ProcessRequestEvent processRequestEvent = new ProcessRequestEvent(form, type);

        EventProcessor.processEvent(processRequestEvent, type.getProperties().getActualScriptName(), new SingleConnectionConnectionSet(con));

        if (Utils.notBlankString(processRequestEvent.getForwardJspName())) {
            return data(con, mapping, form, processRequestEvent.getForwardJspName());
        } else {
            return status(con, form);
        }
    }

    public ActionForward messageRelatedProcessList(ActionMapping mapping, DynActionForm form, Connection con) throws Exception {
        String addressFrom = form.getParam("from");
        Boolean open = form.getParamBoolean("open", null);

        List<CommonObjectLink> objects = new ArrayList<CommonObjectLink>();
        for (String object : form.getSelectedValuesListStr("object")) {
            int pos = object.lastIndexOf(':');
            if (pos <= 0) {
                log.warn("Incorrect object: " + object);
                continue;
            }

            objects.add(new CommonObjectLink(0, object.substring(0, pos), Utils.parseInt(object.substring(pos + 1)), ""));
        }

        SearchResult<Process> processSearchResult = new SearchResult<Process>(form);
        new ProcessDAO(con, form.getUser()).searchProcessListForMessage(processSearchResult, addressFrom, objects, open);

        return data(con, mapping, form, "messageRelatedProcessList");
    }

    public ActionForward unionLog(ActionMapping mapping, DynActionForm form, Connection con) throws BGException {
        new ProcessDAO(con).searchProcessLog(getProcessType(getProcess(new ProcessDAO(con), form.getId()).getTypeId()), form.getId(),
                new SearchResult<EntityLogItem>(form));

        return data(con, mapping, form, "unionLog");
    }

    public ActionForward userProcessList(ActionMapping mapping, DynActionForm form, Connection con) throws BGException {
        new ProcessDAO(con).searchProcessListForUser(new SearchResult<Process>(form), form.getUserId(), form.getParamBoolean("open", true));

        return data(con, mapping, form, "userProcessList");
    }

    public ActionForward processMerge(ActionMapping mapping, DynActionForm form, Connection con) throws Exception {
        var processDao = new ProcessDAO(con);
        var messageDao = new MessageDAO(con);
        
        var process = getProcess(processDao, form.getId());
        var processTo = getProcess(processDao, form.getParamInt("processId"));

        var mergeText = l.l("Merged from: %s\n", process.getId());

        // TODO: Params as in customer!

        for (var m : messageDao.getProcessMessageList(process.getId(), 0)) {
            m.setText(mergeText + m.getText());
            m.setProcessId(processTo.getId());
            messageDao.updateMessage(m);
        }

        var typeConfig = setup.getConfig(MessageTypeConfig.class);
        var noteMessage = typeConfig.getMessageType(MessageTypeNote.class);
        if (noteMessage != null) {
            var m = new Message();
            m.setTypeId(noteMessage.getId());
            m.setProcessId(processTo.getId());
            m.setProcessed(true);
            m.setText(mergeText + process.getDescription());
            m.setFromTime(new Date());
            m.setUserId(form.getUserId());
            m.setToTime(new Date());
            m.setFrom("");
            m.setTo("");
            messageDao.updateMessage(m);

            processDoEvent(form, processTo, new ProcessMessageAddedEvent(form, m, processTo), con);
        }

        processDao.deleteProcess(process.getId());
        processDoEvent(form, process, new ProcessRemovedEvent(form, process), con);

        return status(con, form);
    }

}