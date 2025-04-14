package ru.bgcrm.model.process;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.bgerp.app.l10n.Localization;
import org.bgerp.app.l10n.Localizer;
import org.bgerp.cache.ProcessTypeCache;
import org.bgerp.cache.UserCache;
import org.bgerp.model.base.IdTitle;
import org.bgerp.model.process.ProcessGroups;
import org.bgerp.model.process.config.ProcessTitleConfig;
import org.bgerp.util.Log;

import com.fasterxml.jackson.annotation.JsonIgnore;

import ru.bgcrm.dao.expression.Expression;
import ru.bgcrm.dao.expression.ProcessExpressionObject;
import ru.bgcrm.model.user.Group;
import ru.bgcrm.util.TimeUtils;
import ru.bgcrm.util.Utils;

public class Process extends IdTitle implements Comparable<Process>, Cloneable {
    private static final Log log = Log.getLog();

    public static final String OBJECT_TYPE = "process";

    public static final String LINK_TYPE_LINK = "processLink";
    public static final String LINK_TYPE_DEPEND = "processDepend";
    public static final String LINK_TYPE_MADE = "processMade";

    private int typeId;
    private String typeTitle;

    private Date createTime;
    private int createUserId;

    private Date closeTime;
    private int closeUserId;

    private int statusId;
    private String statusTitle;
    private Date statusTime;
    private int statusUserId;

    private StatusChange statusChange;

    private int priority;

    private String description = "";

    private ProcessGroups groups = new ProcessGroups();
    private Set<ProcessExecutor> executors = new HashSet<>();

    public Process() {}

