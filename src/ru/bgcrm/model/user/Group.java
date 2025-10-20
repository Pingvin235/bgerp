package ru.bgcrm.model.user;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bgerp.app.cfg.ConfigMap;
import org.bgerp.app.cfg.Preferences;
import org.bgerp.cache.UserCache;
import org.bgerp.model.base.IdTitleComment;
import org.bgerp.model.base.iface.TitleWithPath;
import org.bgerp.util.Dynamic;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class Group extends IdTitleComment implements TitleWithPath, Cloneable {
    private int archive;
    private int parentId;
    private int childCount;
    private String config;
    private ConfigMap configMap = new Preferences();
    private Set<Integer> queueIds = new HashSet<>();
    private List<Integer> permsetIds = new ArrayList<>();

    public String getConfig() {
        return config;
    }

    public void setConfig(String config) {
        this.config = config;
        this.configMap = new Preferences(config);
    }

    public ConfigMap getConfigMap() {
        return configMap;
    }

    public int getArchive() {
        return archive;
    }

    public void setArchive(int archive) {
        this.archive = archive;
    }

    public int getParentId() {
        return parentId;
    }

    public void setParentId(int parentId) {
        this.parentId = parentId;
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

    public Set<Integer> getChildSet() {
        Set<Integer> resultSet = new HashSet<>();

        for (Group group : UserCache.getUserGroupChildSet(id)) {
            resultSet.add(group.getId());
        }

        return resultSet;
    }

    @JsonIgnore
    @Dynamic
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