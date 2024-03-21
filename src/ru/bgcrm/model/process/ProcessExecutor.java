package ru.bgcrm.model.process;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.bgerp.app.exception.BGException;

import ru.bgcrm.cache.UserCache;
import ru.bgcrm.model.user.User;
import ru.bgcrm.model.user.UserGroup;
import ru.bgcrm.util.Utils;

/**
 * Process related executor with group and role.
 *
 * @author Shamil Vakhitov
 */
public class ProcessExecutor {
    private int userId;
    private int groupId;
    private int roleId;

    public ProcessExecutor() {}

    public ProcessExecutor(int userId, int groupId) {
        this(userId, groupId, 0);
    }

    public ProcessExecutor(int userId, int groupId, int roleId) {
        if (groupId < 0) {
            groupId = 0;
        }
        if (roleId < 0) {
            roleId = 0;
        }

        this.groupId = groupId;
        this.roleId = roleId;
        this.userId = userId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getGroupId() {
        return groupId;
    }

    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }

    public int getRoleId() {
        return roleId;
    }

    public void setRoleId(int roleId) {
        this.roleId = roleId;
    }

    public boolean isInProcessGroup(ProcessGroup pg) {
        return getGroupId() == pg.getGroupId() && getRoleId() == pg.getRoleId();
    }

    public ProcessExecutor clone() {
        return new ProcessExecutor(userId, groupId, roleId);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + groupId;
        result = prime * result + roleId;
        result = prime * result + userId;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ProcessExecutor other = (ProcessExecutor) obj;
        if (groupId != other.groupId)
            return false;
        if (roleId != other.roleId)
            return false;
        if (userId != other.userId)
            return false;
        return true;
    }

    public static final Set<ProcessExecutor> parseSafe(String value, Set<ProcessGroup> processGroups) {
        Set<ProcessExecutor> resultSet = new LinkedHashSet<ProcessExecutor>();
        Set<String> parsedValues = Utils.toSet(value, ",");

        for (String item : parsedValues) {
            List<Integer> parsedSet = Utils.toIntegerList(item, ":");

            if (parsedSet.size() > 1) {
                resultSet.add(new ProcessExecutor(parsedSet.get(0), parsedSet.get(1), parsedSet.size() == 3 ? parsedSet.get(2) : 0));
            }

            else if (parsedSet.size() == 1) {
                boolean groupExist = false;

                for (UserGroup userGroup : UserCache.getUserGroupList(parsedSet.get(0))) {
                    for (ProcessGroup processGroup : processGroups) {
                        if (userGroup.getGroupId() == processGroup.getGroupId()) {
                            //так как роль не указана для этого исполнителя, добавляем с 0 ролью (перед этим проверяем есть ли такая группа с такой ролью)
                            if (ProcessGroup.isGroupWithRoleExist(processGroups, processGroup.getGroupId(), 0)) {
                                resultSet.add(new ProcessExecutor(parsedSet.get(0), processGroup.getGroupId(), 0));
                            } else {
                                resultSet.add(new ProcessExecutor(parsedSet.get(0), 0, 0));
                            }

                            groupExist = true;

                            break;
                        }
                    }

                    if (groupExist) {
                        break;
                    }
                }

                if (!groupExist) {
                    resultSet.add(new ProcessExecutor(parsedSet.get(0), 0, 0));
                }
            }
        }

        return resultSet;
    }

