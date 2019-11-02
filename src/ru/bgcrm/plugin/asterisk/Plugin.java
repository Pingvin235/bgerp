package ru.bgcrm.plugin.asterisk;

import org.w3c.dom.Document;

public class Plugin
	extends ru.bgcrm.plugin.Plugin
{
	public static final String ID = "asterisk";
	
	public Plugin( Document doc )
	{
		super( doc, ID );
	}
}
