package ru.bgcrm.plugin.bgbilling.proto.model;

import java.util.Date;

import org.bgerp.model.base.IdTitleComment;

public class BGServerFile extends IdTitleComment {
    // ID of the document or some entity the file is attached to
    private int ownerId;
    private int userId;
    private long size;
    private Date date;
    // needed exclusively for saving a file without a yet undefined ownerId,
    // e.g. when adding attachments to a not yet saved helpdesk message
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
