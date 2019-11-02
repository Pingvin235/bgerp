package ru.bgcrm.plugin.dispatch;

import org.w3c.dom.Document;

public class Plugin
	extends ru.bgcrm.plugin.Plugin
{
	public static final String ID = "dispatch";
	
	public Plugin( Document doc )
	{
		super( doc, ID );		
	}
}
