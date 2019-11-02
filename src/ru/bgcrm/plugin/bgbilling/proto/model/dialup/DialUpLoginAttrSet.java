package ru.bgcrm.plugin.bgbilling.proto.model.dialup;

import ru.bgcrm.model.IdTitle;

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
