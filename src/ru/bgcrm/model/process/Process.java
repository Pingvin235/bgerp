package ru.bgcrm.model.process;

import java.sql.SQLException;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonIgnore;

import org.apache.commons.collections.CollectionUtils;

import ru.bgcrm.cache.ProcessTypeCache;
import ru.bgcrm.cache.UserCache;
import ru.bgcrm.model.Id;
import ru.bgcrm.model.user.Group;
import ru.bgcrm.util.TimeUtils;
import ru.bgcrm.util.Utils;

public class Process extends Id implements Comparable<Process>, Cloneable {
    public static final String OBJECT_TYPE = "process";

    public static final String LINK_TYPE_LINK = "processLink";
    public static final String LINK_TYPE_DEPEND = "processDepend";
    public static final String LINK_TYPE_MADE = "processMade";

    // при добавлении полей не забывать править clone() !!!

    // код типа
    private int typeId;
    private String typeTitle;
    // дата и время создания
    private Date createTime;
    // дата и время закрытия
    private Date closeTime;

    // id пользователей создавшего и закрывшего процесс
    private int createUserId;
    private int closeUserId;

    // код текущего статуса
    // TODO: эти три поля стоит в перспективе убрать и не использовать хранение их в таблице process,
    // данные получать из statusChange
    private int statusId;
    private String statusTitle;
    private int statusUserId;

    // информация о последней смене статуса
    private StatusChange statusChange;

    // приоритет
    private int priority;
    // дата и время, когда перешел в текущий статус
    private Date statusTime;
    // текстовое описание, процесс решения
    private String description = "";
    // автоматически генерируемое описание
    private String reference = "";

    private Set<ProcessGroup> groups = new HashSet<>();
    private Set<ProcessExecutor> executors = new HashSet<>();

    public Process() {}

    public Process(int id) {
        this.id = id;
    }

    /**
     * Use {@link #getExecutors()}.
     */
    @Deprecated
    public Set<ProcessExecutor> getProcessExecutors() {
        return getExecutors();
    }

    public Set<ProcessExecutor> getExecutors() {
        return executors;
    }

    /**
     * Use {@link #getExecutorIdsWithRole(int)}.
     */
    @Deprecated
    public Set<Integer> getProcessExecutorsWithRole(int roleId) {
        return getExecutorIdsWithRole(roleId);
    }

    /**
     * Returns the set of executor user IDs for the certain role.
     * @param roleId
     * @return
     */
    public Set<Integer> getExecutorIdsWithRole(int roleId) {
        return executors.stream()
            .filter(pe -> pe.getRoleId() == roleId)
            .map(ProcessExecutor::getUserId).collect(Collectors.toSet());
    }

    /**
     * Use {@link #getExecutorIdsWithRoles(Set)}.
     */
    @Deprecated
    public Set<Integer> getProcessExecutorsWithRoles(Set<Integer> roleIds) {
        return getExecutorIdsWithRoles(roleIds);
    }

    /**
     * Returns the set of executor user IDs for the certain roles.
     * @param roleIds
     * @return
     */
    public Set<Integer> getExecutorIdsWithRoles(Set<Integer> roleIds) {
        return executors.stream()
            .filter(pe -> roleIds.contains(pe.getRoleId()))
            .map(ProcessExecutor::getUserId).collect(Collectors.toSet());
    }

    /**
     * Use {@link #getExecutorIdsWithGroupAndRole(int, int)}.
     */
    @Deprecated
    public Set<Integer> getProcessExecutorsInGroupWithRole(int roleId, int groupId) {
        return getExecutorIdsWithGroupAndRole(groupId, roleId);
    }

    /**
     * Returns the set of executor user IDs for the certain role and group.
     * @param groupId
     * @param roleId
     * @return
     */
    public Set<Integer> getExecutorIdsWithGroupAndRole(int groupId, int roleId) {
        return executors.stream()
            .filter(pe -> roleId == pe.getRoleId() && groupId == pe.getGroupId())
            .map(ProcessExecutor::getUserId).collect(Collectors.toSet());
    }

    /**
     * Use {@link #getExecutorIdsWithGroups(Set)}.
     */
    @Deprecated
    public Set<Integer> getProcessExecutorsWithGroups(Set<Integer> groupIds) {
        return getExecutorIdsWithGroups(groupIds);
    }

    /**
     * Returns the set of executor user IDs for the certain groups.
     * @param groupIds
     * @return
     */
    public Set<Integer> getExecutorIdsWithGroups(Set<Integer> groupIds) {
        return executors.stream()
            .filter(pe -> groupIds.contains(pe.getGroupId()))
            .map(ProcessExecutor::getUserId).collect(Collectors.toSet());
    }

    public Set<Integer> getExecutorIds() {
        return Collections.unmodifiableSet(ProcessExecutor.toExecutorSet(executors));
    }

    /**
     * Use {@link #setExecutors(Set)}.
     */
    @Deprecated
    public void setProcessExecutors(Set<ProcessExecutor> processExecutors) {
        setExecutors(processExecutors);
    }

