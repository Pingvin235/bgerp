package org.bgerp.dao.expression;

import java.sql.Connection;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.bgerp.app.exception.BGMessageException;
import org.bgerp.cache.UserCache;
import org.bgerp.plugin.msg.email.ExpressionObject;
import org.bgerp.util.Log;

import ru.bgcrm.dao.process.ProcessDAO;
import ru.bgcrm.model.process.Process;
import ru.bgcrm.model.process.ProcessExecutor;
import ru.bgcrm.model.process.ProcessGroup;
import ru.bgcrm.model.process.ProcessType;
import ru.bgcrm.model.process.StatusChange;
import ru.bgcrm.model.user.User;
import ru.bgcrm.struts.action.ProcessAction;
import ru.bgcrm.struts.form.DynActionForm;

/**
 * Expression object for performing process related operations.
 *
 * @author Shamil Vakhitov
 */
public class ProcessChangeExpressionObject extends ExpressionContextAccessingObject {
    private static final Log log = Log.getLog();

    private final Process process;
    private final DynActionForm form;
    private final Connection con;

    public ProcessChangeExpressionObject(Process process, DynActionForm form, Connection con) {
        this.process = process;
        this.form = form;
        this.con = con;
    }

    @Override
    public void toContext(Map<String, Object> context) {
        super.toContext(context);
        context.put(null, this);
    }

    /**
     * Delete the current process.
    */
    public void delete() throws Exception {
        ProcessAction.processDelete(form, con, process);
    }

    /**
     * Добавляет группы решения в процесс.
     * @param groupIds
     * @param roleId
     * @throws Exception
     */
    public void addGroups(Set<Integer> groupIds, int roleId) throws Exception {
        ProcessType type = ProcessAction.getProcessType(process.getTypeId());

        Set<ProcessGroup> processGroups = new HashSet<>(process.getGroups());
        Set<ProcessGroup> addingProcessGroups = ProcessGroup.toProcessGroupSet(groupIds, roleId);
        if (type.getProperties().getAllowedGroups().size() > 0) {
            addingProcessGroups = new HashSet<ProcessGroup>(
                    CollectionUtils.intersection(addingProcessGroups, type.getProperties().getAllowedGroups()));
        }

        if (processGroups.addAll(addingProcessGroups))
            ProcessAction.processGroupsUpdate(form, con, process, processGroups);
    }

    /**
     * Удаляет группы решения процесса и связанных с ними исполнителей
     * @param ids коды групп решения.
     */
    public void deleteGroups(Set<Integer> ids) throws Exception {
        Set<ProcessGroup> processGroups = process.getGroups().stream()
                .filter(pg -> !ids.contains(pg.getGroupId())).collect(Collectors.toSet());
        ProcessAction.processGroupsUpdate(form, con, process, processGroups);

        Set<ProcessExecutor> executors = process.getExecutors().stream()
                .filter(pe -> !ids.contains(pe.getGroupId())).collect(Collectors.toSet());
        ProcessAction.processExecutorsUpdate(form, con, process, processGroups, executors);
    }

    /**
     * Добавляет исполнителей в процесс. Группы решения уже должны быть установлены.
     * При этом каждый из добавляемых исполнителей должен входить только в одну из этих групп.
     * @param ids
     * @throws Exception
     */
    public void addExecutors(Set<Integer> ids) throws Exception {
        // определение единственной группороли в которую добавляются исполнители
        ProcessGroup processGroup = null;
        for (ProcessGroup pg : process.getGroups()) {
            for (Integer executorId : ids) {
                User user = UserCache.getUser(executorId);
                if (user.getGroupIds().contains(pg.getGroupId())) {
                    if (processGroup != null && processGroup.getGroupId() != pg.getGroupId())
                        throw new BGMessageException("Устанавливаемые исполнители относится к нескольким группам процесса.");
                    processGroup = pg;
                }
            }
        }

        if (processGroup == null)
            throw new BGMessageException("The set executors '{}' are not members of process execution groups '{}'.", ids, process.getGroupIds());

        // добавление в текущих исполнителей группороли
        Set<ProcessExecutor> executors = ProcessExecutor.getProcessExecutors(process.getExecutors(), Collections.singleton(processGroup));
        executors.addAll(ProcessExecutor.toProcessExecutorSet(ids, processGroup));

        ProcessAction.processExecutorsUpdate(form, con, process, Collections.singleton(processGroup), executors);
    }

    /**
     * Adds process executers with defined groups and roles.
     * Execution groups have to be already added.
     * @param groupIds the group IDs, or {@code null} for adding to all execution groups
     * @param userIds the executor IDs to be added
     * @param roleId the role &gt;= 0 or -1 for adding to execution groups with any role
     * @throws Exception
     */
    public void addExecutors(Set<Integer> groupIds, Set<Integer> userIds, int roleId) throws Exception {
        Set<ProcessGroup> processGroups = process.getGroups().stream()
                .filter(pg ->
                    (roleId < 0 || pg.getRoleId() == roleId) &&
                    (groupIds == null || groupIds.contains(pg.getGroupId()))
                )
                .collect(Collectors.toSet());

        if (processGroups.isEmpty())
            throw new BGMessageException("No matching execution groups found; groupIds: {}, roleId: {}", groupIds, roleId);

        Set<ProcessExecutor> executors = ProcessExecutor.getProcessExecutors(process.getExecutors(), processGroups);
        for (var processGroup : processGroups)
            executors.addAll(ProcessExecutor.toProcessExecutorSet(userIds, processGroup));

        ProcessAction.processExecutorsUpdate(form, con, process, processGroups, executors);
    }

    /**
     * Удаляет исполнителей процесса.
     * @param ids коды пользователей.
     */
    public void deleteExecutors(Set<Integer> ids) throws Exception {
        Set<ProcessGroup> processGroups = process.getGroups();
        Set<ProcessExecutor> executors = process.getExecutors().stream()
                .filter(pe -> !ids.contains(pe.getUserId())).collect(Collectors.toSet());
        ProcessAction.processExecutorsUpdate(form, con, process, processGroups, executors);
    }


    /**
     * Sets process priority.
     * @param value the value.
     * @throws Exception
     */
    public void setPriority(int value) throws Exception {
        process.setPriority(value);
        new ProcessDAO(con).updateProcess(process);
    }

    /**
     * Changes process status.
     * @param value status ID.
     * @comment comment.
     * @throws Exception
     */
    public void setStatus(int value, String comment) throws Exception {
        StatusChange change = new StatusChange();
        change.setDate(new Date());
        change.setProcessId(process.getId());
        change.setUserId(form.getUserId());
        change.setStatusId(value);
        change.setComment(comment);

        ProcessAction.processStatusUpdate(form, con, process, change);
        new ProcessDAO(con).updateProcess(process);
    }

    @Deprecated
    public void emailNotifyExecutors(int paramId, String subject, String text) throws Exception {
        log.warnd("Used deprecated call 'emailNotifyExecutors', use 'email.sendMessageToExecutors' instead.");

        var eo = new ExpressionObject();
        eo.toContext(context);
        eo.sendMessageToExecutors(paramId, subject, text);
    }

    @Deprecated
    public void emailNotifyUsers(Collection<Integer> userIds, int paramId, String subject, String text) throws Exception {
        log.warnd("Used deprecated call 'emailNotifyUsers', use 'email.sendMessageToUsers' instead.");

        var eo = new ExpressionObject();
        eo.toContext(context);
        eo.sendMessageToUsers(userIds, paramId, subject, text);
    }

}