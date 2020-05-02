package ru.bgcrm.model.user;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ru.bgcrm.cache.UserCache;

public class UserGroup {
    private int groupId;
    private Date dateFrom;
    private Date dateTo;

    public UserGroup() {}

    public UserGroup(int groupId, Date dateFrom, Date dateTo) {
        this.groupId = groupId;
        this.dateFrom = dateFrom;
        this.dateTo = dateTo;
    }

    public int getGroupId() {
        return groupId;
    }

    public void setGroupId(int id) {
        this.groupId = id;
    }

    public Date getDateFrom() {
        return dateFrom;
    }

    public void setDateFrom(Date dateFrom) {
        this.dateFrom = dateFrom;
    }

    public Date getDateTo() {
        return dateTo;
    }

    public void setDateTo(Date dateTo) {
        this.dateTo = dateTo;
    }

    public static List<Group> toGroupList(List<UserGroup> userGroupList) {
        List<Group> resultList = new ArrayList<Group>();

        for (UserGroup userGroup : userGroupList) {
            resultList.add(UserCache.getUserGroup(userGroup.getGroupId()));
        }

        return resultList;
    }

    /**
     * Использовать {@link #getGroupId()}.
     * @return
     */
    @Deprecated
    public int getId() {
        return groupId;
    }

    /**
     * Использовать {@link #setGroupId(int)}.
     * @return
     */
    @Deprecated
    public void setId(int id) {
        this.groupId = id;
    }
}