    public void setExecutors(Set<ProcessExecutor> value) {
        this.executors = value;
    }

    public Process withExecutors(Set<ProcessExecutor> value) {
        setExecutors(value);
        return this;
    }

    /**
     * Use {@link #getGroups()}.
     */
    @Deprecated
    public Set<ProcessGroup> getProcessGroups() {
        return getGroups();
    }

    public Set<ProcessGroup> getGroups() {
        return groups;
    }

    /**
     * Use {@link #getGroupIdsWithRole(int)}.
     */
    @Deprecated
    public Set<ProcessGroup> getProcessGroupWithRole(int roleId) {
        Set<ProcessGroup> groupsWithRole = new HashSet<ProcessGroup>();
        for (ProcessGroup group : groups) {
            if (group.getRoleId() == roleId) {
                groupsWithRole.add(group);
            }
        }
        return groupsWithRole;
    }

    /**
     * Returns the set of execution group IDs for the certain role.
     * @param roleId
     * @return
     */
    public Set<Integer> getGroupIdsWithRole(int roleId) {
        return groups.stream()
            .filter(pg -> pg.getRoleId() == roleId)
            .map(ProcessGroup::getGroupId).collect(Collectors.toSet());
    }

    /**
     * Use {@link #getGroupIdsWithRoles(Set)}.
     */
    @Deprecated
    public Set<ProcessGroup> getProcessGroupWithRoles(Set<Integer> roleIds) {
        Set<ProcessGroup> groupsWithRole = new HashSet<ProcessGroup>();
        for (ProcessGroup group : groups) {
            if (roleIds.contains(group.getRoleId())) {
                groupsWithRole.add(group);
            }
        }
        return groupsWithRole;
    }

    /**
     * Returns the set of execution group IDs for the certain roles.
     * @param roleIds
     * @return
     */
    public Set<Integer> getGroupIdsWithRoles(Set<Integer> roleIds) {
        return groups.stream()
            .filter(pg -> roleIds.contains(pg.getRoleId()))
            .map(ProcessGroup::getGroupId).collect(Collectors.toSet());
    }

    public Set<Integer> getGroupIds() {
        return Collections.unmodifiableSet(ProcessGroup.toGroupSet(groups));
    }

    /**
     * Use {@link #setGroups(Set)}.
     */
    @Deprecated
    public void setProcessGroups(Set<ProcessGroup> processGroups) {
        setGroups(processGroups);
    }

    public void setGroups(Set<ProcessGroup> value) {
        this.groups = value;
    }

    public Process withGroups(Set<ProcessGroup> value) {
        setGroups(value);
        return this;
    }

    public int getTypeId() {
        return typeId;
    }

    public void setTypeId(int value) {
        this.typeId = value;
    }

    public Process withTypeId(int value) {
        setTypeId(value);
        return this;
    }

    /**
     * @return process type object from {@link ProcessTypeCache#getProcessTypeSafe(int)}.
     */
    @JsonIgnore
    public ProcessType getType() {
        return ProcessTypeCache.getProcessTypeSafe(typeId);
    }

    /**
     * @return process types title from {@link #getType()}.
     */
    public String getTypeTitle() {
        return getType().getTitle();
    }

    public int getStatusId() {
        return statusId;
    }

    public void setStatusId(int statusId) {
        this.statusId = statusId;
    }

    /**
     * @return process status object from {@link ProcessTypeCache#getStatusSafe(int)}.
     */
    @JsonIgnore
    public Status getStatus() {
        return ProcessTypeCache.getStatusSafe(statusId);
    }

    public Date getStatusTime() {
        return statusTime;
    }

    public void setStatusTime(Date statusTime) {
        this.statusTime = statusTime;
    }

    public int getStatusUserId() {
        return statusUserId;
    }

    public void setStatusUserId(int statusUserId) {
        this.statusUserId = statusUserId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String value) {
        this.description = value;
    }

