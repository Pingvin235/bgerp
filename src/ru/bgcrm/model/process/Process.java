package ru.bgcrm.model.process;

import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;

import ru.bgcrm.cache.ProcessTypeCache;
import ru.bgcrm.cache.UserCache;
import ru.bgcrm.model.BGException;
import ru.bgcrm.model.SearchableIdTitle;
import ru.bgcrm.model.user.Group;
import ru.bgcrm.util.TimeUtils;
import ru.bgcrm.util.Utils;

public class Process extends SearchableIdTitle implements Comparable<Process>, Cloneable {
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

    private Set<ProcessGroup> processGroups = new HashSet<ProcessGroup>();
    private Set<ProcessExecutor> processExecutors = new HashSet<ProcessExecutor>();
    
    public Process() {}
    
    public Process(int id) {
        this.id = id;
    }

    public Set<ProcessExecutor> getProcessExecutors() {
        return processExecutors;
    }

    public Set<ProcessExecutor> getProcessExecutorsWithRole(int roleId) {
        Set<ProcessExecutor> executorsWithRole = new HashSet<ProcessExecutor>();
        for (ProcessExecutor executor : processExecutors) {
            if (executor.getRoleId() == roleId) {
                executorsWithRole.add(executor);
            }
        }
        return executorsWithRole;
    }

    public Set<ProcessExecutor> getProcessExecutorsWithRoles(Set<Integer> roleIds) {
        Set<ProcessExecutor> executorsWithRole = new HashSet<ProcessExecutor>();
        for (ProcessExecutor executor : processExecutors) {
            if (roleIds.contains(executor.getRoleId())) {
                executorsWithRole.add(executor);
            }
        }
        return executorsWithRole;
    }

    public Set<ProcessExecutor> getProcessExecutorsInGroupWithRole(int roleId, int groupId) {
        Set<ProcessExecutor> executorsWithRole = new HashSet<ProcessExecutor>();
        for (ProcessExecutor executor : processExecutors) {
            if (executor.getRoleId() == roleId && executor.getGroupId() == groupId) {
                executorsWithRole.add(executor);
            }
        }
        return executorsWithRole;
    }

    public Set<ProcessExecutor> getProcessExecutorsWithGroups(Set<Integer> groups) {
        Set<ProcessExecutor> result = new HashSet<ProcessExecutor>();
        for (ProcessExecutor executor : processExecutors) {
            if (groups.contains(executor.getGroupId())) {
                result.add(executor);
            }
        }
        return result;
    }

    public void setProcessExecutors(Set<ProcessExecutor> processExecutors) {
        this.processExecutors = processExecutors;
    }

    public Set<ProcessGroup> getProcessGroups() {
        return processGroups;
    }

    public Set<ProcessGroup> getProcessGroupWithRole(int roleId) {
        Set<ProcessGroup> groupsWithRole = new HashSet<ProcessGroup>();
        for (ProcessGroup group : processGroups) {
            if (group.getRoleId() == roleId) {
                groupsWithRole.add(group);
            }
        }
        return groupsWithRole;
    }

    public Set<ProcessGroup> getProcessGroupWithRoles(Set<Integer> roleIds) {
        Set<ProcessGroup> groupsWithRole = new HashSet<ProcessGroup>();
        for (ProcessGroup group : processGroups) {
            if (roleIds.contains(group.getRoleId())) {
                groupsWithRole.add(group);
            }
        }
        return groupsWithRole;
    }

    public void setProcessGroups(Set<ProcessGroup> processGroups) {
        this.processGroups = processGroups;
    }

    public int getTypeId() {
        return typeId;
    }

    public void setTypeId(int typeId) {
        this.typeId = typeId;
    }

    public int getStatusId() {
        return statusId;
    }

    public void setStatusId(int statusId) {
        this.statusId = statusId;
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

    public void setDescription(String description) {
        this.description = description;
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

    public String getTypeTitle() {
        ProcessType processType = ProcessTypeCache.getProcessType(typeId);
        return processType != null ? processType.getTitle() : "??? (" + typeId + ")";
    }

    public Set<Integer> getGroupIds() {
        return new ProcessGroupsWrapper(processGroups);
    }

    public Set<Integer> getExecutorIds() {
        return Collections.unmodifiableSet(ProcessExecutor.toExecutorSet(processExecutors));
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

        for (ProcessGroup processGroup : processGroups) {
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
                && process.getDescription().equals(description) && CollectionUtils.isEqualCollection(process.getProcessGroups(), processGroups)
                && CollectionUtils.isEqualCollection(process.getProcessExecutors(), processExecutors);
        return result;
    }

    public String getChangesLog(Process oldProcess) throws BGException {
        StringBuilder result = new StringBuilder();

        final String separator = "; ";

        if (statusId != oldProcess.getStatusId()) {
            Utils.addSeparated(result, separator, "Статус: " + ProcessTypeCache.getStatusSafe(statusId).getTitle());
        }

        if (!description.equals(oldProcess.getDescription())) {
            Utils.addSeparated(result, separator, "Описание");
        }

        if (typeId != oldProcess.getTypeId()) {
            Utils.addSeparated(result, separator, " Тип: " + ProcessTypeCache.getProcessTypeSafe(typeId).getTitle());
        }

        if (priority != oldProcess.getPriority()) {
            Utils.addSeparated(result, separator, "Приоритет: " + priority);
        }

        if (!CollectionUtils.isEqualCollection(processGroups, oldProcess.getProcessGroups())) {
            StringBuilder groupString = new StringBuilder();

            for (ProcessGroup pg : processGroups) {
                Group group = UserCache.getUserGroup(pg.getGroupId());
                if (group == null) {
                    continue;
                }

                // TODO: вывод ролей
                Utils.addCommaSeparated(groupString, group.getTitle());
            }

            result.append("Группы решения: [" + groupString + "]");
        }

        if (!CollectionUtils.isEqualCollection(processExecutors, oldProcess.getProcessExecutors())) {
            String executorString = "";

            for (Integer item : ProcessExecutor.toExecutorSet(processExecutors)) {
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

    public int getUnboundedExecutorsCount() {
        int result = 0;

        for (ProcessExecutor processExecutor : processExecutors) {
            if (processExecutor.getGroupId() == 0) {
                result++;
            }
        }

        return result;
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
        process.processExecutors = new LinkedHashSet<ProcessExecutor>(processExecutors);
        process.processGroups = new LinkedHashSet<ProcessGroup>(processGroups);
        process.reference = reference;
        process.statusId = statusId;
        process.statusTime = statusTime;
        process.statusTitle = statusTitle;
        process.statusUserId = statusUserId;
        process.title = title;
        process.typeId = typeId;
        process.typeTitle = typeTitle;

        return process;
    }
}
