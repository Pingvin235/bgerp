package ru.bgcrm.model.process;

import java.util.HashSet;
import java.util.Set;

import org.bgerp.model.process.ProcessGroups;

import ru.bgcrm.util.Utils;

public class ProcessGroup implements Comparable<ProcessGroup> {
    protected int groupId;
    protected int roleId;

    public ProcessGroup() {
    }

    public ProcessGroup(int groupId) {
        this(groupId, 0);
    }

    public ProcessGroup(int groupId, int roleId) {
        if (groupId < 0) {
            groupId = 0;
        }
        if (roleId < 0) {
            roleId = 0;
        }

        this.groupId = groupId;
        this.roleId = roleId;
    }

    public int getRoleId() {
        return roleId;
    }

    public void setRoleId(int roleId) {
        this.roleId = roleId;
    }

    public int getGroupId() {
        return groupId;
    }

    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }

    public String toGroupRolePair() {
        return this.groupId + ":" + this.roleId;
    }

    public boolean checkGroupAndRole(int groupId, int roleId) {
        return this.groupId == groupId && this.roleId == roleId;
    }

    public ProcessGroup createProcessGroup() {
        return new ProcessGroup(groupId, roleId);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + groupId;
        result = prime * result + roleId;
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
        ProcessGroup other = (ProcessGroup) obj;
        if (groupId != other.groupId)
            return false;
        if (roleId != other.roleId)
            return false;
        return true;
    }

    @Override
    public int compareTo(ProcessGroup o) {
        return hashCode() - o.hashCode();
    }

    // TODO: Move all the functions below to ProcessGroups.

    public static final String serialize(Set<ProcessGroup> processGroups) {
        StringBuilder roleGroup = new StringBuilder(20);

        for (ProcessGroup item : processGroups) {
            Utils.addCommaSeparated(roleGroup, String.valueOf(item.getGroupId()));
            // нулевую роль в строку не добавляем для максимальной совместимости с предыдущеми версиями и экономии места
            if (item.getRoleId() > 0) {
                roleGroup.append(":");
                roleGroup.append(item.getRoleId());
            }
        }

        return roleGroup.toString();
    }

    public static final Set<Integer> toGroupSet(Set<ProcessGroup> processGroups) {
        Set<Integer> resultSet = new HashSet<>();

        for (ProcessGroup processGroup : processGroups) {
            resultSet.add(processGroup.getGroupId());
        }

        return resultSet;
    }

    public static Set<ProcessGroup> toProcessGroupSet(Set<Integer> set) {
        return toProcessGroupSet(set, 0);
    }

    public static ProcessGroups toProcessGroupSet(Set<Integer> set, int roleId) {
        ProcessGroups resultSet = new ProcessGroups();

        for (Integer item : set) {
            resultSet.add(new ProcessGroup(item, roleId));
        }

        return resultSet;
    }

    public static Set<ProcessGroup> parseFromStringSet(Set<String> values) {
        Set<ProcessGroup> resultSet = new HashSet<>();

        for (String value : values) {
            if (value.indexOf(":") > -1) {
                resultSet.add(new ProcessGroup(Utils.parseInt(value.substring(0, value.indexOf(":"))),
                        Utils.parseInt(value.substring(value.indexOf(":") + 1))));
            }

            else {
                resultSet.add(new ProcessGroup(Utils.parseInt(value), 0));
            }
        }

        return resultSet;
    }

    public static Set<Integer> getGroupsWithRole(Set<ProcessGroup> processGroupSet, int role) {
        Set<Integer> result = new HashSet<>();

        for (ProcessGroup value : processGroupSet) {
            if (value.getRoleId() == role) {
                result.add(value.getGroupId());
            }
        }

        return result;
    }

    public static boolean isGroupWithRoleExist(Set<ProcessGroup> processGroupSet, int group, int role) {
        for (ProcessGroup value : processGroupSet) {
            if (value.getGroupId() == group && value.getRoleId() == role) {
                return true;
            }
        }

        return false;
    }
}
