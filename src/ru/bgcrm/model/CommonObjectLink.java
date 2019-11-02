package ru.bgcrm.model;

import ru.bgcrm.util.Preferences;

public class CommonObjectLink {
	// объект к которому привязывается
	private int objectId;
	private String objectType;
	// привязанный объект
	private int linkedObjectId;
	private String linkedObjectType;
	private String linkedObjectTitle;
	private String linkedObjectComment;

	private Preferences configMap = new Preferences();

	public CommonObjectLink() {
	}

	public CommonObjectLink(String objectType, int objectId, String linkedObjectType, int linkedObjectId,
			String linkedObjectTitle, String linkedObjectComment) {
		this.objectType = objectType;
		this.objectId = objectId;
		this.linkedObjectType = linkedObjectType;
		this.linkedObjectId = linkedObjectId;
		this.linkedObjectTitle = linkedObjectTitle;
		this.linkedObjectComment = linkedObjectComment;
	}

	public CommonObjectLink(String objectType, int objectId, String linkedObjectType, int linkedObjectId,
			String linkedObjectTitle) {
		this(objectType, objectId, linkedObjectType, linkedObjectId, linkedObjectTitle, "");
	}

	public CommonObjectLink(int objectId, String linkedObjectType, int linkedObjectId, String linkedObjectTitle) {
		this(null, objectId, linkedObjectType, linkedObjectId, linkedObjectTitle);
	}

	public CommonObjectLink(int objectId, String linkedObjectType, int linkedObjectId, String linkedObjectTitle,
			String linkedObjectComment) {
		this(null, objectId, linkedObjectType, linkedObjectId, linkedObjectTitle, linkedObjectComment);
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

	public int getLinkedObjectId() {
		return linkedObjectId;
	}

	public void setLinkedObjectId(int objectId) {
		this.linkedObjectId = objectId;
	}

	public String getLinkedObjectType() {
		return linkedObjectType;
	}

	public void setLinkedObjectType(String objectType) {
		this.linkedObjectType = objectType;
	}

	public String getLinkedObjectTitle() {
		return linkedObjectTitle;
	}

	public void setLinkedObjectTitle(String objectTitle) {
		this.linkedObjectTitle = objectTitle;
	}

	public String getLinkedObjectComment() {
		return linkedObjectComment;
	}

	public void setLinkedObjectComment(String linkedObjectComment) {
		this.linkedObjectComment = linkedObjectComment;
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
		result = prime * result + linkedObjectId;
		result = prime * result + ((linkedObjectTitle == null) ? 0 : linkedObjectTitle.hashCode());
		result = prime * result + ((linkedObjectType == null) ? 0 : linkedObjectType.hashCode());
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
		if (linkedObjectId != other.linkedObjectId)
			return false;
		if (linkedObjectTitle == null) {
			if (other.linkedObjectTitle != null)
				return false;
		} else if (!linkedObjectTitle.equals(other.linkedObjectTitle))
			return false;
		if (linkedObjectType == null) {
			if (other.linkedObjectType != null)
				return false;
		} else if (!linkedObjectType.equals(other.linkedObjectType))
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