    public static final Set<ProcessExecutor> parseUnsafe(Set<String> values, Set<ProcessGroup> processGroups) throws BGException {
        Set<ProcessExecutor> resultSet = new LinkedHashSet<ProcessExecutor>();

        for (String item : values) {
            List<Integer> parsedList = Utils.toIntegerList(item, ":");

            // исполнитель сопоставлен с группой
            if (parsedList.size() > 1) {
                resultSet.add(new ProcessExecutor(parsedList.get(0), parsedList.get(1), parsedList.size() == 3 ? parsedList.get(2) : 0));
            }
            // исполнитель просто указан - старый формат, сопоставление его группе
            // старый формат, просто для совместимости
            else if (parsedList.size() == 1) {
                User user = UserCache.getUser(parsedList.get(0));

                Set<Integer> processGroupIds = new HashSet<Integer>();

                for (ProcessGroup processGroup : processGroups) {
                    if (user.getGroupIds().contains(processGroup.getGroupId())) {
                        processGroupIds.add(processGroup.getGroupId());
                    }
                }

                if (processGroupIds.size() != 1) {
                    throw new BGException("Исполнитель состоит либо состоял, в нескольких группах процесса, определить его группу невозможно.");
                }

                resultSet.add(new ProcessExecutor(parsedList.get(0), Utils.getFirst(processGroupIds), 0));
            }
        }

        return resultSet;
    }

    public static final String serialize(Set<ProcessExecutor> processExecutors) {
        StringBuilder result = new StringBuilder();

        if (processExecutors == null) {
            return "";
        }

        for (ProcessExecutor processExecutor : processExecutors) {
            Utils.addCommaSeparated(result, processExecutor.getUserId() + ":" + processExecutor.getGroupId()
                    + (processExecutor.getRoleId() == 0 ? "" : ":" + processExecutor.getRoleId()));
        }

        return result.toString();
    }

    public static final Set<Integer> toGroupSet(Set<ProcessExecutor> processExecutors) {
        Set<Integer> resultSet = new HashSet<Integer>();

        for (ProcessExecutor processExecutor : processExecutors) {
            resultSet.add(processExecutor.getGroupId());
        }

        return resultSet;
    }

    public static final Set<Integer> toExecutorSet(Set<ProcessExecutor> processExecutors) {
        Set<Integer> resultSet = new HashSet<Integer>();

        for (ProcessExecutor processExecutor : processExecutors) {
            resultSet.add(processExecutor.getUserId());
        }

        return resultSet;
    }

    public static final Set<ProcessExecutor> toProcessExecutorSet(Set<Integer> userIds, ProcessGroup processGroup) {
        Set<ProcessExecutor> result = new HashSet<ProcessExecutor>();

        for (Integer userId : userIds) {
            result.add(new ProcessExecutor(userId, processGroup.getGroupId(), processGroup.getRoleId()));
        }

        return result;
    }

    public static final Set<ProcessExecutor> getProcessExecutors(Set<ProcessExecutor> processExecutors, ProcessGroup processGroup) {
        return getProcessExecutors(processExecutors, Collections.singleton(processGroup));
    }

    public static final Set<ProcessExecutor> getProcessExecutors(Set<ProcessExecutor> processExecutors, Set<ProcessGroup> processGroups) {
        Set<ProcessExecutor> result = new HashSet<ProcessExecutor>();

        for (ProcessExecutor pe : processExecutors) {
            if (processGroups.contains(pe.createProcessGroup())) {
                result.add(pe);
            }
        }

        return result;
    }

    public ProcessGroup createProcessGroup() {
        return new ProcessGroup(groupId, roleId);
    }

    public static final void updateProcessExecutors(Set<ProcessExecutor> processExecutors, ProcessGroup processGroup, Set<Integer> executorIds) {
        Iterator<ProcessExecutor> it = processExecutors.iterator();
        while (it.hasNext()) {
            ProcessExecutor processExecutor = it.next();
            if (processExecutor.isInProcessGroup(processGroup)) {
                it.remove();
            }
        }

        processExecutors.addAll(toProcessExecutorSet(executorIds, processGroup));
    }

    public static Set<Integer> getExecutorsWithRole(Set<ProcessExecutor> processExecutorSet, int role) {
        Set<Integer> result = new HashSet<Integer>();

        for (ProcessExecutor value : processExecutorSet) {
            if (value.getRoleId() == role) {
                result.add(value.getUserId());
            }
        }

        return result;
    }
}
