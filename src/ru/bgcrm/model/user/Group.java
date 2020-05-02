package ru.bgcrm.model.user;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ru.bgcrm.cache.UserCache;
import ru.bgcrm.model.IdTitle;
import ru.bgcrm.model.TitleWithPath;
import ru.bgcrm.util.ParameterMap;
import ru.bgcrm.util.Preferences;

public class Group extends IdTitle implements TitleWithPath, Cloneable {
    private int archive;
    private int parentId;
    private int childCount;
    private String comment;
    private String config;
    private ParameterMap configMap = new Preferences();
    private Set<Integer> queueIds = new HashSet<Integer>();
    private List<Integer> permsetIds = new ArrayList<Integer>();

    public String getConfig() {
        return config;
    }

    public void setConfig(String config) {
        this.config = config;
        this.configMap = new Preferences(config);
    }

    public ParameterMap getConfigMap() {
        return configMap;
    }

    public int getArchive() {
        return archive;
    }

    public void setArchive(int archive) {
        this.archive = archive;
    }

    public String getComment() {
        return comment;
    }

    public int getParentId() {
        return parentId;
    }

    public void setParentId(int parentId) {
        this.parentId = parentId;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public List<Integer> getPermsetIds() {
        return permsetIds;
    }

    public void setPermsetIds(List<Integer> permsetIds) {
        this.permsetIds = permsetIds;
    }

    public Set<Integer> getQueueIds() {
        return queueIds;
    }

    public void setQueueIds(Set<Integer> queueIds) {
        this.queueIds = queueIds;
    }

    public int getChildCount() {
        return childCount;
    }

    public void setChildCount(int childCount) {
        this.childCount = childCount;
    }

    public String getTitleWithPath() {
        return UserCache.getUserGroupWithPath(UserCache.getUserGroupMap(), id, false);
    }

    /** Посмотреть, где в JSP вызывается и удалить. **/
    @Deprecated
    public String getTitleWithPathId() {
        return UserCache.getUserGroupWithPath(UserCache.getUserGroupMap(), id, true);
    }

    public Set<Integer> getChildSet() {
        Set<Integer> resultSet = new HashSet<Integer>();

        for (Group group : UserCache.getUserGroupChildSet(id)) {
            resultSet.add(group.getId());
        }

        return resultSet;
    }

    public Set<Group> getChildGroupSet() {
        return UserCache.getUserGroupChildSet(id);
    }

    public boolean isAllowExecutorsSet() {
        return configMap.getBoolean("allowExecutorsSet", true);
    }

    public boolean isChildOf(int groupId) {
        Set<Group> resultSet = UserCache.getUserGroupChildFullSet(groupId);

        return resultSet.contains(this);
    }

    public Set<Integer> getParentGroupTreeSet() {
        int parent = parentId;
        Set<Integer> resultSet = new HashSet<Integer>();

        while (parent > 0) {
            resultSet.add(parent);
            parent = UserCache.getUserGroup(parent).getParentId();
        }

        return resultSet;
    }

    public List<Group> getPath() {
        return UserCache.getGroupPath(id);
    }

    public Group clone() {
        Group result = new Group();

        result.setId(id);
        result.setParentId(parentId);
        result.setTitle(title);
        result.setArchive(archive);
        result.setChildCount(childCount);
        result.setComment(comment);
        result.setConfig(config);
        result.setQueueIds(queueIds);
        result.setPermsetIds(permsetIds);

        return result;
    }
}