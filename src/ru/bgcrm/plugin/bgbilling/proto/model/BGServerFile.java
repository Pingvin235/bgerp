package ru.bgcrm.plugin.bgbilling.proto.model;

import java.util.Date;

import org.bgerp.model.base.IdTitleComment;

public class BGServerFile extends IdTitleComment {
    // id документа или какой-то сущности, куда привязан файл
    private int ownerId;
    private int userId;
    private long size;
    private Date date;
    // нужен исключительно для сохранения файла без ещё не определённого ownerId,
    // например при добавлении вложений в ещё не сохранённое сообщение helpdesk
    private String uuid;

    public int getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(int ownerId) {
        this.ownerId = ownerId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
}
