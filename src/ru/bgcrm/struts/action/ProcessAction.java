package ru.bgcrm.struts.action;

import java.sql.Connection;
import java.sql.SQLException;
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
import org.apache.commons.lang3.StringUtils;
import org.apache.struts.action.ActionForward;
import org.bgerp.action.BaseAction;
import org.bgerp.app.cfg.ConfigMap;
import org.bgerp.app.event.EventProcessor;
import org.bgerp.app.exception.BGException;
import org.bgerp.app.exception.BGIllegalArgumentException;
import org.bgerp.app.exception.BGMessageException;
import org.bgerp.cache.ParameterCache;
import org.bgerp.cache.ProcessTypeCache;
import org.bgerp.cache.UserCache;
import org.bgerp.dao.message.process.MessagePossibleProcessSearch;
import org.bgerp.dao.param.ParamValueDAO;
import org.bgerp.dao.process.ProcessCloneDAO;
import org.bgerp.dao.process.ProcessLogDAO;
import org.bgerp.dao.process.ProcessMessageDAO;
import org.bgerp.event.base.UserEvent;
import org.bgerp.model.Pageable;
import org.bgerp.model.config.IsolationConfig;
import org.bgerp.model.config.IsolationConfig.IsolationProcess;
import org.bgerp.model.param.Parameter;
import org.bgerp.model.process.ProcessGroups;
import org.bgerp.model.process.link.ProcessLink;
import org.bgerp.util.Log;

import ru.bgcrm.dao.IfaceStateDAO;
import ru.bgcrm.dao.message.MessageDAO;
import ru.bgcrm.dao.message.MessageTypeNote;
import ru.bgcrm.dao.process.ProcessDAO;
import ru.bgcrm.dao.process.StatusChangeDAO;
import ru.bgcrm.event.listener.TemporaryObjectOpenListener;
import ru.bgcrm.event.process.ProcessChangedEvent;
import ru.bgcrm.event.process.ProcessChangingEvent;
import ru.bgcrm.event.process.ProcessMessageAddedEvent;
import ru.bgcrm.event.process.ProcessRemovedEvent;
import ru.bgcrm.model.EntityLogItem;
import ru.bgcrm.model.Pair;
import ru.bgcrm.model.message.Message;
import ru.bgcrm.model.message.config.MessageTypeConfig;
import ru.bgcrm.model.process.Process;
import ru.bgcrm.model.process.ProcessExecutor;
import ru.bgcrm.model.process.ProcessGroup;
import ru.bgcrm.model.process.ProcessType;
import ru.bgcrm.model.process.StatusChange;
import ru.bgcrm.model.process.TransactionProperties;
import ru.bgcrm.model.process.TypeProperties;
import ru.bgcrm.model.process.Wizard;
import ru.bgcrm.model.process.config.ProcessReferenceConfig;
import ru.bgcrm.model.process.wizard.WizardData;
import ru.bgcrm.model.user.Group;
import ru.bgcrm.model.user.User;
import ru.bgcrm.servlet.ActionServlet.Action;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.PatternFormatter;
import ru.bgcrm.util.TimeUtils;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.sql.ConnectionSet;
import ru.bgcrm.util.sql.SingleConnectionSet;

@Action(path = "/user/process")
public class ProcessAction extends BaseAction {
    private static final String PATH_JSP = PATH_JSP_USER + "/process";

    @Override
    public ActionForward unspecified(DynActionForm form, ConnectionSet conSet) throws Exception {
        return process(form, conSet);
    }

