package ru.bgcrm.model;

import java.util.Date;
import java.util.Set;

import org.bgerp.model.base.IdTitle;
import org.bgerp.util.Log;

import ru.bgcrm.model.user.User;

public class News extends IdTitle {
    private static final Log log = Log.getLog();

    private int userId = User.USER_SYSTEM_ID;
    private Date createTime;
    private Date updateTime;
    /** Content */
    private String text;
    private boolean popup;
    /** Days before deletion */
    private int lifeTime = 300;
    /** Hours before automatically became read */
    private int readTime = 240;
    /** Receiving group IDs */
    private Set<Integer> groupIds;
    private boolean read;

    public News() {}

    public News(boolean popup, String title, String description) {
        this.popup = popup;
        this.title = title;
        this.text = description;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createDate) {
        this.createTime = createDate;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateDate) {
        this.updateTime = updateDate;
    }

    public String getText() {
        return text;
    }

    public void setText(String description) {
        this.text = description;
    }

    public boolean isPopup() {
        return popup;
    }

    public void setPopup(boolean isPopup) {
        this.popup = isPopup;
    }

    public int getReadTime() {
        return readTime;
    }

    public void setReadTime(int readTime) {
        this.readTime = readTime;
    }

    public int getLifeTime() {
        return lifeTime;
    }

    public void setLifeTime(int lifeTime) {
        this.lifeTime = lifeTime;
    }

    public Set<Integer> getGroupIds() {
        return groupIds;
    }

    public void setGroupIds(Set<Integer> groupIds) {
        this.groupIds = groupIds;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean isRead) {
        this.read = isRead;
    }

    // deprecated

    @Deprecated
    public Date getCreateDate() {
        log.warndMethod("getCreateDate", "getCreateTime");
        return createTime;
    }

    @Deprecated
    public void setCreateDate(Date value) {
        log.warndMethod("setCreateDate", "setCreateTime");
        this.createTime = value;
    }

    @Deprecated
    public Date getUpdateDate() {
        log.warndMethod("getUpdateDate", "getUpdateTime");
        return updateTime;
    }

    @Deprecated
    public void setUpdateDate(Date value) {
        log.warndMethod("setUpdateDate", "setUpdateTime");
        this.updateTime = value;
    }

    @Deprecated
    public String getDescription() {
        log.warndMethod("getDescription", "getText");
        return text;
    }

    @Deprecated
    public void setDescription(String value) {
        log.warndMethod("setDescription", "setText");
        this.text = value;
    }
}
