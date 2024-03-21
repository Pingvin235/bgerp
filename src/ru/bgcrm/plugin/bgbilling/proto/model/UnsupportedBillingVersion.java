package ru.bgcrm.plugin.bgbilling.proto.model;

import org.bgerp.app.exception.BGException;

public class UnsupportedBillingVersion
	extends BGException
{
	public UnsupportedBillingVersion( String versions )
	{
		super( "Данный функционал поддержан для версий BGBilling: " + versions );
	}
}