    public ActionForward process(DynActionForm form, ConnectionSet conSet) throws Exception {
        var con = conSet.getConnection();
        var conSlave = conSet.getSlaveConnection();

        ProcessDAO processDAO = new ProcessDAO(con, form);

        var process = processDAO.getProcess(form.getId());
        if (process == null) {
            process = new Process(form.getId());
            process.setReference(l.l("ПРОЦЕСС ДЛЯ ВАС НЕ СУЩЕСТВУЕТ"));
            form.setResponseData("process", process);
        } else {
            ProcessType type = ProcessTypeCache.getProcessType(process.getTypeId());
            form.setResponseData("process", process);

            form.setRequestAttribute("processType", type);

            // reference generation
            if (type != null) {
                ProcessReferenceConfig config = type.getProperties().getConfigMap().getConfig(ProcessReferenceConfig.class);
                process.setReference(config.getReference(conSlave, form, process, "processCard"));
            }

            form.setRequestAttribute("ifaceStateMap", new IfaceStateDAO(conSlave).getIfaceStates(Process.OBJECT_TYPE, process.getId()));

            // wizard
            if (Utils.notBlankString(form.getParam("wizard")) || form.getId() < 0) {
                Wizard wizard = type.getProperties().getCreateWizard();
                if (wizard != null) {
                    form.getHttpRequest().setAttribute("wizardData",
                            new WizardData(con, form, wizard, process, form.getId() < 0 ? wizard.getCreateStepList() : wizard.getStepList()));
                }
            }
        }

        return html(conSet, form, getForwardJspPath(form, Map.of(
            "processGroupsWithRoles", PATH_JSP + "/process/editor_groups_with_roles.jsp",
            "processExecutors", PATH_JSP + "/process/editor_executors.jsp",
            "processStatus", PATH_JSP + "/process/editor_status.jsp",
            "", PATH_JSP + "/process/process.jsp")));
    }

    /**
     * Cleans not allowed process types out of full list.
     * @param typeList initial full list, will be changed.
     * @param form current request info with user and permission.
     */
    public static void applyProcessTypePermission(List<ProcessType> typeList, DynActionForm form) {
        var isolation = form.getUser().getConfigMap().getConfig(IsolationConfig.class).getIsolationProcess();

        final boolean onlyPermittedTypes =
            // when process isolation for the user is 'group'
            isolation == IsolationProcess.GROUP ||
            // or explicitly set by action
            form.getPermission().getBoolean("onlyPermittedTypes", false);
        if (onlyPermittedTypes) {
            var user = form.getUser();

            Iterator<ProcessType> iterator = typeList.iterator();
            while (iterator.hasNext()) {
                var type = iterator.next();
                if (!isolationCheck(type, user)) {
                    iterator.remove();
                }
            }
        }
    }

    /**
     * Checks if the given process type included to user process isolation.
     * For that type allowed or initial groups have to intersect with user groups.
     * Or the check must pass for one of child types.
     * @param type
     * @param user
     * @return
     */
    private static boolean isolationCheck(ProcessType type, User user) {
        boolean result =
                !CollectionUtils.intersection(type.getProperties().getAllowedGroups().getGroupIds(), user.getGroupIds()).isEmpty() ||
                !CollectionUtils.intersection(type.getProperties().getGroups(), user.getGroupIds()).isEmpty();
        if (result)
            return true;

        for (ProcessType child : type.getChildren()) {
            if (isolationCheck(child, user))
                return true;
        }

        return false;
    }

    public ActionForward processCreateGroups(DynActionForm form, Connection con) {
        int typeId = form.getParamInt("typeId", 0);
        ProcessType type = ProcessTypeCache.getProcessType(typeId);

        if (type != null) {
            List<Group> groups = new ArrayList<>();
            for (int groupId : Utils.toIntegerSet(type.getProperties().getConfigMap().get("onCreateSelectGroup"))) {
                groups.add(UserCache.getUserGroup(groupId));
            }

            form.setResponseData("groups", groups);
        }

        return html(con, form, PATH_JSP + "/tree/group_select.jsp");
    }

    public static Process processCreateAndGet(DynActionForm form, Connection con) throws Exception {
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

        ProcessType type = ProcessTypeCache.getProcessTypeOrThrow(process.getTypeId());

        TypeProperties typeProperties = type.getProperties();

        process.setStatusId(0);
        process.setCreateUserId(form.getUser().getId());

        processDAO.updateProcess(process);

        StatusChange change = new StatusChange();
        change.setDate(new Date());
        change.setProcessId(process.getId());
        change.setUserId(form.getUserId());
        change.setComment(form.l.l("Процесс создан"));

        change.setStatusId(type.getProperties().getCreateStatusId());
        if (!ProcessTypeCache.getStatusMap().containsKey(change.getStatusId())) {
            throw new BGException("No initial status defined for the process type");
        }

        changeDao.changeStatus(process, type, change);

        if (groupId > 0) {
            // если вручную указали группу из списка в конфига типа процесса onCreateSelectGroup, то выбраем ее
            ProcessGroups processGroups = new ProcessGroups();
            processGroups.add(new ProcessGroup(groupId, 0));
            process.setGroups(processGroups);
        } else {
            // иначе выставляем то что указано в конфигурации типа процесса
            process.setGroups(new ProcessGroups(typeProperties.getGroups()));
        }
        processDAO.updateProcessGroups(process.getGroups(), process.getId());

        // wizard=0 в обработке сообщений
        if (form.getParamBoolean("wizard", true)) {
            doCreateWizard(form, con, process, type);
        }

        EventProcessor.processEvent(new ProcessChangedEvent(form, process, ProcessChangedEvent.MODE_CREATED), new SingleConnectionSet(con));

        form.setResponseData("process", process);
    }