    public Process(int id) {
        this.id = id;
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

    public int getStatusId() {
        return statusId;
    }

    public void setStatusId(int statusId) {
        this.statusId = statusId;
    }

    public Process withStatusId(int value) {
        setStatusId(value);
        return this;
    }

    /**
     * @return process status object from {@link ProcessTypeCache#getStatusSafe(int)}.
     */
    @JsonIgnore
    public Status getStatus() {
        return ProcessTypeCache.getStatusSafe(statusId);
    }

    public String getStatusTitle() {
        return getStatus().getTitle();
    }

    public Set<Integer> getAllowedToChangeStatusIds() {
        Set<Integer> result = new HashSet<>();

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

    @Override
    public String getTitle() {
        String result = title;

        if (Utils.isBlankString(result)) {
            TypeProperties typeProperties = getType().getProperties();
            if (typeProperties != null) {
                var config = typeProperties.getConfigMap().getConfig(ProcessTitleConfig.class);
                if (config != null && config.isProcessUsed() && !config.isAnyParamUsed()) {
                    var context = new HashMap<String, Object>();
                    new ProcessExpressionObject(this).toContext(context);
                    result = new Expression(context).executeGetString(config.getExpression());
                }
            }
        }

        if (Utils.isBlankString(result))
            result = "#" + id + " " + description;

        return result;
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

    /**
     * @return execution groups.
     */
    public ProcessGroups getGroups() {
        return groups;
    }

    public void setGroups(ProcessGroups value) {
        this.groups = value;
    }

    public Process withGroups(ProcessGroups value) {
        setGroups(value);
        return this;
    }

    public Set<Integer> getGroupIds() {
        return Collections.unmodifiableSet(ProcessGroup.toGroupSet(groups));
    }

    public Set<Integer> getRoleSet() {
        Set<Integer> resultSet = new HashSet<>();

        for (ProcessGroup processGroup : groups) {
            if (!resultSet.contains(processGroup.getRoleId())) {
                resultSet.add(processGroup.getRoleId());
            }
        }

        return resultSet;
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
     * Returns the set of execution group IDs for the certain roles.
     * @param roleIds
     * @return
     */
    public Set<Integer> getGroupIdsWithRoles(Set<Integer> roleIds) {
        return groups.stream()
            .filter(pg -> roleIds.contains(pg.getRoleId()))
            .map(ProcessGroup::getGroupId).collect(Collectors.toSet());
    }


    public Set<ProcessExecutor> getExecutors() {
        return executors;
    }

    public void setExecutors(Set<ProcessExecutor> value) {
        this.executors = value;
    }

    public Process withExecutors(Set<ProcessExecutor> value) {
        setExecutors(value);
        return this;
    }

    public Set<Integer> getExecutorIds() {
        return Collections.unmodifiableSet(ProcessExecutor.toExecutorSet(executors));
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
     * Returns the set of executor user IDs for the certain groups.
     * @param groupIds
     * @return
     */
    public Set<Integer> getExecutorIdsWithGroups(Set<Integer> groupIds) {
        return executors.stream()
            .filter(pe -> groupIds.contains(pe.getGroupId()))
            .map(ProcessExecutor::getUserId).collect(Collectors.toSet());
    }

    public String getChangesLog(Process oldProcess) {
        Localizer l = Localization.getLocalizer();

        StringBuilder result = new StringBuilder();

        final String separator = "; ";

        if (!description.equals(oldProcess.getDescription())) {
            Utils.addSeparated(result, separator, l.l("Description: {}", description));
        }

        if (typeId != oldProcess.getTypeId()) {
            Utils.addSeparated(result, separator, l.l("Type: {}", ProcessTypeCache.getProcessTypeSafe(typeId).getTitle()));
        }

        if (priority != oldProcess.getPriority()) {
            Utils.addSeparated(result, separator, l.l("Priority: {}",  priority));
        }

        if (!CollectionUtils.isEqualCollection(groups, oldProcess.groups)) {
            StringBuilder groupString = new StringBuilder();

            for (ProcessGroup pg : groups) {
                Group group = UserCache.getUserGroup(pg.getGroupId());
                if (group == null) {
                    continue;
                }

                // TODO: Handle roles.
                Utils.addCommaSeparated(groupString, group.getTitle());
            }

            result.append(l.l("Execution groups: [{}]", groupString));
        }

        if (!CollectionUtils.isEqualCollection(executors, oldProcess.executors)) {
            String executorString = "";

            for (Integer item : ProcessExecutor.toExecutorSet(executors)) {
                executorString += UserCache.getUser(item).getTitle() + ", ";
            }

            if (executorString.length() > 2) {
                executorString = executorString.substring(0, executorString.length() - 2);
            }

            // TODO: handle roles
            result.append(l.l("Executors: [{}]", executorString));
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

    public boolean isEqualProperties(Process process) {
        boolean result = id == process.getId() && (createTime == null ? process.getCreateTime() == null : createTime.equals(process.getCreateTime()))
                && (closeTime == null ? process.getCloseTime() == null : closeTime.equals(process.getCloseTime()))
                && statusId == process.getStatusId() && priority == process.getPriority() && typeId == process.getTypeId()
                && (statusTime == null ? process.getStatusTime() == null : statusTime.equals(process.getStatusTime()))
                && process.getDescription().equals(description) && CollectionUtils.isEqualCollection(process.groups, groups)
                && CollectionUtils.isEqualCollection(process.executors, executors);
        return result;
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public String toString() {
        StringBuffer result = new StringBuffer(200);
        result
            .append("ID: ")
            .append(Integer.toString(id))
            .append("; Type: ")
            .append(typeTitle);

        if (createTime != null) {
            result
                .append("; Created: ")
                .append(TimeUtils.format(createTime, TimeUtils.FORMAT_TYPE_YMDHMS));
        }

        if (closeTime != null) {
            result
                .append("; Closed: ")
                .append(TimeUtils.format(closeTime, TimeUtils.FORMAT_TYPE_YMDHMS));
        }
        result
            .append("; Priority: ")
            .append(priority)
            .append("; Status: ")
            .append(statusTitle)
            .append("; Description: ")
            .append(description);

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
        process.groups = new ProcessGroups(groups);
        process.statusId = statusId;
        process.statusTime = statusTime;
        process.statusTitle = statusTitle;
        process.statusUserId = statusUserId;
        process.typeId = typeId;
        process.typeTitle = typeTitle;

        return process;
    }

    // DEPRECATED

    /**
     * Use {@link #getExecutors()}.
     */
    @Deprecated
    @JsonIgnore
    public Set<ProcessExecutor> getProcessExecutors() {
        log.warndMethod("getProcessExecutors", "getExecutors");
        return getExecutors();
    }

    /**
     * Use {@link #getExecutorIdsWithRole(int)}.
     */
    @Deprecated
    public Set<Integer> getProcessExecutorsWithRole(int roleId) {
        log.warndMethod("getProcessExecutorsWithRole", "getExecutorIdsWithRole");
        return getExecutorIdsWithRole(roleId);
    }

    /**
     * Use {@link #getExecutorIdsWithRoles(Set)}.
     */
    @Deprecated
    public Set<Integer> getProcessExecutorsWithRoles(Set<Integer> roleIds) {
        log.warndMethod("getProcessExecutorsWithRoles", "getExecutorIdsWithRoles");
        return getExecutorIdsWithRoles(roleIds);
    }

    /**
     * Use {@link #getExecutorIdsWithGroupAndRole(int, int)}.
     */
    @Deprecated
    public Set<Integer> getProcessExecutorsInGroupWithRole(int roleId, int groupId) {
        log.warndMethod("getProcessExecutorsInGroupWithRole", "getExecutorIdsWithGroupAndRole");
        return getExecutorIdsWithGroupAndRole(groupId, roleId);
    }

    /**
     * Use {@link #getExecutorIdsWithGroups(Set)}.
     */
    @Deprecated
    public Set<Integer> getProcessExecutorsWithGroups(Set<Integer> groupIds) {
        log.warndMethod("getProcessExecutorsWithGroups", "getExecutorIdsWithGroups");
        return getExecutorIdsWithGroups(groupIds);
    }

    /**
     * Use {@link #setExecutors(Set)}.
     */
    @Deprecated
    public void setProcessExecutors(Set<ProcessExecutor> processExecutors) {
        log.warndMethod("setProcessExecutors", "setExecutors");
        setExecutors(processExecutors);
    }

    /**
     * Use {@link #getGroups()}.
     */
    @Deprecated
    @JsonIgnore
    public Set<ProcessGroup> getProcessGroups() {
        log.warndMethod("getProcessGroups", "setGroups");
        return getGroups();
    }

    /**
     * Use {@link #getGroupIdsWithRole(int)}.
     */
    @Deprecated
    public Set<ProcessGroup> getProcessGroupWithRole(int roleId) {
        Set<ProcessGroup> groupsWithRole = new HashSet<>();
        for (ProcessGroup group : groups) {
            if (group.getRoleId() == roleId) {
                groupsWithRole.add(group);
            }
        }
        return groupsWithRole;
    }

    /**
     * Use {@link #getGroupIdsWithRoles(Set)}.
     */
    @Deprecated
    public Set<ProcessGroup> getProcessGroupWithRoles(Set<Integer> roleIds) {
        Set<ProcessGroup> groupsWithRole = new HashSet<>();
        for (ProcessGroup group : groups) {
            if (roleIds.contains(group.getRoleId())) {
                groupsWithRole.add(group);
            }
        }
        return groupsWithRole;
    }

    /**
     * Use {@link #setGroups(Set)}.
     */
    @Deprecated
    public void setProcessGroups(Set<ProcessGroup> processGroups) {
        setGroups(new ProcessGroups( processGroups));
    }
}
