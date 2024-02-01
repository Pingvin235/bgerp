package ru.bgcrm.plugin.bgbilling.proto.model.inet;

import java.util.Date;

import org.bgerp.model.base.tree.TreeItem;

import ru.bgcrm.util.TimeUtils;

public class InetDevice extends TreeItem<Integer, InetDevice> {
    private static final String ICON_TAG_ROOT = iconTag("globe-network");
    private static final String ICON_TAG_NODE = iconTag("server-network");
    private static final String ICON_TAG_LEAF = iconTag("network-ethernet");
    private static final String ICON_TAG_FOLDER = iconTag("folder-network");

    private static String iconTag(String icon) {
        return " <img src='/img/fugue/" + icon + ".png'/> ";
    }

    private int deviceTypeId;
    private int entityId;
    private int entitySpecId;
    private String entityTitle;
    private String invIdentifier;
    private int invDeviceId;
    private String invTitle;
    private String comment;
    private Date dateFrom;
    private Date dateTo;

    public int getDeviceTypeId() {
        return deviceTypeId;
    }

    public void setDeviceTypeId(int deviceTypeId) {
        this.deviceTypeId = deviceTypeId;
    }

    public int getEntityId() {
        return entityId;
    }

    public void setEntityId(int entityId) {
        this.entityId = entityId;
    }

    public int getEntitySpecId() {
        return entitySpecId;
    }

    public void setEntitySpecId(int entitySpecId) {
        this.entitySpecId = entitySpecId;
    }

    public String getEntityTitle() {
        return entityTitle;
    }

    public void setEntityTitle(String entityTitle) {
        this.entityTitle = entityTitle;
    }

    public String getInvIdentifier() {
        return invIdentifier;
    }

    public void setInvIdentifier(String ident) {
        this.invIdentifier = ident;
    }

    public int getInvDeviceId() {
        return invDeviceId;
    }

    public void setInvDeviceId(int invDeviceId) {
        this.invDeviceId = invDeviceId;
    }

    public String getInvTitle() {
        return invTitle;
    }

    public void setInvTitle(String invTitle) {
        this.invTitle = invTitle;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
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

    @Override
    public String getIcon() {
        if (id <= 0)
            return ICON_TAG_ROOT;

        if (deviceTypeId == 0)
            return ICON_TAG_FOLDER;

        if(children == null || children.isEmpty())
            return ICON_TAG_LEAF;

        return ICON_TAG_NODE;
    }

    /**
     * @see InetDevicePanel#getTitle in BGBilling client.
     */
    @Override
    public String getTextStyle() {
        if (entityId == -100)
            return "color: #666666";
        if (dateTo != null && TimeUtils.dateBefore(dateTo, new Date()))
            return "color: #666666";
        return null;
    }

    @Override
    protected boolean isRootNode() {
        return isRootNodeWithIntegerId(id, parentId);
    }
}