    protected static void doCreateWizard(DynActionForm form, Connection con, Process process, ProcessType type) throws SQLException {
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
    public ActionForward processCreate(DynActionForm form, Connection con) throws Exception {
        ProcessAction.processCreateAndGet(form, con);

        return json(con, form);
    }

    public ActionForward processClone(DynActionForm form, Connection con) throws Exception {
        var process = new ProcessCloneDAO(con, form).withParams(true).clone(form.getId());

        form.setResponseData("process", process);

        return json(con, form);
    }

    public ActionForward processDeleteTmp(DynActionForm form, Connection con) throws Exception {
        ProcessDAO processDao = new ProcessDAO(con);

        if (form.getId() > 0) {
            throw new BGIllegalArgumentException();
        }

        Process process = getProcess(processDao, form.getId());
        processDao.deleteProcess(process.getId());

        TemporaryObjectOpenListener.flushUserData(form.getUserId());

        return json(con, form);
    }

    public ActionForward processDelete(DynActionForm form, Connection con) throws Exception {
        var process = getProcess(new ProcessDAO(con), form.getId());
        processDelete(form, con, process);
        return json(con, form);
    }

    public static void processDelete(DynActionForm form, Connection con, Process process) throws Exception {
        new ProcessDAO(con).deleteProcess(process.getId());
        processDoEvent(form, process, new ProcessRemovedEvent(form, process), con);
    }

    public ActionForward processFinishCreateTmp(DynActionForm form, Connection con) throws Exception {
        ProcessDAO processDao = new ProcessDAO(con);

        Process process = getProcess(processDao, form.getId());

        processDao.processIdInvert(process);

        EventProcessor.processEvent(new ProcessChangedEvent(form, process, ProcessChangedEvent.MODE_CREATE_FINISHED), new SingleConnectionSet(con));

        TemporaryObjectOpenListener.flushUserData(form.getUserId());

        return json(con, form);
    }

    public ActionForward processDoCommands(DynActionForm form, Connection con) throws Exception {
        Process process = getProcess(new ProcessDAO(con), form.getId());

        // FIXME: Security issue, send action ID instead with possibility there define doExpression or use these old commands.
        List<String> commands = Utils.toList(form.getParam("commands"), ";");
        if (commands.size() == 0) {
            throw new BGException("Пустой список команд");
        }

        ProcessCommandExecutor.processDoCommands(con, form, process, null, commands);

        return json(con, form);
    }

    public ActionForward processStatusUpdate(DynActionForm form, Connection con) throws Exception {
        Process process = getProcess(new ProcessDAO(con), form.getId());

        int statusId = Utils.parseInt(form.getParam("statusId"));
        if ("prev".equals(form.getParam("statusId"))) {
            Pageable<StatusChange> searchResult = new Pageable<>();
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

        return json(con, form);
    }

    public static void processStatusUpdate(DynActionForm form, Connection con, Process process, StatusChange change) throws Exception {
        StatusChangeDAO changeDao = new StatusChangeDAO(con, form);

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

            EventProcessor.processEvent(new ProcessChangingEvent(form, process, change, ProcessChangingEvent.MODE_STATUS_CHANGING), new SingleConnectionSet(con));
        }

        processDoEvent(form, process, new ProcessChangingEvent(form, process, change, ProcessChangingEvent.MODE_STATUS_CHANGING), con);

        TransactionProperties transactionProperties = type.getProperties().getTransactionProperties(process.getStatusId(), change.getStatusId());
        if (process.getStatusId() != 0 && !transactionProperties.isEnable())
            throw new BGMessageException("Переход со статуса {} на статус {} невозможен", process.getStatusId(), change.getStatusId());

        changeDao.changeStatus(process, type, change);
        process.setStatusChange(change);

        processDoEvent(form, process, new ProcessChangedEvent(form, process, ProcessChangedEvent.MODE_STATUS_CHANGED), con);
    }

    public ActionForward processStatusHistory(DynActionForm form, Connection con) throws Exception {
        new StatusChangeDAO(con).searchProcessStatus(new Pageable<>(form), form.getId(), form.getParamValues("statusId"));
        return html(con, form, PATH_JSP + "/process/status_history.jsp");
    }

    public ActionForward processPriorityUpdate(DynActionForm form, Connection con) throws Exception {
        ProcessDAO processDAO = new ProcessDAO(con, form);

        Process process = getProcess(processDAO, form.getId());
        int priority = Utils.parseInt(form.getParam("priority"));

        processPriorityUpdate(form, process, con, priority);

        return json(con, form);
    }

    public static void processPriorityUpdate(DynActionForm form, Process process, Connection con, Integer priority) throws Exception {
        ProcessDAO processDAO = new ProcessDAO(con, form);

        processDoEvent(form, process, new ProcessChangingEvent(form, process, priority, ProcessChangingEvent.MODE_PRIORITY_CHANGING), con);
        process.setPriority(priority);
        processDAO.updateProcess(process);
        processDoEvent(form, process, new ProcessChangedEvent(form, process, ProcessChangedEvent.MODE_PRIORITY_CHANGED), con);
    }

    public ActionForward processTypeEdit(DynActionForm form, Connection con) throws Exception {
        form.getHttpRequest().setAttribute("typeTreeRoot", ProcessTypeCache.getTypeTreeRoot());

        return html(con, form, PATH_JSP + "/process/editor_type.jsp");
    }

    public ActionForward processTypeUpdate(DynActionForm form, Connection con) throws Exception {
        ProcessDAO processDAO = new ProcessDAO(con, form);
        Process process = getProcess(processDAO, form.getId());
        int typeId = Utils.parseInt(form.getParam("typeId"));
        processTypeUpdate(form, process, con, typeId);

        return json(con, form);
    }

    private static void processTypeUpdate(DynActionForm form, Process process, Connection con, Integer typeId) throws Exception {
        ProcessDAO processDAO = new ProcessDAO(con, form);
        processDoEvent(form, process, new ProcessChangingEvent(form, process, typeId, ProcessChangingEvent.MODE_TYPE_CHANGING), con);
        process.setTypeId(typeId);

        processDAO.updateProcess(process);

        processDoEvent(form, process, new ProcessChangedEvent(form, process, ProcessChangedEvent.MODE_TYPE_CHANGED), con);
    }

    public ActionForward processDescriptionUpdate(DynActionForm form, Connection con) throws Exception {
        ProcessDAO processDAO = new ProcessDAO(con, form);

        Process process = getProcess(processDAO, form.getId());
        String description = form.getParam("description");

        processDoEvent(form, process, new ProcessChangingEvent(form, process, description, ProcessChangingEvent.MODE_DESCRIPTION_CHANGING), con);
        process.setDescription(description);
        processDAO.updateProcess(process);
        processDoEvent(form, process, new ProcessChangedEvent(form, process, ProcessChangedEvent.MODE_DESCRIPTION_CHANGED), con);

        return json(con, form);
    }

    public ActionForward processDescriptionAdd(DynActionForm form, Connection con) throws Exception {
        ProcessDAO processDAO = new ProcessDAO(con, form);

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

        String newDescription = PatternFormatter.processPattern(pattern, variable -> {
            if ("time".equals(variable))
                return TimeUtils.format(new Date(), timePattern);
            if ("user".equals(variable))
                return form.getUser().getTitle();
            if ("text".equals(variable))
                return description;
            if ("description".equals(variable))
                return process.getDescription();
            return "";
        });

        processDoEvent(form, process, new ProcessChangingEvent(form, process, description, ProcessChangingEvent.MODE_DESCRIPTION_ADDING), con);
        process.setDescription(newDescription);
        processDAO.updateProcess(process);
        processDoEvent(form, process, new ProcessChangedEvent(form, process, ProcessChangedEvent.MODE_DESCRIPTION_ADDED), con);

        return json(con, form);
    }

    public ActionForward processGroupsUpdate(DynActionForm form, Connection con) throws Exception {
        ProcessDAO processDao = new ProcessDAO(con, form);

        Process process = getProcess(processDao, form.getId());
        Set<ProcessGroup> allowedGroups = ProcessTypeCache.getProcessType(process.getTypeId()).getProperties().getAllowedGroups();

        Set<String> groupRoleSet = form.getParamValuesStr("groupRole");
        Set<ProcessGroup> processGroupList = new LinkedHashSet<>();

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
                    throw new BGMessageException("Forbidden to add group {} with role {}", UserCache.getUserGroup(item.getGroupId()).getTitle(), item.getRoleId());
            }
        }

