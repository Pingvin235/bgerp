package ru.bgcrm.model;

import java.util.Date;
import java.util.Set;

import org.bgerp.model.base.Id;

import ru.bgcrm.model.user.User;

public class News extends Id {
    /** Popup */
    private boolean popup;
    /** Прочитана. */
    private boolean read;
    private int userId = User.USER_SYSTEM_ID;
    /** Время в днях, после которого новость удаляется. */
    private int lifeTime = 300;
    /** Время в часах, после которого новость становится прочитанной автоматически. */
    private int readTime = 240;
    /** Заголовок. */
    private String title;
    /** Содержимое новости. */
    private String description;
    private Date createDate;
    private Date updateDate;
    /** Группы - получатели новости.*/
    private Set<Integer> groupIds;

    public News() {}

    public News(boolean popup, String title, String description) {
        this.popup = popup;
        this.title = title;
        this.description = description;
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

    public String getDescription() {
        return description;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean isRead) {
        this.read = isRead;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isPopup() {
        return popup;
    }

    public void setPopup(boolean isPopup) {
        this.popup = isPopup;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public Date getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(Date updateDate) {
        this.updateDate = updateDate;
    }

    public Set<Integer> getGroupIds() {
        return groupIds;
    }

    public void setGroupIds(Set<Integer> groupIds) {
        this.groupIds = groupIds;
    }
}
