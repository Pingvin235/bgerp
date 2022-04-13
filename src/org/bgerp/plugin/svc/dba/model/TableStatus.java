package org.bgerp.plugin.svc.dba.model;

import java.util.Date;

/**
 * DB table info.
 *
 * @author Shamil Vakhitov
 */
public class TableStatus {
    private String name;
    private long rows;
    private long dataLength;
    private long indexLength;
    private Date createTime;
    private Date updateTime;
    private boolean dropCandidate;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getRows() {
        return rows;
    }

    public void setRows(long rows) {
        this.rows = rows;
    }

    public long getDataLength() {
        return dataLength;
    }

    public void setDataLength(long dataLength) {
        this.dataLength = dataLength;
    }

    public long getIndexLength() {
        return indexLength;
    }

    public void setIndexLength(long indexLength) {
        this.indexLength = indexLength;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public boolean isDropCandidate() {
        return dropCandidate;
    }

    public void setDropCandidate(boolean value) {
        this.dropCandidate = value;
    }
}
