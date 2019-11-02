package ru.bgcrm.plugin.bgbilling.proto.model.inet;

import ru.bgcrm.model.IdTitle;

public class InetDevice extends IdTitle {
	private int parentId;
	private int deviceTypeId;
	private int entityId;
	private int entitySpecId;
	private String entityTitle;
	private String invIdentifier;
	private int invDeviceId;
		

    public int getDeviceTypeId() {
		return deviceTypeId;
	}

	public void setDeviceTypeId(int deviceTypeId) {
		this.deviceTypeId = deviceTypeId;
	}

	public int getParentId() {
		return parentId;
	}

	public void setParentId(int parentId) {
		this.parentId = parentId;
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
	
	public int getInvDeviceId()
    {
        return invDeviceId;
    }

    public void setInvDeviceId( int invDeviceId )
    {
        this.invDeviceId = invDeviceId;
    }
}