    public Process withDescription(String value) {
        setDescription(value);
        return this;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public int getCreateUserId() {
        return createUserId;
    }

    public void setCreateUserId(int createUserId) {
        this.createUserId = createUserId;
    }

    public int getCloseUserId() {
        return closeUserId;
    }

    public void setCloseUserId(int closeUserId) {
        this.closeUserId = closeUserId;
    }

    public Date getCloseTime() {
        return closeTime;
    }

    public void setCloseTime(Date closeTime) {
        this.closeTime = closeTime;
    }

    public String getStatusTitle() {
        return ProcessTypeCache.getStatusMap().get(statusId) == null ? "" : ProcessTypeCache.getStatusMap().get(statusId).getTitle();
    }

    public StatusChange getStatusChange() {
        return statusChange;
    }

    public void setStatusChange(StatusChange status) {
        this.statusChange = status;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public Set<Integer> getRoleSet() {
        Set<Integer> resultSet = new HashSet<Integer>();

        for (ProcessGroup processGroup : groups) {
            if (!resultSet.contains(processGroup.getRoleId())) {
                resultSet.add(processGroup.getRoleId());
            }
        }

        return resultSet;
    }

    public Set<Integer> getAllowedToChangeStatusIds() {
        Set<Integer> result = new HashSet<Integer>();

        ProcessType type = ProcessTypeCache.getProcessType(typeId);
        if (type == null) {
            return result;
        }

        Set<Integer> allowedStatusSet = type.getProperties().getAllowedStatusSet(statusId);
        allowedStatusSet.add(statusId);

        List<Status> statusList = Utils.getObjectList(ProcessTypeCache.getStatusMap(), type.getProperties().getStatusIds());
        for (Status status : statusList) {
            if (allowedStatusSet.contains(status.getId())) {
                result.add(status.getId());
            }
        }

        return result;
    }

    public boolean isEqualProperties(Process process) {
        boolean result = id == process.getId() && (createTime == null ? process.getCreateTime() == null : createTime.equals(process.getCreateTime()))
                && (closeTime == null ? process.getCloseTime() == null : closeTime.equals(process.getCloseTime()))
                && statusId == process.getStatusId() && priority == process.getPriority() && typeId == process.getTypeId()
                && (statusTime == null ? process.getStatusTime() == null : statusTime.equals(process.getStatusTime()))
                && process.getDescription().equals(description) && CollectionUtils.isEqualCollection(process.getProcessGroups(), groups)
                && CollectionUtils.isEqualCollection(process.getProcessExecutors(), executors);
        return result;
    }

    public String getChangesLog(Process oldProcess) throws SQLException {
        StringBuilder result = new StringBuilder();

        final String separator = "; ";

        /*if (statusId != oldProcess.getStatusId()) {
            Utils.addSeparated(result, separator, "Статус: " + ProcessTypeCache.getStatusSafe(statusId).getTitle());
        }*/

        if (!description.equals(oldProcess.getDescription())) {
            Utils.addSeparated(result, separator, "Описание");
        }

        if (typeId != oldProcess.getTypeId()) {
            Utils.addSeparated(result, separator, " Тип: " + ProcessTypeCache.getProcessTypeSafe(typeId).getTitle());
        }

        if (priority != oldProcess.getPriority()) {
            Utils.addSeparated(result, separator, "Приоритет: " + priority);
        }

        if (!CollectionUtils.isEqualCollection(groups, oldProcess.getProcessGroups())) {
            StringBuilder groupString = new StringBuilder();

            for (ProcessGroup pg : groups) {
                Group group = UserCache.getUserGroup(pg.getGroupId());
                if (group == null) {
                    continue;
                }

                // TODO: вывод ролей
                Utils.addCommaSeparated(groupString, group.getTitle());
            }

            result.append("Группы решения: [" + groupString + "]");
        }

        if (!CollectionUtils.isEqualCollection(executors, oldProcess.getProcessExecutors())) {
            String executorString = "";

            for (Integer item : ProcessExecutor.toExecutorSet(executors)) {
                executorString += UserCache.getUser(item).getTitle() + ", ";
            }

            if (executorString.length() > 2) {
                executorString = executorString.substring(0, executorString.length() - 2);
            }

            // TODO: вывод ролей
            result.append("Исполнители: [" + executorString + "]");
        }

        return result.toString();
    }

    @Override
    public int compareTo(Process o) {
        return o.getId() - id;
    }

    @Override
    public boolean equals(Object obj) {
        return ((Process) obj).getId() == id;
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public String toString() {
        StringBuffer result = new StringBuffer();
        result.append("ID: ");
        result.append(Integer.toString(id));
        result.append("; Тип: ");
        result.append(typeTitle);

        if (createTime != null) {
            result.append("; Дата создания: ");
            result.append(TimeUtils.format(createTime, TimeUtils.FORMAT_TYPE_YMDHMS));
        }

        if (closeTime != null) {
            result.append("; Дата закрытия: ");
            result.append(TimeUtils.format(closeTime, TimeUtils.FORMAT_TYPE_YMDHMS));
        }
        result.append("; Приоритет: ");
        result.append(priority);
        result.append("; Статус: ");
        result.append(statusTitle);
        result.append("; Описание: ");
        result.append(description);

        return result.toString();
    }

    @Override
    public Process clone() {
        Process process = new Process();

        process.closeTime = closeTime;
        process.closeUserId = closeUserId;
        process.createTime = createTime;
        process.createUserId = createUserId;
        process.description = description;
        process.id = id;
        process.priority = priority;
        process.executors = new LinkedHashSet<>(executors);
        process.groups = new LinkedHashSet<>(groups);
        process.reference = reference;
        process.statusId = statusId;
        process.statusTime = statusTime;
        process.statusTitle = statusTitle;
        process.statusUserId = statusUserId;
        process.typeId = typeId;
        process.typeTitle = typeTitle;

        return process;
    }
}
