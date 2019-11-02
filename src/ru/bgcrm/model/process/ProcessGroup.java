package ru.bgcrm.model.process;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ru.bgcrm.model.IdTitle;
import ru.bgcrm.util.Utils;

public class ProcessGroup {
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

    public static Set<ProcessGroup> parseIdTitleSet(List<IdTitle> set) {

        Set<ProcessGroup> resultSet = new HashSet<ProcessGroup>();

        for (IdTitle item : set) {
            ProcessGroup processGroup = new ProcessGroup();
            processGroup.setGroupId(item.getId());
            processGroup.setRoleId(Utils.parseInt(item.getTitle()));

            resultSet.add(processGroup);
        }

        return resultSet;
    }

    public static Set<ProcessGroup> parseStringArray(String[] array) {
        Set<ProcessGroup> processGroups = new HashSet<ProcessGroup>();

        if (array == null) {
            return processGroups;
        }

        if (array != null) {
            for (String item : array) {
                ProcessGroup processGroup = new ProcessGroup();

                if (item.indexOf(":") > -1) {
                    processGroup.setGroupId(Utils.parseInt(item.substring(0, item.indexOf(":"))));
                    processGroup.setRoleId(Utils.parseInt(item.substring(item.indexOf(":") + 1)));
                } else {
                    processGroup.setGroupId(Utils.parseInt(item));
                    processGroup.setRoleId(0);
                }

                processGroups.add(processGroup);
            }
        }

        return processGroups;
    }

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
        Set<Integer> resultSet = new HashSet<Integer>();

        for (ProcessGroup processGroup : processGroups) {
            resultSet.add(processGroup.getGroupId());
        }

        return resultSet;
    }

    public static Set<ProcessGroup> toProcessGroupSet(Set<Integer> set) {
        return toProcessGroupSet(set, 0);
    }

    public static Set<ProcessGroup> toProcessGroupSet(Set<Integer> set, int roleId) {
        Set<ProcessGroup> resultSet = new HashSet<ProcessGroup>();

        for (Integer item : set) {
            resultSet.add(new ProcessGroup(item, roleId));
        }

        return resultSet;
    }

    public static Set<ProcessGroup> parseFromStringSet(Set<String> values) {
        Set<ProcessGroup> resultSet = new HashSet<ProcessGroup>();

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
        Set<Integer> result = new HashSet<Integer>();

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
