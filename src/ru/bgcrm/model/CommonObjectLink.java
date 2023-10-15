package ru.bgcrm.model;

import org.bgerp.app.cfg.Preferences;
import org.bgerp.util.Log;

public class CommonObjectLink {
    private static final Log log = Log.getLog();

    // object, having the link
    private String objectType;
    private int objectId;
    // link object
    private String linkObjectType;
    private int linkObjectId;
    private String linkObjectTitle;
    private String linkObjectComment;

    private Preferences configMap = new Preferences();

    public CommonObjectLink() {}

    public CommonObjectLink(String objectType, int objectId, String linkObjectType, int linkObjectId, String linkObjectTitle,
            String linkObjectComment) {
        this.objectType = objectType;
        this.objectId = objectId;
        this.linkObjectType = linkObjectType;
        this.linkObjectId = linkObjectId;
        this.linkObjectTitle = linkObjectTitle;
        this.linkObjectComment = linkObjectComment;
    }

    public CommonObjectLink(String objectType, int objectId, String linkObjectType, int linkObjectId, String linkObjectTitle) {
        this(objectType, objectId, linkObjectType, linkObjectId, linkObjectTitle, "");
    }

    public CommonObjectLink(int objectId, String linkObjectType, int linkObjectId, String linkObjectTitle) {
        this(null, objectId, linkObjectType, linkObjectId, linkObjectTitle);
    }

    public CommonObjectLink(int objectId, String linkObjectType, int linkObjectId, String linkObjectTitle, String linkObjectComment) {
        this(null, objectId, linkObjectType, linkObjectId, linkObjectTitle, linkObjectComment);
    }

    public int getObjectId() {
        return objectId;
    }

    public void setObjectId(int id) {
        this.objectId = id;
    }

    public String getObjectType() {
        return objectType;
    }

    public void setObjectType(String objectType) {
        this.objectType = objectType;
    }

    public int getLinkObjectId() {
        return linkObjectId;
    }

    public void setLinkObjectId(int objectId) {
        this.linkObjectId = objectId;
    }

    public String getLinkObjectType() {
        return linkObjectType;
    }

    public void setLinkObjectType(String objectType) {
        this.linkObjectType = objectType;
    }

    public String getLinkObjectTitle() {
        return linkObjectTitle;
    }

    public void setLinkObjectTitle(String objectTitle) {
        this.linkObjectTitle = objectTitle;
    }

    public String getLinkObjectComment() {
        return linkObjectComment;
    }

    public void setLinkObjectComment(String linkedObjectComment) {
        this.linkObjectComment = linkedObjectComment;
    }

    @Deprecated
    public int getLinkedObjectId() {
        log.warnd("Deprecated method 'getLinkedObjectId' was called. Use 'getLinkObjectId' instead.");
        return linkObjectId;
    }

    @Deprecated
    public void setLinkedObjectId(int objectId) {
        log.warnd("Deprecated method 'setLinkedObjectId' was called. Use 'setLinkObjectId' instead.");
        this.linkObjectId = objectId;
    }

    @Deprecated
    public String getLinkedObjectType() {
        log.warnd("Deprecated method 'getLinkedObjectType' was called. Use 'getLinkObjectType' instead.");
        return linkObjectType;
    }

    @Deprecated
    public void setLinkedObjectType(String objectType) {
        log.warnd("Deprecated method 'setLinkedObjectType' was called. Use 'setLinkObjectType' instead.");
        this.linkObjectType = objectType;
    }

    @Deprecated
    public String getLinkedObjectTitle() {
        log.warnd("Deprecated method 'getLinkedObjectTitle' was called. Use 'getLinkObjectTitle' instead.");
        return linkObjectTitle;
    }

    @Deprecated
    public void setLinkedObjectTitle(String objectTitle) {
        log.warnd("Deprecated method 'setLinkedObjectTitle' was called. Use 'setLinkObjectTitle' instead.");
        this.linkObjectTitle = objectTitle;
    }

    @Deprecated
    public String getLinkedObjectComment() {
        log.warnd("Deprecated method 'getLinkedObjectComment' was called. Use 'getLinkObjectComment' instead.");
        return linkObjectComment;
    }

    @Deprecated
    public void setLinkedObjectComment(String linkedObjectComment) {
        log.warnd("Deprecated method 'setLinkedObjectComment' was called. Use 'setLinkObjectComment' instead.");
        this.linkObjectComment = linkedObjectComment;
    }

    public Preferences getConfigMap() {
        return configMap;
    }

    public void setConfigMap(Preferences configMap) {
        this.configMap = configMap;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + linkObjectId;
        result = prime * result + ((linkObjectTitle == null) ? 0 : linkObjectTitle.hashCode());
        result = prime * result + ((linkObjectType == null) ? 0 : linkObjectType.hashCode());
        result = prime * result + objectId;
        result = prime * result + ((objectType == null) ? 0 : objectType.hashCode());
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
        CommonObjectLink other = (CommonObjectLink) obj;
        if (linkObjectId != other.linkObjectId)
            return false;
        if (linkObjectTitle == null) {
            if (other.linkObjectTitle != null)
                return false;
        } else if (!linkObjectTitle.equals(other.linkObjectTitle))
            return false;
        if (linkObjectType == null) {
            if (other.linkObjectType != null)
                return false;
        } else if (!linkObjectType.equals(other.linkObjectType))
            return false;
        if (objectId != other.objectId)
            return false;
        if (objectType == null) {
            if (other.objectType != null)
                return false;
        } else if (!objectType.equals(other.objectType))
            return false;
        return true;
    }
}