package ru.bgcrm.plugin.bgbilling.proto.model.dialup;

import org.bgerp.model.base.IdTitle;

public class DialUpLoginAttrSet
	extends IdTitle
{
	private String realm;

	public String getRealm()
	{
		return realm;
	}

	public void setRealm( String realm )
	{
		this.realm = realm;
	}
}