        processGroupsUpdate(form, con, process, processGroupList);

        return json(con, form);
    }

    public static void processGroupsUpdate(DynActionForm form, Connection con, Process process, Set<ProcessGroup> processGroups) throws Exception {
        ProcessDAO processDao = new ProcessDAO(con, form);

        processDoEvent(form, process, new ProcessChangingEvent(form, process, processGroups, ProcessChangingEvent.MODE_GROUPS_CHANGING), con);

        process.setGroups(new ProcessGroups(processGroups));
        processDao.updateProcessGroups(processGroups, process.getId());

        // удаление исполнителей, привязанных к удалённым группоролям
        boolean updated = false;
        Set<ProcessExecutor> processExecutors = process.getExecutors();
        Iterator<ProcessExecutor> processExecutorsIt = processExecutors.iterator();

        Log log = Log.getLog(ProcessAction.class);

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

    public ActionForward processExecutorsUpdate(DynActionForm form, Connection con) throws Exception {
        Process process = getProcess(new ProcessDAO(con, form), form.getId());

        // группороли в которых обновляются исполнители
        Set<ProcessGroup> updateGroups = ProcessGroup.parseFromStringSet(form.getParamValuesStr("group"));
        Set<ProcessExecutor> executors = ProcessExecutor.parseUnsafe(form.getParamValuesStr("executor"), updateGroups);

        processExecutorsUpdate(form, con, process, updateGroups, executors);

        return json(con, form);
    }

    public ActionForward processExecutorsSwap(DynActionForm form, Connection con) throws Exception {
        ProcessDAO dao = new ProcessDAO(con, form);
        Process process = getProcess(dao, form.getId());

        var groups = process.getGroups();
        if (groups.size() != 2)
            throw new BGMessageException("Swap operation is possible only with two groups");

        var it = groups.iterator();

        var groupId1 = it.next();
        var groupId2 = it.next();

        for (var pe : process.getExecutors()) {
            if (pe.getGroupId() == groupId1.getGroupId() && pe.getRoleId() == groupId1.getRoleId()) {
                pe.setGroupId(groupId2.getGroupId());
                pe.setRoleId(groupId2.getRoleId());
            }
            else {
                pe.setGroupId(groupId1.getGroupId());
                pe.setRoleId(groupId1.getRoleId());
            }
        }

        dao.updateProcessExecutors(process.getExecutors(), form.getId());

        return json(con, form);
    }

    @SuppressWarnings("unchecked")
    public static void processExecutorsUpdate(DynActionForm form, Connection con, Process process, Set<ProcessGroup> processGroups,
            Set<ProcessExecutor> processExecutors) throws Exception {
        ProcessDAO processDao = new ProcessDAO(con, form);
        ConfigMap perm = form.getPermission();

        // различные проверки
        Set<Integer> allowOnlyGroupIds = Utils.toIntegerSet(perm.get("allowOnlyGroups"));
        if (allowOnlyGroupIds.size() != 0) {
            Collection<Integer> denyGroupIds = CollectionUtils.subtract(ProcessGroup.toGroupSet(processGroups), allowOnlyGroupIds);
            if (denyGroupIds.size() > 0)
                throw new BGMessageException("Запрещена правка исполнителей в группах:\n"
                        + Utils.getObjectList(UserCache.getUserGroupList(), new HashSet<>(denyGroupIds)));
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
                        "Запрещена правка исполнителей:\n" + Utils.getObjectList(UserCache.getUserList(), new HashSet<>(denyUserIds)));
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
        Set<ProcessExecutor> executors = new LinkedHashSet<>(process.getExecutors());

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
        for (Map.Entry<Integer, ConfigMap> entry : processType.getProperties().getConfigMap().subIndexed("executorRestriction.").entrySet()) {
            ConfigMap paramMap = entry.getValue();
            int groupId = paramMap.getInt("groupId", 0);
            int maxCount = paramMap.getInt("maxCount", 0);

            if (groupId > 0 && maxCount > 0) {
                int count = 0;
                List<User> userList = UserCache.getUserList(new HashSet<>(Arrays.asList(new Integer[]{groupId})));

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
            EventProcessor.processEvent(event, new SingleConnectionSet(con));
        }
    }

    /**
     * Gets process entity by ID.
     * @param processDao DAO.
     * @param id process ID.
     * @return
     * @throws SQLException - DB exception.
     * @throws BGMessageException - entity not found.
     */
    protected Process getProcess(ProcessDAO processDao, int id) throws SQLException, BGMessageException {
        Process process = processDao.getProcess(id);
        if (process == null) {
            throw new BGMessageException("Процесс не найден.");
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
            throw new BGMessageException("Не найден тип процесса: {}", typeId);
        }
        return type;
    }

    public ActionForward messagePossibleProcessList(DynActionForm form, ConnectionSet conSet) throws Exception {
        restoreRequestParams(conSet.getConnection(), form, true, true, "open");

        String addressFrom = form.getParam("from");
        Boolean open = form.getParamBoolean("open", null);

        Iterator<String> linkObjectTypeIt = form.getParamValuesListStr("linkObjectType").iterator();
        Iterator<Integer> linkObjectIdIt = form.getParamValuesList("linkObjectId").iterator();

        List<ProcessLink> objects = new ArrayList<>();

        while (linkObjectTypeIt.hasNext())
            objects.add(new ProcessLink(0, linkObjectTypeIt.next(), linkObjectIdIt.next(), ""));

        Pageable<Pair<Process, MessagePossibleProcessSearch>> processSearchResult = new Pageable<>(form);
        new ProcessMessageDAO(conSet.getConnection(), form).searchProcessListForMessage(processSearchResult, addressFrom, objects, open);

        var conSlave = conSet.getSlaveConnection();
        for (var pair : processSearchResult.getList())
            setProcessReference(conSlave, form, pair.getFirst(), "messagePossibleProcess");

        return html(conSet, form, PATH_JSP + "/message_possible_process_list.jsp");
    }

    public ActionForward unionLog(DynActionForm form, Connection con) throws Exception {
        new ProcessLogDAO(con).searchProcessLog(form.l,
                getProcessType(getProcess(new ProcessDAO(con), form.getId()).getTypeId()), form.getId(),
                new Pageable<>(form));

        return html(con, form, "/WEB-INF/jspf/union_log.jsp");
    }

    public ActionForward userProcessList(DynActionForm form, ConnectionSet conSet) throws Exception {
        new ProcessDAO(conSet.getSlaveConnection()).searchProcessListForUser(new Pageable<>(form),
                form.getUserId(), form.getParamBoolean("open", null));

        return html(conSet, form, PATH_JSP + "/user_process_list.jsp");
    }

    public ActionForward processMerge(DynActionForm form, Connection con) throws Exception {
        var processDao = new ProcessDAO(con);
        var messageDao = new MessageDAO(con);

        var process = getProcess(processDao, form.getId());
        var processTo = getProcess(processDao, form.getParamInt("processId"));

        var mergeText = l.l("Merged from: {}\n", process.getId());

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

        return json(con, form);
    }

    protected void setProcessReference(Connection con, DynActionForm form, Process process, String objectType) {
        try {
            ProcessType type = ProcessTypeCache.getProcessType(process.getTypeId());
            if (type != null) {
                ProcessReferenceConfig config = type.getProperties().getConfigMap().getConfig(ProcessReferenceConfig.class);
                process.setReference(config.getReference(con, form, process, objectType));
            }
        } catch (Exception e) {
            process.setReference(e.getMessage());
            log.error(e);
        }
    }
}
